package net.danielgill.ros.json2ttb;

import java.util.ArrayList;
import net.danielgill.ros.service.Service;
import net.danielgill.ros.service.ServiceInvalidException;
import net.danielgill.ros.service.time.Time;

public class Timetable {
    private ArrayList<Service> services;
    private final Time startTime;
    
    public Timetable(Time startTime) {
        services = new ArrayList<>();
        this.startTime = startTime;
    }
    
    public void addService(Service service) {
        services.add(service);
    }
    
    public String getTextTimetable() {
        String output = "";
        output += startTime.toString();
        for(Service service : services) {
            try {
                output += "\u0000" + service.toTimetableString();
            } catch (ServiceInvalidException e) {
                System.err.println("[" + e.getRef() + "]: " + e.getMessage());
            }
        }
        output += "\u0000";
        return output;
    }
}
