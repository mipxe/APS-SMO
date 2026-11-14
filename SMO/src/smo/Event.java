package smo;

public class Event {
    private final double time;
    private final EventType type;
    private final Source source;
    private final Device device;
    long seq;

    public Event(double time, EventType type, Source source, Device device) {
        this.time = time;
        this.type = type;
        this.source = source;
        this.device = device;
    }

    public double getTime() {
        return time;
    }
    public EventType getType() {
        return type;
    }
    public Source getSource() {
        return source;
    }
    public Device getDevice() {
        return device;
    }

    public String describe() {
        String who = type == EventType.REQUEST_GENERATED
                ? "источник И" + source.getId()
                : "прибор П" + device.getId();
        return type + " (" + who + ")";
    }

    @Override
    public String toString() {
        return String.format(java.util.Locale.US, "%.4f %s", time, describe());
    }
}
