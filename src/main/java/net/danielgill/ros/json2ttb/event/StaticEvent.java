package net.danielgill.ros.json2ttb.event;

import net.danielgill.ros.timetable.event.Event;

public class StaticEvent {
    public int index;
    public Event e;
    
    public StaticEvent(int index, Event e) {
        this.index = index;
        this.e = e;
    }
}
