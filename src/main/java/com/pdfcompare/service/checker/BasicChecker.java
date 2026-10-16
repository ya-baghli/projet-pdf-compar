package com.pdfcompare.service.checker;

import com.pdfcompare.model.ComparisonResult;
import com.pdfcompare.model.DifferenceDetail;
import com.pdfcompare.service.CheckerFileInterface;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Vérification de base : taille du fichier, nombre de pages et métadonnées.
 */
@Component
public class BasicChecker implements CheckerFileInterface {

    @Override
    public ComparisonResult check(File file1, File file2) {
        List<DifferenceDetail> differences = new ArrayList<>();

        try {
            // 1. Taille du fichier (en octets)
            compare("File Size",
                    String.valueOf(Files.size(file1.toPath())),
                    String.valueOf(Files.size(file2.toPath())),
                    differences);

            // 2. Pages et métadonnées via PDFBox
            try (PDDocument doc1 = Loader.loadPDF(file1);
                 PDDocument doc2 = Loader.loadPDF(file2)) {

                compare("Page Count",
                        String.valueOf(doc1.getNumberOfPages()),
                        String.valueOf(doc2.getNumberOfPages()),
                        differences);

                PDDocumentInformation info1 = doc1.getDocumentInformation();
                PDDocumentInformation info2 = doc2.getDocumentInformation();

                compare("Metadata Title",    info1.getTitle(),    info2.getTitle(),    differences);
                compare("Metadata Author",   info1.getAuthor(),   info2.getAuthor(),   differences);
                compare("Metadata Subject",  info1.getSubject(),  info2.getSubject(),  differences);
                compare("Metadata Keywords", info1.getKeywords(), info2.getKeywords(), differences);
                compare("Metadata Creator",  info1.getCreator(),  info2.getCreator(),  differences);
            }

        } catch (IOException e) {
            return ComparisonResult.error("Lecture impossible : " + e.getMessage());
        }

        return differences.isEmpty()
                ? ComparisonResult.identical()
                : ComparisonResult.different(differences);
    }

    /**
     * Ajoute une différence à la liste si les deux valeurs ne sont pas égales.
     */
    private void compare(String field, String ref, String test, List<DifferenceDetail> differences) {
        if (!Objects.equals(ref, test)) {
            differences.add(new DifferenceDetail(field, ref, test));
        }
    }
}