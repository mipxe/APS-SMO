package smo;

import java.util.Random;

public class Source {
    private final int id;
    private final double lambda;
    private int requestCounter = 0;
    private final EventCalendar calendar;
    private final Random rnd;

    public Source(int id, double lambda, EventCalendar calendar, Random rnd) {
        this.id = id;
        this.lambda = lambda;
        this.calendar = calendar;
        this.rnd = rnd;
    }

    public int getId() { return id; }

    public Request createRequest(double t) {
        requestCounter++;
        Coordinate from = new Coordinate(55.60 + 0.30 * rnd.nextDouble(), 37.40 + 0.40 * rnd.nextDouble());
        Coordinate to = new Coordinate(55.60 + 0.30 * rnd.nextDouble(), 37.40 + 0.40 * rnd.nextDouble());
        return new Request(requestCounter, id, t, from, to);
    }

    public void scheduleNext(double t) {
        calendar.schedule(new Event(t + nextInterval(), EventType.REQUEST_GENERATED, this, null));
    }

    private double nextInterval() {
        return -Math.log(1.0 - rnd.nextDouble()) / lambda;
    }
}
