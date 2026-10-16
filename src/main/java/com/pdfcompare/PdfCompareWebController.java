package com.pdfcompare.controller;

import com.pdfcompare.model.ComparisonResult;
import com.pdfcompare.model.Pair;
import com.pdfcompare.service.MatchingFileService;
import com.pdfcompare.service.checker.BasicChecker;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class PdfCompareWebController {

private final MatchingFileService matchingFileService;
private final BasicChecker basicChecker;

public PdfCompareWebController(MatchingFileService matchingFileService, BasicChecker basicChecker) {
    this.matchingFileService = matchingFileService;
    this.basicChecker = basicChecker;
}

@GetMapping("/")
public String index() {
    return "index";
}

@PostMapping("/compare")
public String runComparison(@RequestParam("refDir") String refDir, 
                            @RequestParam("testDir") String testDir, 
                            @RequestParam(name = "regex", defaultValue = ".*?-([0-9]{6})-?") String regex,
                            Model model) {
    try {
        List<File> refFiles = listPdfFilesInDirectory(Path.of(refDir));
        List<File> targetFiles = listPdfFilesInDirectory(Path.of(testDir));

        if (refFiles.isEmpty() || targetFiles.isEmpty()) {
            model.addAttribute("message", "[ERREUR] L'un des répertoires est vide ou ne contient pas de PDF.");
            model.addAttribute("success", false);
            return "index";
        }

        List<Pair> pairs = matchingFileService.matchFilesWithRegex(refFiles, targetFiles, regex);
        Map<Pair, ComparisonResult> comparisonResults = basicChecker.checkAllPairs(pairs);

        model.addAttribute("comparisonResults", comparisonResults);
        model.addAttribute("message", "Comparaison de " + pairs.size() + " paires effectuée avec succès !");
        model.addAttribute("success", true);

    } catch (Exception e) {
        model.addAttribute("message", "Erreur lors du traitement : " + e.getMessage());
        model.addAttribute("success", false);
    }
    return "index";
}

private List<File> listPdfFilesInDirectory(Path dir) throws IOException {
    if (!Files.exists(dir) || !Files.isDirectory(dir)) {
        throw new IllegalArgumentException("Le chemin n'est pas un dossier valide : " + dir);
    }
    try (var stream = Files.list(dir)) {
        return stream
                .filter(p -> !Files.isDirectory(p))
                .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".pdf"))
                .map(Path::toFile)
                .collect(Collectors.toList());
    }
}


}