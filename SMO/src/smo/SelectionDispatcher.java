package smo;

import java.util.function.Consumer;

public class SelectionDispatcher {
    private final Buffer buffer;
    private final Device[] devices;
    private int selectPointer = 0; // УВ
    private int devicePointer = 0; // УП
    private int lastSlot = -1;
    private Consumer<String> logger = _ -> { };

    public SelectionDispatcher(Buffer buffer, Device[] devices) {
        this.buffer = buffer;
        this.devices = devices;
    }

    public void setLogger(Consumer<String> logger) {
        this.logger = logger;
    }

    public int getSelectPointer() {
        return selectPointer;
    }
    public int getDevicePointer() {
        return devicePointer;
    }

    public void onBufferChanged(double t) {
        dispatch(t);
    }

    public void onDeviceFreed(double t) {
        dispatch(t);
    }

    private void dispatch(double t) {
        while (buffer.isEmpty()) {
            Device d = selectDevice();
            if (d == null) break;
            Request r = selectRequest();
            int oldPb = selectPointer, oldPp = devicePointer;
            selectPointer = (lastSlot + 1) % buffer.getCapacity();
            devicePointer = (d.getId() + 1) % devices.length;
            assert r != null;
            r.markProcessing(t);
            d.startService(r, t);
            logger.accept("Д2П2/Д2Б3: заявка " + r + " из ячейки " + lastSlot + " -> прибор П" + d.getId()
                    + " (конец обслуживания t=" + String.format(java.util.Locale.US, "%.4f", d.getBusyUntil())
                    + "); УВ " + oldPb + "->" + selectPointer + ", УП " + oldPp + "->" + devicePointer);
        }
        if (buffer.isEmpty()) {
            logger.accept("Все приборы заняты, заявки ждут в буфере; УВ и УП не изменились");
        }
    }

    private Device selectDevice() {
        for (int k = 0; k < devices.length; k++) {
            Device d = devices[(devicePointer + k) % devices.length];
            if (!d.isBusy()) return d;
        }
        return null;
    }

    private Request selectRequest() {
        int cap = buffer.getCapacity();
        for (int k = 0; k < cap; k++) {
            int idx = (selectPointer + k) % cap;
            if (buffer.get(idx) != null) {
                lastSlot = idx;
                return buffer.remove(idx);
            }
        }
        return null;
    }
}
