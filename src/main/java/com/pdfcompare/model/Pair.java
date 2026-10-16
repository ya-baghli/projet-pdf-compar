package com.pdfcompare.model;
import java.io.File;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class Pair {
	private File refPdf;
    private File testPdf;
    private String identifier;
}
