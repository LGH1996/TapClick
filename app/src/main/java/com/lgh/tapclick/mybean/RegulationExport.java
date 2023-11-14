package com.lgh.tapclick.mybean;

import android.util.DisplayMetrics;

import java.util.ArrayList;
import java.util.List;

public class RegulationExport {
    public String fingerPrint;
    public DisplayMetrics displayMetrics;
    public List<Regulation> regulationList;

    public RegulationExport() {
        regulationList = new ArrayList<>();
    }
}
