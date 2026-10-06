package com.pdfcompare.modules;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

public class BasicChecker {
    public static boolean compareBasicAttributes(File file1, File file2) {
        boolean identical = true;

        try {
            long size1 = Files.size(file1.toPath());
            long size2 = Files.size(file2.toPath());
            
            if (size1 != size2) {
                System.out.println("[Écart Poids] Tailles différentes : " + size1 + " o vs " + size2 + " o.");
                identical = false;
            } else {
                System.out.println("[OK Poids] Identique (" + size1 + " octets).");
            }

            try (PDDocument doc1 = Loader.loadPDF(file1);
                 PDDocument doc2 = Loader.loadPDF(file2)) {
                
                // Comparaison du nombre de pages
                int pages1 = doc1.getNumberOfPages();
                int pages2 = doc2.getNumberOfPages();
                
                if (pages1 != pages2) {
                    System.out.println("[Écart Pages] Nombre de pages différent : " + pages1 + " vs " + pages2);
                    identical = false;
                } else {
                    System.out.println("[OK Pages] Nombre de pages identique (" + pages1 + ").");
                }

                PDDocumentInformation info1 = doc1.getDocumentInformation();
                PDDocumentInformation info2 = doc2.getDocumentInformation();

                List<String> ecartsMetadonnees = new ArrayList<>();

                if (!Objects.equals(info1.getTitle(), info2.getTitle())) {
                    ecartsMetadonnees.add("Titre");
                }
                if (!Objects.equals(info1.getAuthor(), info2.getAuthor())) {
                    ecartsMetadonnees.add("Auteur");
                }
                if (!Objects.equals(info1.getSubject(), info2.getSubject())) {
                    ecartsMetadonnees.add("Sujet");
                }
                if (!Objects.equals(info1.getKeywords(), info2.getKeywords())) {
                    ecartsMetadonnees.add("Mots-clés");
                }
                if (!Objects.equals(info1.getCreator(), info2.getCreator())) {
                    ecartsMetadonnees.add("Créateur");
                }

                if (!ecartsMetadonnees.isEmpty()) {
                    System.out.println("Écart ont été détectées dans les métadonnées (" + String.join(", ", ecartsMetadonnees) + ").");
                    identical = false;
                } else {
                    System.out.println("[OK Métadonnées] Toutes les métadonnées sont identiques.");
                }
            }

        } catch (IOException e) {
            System.err.println("Erreur de lecture des fichiers : " + e.getMessage());
            return false;
        }

        return identical;
    }
}