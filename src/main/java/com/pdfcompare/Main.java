package com.pdfcompare;

import com.pdfcompare.modules.BasicChecker;
import com.pdfcompare.modules.TextDiffChecker;
import com.pdfcompare.modules.VisualPixelChecker;
import com.pdfcompare.reporting.DiffReport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    private static class FilePair {
        File ref;
        File test;

        FilePair(File ref, File test) {
            this.ref = ref;
            this.test = test;
        }
    }

    public static void processBatch(String referenceDirPath, String testDirPath) {
        Path refDir = Paths.get(referenceDirPath);
        Path testDir = Paths.get(testDirPath);

        if (!Files.exists(refDir) || !Files.exists(testDir)) {
            throw new IllegalArgumentException("L'un des dossiers spécifiés est introuvable.");
        }

        try {
            List<Path> testFiles;
            try (Stream<Path> paths = Files.list(testDir)) {
                testFiles = paths
                    .filter(p -> p.toString().toLowerCase().endsWith(".pdf"))
                    .collect(Collectors.toList());
            }

            List<FilePair> validPairs = new ArrayList<>();

            try (Stream<Path> refPaths = Files.list(refDir)) {
                List<Path> refFiles = refPaths
                    .filter(p -> p.toString().toLowerCase().endsWith(".pdf"))
                    .collect(Collectors.toList());

                for (Path refPath : refFiles) {
                    String refFileName = refPath.getFileName().toString();

                    if (Files.size(refPath) == 0) continue;
                    if (refFileName.length() < 6) continue;

                    String prefixRef = refFileName.substring(0, 6);

                    for (Path testPath : testFiles) {
                        String testFileName = testPath.getFileName().toString();
                        if (testFileName.length() >= 6 && testFileName.substring(0, 6).equalsIgnoreCase(prefixRef)) {
                            if (Files.size(testPath) > 0) {
                                validPairs.add(new FilePair(refPath.toFile(), testPath.toFile()));
                            }
                        }
                    }
                }
            }

            if (validPairs.isEmpty()) {
                throw new IllegalStateException("Aucune paire valide trouvée avec le préfixe de 6 caractères.");
            }

            DiffReport globalReport = new DiffReport();

            for (FilePair pair : validPairs) {
                System.out.println("\n--------------------------------------------------");
                System.out.println("Traitement de la paire :");
                System.out.println("  - Référence : " + pair.ref.getName());
                System.out.println("  - Test      : " + pair.test.getName());
                System.out.println("--------------------------------------------------");

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream dualStream = new PrintStream(baos) {
                    private final PrintStream console = System.out;
                    @Override
                    public void write(byte[] buf, int off, int len) {
                        console.write(buf, off, len);
                        super.write(buf, off, len);
                    }
                };

                PrintStream oldOut = System.out;
                System.setOut(dualStream);

                System.out.println("--- 1. Métadonnées ---");
                BasicChecker.compareBasicAttributes(pair.ref, pair.test);

                System.out.println("--- 2. Texte ---");
                List<String> textLogs = TextDiffChecker.compareTextContent(pair.ref, pair.test);
                for (String log : textLogs) {
                    System.out.println(log);
                }
                if (textLogs.isEmpty()) {
                    System.out.println("[RÉSULTAT TEXTE] Le contenu textuel est identique.");
                } else {
                    System.out.println("[RÉSULTAT TEXTE] Des écarts textuels ont été détectés.");
                }

                System.out.println("--- 3. Visuel ---");
                VisualPixelChecker.compareVisualContent(pair.ref, pair.test);

                System.out.flush();
                System.setOut(oldOut);

                List<String> allLogs = Arrays.asList(baos.toString().split("\\r?\\n"));
                List<String> basicLogs = new ArrayList<>();
                List<String> cleanTextLogs = new ArrayList<>();
                boolean captureBasic = false;
                boolean captureText = false;

                for (String log : allLogs) {
                    if (log.contains("--- 1. Métadonnées ---")) { captureBasic = true; continue; }
                    if (log.contains("--- 2. Texte ---")) { captureBasic = false; captureText = true; continue; }
                    if (log.contains("--- 3. Visuel ---")) { captureText = false; continue; }

                    if (captureBasic && !log.trim().isEmpty()) basicLogs.add(log);
                    if (captureText && !log.trim().isEmpty()) cleanTextLogs.add(log);
                }

                List<File> diffImages = new ArrayList<>();
                File testDirFile = pair.test.getParentFile();
                if (testDirFile != null && testDirFile.exists()) {
                    File[] files = testDirFile.listFiles((dir, name) -> name.endsWith(".png"));
                    if (files != null) {
                        for (File img : files) {
                            diffImages.add(img);
                        }
                    }
                }

                globalReport.addPairSection(pair.ref, pair.test, basicLogs, cleanTextLogs, diffImages);
            }

            String globalReportPath = testDir.resolve("Rapport_Global_Comparaison.pdf").toString();
            globalReport.save(globalReportPath);

        } catch (IOException e) {
            throw new RuntimeException("Erreur I/O lors du traitement : " + e.getMessage(), e);
        }
    }
}