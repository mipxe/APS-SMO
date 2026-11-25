package smo;

import java.util.Random;

public class Device {
    private final int id;
    private final double mu;
    private Request currentRequest;
    private double busyUntil = Double.NaN;
    private final EventCalendar calendar;
    private final Random rnd;

    public Device(int id, double mu, EventCalendar calendar, Random rnd) {
        this.id = id;
        this.mu = mu;
        this.calendar = calendar;
        this.rnd = rnd;
    }

    public int getId() {
        return id;
    }

    public boolean isBusy() {
        return currentRequest != null;
    }

    public Request getCurrentRequest() {
        return currentRequest;
    }

    public double getBusyUntil() {
        return busyUntil;
    }

    public void startService(Request r, double t) {
        currentRequest = r;
        busyUntil = t + serviceTime();
        calendar.schedule(new Event(busyUntil, EventType.DEVICE_FINISHED, null, this));
    }

    public Request finishService() {
        Request r = currentRequest;
        r.markCompleted();
        currentRequest = null;
        busyUntil = Double.NaN;
        return r;
    }

    private double serviceTime() {
        return -Math.log(1.0 - rnd.nextDouble()) / mu;
    }
}
