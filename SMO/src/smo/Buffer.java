package smo;

import java.util.Arrays;

public class Buffer {
    private final Request[] slots;
    private final int capacity;

    public Buffer(int capacity) {
        this.capacity = capacity;
        this.slots = new Request[capacity];
    }

    public int getCapacity() {
        return capacity;
    }

    public void put(int index, Request r) {
        slots[index] = r;
    }

    public Request get(int index) {
        return slots[index];
    }

    public Request remove(int index) {
        Request r = slots[index];
        slots[index] = null;
        return r;
    }

    public boolean isFull() {
        for (Request r : slots) if (r == null) return false;
        return true;
    }

    public boolean isEmpty() {
        for (Request r : slots) if (r != null) return true;
        return false;
    }

    public Request[] snapshot() {
        return Arrays.copyOf(slots, capacity);
    }
}
