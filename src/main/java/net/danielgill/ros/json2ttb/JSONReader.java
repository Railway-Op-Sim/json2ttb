package net.danielgill.ros.json2ttb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import net.danielgill.ros.service.*;
import net.danielgill.ros.service.parse.ParseEvent;
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
                    
                    Service tempService = new Service(new Reference(ref), description, Integer.parseInt(service.get("startSpeed").toString()), 
                            Integer.parseInt(service.get("maxSpeed").toString()), Integer.parseInt(service.get("mass").toString()), 
                            Integer.parseInt(service.get("maxBrake").toString()), Integer.parseInt(service.get("power").toString()));
                    
                    tempService.addTemplate(template, new Time(timeJSON.get("time").toString()), 
                            Integer.parseInt(service.get("increment").toString()) * j);
                    
                    timetable.addService(tempService);
                } else {
                    String ref = service.get("ref").toString();
                    ref = ref.substring(0, 2) + String.format("%02d", (Integer.parseInt(ref.substring(2, 4)) + (Integer.parseInt(service.get("increment").toString()) * j)));
                    
                    Service tempService = new Service(new Reference(ref), service.get("description").toString(), Integer.parseInt(service.get("startSpeed").toString()), 
                            Integer.parseInt(service.get("maxSpeed").toString()), Integer.parseInt(service.get("mass").toString()), 
                            Integer.parseInt(service.get("maxBrake").toString()), Integer.parseInt(service.get("power").toString()));
                    
                    tempService.addTemplate(template, new Time(times.get(j).toString()), 
                            Integer.parseInt(service.get("increment").toString()) * j);
                    
                    timetable.addService(tempService);
                }
            }
        }
        return timetable.getTextTimetable();
    }
    
    private Template createTemplate(JSONArray events, String description) {
        Template template = new Template(description);
        ParseEvent parse = new ParseEvent();
        for(int i = 0; i < events.size(); i++) {
            template.addEvent(parse.getEventFromString(events.get(i).toString()));
        }
        return template;
    }
}
