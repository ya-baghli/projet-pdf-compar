package com.pdfcompare.service;

import com.pdfcompare.model.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class MatchingFileService {

    public List<Pair> matchFilesWithRegex(List<File> refFiles, List<File> testFiles, String regexPattern) {
        List<Pair> pairs = new ArrayList<>();
        Pattern pattern = Pattern.compile(regexPattern);

        // 1. Indexer les fichiers de test par clé
        Map<String, List<File>> testByKey = new HashMap<>();
        for (File testFile : testFiles) {
            String key = extractKey(testFile.getName(), pattern);
            if (key == null) {
                log.warn("Fichier cible ignoré (nom non conforme à la regex) : {}", testFile.getName());
                continue;
            }
            testByKey.computeIfAbsent(key, k -> new ArrayList<>()).add(testFile);
        }

        // 2. Parcourir les fichiers de référence
        for (File refFile : refFiles) {
            String key = extractKey(refFile.getName(), pattern);
            if (key == null) {
                log.warn("Fichier de référence ignoré (nom non conforme à la regex) : {}", refFile.getName());
                continue;
            }

            List<File> matches = testByKey.get(key);
            if (matches == null) {
                log.warn("Aucun correspondant pour le fichier de référence : {}", refFile.getName());
                continue;
            }
            if (matches.size() > 1) {
                log.warn("Identifiant {} présent dans {} fichiers cibles", key, matches.size());
            }

            for (File testFile : matches) {
                pairs.add(new Pair(refFile, testFile, key));
            }
        }
        return pairs;
    }

    private String extractKey(String fileName, Pattern pattern) {
        Matcher matcher = pattern.matcher(fileName);
        if (!matcher.find()) {
            return null;
        }
        return matcher.groupCount() > 0 ? matcher.group(1) : matcher.group(0);
    }
}