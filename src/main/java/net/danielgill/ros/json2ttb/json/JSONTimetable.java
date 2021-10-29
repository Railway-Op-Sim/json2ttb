package net.danielgill.ros.json2ttb.json;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import net.danielgill.ros.json2ttb.Timetable;
import net.danielgill.ros.service.*;
import net.danielgill.ros.service.data.Data;
import net.danielgill.ros.service.data.DataTemplates;
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
        JSONParser jsonParser = new JSONParser();
        json = (JSONObject) jsonParser.parse(new FileReader(file));
        timetable = new Timetable(new Time(json.get("startTime").toString()));
    }
    
    public String createTimetable() {
        JSONArray services = (JSONArray) json.get("services");
        
        DataTemplates dts = new DataTemplates();
        if(json.containsKey("dataTemplates")) {
            JSONArray custom = (JSONArray) json.get("dataTemplates");
            for(int i = 0; i < custom.size(); i++) {
                JSONObject customTemp = (JSONObject) custom.get(i);
                String keyword = customTemp.get("keyword").toString();
                int maxSpeed = Integer.parseInt(customTemp.get("maxSpeed").toString());
                int mass = Integer.parseInt(customTemp.get("mass").toString());
                int maxBrake = Integer.parseInt(customTemp.get("maxBrake").toString());
                int power = Integer.parseInt(customTemp.get("power").toString());
                dts.addTemplate(keyword, maxSpeed, mass, maxBrake, power);
            }
        }
        
        for(int i = 0; i < services.size(); i++) {
            JSONService s = new JSONService((JSONObject) services.get(i));
            Template template = createTemplate((JSONArray) s.events, s.description);
            
            Data data;
            
            if(s.usesDataTemplate) {
                data = new Data(s.startSpeed, dts.getTemplate(s.dataTemplate).getData());
            } else {
                data = new Data(s.startSpeed, s.maxSpeed, s.mass, s.maxBrake, s.power);
            }
            
            JSONArray times = (JSONArray) s.times;
            for(int j = 0; j < times.size(); j++) {
                Object time = times.get(j);
                if(time instanceof JSONObject) {
                    JSONObject timeJSON = (JSONObject) time;
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
                    
                    Service tempService = new Service(new Reference(ref), description, data);

                    tempService.addTemplate(template, new Time(timeJSON.get("time").toString()), s.increment * j);
                    
                    timetable.addService(tempService);
                } else {
                    String ref = s.ref;
                    ref = ref.substring(0, 2) + String.format("%02d", (Integer.parseInt(ref.substring(2, 4)) + (s.increment * j)));

                    Service tempService = new Service(new Reference(ref), s.description, data);
                    
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
