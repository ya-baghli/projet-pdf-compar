package com.pdfcompare.modules;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import java.awt.Color;
import java.awt.image.BufferedImage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

public class VisualPixelChecker {

    public static boolean compareVisualContent(File file1, File file2) {
        boolean identical = true;

        try (PDDocument doc1 = Loader.loadPDF(file1);
             PDDocument doc2 = Loader.loadPDF(file2)) {

            PDFRenderer renderer1 = new PDFRenderer(doc1);
            PDFRenderer renderer2 = new PDFRenderer(doc2);

            int maxPages = Math.max(doc1.getNumberOfPages(), doc2.getNumberOfPages());
            File testDir = file2.getParentFile();
            String testFileName = file2.getName().replaceAll("\\.pdf$", "");

            for (int p = 0; p < maxPages; p++) {
                BufferedImage img1 = null;
                BufferedImage img2 = null;

                if (p < doc1.getNumberOfPages()) {
                    img1 = renderer1.renderImageWithDPI(p, 150);
                }
                if (p < doc2.getNumberOfPages()) {
                    img2 = renderer2.renderImageWithDPI(p, 150);
                }

                if (img1 == null || img2 == null) {
                    System.out.println("[Écart Visuel] Page manquante à la page " + (p + 1));
                    identical = false;
                    continue;
                }

                if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
                    System.out.println("[Écart Visuel] Différence de dimensions à la page " + (p + 1));
                    identical = false;
                    continue;
                }

                int width = img1.getWidth();
                int height = img1.getHeight();

                BufferedImage diffImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                boolean pageIdentical = true;

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int rgb1 = img1.getRGB(x, y);
                        int rgb2 = img2.getRGB(x, y);

                        if (rgb1 == rgb2) {
                            // Fond atténué en gris pour les parties communes
                            int red = (rgb2 >> 16) & 0xFF;
                            int green = (rgb2 >> 8) & 0xFF;
                            int blue = rgb2 & 0xFF;
                            int gray = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
                            int mutedGray = (gray + 200) / 2; 
                            Color mutedColor = new Color(mutedGray, mutedGray, mutedGray);
                            diffImage.setRGB(x, y, mutedColor.getRGB());
                        } else {
                            pageIdentical = false;
                            // Différence mise en évidence en ROUGE vif
                            diffImage.setRGB(x, y, Color.RED.getRGB());
                        }
                    }
                }

                if (!pageIdentical) {
                    identical = false;
                    System.out.println("[Écart Visuel] Différence détectée à la page " + (p + 1));

                    // Génération d'un unique fichier image fusionné par page
                    File outDiff = new File(testDir, testFileName + "_page_" + (p + 1) + ".png");
                    ImageIO.write(diffImage, "png", outDiff);
                } else {
                    System.out.println("[RÉSULTAT VISUEL] Le rendu visuel est identique.");
                }
            }

        } catch (IOException e) {
            System.err.println("Erreur lors de l'analyse visuelle : " + e.getMessage());
            return false;
        }

        return identical;
    }
}