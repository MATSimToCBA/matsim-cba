package org.matsim.contrib.cba.analyzers.genericAnalysis;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.matsim.contrib.cba.CbaAnalysis;
import org.matsim.contrib.cba.CbaConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.events.MobsimScopeEventHandler;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;

public class GenericAnalysisModule extends AbstractModule {

    private final GenericAnalyzerConfigGroup configGroup;
    private final CbaConfigGroup cbaConfigGroup;

    public GenericAnalysisModule(GenericAnalyzerConfigGroup configGroup, CbaConfigGroup cbaConfigGroup) {
        this.configGroup = configGroup;
        this.cbaConfigGroup = cbaConfigGroup;
    }

    @Override
    public void install() {
        installQSimModule(new AbstractQSimModule() {
            @Override
            protected void configureQSim() {
                if(this.getIterationNumber() % cbaConfigGroup.getOutputFrequency() != 0) {
                    return;
                }
                addMobsimScopeEventHandlerBinding().toProvider(new Provider<MobsimScopeEventHandler>() {
                    @Inject
                    CbaAnalysis cbaAnalysis;

                    @Override
                    public MobsimScopeEventHandler get() {
                        GenericAnalyzer genericAnalyzer = new GenericAnalyzer(configGroup);
                        cbaAnalysis.addSingleIterationAnalyzer(genericAnalyzer);
                        return genericAnalyzer;
                    }
                });
            }
        });
    }
}
