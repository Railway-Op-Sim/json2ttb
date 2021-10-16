package net.danielgill.ros.json2ttb.json;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import net.danielgill.ros.json2ttb.Timetable;
import net.danielgill.ros.service.*;
import net.danielgill.ros.service.parse.ParseEvent;
import net.danielgill.ros.service.reference.Reference;
import net.danielgill.ros.service.template.Template;
import net.danielgill.ros.service.time.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONTimetable {
    private JSONObject json;
    private Timetable timetable;
    
    public JSONTimetable(File file) throws IOException, ParseException {
        org.json.simple.parser.JSONParser jsonParser = new org.json.simple.parser.JSONParser();
        json = (JSONObject) jsonParser.parse(new FileReader(file));
        timetable = new Timetable(new Time(json.get("startTime").toString()));
    }
    
    public String createTimetable() {
        JSONArray services = (JSONArray) json.get("services");
        for(int i = 0; i < services.size(); i++) {
            JSONService s = new JSONService((JSONObject) services.get(i));
            Template template = createTemplate((JSONArray) s.events, s.description);
            JSONArray times = (JSONArray) s.times;
            for(int j = 0; j < times.size(); j++) {
                Object time = times.get(j);
                if(time instanceof JSONObject timeJSON) {
                    String ref = s.ref;
                    String description = s.description;
                    
                    if(timeJSON.containsKey("ref")) {
                        ref = timeJSON.get("ref").toString();
                    } else {
                        ref = ref.substring(0, 2) + String.format("%02d", (Integer.parseInt(ref.substring(2, 4)) + (s.increment * j)));
                    }
                    
                    if(timeJSON.containsKey("description")) {
                        description = timeJSON.get("description").toString();
                    }
                    
                    Service tempService = new Service(new Reference(ref), description, s.startSpeed, s.maxSpeed, s.mass, s.maxBrake, s.power);
                    
                    tempService.addTemplate(template, new Time(timeJSON.get("time").toString()), s.increment * j);
                    
                    timetable.addService(tempService);
                } else {
                    String ref = s.ref;
                    ref = ref.substring(0, 2) + String.format("%02d", (Integer.parseInt(ref.substring(2, 4)) + (s.increment * j)));

                    Service tempService = new Service(new Reference(ref), s.description, s.startSpeed, s.maxSpeed, s.mass, s.maxBrake, s.power);
                    
                    tempService.addTemplate(template, new Time(times.get(j).toString()), s.increment * j);
                    
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
