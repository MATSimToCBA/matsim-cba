package org.matsim.contrib.cba.run;

import org.junit.Test;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.cba.CbaConfigGroup;
import org.matsim.contrib.cba.CbaModule;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.DrtControlerCreator;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.drt.speedup.DrtSpeedUpParams;
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

    @Test
    public void testCbaWithDrtSpeedUp() {
        String configFile = "test_scenario/test_scenario.config.xml";
        Config config = ConfigUtils.loadConfig(configFile, new CbaConfigGroup(), new MultiModeDrtConfigGroup(), new DvrpConfigGroup());
        for (DrtConfigGroup drtCfg : MultiModeDrtConfigGroup.get(config).getModalElements()) {
            if (drtCfg.getDrtSpeedUpParams().isEmpty()) {
                drtCfg.addParameterSet(new DrtSpeedUpParams());
            }
        }
        Controler controler = DrtControlerCreator.createControler(config, false);
        for (Person person : controler.getScenario().getPopulation().getPersons().values()) {
            person.getPlans().removeIf((plan) -> plan != person.getSelectedPlan());
        }
        controler.addOverridingModule(new CbaModule());
        controler.run();
    }
}
