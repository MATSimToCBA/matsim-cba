package org.matsim.contrib.cba.analyzers.walkAnalysis;

import org.matsim.core.config.ReflectiveConfigGroup;

import javax.validation.constraints.NotNull;
import java.util.Map;

public class WalkAnalyzerConfigGroup extends ReflectiveConfigGroup {

    public static final String SET_NAME = "walkAnalyzer";

    public static final String TRIPS_SHEET_NAME = "tripsSheetName";
    public static final String TRIPS_SHEET_NAME_EXP = "The name of the sheet that will contain the trips information";

    @NotNull
    private String tripsSheetName;


    public WalkAnalyzerConfigGroup() {
        super(SET_NAME);
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

    @Override
    public Map<String, String> getComments() {
        Map<String, String> comments = super.getComments();
        comments.put(TRIPS_SHEET_NAME, TRIPS_SHEET_NAME_EXP);
        return comments;
    }
}
