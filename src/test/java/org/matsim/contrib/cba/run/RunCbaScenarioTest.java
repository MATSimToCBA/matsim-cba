package org.matsim.contrib.cba.run;

import org.junit.Test;
import org.matsim.contrib.cba.CbaConfigGroup;
import org.matsim.contrib.cba.CbaModule;
import org.matsim.contrib.drt.run.DrtControlerCreator;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;

public class RunCbaScenarioTest {

    @Test
    public void testCba() {
        String configFile = "test_scenario/test_scenario.config.xml";
        Config config = ConfigUtils.loadConfig(configFile, new CbaConfigGroup(), new MultiModeDrtConfigGroup(), new DvrpConfigGroup());
        Controler controler = DrtControlerCreator.createControler(config, false);
        controler.addOverridingModule(new CbaModule());
        controler.run();
    }
}
