package org.matsim.contrib.cba;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.cba.analyzers.agentsAnalysis.AgentsAnalysisModule;
import org.matsim.contrib.cba.analyzers.agentsAnalysis.AgentsAnalyzerConfigGroup;
import org.matsim.contrib.cba.analyzers.drtAnalysis.DrtAnalysisModule;
import org.matsim.contrib.cba.analyzers.drtAnalysis.DrtAnalyzerConfigGroup;
import org.matsim.contrib.cba.analyzers.privateVehiclesAnalysis.PrivateVehiclesAnalysisModule;
import org.matsim.contrib.cba.analyzers.privateVehiclesAnalysis.PrivateVehiclesAnalyzerConfigGroup;
import org.matsim.contrib.cba.analyzers.ptAnalysis.PtAnalysisModule;
import org.matsim.contrib.cba.analyzers.ptAnalysis.PtAnalyzerConfigGroup;
import org.matsim.contrib.cba.analyzers.walkAnalysis.WalkAnalysisModule;
import org.matsim.contrib.cba.analyzers.walkAnalysis.WalkAnalyzerConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.MatsimServices;


public class CbaModule extends AbstractModule {

    @Override
    public void install() {

        CbaConfigGroup configGroup = (CbaConfigGroup) getConfig().getModules().get(CbaConfigGroup.GROUP_NAME);

        bind(CbaAnalysis.class).toProvider(new Provider<CbaAnalysis>() {
            @Inject
            private MatsimServices matsimServices;
            @Inject
            private Network network;
            @Override
            public CbaAnalysis get() {
                return new CbaAnalysis(configGroup, matsimServices, network);
            }
        }).asEagerSingleton();

        addControlerListenerBinding().toProvider(new Provider<CbaControlerListener>() {
            @Inject
            private CbaAnalysis cbaAnalysis;
            @Inject
            private Network network;
            @Inject
            private MatsimServices matsimServices;
            @Override
            public CbaControlerListener get() {
                return new CbaControlerListener(cbaAnalysis, matsimServices, network, null, null);
            }
        });

        for(DrtAnalyzerConfigGroup drtAnalyzerConfigGroup : configGroup.getDrtAnalyzersConfigs()) {
            install(new DrtAnalysisModule(drtAnalyzerConfigGroup, configGroup));
        }

        for(PtAnalyzerConfigGroup ptAnalyzerConfigGroup: configGroup.getPtAnalyzersConfigs()) {
            install(new PtAnalysisModule(ptAnalyzerConfigGroup, configGroup));
        }

        for(AgentsAnalyzerConfigGroup agentsAnalyzerConfigGroup : configGroup.getAgentsAnalyzersConfigs()) {
            install(new AgentsAnalysisModule(agentsAnalyzerConfigGroup));
        }

        for(PrivateVehiclesAnalyzerConfigGroup privateVehiclesAnalyzerConfigGroup : configGroup.getPrivateVehiclesAnalyzersConfigs()) {
            install(new PrivateVehiclesAnalysisModule(privateVehiclesAnalyzerConfigGroup, configGroup));
        }

        for(WalkAnalyzerConfigGroup walkAnalyzerConfigGroup : configGroup.getWalkAnalyzersConfigs()) {
            install(new WalkAnalysisModule(walkAnalyzerConfigGroup, configGroup));
        }
    }
}
