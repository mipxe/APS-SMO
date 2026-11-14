package smo;

import java.util.function.Consumer;

public class PlacementDispatcher {
    private final Buffer buffer;
    private int bufferPointer = 0; // УБ
    private Consumer<String> logger = _ -> { };

    public PlacementDispatcher(Buffer buffer) {
        this.buffer = buffer;
    }

    public void setLogger(Consumer<String> logger) {
        this.logger = logger;
    }

    public int getBufferPointer() {
        return bufferPointer;
    }

    public Request put(Request r) {
        int cap = buffer.getCapacity();
        if (!buffer.isFull()) {
            int i = findFreeSlotFromPointer();
            buffer.put(i, r);
            logger.accept("Д1ОЗ1: заявка " + r + " записана в ячейку " + i + " (УБ был " + bufferPointer + ")");
            bufferPointer = (i + 1) % cap;
            return null;
        }
        int i = findOldest();
        Request old = buffer.remove(i);
        old.markRefused();
        buffer.put(i, r);
        logger.accept("Д1ОО3: буфер полон -> ОТКАЗ заявке " + old + " (создана в t="
                + String.format(java.util.Locale.US, "%.4f", old.getTimeCreated())
                + ", самая старая); заявка " + r + " записана в ячейку " + i);
        bufferPointer = (i + 1) % cap;
        return old;
    }

    private int findFreeSlotFromPointer() {
        int cap = buffer.getCapacity();
        for (int k = 0; k < cap; k++) {
            int idx = (bufferPointer + k) % cap;
            if (buffer.get(idx) == null) return idx;
        }
        return -1;
    }

    private int findOldest() {
        int best = -1;
        for (int i = 0; i < buffer.getCapacity(); i++) {
            Request r = buffer.get(i);
            if (r != null && (best == -1 || r.getTimeCreated() < buffer.get(best).getTimeCreated())) best = i;
        }
        return best;
    }
}
