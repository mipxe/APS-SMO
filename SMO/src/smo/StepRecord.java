package smo;

import java.util.List;

public class StepRecord {
    public int stepNumber;
    public double time;
    public Event event;
    public Event[] calendar;
    public Request[] buffer;
    public int bufferPointer;        // УБ
    public int selectPointer;        // УВ
    public int devicePointer;        // УП
    public String[] deviceStates;
    public String[] sourceStates;
    public int totalGenerated;
    public int totalRejected;
    public List<String> log;
}
