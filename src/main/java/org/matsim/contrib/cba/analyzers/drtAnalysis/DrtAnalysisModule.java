package org.matsim.contrib.cba.analyzers.drtAnalysis;

import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.cba.CbaAnalysis;
import org.matsim.contrib.cba.CbaConfigGroup;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.dvrp.fleet.Fleet;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeModule;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeQSimModule;


public class DrtAnalysisModule extends AbstractDvrpModeModule {

    private final DrtAnalyzerConfigGroup configGroup;
    private final CbaConfigGroup cbaConfigGroup;

    public DrtAnalysisModule(DrtAnalyzerConfigGroup configGroup, CbaConfigGroup cbaConfigGroup) {
        super(configGroup.getMode());
        this.configGroup = configGroup;
        this.cbaConfigGroup = cbaConfigGroup;
    }

    @Override
    public void install() {
        installQSimModule(new AbstractDvrpModeQSimModule(this.getMode()) {
            @Override
            protected void configureQSim() {
                if(this.getIterationNumber() % cbaConfigGroup.getOutputFrequency() != 0) {
                    return;
                }
                addMobsimScopeEventHandlerBinding().toProvider(modalProvider(getter -> {
                    Network network = getter.get(Network.class);
                    Fleet fleet = getter.getModal(Fleet.class);
                    CbaAnalysis cbaAnalysis = getter.get(CbaAnalysis.class);
                    DrtAnalyzer drtAnalyzer = new DrtAnalyzer(this.getMode(), network, this.getScheme(), fleet, configGroup);
                    cbaAnalysis.addSingleIterationAnalyzer(drtAnalyzer);
                    return drtAnalyzer;
                }));
            }

            private DrtConfigGroup.OperationalScheme getScheme(){
                MultiModeDrtConfigGroup multiModeDrtConfigGroup = (MultiModeDrtConfigGroup) getConfig().getModules().get(MultiModeDrtConfigGroup.GROUP_NAME);
                DrtConfigGroup.OperationalScheme scheme = DrtConfigGroup.OperationalScheme.stopbased;
                for(DrtConfigGroup drtConfigGroup : multiModeDrtConfigGroup.getModalElements()) {
                    if(drtConfigGroup.getMode().equals(this.getMode())) {
                        scheme = drtConfigGroup.getOperationalScheme();
                    }
                }
                return scheme;
            }
        });
    }
}