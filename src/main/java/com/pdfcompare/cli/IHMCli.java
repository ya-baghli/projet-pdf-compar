package com.pdfcompare.cli;

import com.pdfcompare.model.Pair;
import com.pdfcompare.service.MatchingFileService;
import com.pdfcompare.service.ScannerService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@ShellComponent
public class IHMCli {

    private final ScannerService scannerService;
    private final MatchingFileService matchingFileService;

    public IHMCli(ScannerService scannerService, MatchingFileService matchingFileService) {
        this.scannerService = scannerService;
        this.matchingFileService = matchingFileService;
    }

    @ShellMethod(key = "compare-dirs", value = "Compare les PDF de deux repertoires via une regex.")
    public String compareDirectories(
            @ShellOption(help = "Chemin du repertoire de reference") String refDir,
            @ShellOption(help = "Chemin du repertoire cible") String testDir,
            @ShellOption(defaultValue = ".*?-([0-9]{6})-?",
                         help = "Expression reguliere pour l'extraction de l'identifiant") String regex) {

        try {
            // 1. Scanner les deux répertoires
            List<File> refFiles = scannerService.listPdfFiles(Path.of(refDir));
            List<File> testFiles = scannerService.listPdfFiles(Path.of(testDir));

            if (refFiles.isEmpty()) {
                return "[ERREUR] Aucun PDF dans le répertoire de référence : " + refDir;
            }
            if (testFiles.isEmpty()) {
                return "[ERREUR] Aucun PDF dans le répertoire cible : " + testDir;
            }

            // 2. Apparier les fichiers
            List<Pair> pairs = matchingFileService.matchFilesWithRegex(refFiles, testFiles, regex);

            // 3. Afficher
            return format(pairs);

        } catch (IOException | IllegalArgumentException e) {
            return "[ERREUR] " + e.getMessage();
        }
    }

    private String format(List<Pair> pairs) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%n[SUCCÈS] %d paires de fichiers PDF identifiées :%n", pairs.size()));
        sb.append("--------------------------------------------------\n");
        for (Pair pair : pairs) {
            sb.append(String.format(" > Identifiant : %s%n", pair.getIdentifier()));
            sb.append(String.format("   - Réf   : %s%n", pair.getRefPdf().getName()));
            sb.append(String.format("   - Cible : %s%n", pair.getTestPdf().getName()));
            sb.append("--------------------------------------------------\n");
        }
        return sb.toString();
    }
}