package com.pdfcompare.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DifferenceDetail {
    private String property;
    private String refValue;
    private String targetValue;
}