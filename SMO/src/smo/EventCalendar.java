package smo;

import java.util.Comparator;
import java.util.PriorityQueue;

public class EventCalendar {
    private final PriorityQueue<Event> events = new PriorityQueue<>(
            Comparator.comparingDouble(Event::getTime).thenComparingLong(e -> e.seq));
    private long counter = 0;

    public void schedule(Event e) {
        e.seq = counter++;
        events.add(e);
    }

    public Event next() {
        return events.poll();
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }

    public Event[] snapshot() {
        return events.stream()
                .sorted(Comparator.comparingDouble(Event::getTime).thenComparingLong(e -> e.seq))
                .toArray(Event[]::new);
    }
}
