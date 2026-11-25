package smo;

import java.util.ArrayList;
import java.util.List;

public record AutoMode(int nSources, int nDevices, int capacity, double lambda, double mu, long seed) {
    public static final double T_ALPHA = 1.643;
    public static final double DELTA = 0.1;
    public static final int N0 = 100;
    public static final int MAX_N = 2_000_000;
    public static final int MAX_ITERATIONS = 100;

    public record Iteration(int index, int n, double p, double diff, double threshold, boolean accurate) {
    }

    public static class Result {
        public final List<Iteration> iterations = new ArrayList<>();
        public SummaryTable table;
        public int finalN;
        public boolean converged;
        public String note = "";
    }

    public static int requiredN(double p, int currentN) {
        if (p <= 0) return (int) Math.min(MAX_N, 2L * currentN);
        double n = T_ALPHA * T_ALPHA * (1 - p) / (DELTA * DELTA * p);
        return (int) Math.max(1, Math.min(MAX_N, Math.ceil(n)));
    }

    public Result run() {
        Result res = new Result();
        int n = N0;
        double pPrev = Double.NaN;

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            SummaryTable table = new Simulation(nSources, nDevices, capacity, lambda, mu, n, seed + i).runAuto();
            double p = table.rejectProbability()[nSources];

            boolean accurate = false;
            double diff = Double.NaN, threshold = Double.NaN;
            if (i > 0) {
                diff = Math.abs(pPrev - p);
                threshold = DELTA * pPrev;
                accurate = (pPrev == 0 && p == 0) || diff < threshold;
            }
            res.iterations.add(new Iteration(i, n, p, diff, threshold, accurate));
            res.table = table;
            res.finalN = n;

            if (accurate) {
                res.converged = true;
                if (p == 0) res.note = "Отказов не зафиксировано: вероятность отказа равна 0.";
                return res;
            }
            if (n >= MAX_N) {
                res.note = "Достигнут предел числа заявок (" + MAX_N + "), заданная точность не подтверждена.";
                return res;
            }
            pPrev = p;
            n = requiredN(p, n);
        }
        res.note = "Достигнут предел числа итераций (" + MAX_ITERATIONS + "), заданная точность не подтверждена.";
        return res;
    }
}
