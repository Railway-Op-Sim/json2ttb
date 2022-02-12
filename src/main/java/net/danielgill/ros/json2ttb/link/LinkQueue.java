package net.danielgill.ros.json2ttb.link;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.danielgill.ros.timetable.event.SnsEvent;
import net.danielgill.ros.timetable.reference.Reference;
import net.danielgill.ros.timetable.time.Time;

public class LinkQueue {
    private static Logger logger = LogManager.getLogger(LinkQueue.class);
    private Queue<LinkQueueItem> queue;
    public String parentRef;
    public String childRef;

    public LinkQueue(String parentRef, String childRef) {
        this.parentRef = parentRef;
        this.childRef = childRef;
        queue = new LinkedList<>();
    }

    public void add(String instance_ref, Time fnsTime) {
        LinkQueueItem lqItem = new LinkQueueItem(instance_ref, fnsTime);
        queue.add(lqItem);
    }

    public LinkQueueItem remove() {
        return queue.remove();
    }

    public SnsEvent removeSnsEventAfterTime(Time tm, Reference ref) {
        try {
            LinkQueueItem lqItem = queue.peek();
            if(lqItem.fnsTime.laterThan(tm)) {
                logger.error("Instance {} does not have an arriving service to link with, timetable will not be generated. (Next Arr: {} > Dep: {})", ref, lqItem.fnsTime, tm);
                System.exit(0);
            } else {
                lqItem = queue.remove();
                return new SnsEvent(lqItem.fnsTime, new Reference(lqItem.ref));
            }
        } catch(NullPointerException e) {
            logger.error("There are no arrival services for instance {} to form from, timetable will not generated.", ref);
            System.exit(0);
        }
        return null;
    }

    public LinkQueueItem element() {
        return queue.element();
    }
}