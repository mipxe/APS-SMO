package smo;

public class Request {
    private final int id;
    private final int sourceId;
    private final double timeCreated;
    private double timeStart = Double.NaN;
    private RequestStatus status = RequestStatus.CREATED;

    public Request(int id, int sourceId, double timeCreated, Coordinate coorFrom, Coordinate coorTo) {
        this.id = id;
        this.sourceId = sourceId;
        this.timeCreated = timeCreated;
    }

    public int getSourceId() { return sourceId; }
    public double getTimeCreated() { return timeCreated; }
    public double getTimeStart() { return timeStart; }

    public void markProcessing(double t) {
        this.timeStart = t;
        this.status = RequestStatus.PROCESSING;
    }

    public void markCompleted() {
        this.status = RequestStatus.COMPLETED;
    }

    public void markRefused() {
        this.status = RequestStatus.REFUSED;
    }

    public String getId() {
        return sourceId + "." + id;
    }

    @Override
    public String toString() {
        return getId();
    }
}
