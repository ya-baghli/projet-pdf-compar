package com.pdfcompare.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Service
public class ScannerService {

    /**
     * Liste les fichiers PDF situés directement dans un dossier
     * (sans descendre dans les sous-dossiers), triés par nom.
     *
     * @param dir le dossier à scanner
     * @return la liste des PDF, éventuellement vide
     * @throws IllegalArgumentException si le chemin n'est pas un dossier valide
     * @throws IOException              si le dossier ne peut pas être lu
     */
    public List<File> listPdfFiles(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Le chemin n'est pas un dossier valide : " + dir);
        }
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".pdf"))
                    .sorted()
                    .map(Path::toFile)
                    .toList();
        }
    }
}