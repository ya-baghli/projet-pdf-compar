# Projet-pdf-compar

> **Outil d'automatisation des tests de non-régression (TNR) pour la comparaison de documents PDF éditiques.**

---------------------------------------

## Problématique

Dans les chaînes d'impression et de gestion documentaire (éditique), les tests de non-régression (TNR) nécessitent souvent la comparaison de milliers de documents générés suite à une mise à jour d'application ou de modèle. 

Actuellement, ces vérifications reposent en partie sur des contrôles visuels manuels :
* **Chronophages** et impossibles à passer à l'échelle.
* **Sujets aux erreurs humaines** et à la fatigue visuelle.

---------------------------------------

## Proposition de Valeur

**PDF Diff Automation Tool** résout ce problème en automatisant la détection des écarts entre les versions *Master* (référence) et *Target* (nouvelle version) :
1. **Fiabilité et précision :** Analyse multicritère (métadonnées, texte et rendu pixel/image).
2. **Réduction du bruit :** Gestion de masques d'exclusion et de règles de tolérance pour ignorer les variables dynamiques (dates, numéros de facture, IDs de session).
3. **Productivité :** Génération automatique de rapports exploitables et intégration simple dans un pipeline CI/CD.

---------------------------------------

## Cible & Utilisateurs

* **Développeurs Éditiques :** Pour valider les évolutions de scripts de composition ou de moteurs de rendu.
* **Ingénieurs QA / Testeurs :** Pour exécuter des campagnes TNR automatisées et analyser rapidement les anomalies.
* **Chefs de Projet / PO :** Pour disposer d'un bilan clair de non-régression avant déploiement.

---------------------------------------

### MVP (Minimum Viable Product) — *Phase 1 : Socle TNR*
- [x] **Epic 1 : Métadonnées & Structure globale**
  - Validation du nombre de pages, de la taille du fichier et des métadonnées clés.
- [ ] **Epic 2 : Comparaison Textuelle des PDF**
  - Extraction et comparaison du texte brut page par page (normalisation des espaces/sauts de ligne).
- [ ] **Epic 4 : Restitution & Reporting (Basique)**
  - Génération d'un rapport de synthèse au format JSON/CLI avec code de retour (`exit code`).

### Phase 2 : Rendu Visuel & Configuration Avancée
- [ ] **Epic 3 : Comparaison Visuelle Pixel-to-Pixel**
  - Rasterisation des pages en images HD, comparaison matricielle et surlignage visuel des différences (*diff overlay*).
- [ ] **Epic 5 : Configuration & Masquage de Zones**
  - Fichier de configuration (`config.yaml` / `config.json`).
  - Masques d'exclusion (coordonnées X/Y) et Regex pour ignorer les variables dynamiques (dates, numéros de document, etc.).

### Phase 3 : Vision à Long Terme & Gestion des Évolutions
- [ ] **Epic 6 : Gestion Intelligente des Faux Positifs & Évolutions Validées**
  - **Tolérance textuelle métier :** Prise en compte de dictionnaires de remplacement/corrections autorisées (ex: remplacer *"Que seras"* par *"Que sera"* ne déclenche pas une alerte bloquante, mais un statut *Avertissement / À valider*).
  - **Mise à jour d'assets / Charte graphique :** Tolérance sur les changements de logos ou d'éléments visuels récurrents identifiés comme "évolutions validées".
  - **Workflow de validation :** Possibilité pour le testeur/PO de marquer un écart comme *"Faux positif / Évolution acceptée"* pour enrichir la suite de tests future.

---------------------------------------

## Stack Technique & Prérequis

* **Langage :** *(ex: Python 3.10+ / Node.js / Java)*
* **Bibliothèques PDF :** *(ex: PyPDF2, pdfplumber, Poppler, PDF.js)*
* **Gestion de Projet :** Suivi des tâches et du Backlog via **GitHub Projects** & **GitHub Issues**.

---------------------------------------

## Prise en main rapide

```bash
# 1. Cloner le dépôt
git clone [https://github.com/votre-compte/pdf-diff-automation.git](https://github.com/votre-compte/pdf-diff-automation.git)

# 2. Se déplacer dans le dossier
cd pdf-diff-automation

# 3. Lancer une comparaison basique (Exemple)
python pdf_diff.py --source input/master.pdf --target input/target.pdf
