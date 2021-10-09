package net.danielgill.ros.json2ttb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import net.danielgill.ros.service.*;
import net.danielgill.ros.service.event.*;
import net.danielgill.ros.service.location.*;
import net.danielgill.ros.service.reference.Reference;
import net.danielgill.ros.service.template.Template;
import net.danielgill.ros.service.time.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONReader {
    private JSONObject json;
    private Timetable timetable;
    
    public JSONReader(File file) throws FileNotFoundException, IOException, IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        json = (JSONObject) jsonParser.parse(new FileReader(file));
        timetable = new Timetable(new Time(json.get("startTime").toString()));
    }
    
    public String createTimetable() {
        JSONArray services = (JSONArray) json.get("services");
        for(int i = 0; i < services.size(); i++) {
            JSONObject service = (JSONObject) services.get(i);
            Template template = createTemplate((JSONArray) service.get("events"), service.get("description").toString());
            JSONArray times = (JSONArray) service.get("times");
            for(int j = 0; j < times.size(); j++) {
                Object time = times.get(j);
                if(time instanceof JSONObject) {
                    JSONObject timeJSON = (JSONObject) time;
                    String ref = service.get("ref").toString();
                    String description = service.get("description").toString();
                    if(timeJSON.containsKey("ref")) {
                        ref = timeJSON.get("ref").toString();
                    } else {
                        ref = ref.substring(0, 2) + String.format("%02d", (Integer.parseInt(ref.substring(2, 4)) + (Integer.parseInt(service.get("increment").toString()) * j)));
                    }
                    if(timeJSON.containsKey("description")) {
                        description = timeJSON.get("description").toString();
                    }
                    Service tempService = new Service(new Reference(ref), timeJSON.get("description").toString(), Integer.parseInt(service.get("startSpeed").toString()), Integer.parseInt(service.get("maxSpeed").toString()), Integer.parseInt(service.get("mass").toString()), Integer.parseInt(service.get("maxBrake").toString()), Integer.parseInt(service.get("power").toString()));
                    tempService.addTemplate(template, new Time(timeJSON.get("time").toString()));
                    timetable.addService(tempService);
                } else {
                    String ref = service.get("ref").toString();
                    ref = ref.substring(0, 2) + String.format("%02d", (Integer.parseInt(ref.substring(2, 4)) + (Integer.parseInt(service.get("increment").toString()) * j)));
                    Service tempService = new Service(new Reference(ref), service.get("description").toString(), Integer.parseInt(service.get("startSpeed").toString()), Integer.parseInt(service.get("maxSpeed").toString()), Integer.parseInt(service.get("mass").toString()), Integer.parseInt(service.get("maxBrake").toString()), Integer.parseInt(service.get("power").toString()));
                    tempService.addTemplate(template, new Time(times.get(j).toString()));
                    timetable.addService(tempService);
                }
            }
        }
        return timetable.getTextTimetable();
    }
    
    private Template createTemplate(JSONArray events, String description) {
        Template template = new Template(description);
        for(int i = 0; i < events.size(); i++) {
            template.addEvent(getEventFromString(events.get(i).toString()));
        }
        return template;
    }
    
    private Event getEventFromString(String eventString) {
        //TODO: Add more events in here when they are added to the main library.
        String[] eventSplit = eventString.split(";");
        if(eventSplit.length == 2) {
            if(eventSplit[1].equalsIgnoreCase("cdt")) {
                return new CdtEvent(new Time(eventSplit[0]));
            } else {
                return new StopEvent(new Time(eventSplit[0]), new NamedLocation(eventSplit[1]));
            }
        } else if(eventSplit.length == 3) {
            if(eventSplit[1].equalsIgnoreCase("Fer")) {
                return new FerEvent(new Time(eventSplit[0]), new Location(eventSplit[2]));
            } else if(eventSplit[1].equalsIgnoreCase("Fns")) {
                return new FnsEvent(new Time(eventSplit[0]), new Reference(eventSplit[2]));
            } else if(eventSplit[1].equalsIgnoreCase("pas")) {
                return new PassEvent(new Time(eventSplit[0]), new NamedLocation(eventSplit[2]));
            } else if(eventSplit[1].equalsIgnoreCase("Sns")) {
                return new SnsEvent(new Time(eventSplit[0]), new Reference(eventSplit[2]));
            } else if(eventSplit[1].equalsIgnoreCase("Snt")) {
                return new SntEvent(new Time(eventSplit[0]), new StartLocation(eventSplit[2]));
            } else {
                return new StopEvent(new Time(eventSplit[0]), new Time(eventSplit[1]), new NamedLocation(eventSplit[2]));
            }
        }
        
        return null;
    }
}
