package smo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Simulation {
    private final Source[] sources;
    private final Device[] devices;
    private final Buffer buffer;
    private final EventCalendar calendar;
    private final PlacementDispatcher placeDisp;
    private final SelectionDispatcher selectDisp;
    private final Statistic statistic;
    private double currentTime = 0;
    private final int maxRequests;
    private int scheduledGenerations = 0;
    private int stepNumber = 0;
    private List<String> stepLog = new ArrayList<>();

    public Simulation(int nSources, int nDevices, int bufferCapacity,
                      double lambda, double mu, int maxRequests, long seed) {
        Random rnd = new Random(seed);
        this.maxRequests = maxRequests;
        this.calendar = new EventCalendar();
        this.buffer = new Buffer(bufferCapacity);
        this.sources = new Source[nSources];
        for (int i = 0; i < nSources; i++) sources[i] = new Source(i, lambda, calendar, rnd);
        this.devices = new Device[nDevices];
        for (int i = 0; i < nDevices; i++) devices[i] = new Device(i, mu, calendar, rnd);
        this.placeDisp = new PlacementDispatcher(buffer);
        this.selectDisp = new SelectionDispatcher(buffer, devices);
        this.statistic = new Statistic(nSources, nDevices);
        placeDisp.setLogger(s -> stepLog.add(s));
        selectDisp.setLogger(s -> stepLog.add(s));

        for (Source s : sources) {
            if (scheduledGenerations < maxRequests) {
                s.scheduleNext(0);
                scheduledGenerations++;
            }
        }
    }

    public boolean isFinished() {
        return calendar.isEmpty();
    }

    public double getCurrentTime() {
        return currentTime;
    }

    public StepRecord step() {
        if (isFinished()) throw new IllegalStateException("Моделирование завершено");
        stepLog = new ArrayList<>();
        Event e = calendar.next();
        currentTime = e.getTime();
        stepNumber++;
        if (e.getType() == EventType.REQUEST_GENERATED) onRequestGenerated(e);
        else onDeviceFinished(e);
        return snapshot(e);
    }

    public SummaryTable runAuto() {
        while (!isFinished()) step();
        return getSummary();
    }

    public SummaryTable getSummary() {
        return statistic.getSummaryTable(currentTime);
    }

    public StepRecord initialRecord() {
        stepLog = new ArrayList<>();
        stepLog.add("Запуск модели: поставлено событий REQUEST_GENERATED: " + scheduledGenerations);
        return snapshot(null);
    }

    private void onRequestGenerated(Event e) {
        double t = currentTime;
        Source src = e.getSource();
        Request r = src.createRequest(t);
        statistic.recordGenerated(r);
        stepLog.add("Источник И" + src.getId() + " создал заявку " + r);

        if (scheduledGenerations < maxRequests) {
            src.scheduleNext(t);
            scheduledGenerations++;
        } else {
            stepLog.add("Лимит заявок исчерпан: источник И" + src.getId() + " больше не планирует заявки");
        }

        Request refused = placeDisp.put(r);
        if (refused != null) statistic.recordRejected(refused);

        selectDisp.onBufferChanged(t);
    }

    private void onDeviceFinished(Event e) {
        double t = currentTime;
        Device d = e.getDevice();
        Request r = d.finishService();
        statistic.recordServiceFinish(r, d.getId(), t);
        stepLog.add(String.format(Locale.US, "Прибор П%d закончил заявку %s (ожидание %.4f, обслуживание %.4f)",
                d.getId(), r, r.getTimeStart() - r.getTimeCreated(), t - r.getTimeStart()));
        selectDisp.onDeviceFreed(t);
    }

    private StepRecord snapshot(Event e) {
        StepRecord rec = new StepRecord();
        rec.stepNumber = stepNumber;
        rec.time = currentTime;
        rec.event = e;
        rec.calendar = calendar.snapshot();
        rec.buffer = buffer.snapshot();
        rec.bufferPointer = placeDisp.getBufferPointer();
        rec.selectPointer = selectDisp.getSelectPointer();
        rec.devicePointer = selectDisp.getDevicePointer();
        rec.totalGenerated = statistic.getTotalGenerated();
        rec.totalRejected = statistic.getTotalRejected();
        rec.log = stepLog;

        rec.deviceStates = new String[devices.length];
        for (int i = 0; i < devices.length; i++) {
            Device d = devices[i];
            rec.deviceStates[i] = d.isBusy()
                    ? String.format(Locale.US, "П%d: ЗАНЯТ заявкой %s (старт %.4f, конец %.4f)", i,
                        d.getCurrentRequest(), d.getCurrentRequest().getTimeStart(), d.getBusyUntil())
                    : "П" + i + ": свободен";
        }

        rec.sourceStates = new String[sources.length];
        for (int i = 0; i < sources.length; i++) {
            int g = statistic.getGenerated(i), rj = statistic.getRejected(i);
            rec.sourceStates[i] = String.format(Locale.US,
                    "И%d: создано %d, отказов %d (%.1f%%), обслужено %d",
                    i, g, rj, g == 0 ? 0.0 : 100.0 * rj / g, statistic.getProcessed(i));
        }
        return rec;
    }
}
