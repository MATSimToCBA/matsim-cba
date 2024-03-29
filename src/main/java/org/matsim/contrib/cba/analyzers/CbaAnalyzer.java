package org.matsim.contrib.cba.analyzers;

import org.apache.poi.ss.usermodel.Sheet;

import java.util.List;

public interface CbaAnalyzer {
    public String[] getSheetsNames();

    public void fillSheets(List<Sheet> sheets);
}
