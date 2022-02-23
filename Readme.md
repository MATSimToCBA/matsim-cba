# MATSim-CBA

This contribution is implemented as a MATSim module and aims at allowing economists to analyze MATSim outputs more
easily by generating relevant outputs in Microsoft Excel format during the simulation. These files can be directly used
in Cost-Benefit Analysis that use such file format.

## How it works

This module allows to generale Excel files at the end of MATSim iterations. The frequency of the generation can be
controlled in the configuration. The content of the Excel files depend on the analyzers that are configured. Currently,
the module offers three types of analyzers:

- Agents analyzer: Adds one sheet to the generated files containing the IDs and obtained scores of MATSim agents during
  the iteration
- Private vehicles analyzer: To analyze the trips operated with regular cars, adds one sheet to the generated files.
  This sheet will contain information on the trips
- PT analyzer: To analyze modes operated by the transit module, adds two sheets to the generated files: one centered on
  the trips performed using the **pt** mode and one centered on the vehicles of that mode.
- DRT analyzer: To analyze modes operated by the drt module, adds two sheet to the generated files similar to the PT
  analyzer.

Below is an example of configuration for the cba module

```xml

<module name="cba">
    <!-- The frequency at which cba output are generated -->
    <param name="outputFrequency" value="1"/>
    <parameterset type="agentsAnalyzer">
        <!-- The name of the sheet where the agents' scores will be printed -->
        <param name="scoresSheetName" value="scores"/>
    </parameterset>
    <parameterset type="drtAnalyzer">
        <!-- the mode implemented by the DRT module -->
        <param name="mode" value="drt"/>
        <!-- The name of the sheet where the trips information will be written -->
        <param name="tripsSheetName" value="Trips_drt"/>
        <!-- The name of the sheet where the vehicles' information will be written -->
        <param name="vehiclesSheetName" value="Vehicles_drt"/>
    </parameterset>
    <parameterset type="privateVehiclesAnalyzer">
        <!-- A comma separated list of activity types to ignore. If an activity type is ignored, the trips leading to or following activities of that type wil be ignored -->
        <param name="ignoredActivityTypes" value="DrtStay,DrtBusStop"/>
        <!-- A comma separated list of the The names of modes that will be analyzed -->
        <param name="modes" value="car"/>
        <!-- The name of the sheet that will contain the trips information -->
        <param name="tripsSheetName" value="Trips_pv"/>
    </parameterset>
    <parameterset type="ptAnalyzer">
        <!-- The name of the pt mode that will be analyzed -->
        <param name="mode" value="pt"/>
        <!-- The name of the sheet that will contain the trips information -->
        <param name="tripsSheetName" value="Trips_pt"/>
        <!-- The name of the sheet that will contain vehicles information -->
        <param name="vehiclesSheetName" value="Vehicles_pt"/>
    </parameterset>
</module>
```

## How to use

### Maven Import

To use this module, you can import it using maven by adding the following to your pom.xml

```xml

<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

```xml

<dependency>
    <groupId>com.github.MATSimToCBA</groupId>
    <artifactId>matsim-cba</artifactId>
    <version>master-SNAPSHOT</version>
</dependency>
```

### Java code example
Below is an example of a class that runs a MATSim scenario that features the cba module including a DRT service.

```java
public class RunCbaScenarioWithDrt
{ 
    public static void main(String[] args) {
      Config config = ConfigUtils.loadConfig(args[0], new CbaConfigGroup(), new MultiModeDrtConfigGroup(), new DvrpConfigGroup());
      Controler controler = DrtControlerCreator.createControler(config, false);
      controler.addOverridingModule(new CbaModule());
      controler.run();
    }
}
```
