package net.danielgill.ros.json2ttb.link;

import net.danielgill.ros.timetable.time.Time;

public class LinkQueueItem {
    public String ref;
    public Time fnsTime;

    public LinkQueueItem(String ref, Time fnsTime) {
        this.ref = ref;
        this.fnsTime = fnsTime;
    }
}