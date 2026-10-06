package com.pdfcompare.modules;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class TextDiffChecker {

    public static List<String> compareTextContent(File file1, File file2) {
        List<String> diffLogs = new ArrayList<>();

        try (PDDocument doc1 = Loader.loadPDF(file1);
             PDDocument doc2 = Loader.loadPDF(file2)) {

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            int maxPages = Math.max(doc1.getNumberOfPages(), doc2.getNumberOfPages());

            Integer startPage = null;
            Integer startLine = null;
            Integer endPage = null;
            Integer endLine = null;

            for (int p = 1; p <= maxPages; p++) {
                String text1 = "";
                String text2 = "";

                if (p <= doc1.getNumberOfPages()) {
                    stripper.setStartPage(p);
                    stripper.setEndPage(p);
                    text1 = stripper.getText(doc1);
                }
                if (p <= doc2.getNumberOfPages()) {
                    stripper.setStartPage(p);
                    stripper.setEndPage(p);
                    text2 = stripper.getText(doc2);
                }

                List<String> lines1 = Arrays.asList(text1.split("\\r?\\n"));
                List<String> lines2 = Arrays.asList(text2.split("\\r?\\n"));

                int maxLines = Math.max(lines1.size(), lines2.size());

                for (int l = 0; l < maxLines; l++) {
                    String line1 = (l < lines1.size()) ? lines1.get(l).trim() : "[Ligne absente]";
                    String line2 = (l < lines2.size()) ? lines2.get(l).trim() : "[Ligne absente]";

                    String norm1 = line1.replaceAll("\\s+", " ");
                    String norm2 = line2.replaceAll("\\s+", " ");

                    boolean isDiff = !norm1.equals(norm2);

                    if (isDiff) {
                        if (startPage == null) {
                            startPage = p;
                            startLine = l + 1;
                            endPage = p;
                            endLine = l + 1;
                        } else {
                            if (p == endPage && (l + 1) == endLine + 1) {
                                endLine = l + 1;
                            } else {
                                diffLogs.add(formatDiffBlock(startPage, startLine, endPage, endLine));
                                startPage = p;
                                startLine = l + 1;
                                endPage = p;
                                endLine = l + 1;
                            }
                        }
                    }
                }
            }

            if (startPage != null) {
                diffLogs.add(formatDiffBlock(startPage, startLine, endPage, endLine));
            }

        } catch (IOException e) {
            diffLogs.add("Erreur de lecture ou d'extraction du texte : " + e.getMessage());
        }

        return diffLogs;
    }

    private static String formatDiffBlock(int startPage, int startLine, int endPage, int endLine) {
        if (startPage == endPage && startLine == endLine) {
            return "[Écart Texte] Différence à la page " + startPage + ", ligne " + startLine;
        } else {
            return "[Écart Texte] Différence de la page " + startPage + ", ligne " + startLine + " à la page " + endPage + ", ligne " + endLine;
        }
    }
}