package org.matsim.contrib.cba.analyzers.agentsAnalysis;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.cba.CbaAnalysis;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.listener.ControlerListener;

public class AgentsAnalysisModule extends AbstractModule {

    private final AgentsAnalyzerConfigGroup configGroup;

    public AgentsAnalysisModule(AgentsAnalyzerConfigGroup configGroup) {
        this.configGroup = configGroup;
    }

    @Override
    public void install() {
        addControlerListenerBinding().toProvider(new Provider<ControlerListener>() {

            @Inject
            private Population population;

            @Inject
            private CbaAnalysis cbaAnalysis;

            @Override
            public ControlerListener get() {
                AgentsAnalyzer agentsAnalyzer = new AgentsAnalyzer(configGroup, population);
                cbaAnalysis.addPermanentAnalyzer(agentsAnalyzer);
                return agentsAnalyzer;
            }
        });
    }
}
