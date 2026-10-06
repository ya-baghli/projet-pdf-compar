package com.pdfcompare.controller;

import com.pdfcompare.Main;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index"; // Charge src/main/resources/templates/index.html
    }

    @PostMapping("/compare")
    public String runComparison(@RequestParam("refDir") String refDir, 
                                @RequestParam("testDir") String testDir, 
                                Model model) {
        try {
            // Appel de votre logique existante de traitement par lot
        	Main.processBatch(refDir, testDir);
            model.addAttribute("message", "Comparaison terminée avec succès ! Rapport généré dans le dossier test.");
            model.addAttribute("success", true);
        } catch (Exception e) {
            model.addAttribute("message", "Erreur lors du traitement : " + e.getMessage());
            model.addAttribute("success", false);
        }
        return "index";
    }
}