package com.pdfcompare.service;

import java.io.File;
import com.pdfcompare.model.ComparisonResult;

public interface CheckerFileInterface {
    ComparisonResult check(File file1, File file2);
}