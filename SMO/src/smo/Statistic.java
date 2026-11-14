package smo;

public class Statistic {
    private final int nSources;
    private final int[] generated;
    private final int[] rejected;
    private final int[] processed;
    private final double[] waitSum;
    private final double[] waitSqSum;
    private final double[] serviceSum;
    private final double[] serviceSqSum;
    private final double[] deviceBusyTime;

    public Statistic(int nSources, int nDevices) {
        this.nSources = nSources;
        int n = nSources + 1;
        generated = new int[n];
        rejected = new int[n];
        processed = new int[n];
        waitSum = new double[n];
        waitSqSum = new double[n];
        serviceSum = new double[n];
        serviceSqSum = new double[n];
        deviceBusyTime = new double[nDevices];
    }

    public int getTotalGenerated() {
        return generated[nSources];
    }
    public int getTotalRejected() {
        return rejected[nSources];
    }
    public int getGenerated(int src) {
        return generated[src];
    }
    public int getRejected(int src) {
        return rejected[src];
    }
    public int getProcessed(int src) {
        return processed[src];
    }

    public void recordGenerated(Request r) {
        generated[r.getSourceId()]++;
        generated[nSources]++;
    }

    public void recordRejected(Request r) {
        rejected[r.getSourceId()]++;
        rejected[nSources]++;
    }

    public void recordServiceFinish(Request r, int deviceId, double t) {
        double wait = r.getTimeStart() - r.getTimeCreated();
        double service = t - r.getTimeStart();
        for (int idx : new int[]{r.getSourceId(), nSources}) {
            processed[idx]++;
            waitSum[idx] += wait;
            waitSqSum[idx] += wait * wait;
            serviceSum[idx] += service;
            serviceSqSum[idx] += service * service;
        }
        deviceBusyTime[deviceId] += service;
    }

    public SummaryTable getSummaryTable(double tEnd) {
        int n = nSources + 1;
        double[] pRej = new double[n], sys = new double[n], w = new double[n], s = new double[n];
        double[] wVar = new double[n], sVar = new double[n];
        for (int i = 0; i < n; i++) {
            pRej[i] = generated[i] == 0 ? 0 : (double) rejected[i] / generated[i];
            if (processed[i] > 0) {
                w[i] = waitSum[i] / processed[i];
                s[i] = serviceSum[i] / processed[i];
                sys[i] = w[i] + s[i];
                wVar[i] = Math.max(0, waitSqSum[i] / processed[i] - w[i] * w[i]);
                sVar[i] = Math.max(0, serviceSqSum[i] / processed[i] - s[i] * s[i]);
            }
        }
        double[] load = new double[deviceBusyTime.length];
        for (int i = 0; i < load.length; i++) load[i] = tEnd > 0 ? deviceBusyTime[i] / tEnd : 0;
        return new SummaryTable(tEnd, generated.clone(), rejected.clone(), processed.clone(),
                pRej, sys, w, s, wVar, sVar, load);
    }
}
