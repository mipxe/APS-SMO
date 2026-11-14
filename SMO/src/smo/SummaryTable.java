package smo;

public record SummaryTable(
        double tEnd,
        int[] generated,
        int[] rejected,
        int[] processed,
        double[] rejectProbability,
        double[] avgSystem,
        double[] avgWait,
        double[] avgService,
        double[] waitVariance,
        double[] serviceVariance,
        double[] deviceLoad
) {}
