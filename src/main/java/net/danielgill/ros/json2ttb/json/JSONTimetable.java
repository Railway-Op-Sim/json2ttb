package net.danielgill.ros.json2ttb.json;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.danielgill.ros.json2ttb.link.LinkQueue;
import net.danielgill.ros.timetable.*;
import net.danielgill.ros.timetable.data.Data;
import net.danielgill.ros.timetable.data.DataTemplates;
import net.danielgill.ros.timetable.event.Event;
import net.danielgill.ros.timetable.event.FnsEvent;
import net.danielgill.ros.timetable.event.SfsEvent;
import net.danielgill.ros.timetable.event.SnsEvent;
import net.danielgill.ros.timetable.event.SntEvent;
import net.danielgill.ros.timetable.parse.ParseEvent;
import net.danielgill.ros.timetable.reference.Reference;
import net.danielgill.ros.timetable.service.Service;
import net.danielgill.ros.timetable.service.ServiceInvalidException;
import net.danielgill.ros.timetable.template.Template;
import net.danielgill.ros.timetable.time.Time;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONTimetable {
    private final static String START_TIME = "startTime";
    private JSONObject json;
    private Timetable timetable;
    private static Logger logger = LogManager.getLogger(JSONTimetable.class);
    private boolean warnEarlyService;
    private Time startTime;
    private List<String> earlyRefs;
    private Map<String, LinkQueue> links;

    public JSONTimetable(File file) throws IOException, ParseException {

        JSONParser jsonParser = new JSONParser();
        json = (JSONObject) jsonParser.parse(new FileReader(file));
        timetable = new Timetable(new Time(json.get(START_TIME).toString()));
        this.startTime = new Time(json.get(START_TIME).toString());
        warnEarlyService = true;
        earlyRefs = new ArrayList<>();
        links = new HashMap<>();
    }
    
    public JSONTimetable(File file, Time interval) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        json = (JSONObject) jsonParser.parse(new FileReader(file));
        timetable = new Timetable(new Time(json.get(START_TIME).toString()).addMinutes(interval.getMinutes()));
        this.startTime = new Time(json.get(START_TIME).toString()).addMinutes(interval.getMinutes());
        warnEarlyService = false;
        earlyRefs = new ArrayList<>();
        links = new HashMap<>();
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
            
            Data data;
            if(s.usesDataTemplate) {
                data = new Data(s.startSpeed, dts.getTemplate(s.dataTemplate).getData());
            } else {
                data = new Data(s.startSpeed, s.maxSpeed, s.mass, s.maxBrake, s.power);
            }

            if(s.linksForward) {
                links.put(s.ref, new LinkQueue(s.ref, s.becomes));
            }
            
            JSONArray times = (JSONArray) s.times;
            for(int j = 0; j < times.size(); j++) {
                Object time = times.get(j);
                if(time instanceof JSONObject) {
                    JSONObject timeJSON = (JSONObject) time;
                    String ref = s.ref;
                    String description = s.description;
                    Time tm = new Time(timeJSON.get("time").toString());
                    
                    if(timeJSON.containsKey("ref")) {
                        ref = timeJSON.get("ref").toString();
                    } else {
                        ref = ref.substring(0, 2) + String.format("%02d", (Integer.parseInt(ref.substring(2, 4)) + (s.increment * j)));
                    }
                    
                    if(timeJSON.containsKey("description")) {
                        description = timeJSON.get("description").toString();
                    }
                    
                    description = updateDescription(description, tm);
                    
                    Service tempService;
                    
                    if(timeJSON.containsKey("dataTemplate")) {
                        Data instancedata = new Data(s.startSpeed, dts.getTemplate(timeJSON.get("dataTemplate").toString()).getData());
                        tempService = new Service(new Reference(ref), description, instancedata);
                    } else {
                        tempService = new Service(new Reference(ref), description, data);
                    }

                    Template template = createTemplate(s.events, ref, description);
                    
                    if(s.linksForward) {
                        FnsEvent fns = (FnsEvent) template.getEvents().get(template.getEventCount() - 1);
                        links.get(s.ref).add(ref, new Time(fns.getTime()).addMinutes(tm.getMinutes()));
                    }

                    tempService.addTemplate(template, tm, s.increment * j);

                    if(s.linksBackward) {
                        SnsEvent sns = links.get(s.from).removeSnsEventAfterTime(tm, tempService.getRef());
                        tempService.setEventAtIndex(0, sns);
                    }
                    
                    if(!checkEarlyService(tempService.getEventFromIndex(0), tempService.getRef())) {
                        timetable.addService(tempService);
                    } else {
                        earlyRefs.add(tempService.getRef().toString());
                        if(warnEarlyService) {
                            logger.warn("Instance {} starts before timetable start time, it will not be included.", tempService.getRef());
                        }
                    }
                } else {
                    String ref = s.ref;
                    ref = ref.substring(0, 2) + String.format("%02d", (Integer.parseInt(ref.substring(2, 4)) + (s.increment * j)));

                    Time tm = new Time(times.get(j).toString());
                    
                    String description = s.description;
                    description = updateDescription(description, tm);

                    Service tempService = new Service(new Reference(ref), description, data);
                    
                    Template template = createTemplate(s.events, ref, description);

                    if(s.linksForward) {
                        FnsEvent fns = (FnsEvent) template.getEvents().get(template.getEventCount() - 1);
                        links.get(s.ref).add(ref, new Time(fns.getTime()).addMinutes(tm.getMinutes()));
                    }

                    tempService.addTemplate(template, tm, s.increment * j);

                    if(s.linksBackward) {
                        SnsEvent sns = links.get(s.from).removeSnsEventAfterTime(tm, tempService.getRef());
                        tempService.setEventAtIndex(0, sns);
                    }
                    
                    if(!checkEarlyService(tempService.getEventFromIndex(0), tempService.getRef())) {
                        timetable.addService(tempService);
                    } else {
                        earlyRefs.add(tempService.getRef().toString());
                        if(warnEarlyService) {
                            logger.warn("Instance {} starts before timetable start time, it will not be included.", tempService.getRef());
                        }
                    }
                }
            }
        }
        try {
            return timetable.getTextTimetable();
        } catch (ServiceInvalidException e) {
            logger.error(String.format("Unexpected error in timetable service %s", e.getRef()));
            System.exit(0);
            return null;
        }
    }

    public Time getStartTime() {
        return this.startTime;
    }
    
    private Template createTemplate(JSONArray events, String reference, String description) {
        Template template = new Template(description);
        ParseEvent parse = new ParseEvent();
        for(int i = 0; i < events.size(); i++) {
            Object evt = events.get(i);
            if(evt instanceof JSONObject) {
                Set<String> set = castStringSet(((JSONObject) evt).keySet());
                boolean matches = false;
                for(String regex : set) {
                    if(Pattern.matches(regex, reference)) {
                        template.addEvent(parse.getEventFromString(((JSONObject) evt).get(regex).toString()));
                        matches = true;
                        break;
                    }
                }
                if(!matches) {
                    logger.info("Service with ref {} does not match any regex for an event.", reference);
                }
            } else {
                template.addEvent(parse.getEventFromString(events.get(i).toString()));
            }
        }
        return template;
    }

    private static Set<String> castStringSet(Collection<?> c) {
        Set<String> r = new HashSet<String>();
        for(Object o : c) {
            r.add(o.toString());
        }
        return r;
    }
    
    private String updateDescription(String old, Time tm) {
        while(old.contains("%t")) {
            int index = old.indexOf("%t");
            String timeUpd = old.substring(index, index + 8);
            Time descTime = new Time(old.substring(index + 2, index + 7));
            descTime = descTime.getNewAddMinutes(tm.getMinutes());
            old = old.replace(timeUpd, descTime.toString());
        }
        return old;
    }

    private boolean checkEarlyService(Event evt, Reference ref) {
        if(evt instanceof SntEvent snt) {
            return (snt.getTime().earlierThan(startTime));
        } else if(evt instanceof SnsEvent sns) {
            if(sns.getTime().earlierThan(startTime)) {
                return true;
            } else {
                return (earlyRefs.contains(sns.getRef().toString()));
            }
        } else if(evt instanceof SfsEvent sfs) {
            if(sfs.getTime().earlierThan(startTime)) {
                return true;
            } else {
                return (earlyRefs.contains(sfs.getRef().toString()));
            }
        } else {
            logger.error("Instance {} does not appear to have a starting event type, please check.", ref.getRef());
            return false;
        }
    }
}
