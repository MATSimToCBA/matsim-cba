package org.matsim.contrib.cba.analyzers.walkAnalysis;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.matsim.contrib.cba.CbaAnalysis;
import org.matsim.contrib.cba.CbaConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.events.MobsimScopeEventHandler;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;

public class WalkAnalysisModule extends AbstractModule {

    private final WalkAnalyzerConfigGroup configGroup;
    private final CbaConfigGroup cbaConfigGroup;

    public WalkAnalysisModule(WalkAnalyzerConfigGroup configGroup, CbaConfigGroup cbaConfigGroup) {
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
                        WalkAnalyzer walkAnalyzer = new WalkAnalyzer(configGroup);
                        cbaAnalysis.addSingleIterationAnalyzer(walkAnalyzer);
                        return walkAnalyzer;
                    }
                });
            }
        });
    }
}
