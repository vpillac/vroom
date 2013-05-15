package vroom.trsp.bench;

import vroom.trsp.util.TRSPGlobalParameters;

public class TRSPBenchParams extends TRSPBench {

    private final TRSPGlobalParameters[] mConfigurations;

    protected TRSPBenchParams(TRSPGlobalParameters baseParams, boolean noStat, String fileCom,
            TRSPGlobalParameters[] config) {
        super(baseParams, noStat, fileCom);
        mConfigurations = config;
    }

    @Override
    public void createRuns() {
        super.createRuns();

    }

    public static void main(String[] args) {

    }
}
