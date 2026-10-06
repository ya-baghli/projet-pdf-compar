package com.pdfcompare.reporting;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DiffReport {

    private final PDDocument document;
    private PDPage page;
    private PDPageContentStream contentStream;
    private float yPosition;
    private final float margin = 50;
    private final float yStart = PDRectangle.A4.getHeight() - 50;
    private final float width = PDRectangle.A4.getWidth() - (2 * 50);
    
    private final PDType1Font fontTitle = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private final PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private final PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    private boolean isTextWriting = false;

    public DiffReport() throws IOException {
        this.document = new PDDocument();
        addNewPage();
        
        beginTextMode(fontTitle, 16);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Rapport Global de Comparaison PDF");
        endTextMode();
        yPosition -= 40;
    }

    private void addNewPage() throws IOException {
        if (isTextWriting) {
            endTextMode();
        }
        if (contentStream != null) {
            contentStream.close();
        }
        page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        contentStream = new PDPageContentStream(document, page);
        yPosition = yStart;
    }

    private void beginTextMode(PDType1Font font, float fontSize) throws IOException {
        if (!isTextWriting) {
            contentStream.beginText();
            isTextWriting = true;
        }
        contentStream.setFont(font, fontSize);
    }

    private void endTextMode() throws IOException {
        if (isTextWriting) {
            contentStream.endText();
            isTextWriting = false;
        }
    }

    private void checkSpace(float neededHeight) throws IOException {
        if (yPosition - neededHeight < margin) {
            addNewPage();
        }
    }

    public void addPairSection(File refFile, File testFile, List<String> basicLogs, List<String> textLogs, List<File> diffImages) throws IOException {
        checkSpace(60);

        endTextMode();
        beginTextMode(fontBold, 12);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Comparaison : " + refFile.getName() + " vs " + testFile.getName());
        endTextMode();
        yPosition -= 25;

        // 1. Métadonnées
        checkSpace(30);
        beginTextMode(fontBold, 10);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("--- 1. Métadonnées & Attributs ---");
        endTextMode();
        yPosition -= 15;

        for (String log : basicLogs) {
            checkSpace(15);
            beginTextMode(fontRegular, 9);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText(log);
            endTextMode();
            yPosition -= 12;
        }

        yPosition -= 10;

        // 2. Texte
        checkSpace(30);
        beginTextMode(fontBold, 10);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("--- 2. Contenu Textuel ---");
        endTextMode();
        yPosition -= 15;

        if (textLogs.isEmpty()) {
            checkSpace(15);
            beginTextMode(fontRegular, 9);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Aucun écart textuel détecté.");
            endTextMode();
            yPosition -= 15;
        } else {
            for (String log : textLogs) {
                checkSpace(15);
                beginTextMode(fontRegular, 9);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText(log);
                endTextMode();
                yPosition -= 12;
            }
        }

        yPosition -= 10;

        // 3. Visuel
        checkSpace(30);
        beginTextMode(fontBold, 10);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("--- 3. Rendu Visuel (Pixels) ---");
        endTextMode();
        yPosition -= 20;

        if (diffImages.isEmpty()) {
            checkSpace(15);
            beginTextMode(fontRegular, 9);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Aucun écart visuel détecté.");
            endTextMode();
            yPosition -= 25;
        } else {
            endTextMode(); // Fermeture du mode texte pour l'insertion d'images

            for (File imgFile : diffImages) {
                PDImageXObject pdImage = PDImageXObject.createFromFile(imgFile.getAbsolutePath(), document);
                float imgWidth = width;
                float imgHeight = (float) pdImage.getHeight() * imgWidth / pdImage.getWidth();

                if (yPosition - imgHeight < margin) {
                    addNewPage();
                }

                contentStream.drawImage(pdImage, margin, yPosition - imgHeight, imgWidth, imgHeight);
                yPosition -= (imgHeight + 20);
            }
        }

        yPosition -= 20;
    }

    public void save(String outputPath) throws IOException {
        endTextMode();
        if (contentStream != null) {
            contentStream.close();
        }
        document.save(outputPath);
        document.close();
        System.out.println("[RAPPORT] Rapport global généré avec succès : " + outputPath);
    }
}