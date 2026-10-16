package com.pdfcompare;

import com.pdfcompare.model.ComparisonResult;
import com.pdfcompare.model.Pair;
import com.pdfcompare.service.MatchingFileService;
import com.pdfcompare.service.ScannerService;
import com.pdfcompare.service.checker.BasicChecker;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PdfCompareWebController {

    private final ScannerService scannerService;
    private final MatchingFileService matchingFileService;
    private final BasicChecker basicChecker;

    public PdfCompareWebController(ScannerService scannerService,
                                   MatchingFileService matchingFileService,
                                   BasicChecker basicChecker) {
        this.scannerService = scannerService;
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
            // 1. Scanner les deux répertoires
            List<File> refFiles = scannerService.listPdfFiles(Path.of(refDir));
            List<File> testFiles = scannerService.listPdfFiles(Path.of(testDir));

            if (refFiles.isEmpty()) {
                return showError(model, "Aucun PDF dans le répertoire de référence : " + refDir);
            }
            if (testFiles.isEmpty()) {
                return showError(model, "Aucun PDF dans le répertoire cible : " + testDir);
            }

            // 2. Apparier les fichiers
            List<Pair> pairs = matchingFileService.matchFilesWithRegex(refFiles, testFiles, regex);
            if (pairs.isEmpty()) {
                return showError(model, "Aucune paire trouvée : vérifiez la regex.");
            }

            // 3. Comparer chaque paire (l'ordre des paires est conservé)
            Map<Pair, ComparisonResult> comparisonResults = new LinkedHashMap<>();
            for (Pair pair : pairs) {
                comparisonResults.put(pair, basicChecker.check(pair.getRefPdf(), pair.getTestPdf()));
            }

            model.addAttribute("comparisonResults", comparisonResults);
            model.addAttribute("message", "Comparaison de " + pairs.size() + " paires effectuée avec succès !");
            model.addAttribute("success", true);
            return "index";

        } catch (IOException | IllegalArgumentException e) {
            // IllegalArgumentException couvre aussi une regex invalide ou un chemin incorrect
            return showError(model, "Erreur lors du traitement : " + e.getMessage());
        }
    }

    private String showError(Model model, String message) {
        model.addAttribute("message", "[ERREUR] " + message);
        model.addAttribute("success", false);
        return "index";
    }
}