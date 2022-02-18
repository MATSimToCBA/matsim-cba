package org.matsim.contrib.cba.analyzers.ptAnalysis;

import org.matsim.core.config.ReflectiveConfigGroup;

import javax.validation.constraints.NotNull;

public class PtAnalyzerConfigGroup extends ReflectiveConfigGroup {

    public static final String SET_NAME = "ptAnalyzer";

    public static final String MODE = "mode";
    public static final String MODE_EXP = "The name of the pt mode that will be analyzed";

    public static final String TRIPS_SHEET_NAME = "tripsSheetName";
    public static final String TRIPS_SHEET_NAME_EXP = "The name of the sheet that will contain the trips information";

    @NotNull
    private String mode;
    @NotNull
    private String tripsSheetName;

    public PtAnalyzerConfigGroup() {
        super(SET_NAME);
    }

    /**
     * @param mode -- {@value MODE_EXP}
     */
    @StringSetter(MODE)
    public void setMode(String mode){
        this.mode = mode;
    }

    /**
     * @return -- {@value MODE_EXP}
     */
    @StringGetter(MODE)
    public String getMode(){
        return this.mode;
    }

    /**
     * @param tripsSheetName -- {@value TRIPS_SHEET_NAME_EXP}
     */
    @StringSetter(TRIPS_SHEET_NAME)
    public void setTripsSheetName(String tripsSheetName){
        this.tripsSheetName = tripsSheetName;
    }

    /**
     * @return -- {@value TRIPS_SHEET_NAME_EXP}
     */
    @StringGetter(TRIPS_SHEET_NAME)
    public String getTripsSheetName() {
        return this.tripsSheetName;
    }
}
