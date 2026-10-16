package com.pdfcompare.model;

import java.util.List;

/**
 * Résultat de la comparaison de deux fichiers par un checker.
 *
 * @param status       issue de la comparaison
 * @param differences  écarts constatés (vide si IDENTICAL ou ERROR)
 * @param errorMessage message d'erreur (null sauf si ERROR)
 */
public record ComparisonResult(
        ComparisonStatus status,
        List<DifferenceDetail> differences,
        String errorMessage) {

    public ComparisonResult {
        differences = List.copyOf(differences);   // copie défensive, liste non modifiable
    }

    public static ComparisonResult identical() {
        return new ComparisonResult(ComparisonStatus.IDENTICAL, List.of(), null);
    }

    public static ComparisonResult different(List<DifferenceDetail> differences) {
        return new ComparisonResult(ComparisonStatus.DIFFERENT, differences, null);
    }

    public static ComparisonResult error(String message) {
        return new ComparisonResult(ComparisonStatus.ERROR, List.of(), message);
    }

    public boolean isIdentical() {
        return status == ComparisonStatus.IDENTICAL;
    }
}