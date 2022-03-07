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

import net.danielgill.ros.json2ttb.event.StaticEvent;
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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
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

    public JSONTimetable(File file, boolean debug) throws IOException, ParseException {
        if(debug) {
            Configurator.setLevel(logger.getName(), Level.DEBUG);
        }
        JSONParser jsonParser = new JSONParser();
        json = (JSONObject) jsonParser.parse(new FileReader(file));
        timetable = new Timetable(new Time(json.get(START_TIME).toString()));
        this.startTime = new Time(json.get(START_TIME).toString());
        warnEarlyService = true;
        earlyRefs = new ArrayList<>();
        links = new HashMap<>();
    }
    
    public JSONTimetable(File file, boolean debug, Time interval) throws IOException, ParseException {
        if(debug) {
            Configurator.setLevel(logger.getName(), Level.DEBUG);
        }
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
                logger.debug("Added custom data template {}", keyword);
            }
        }
        
        for(int i = 0; i < services.size(); i++) {
            JSONService s = new JSONService((JSONObject) services.get(i));
            logger.debug("Working on service {}", s.ref);
            logger.debug("");
            
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

                    logger.debug("  Found object instance with ref {} at time {} ({})", ref, tm.toString(), description);
                    
                    Service tempService;
                    
                    if(timeJSON.containsKey("dataTemplate")) {
                        Data instancedata = new Data(s.startSpeed, dts.getTemplate(timeJSON.get("dataTemplate").toString()).getData());
                        tempService = new Service(new Reference(ref), description, instancedata);
                    } else {
                        tempService = new Service(new Reference(ref), description, data);
                    }

                    tempService = createService(tempService, s, ref, description, tm, j);
                    
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

                    logger.debug("  Found instance with ref {} at time {} ({})", ref, tm.toString(), description);

                    Service tempService = new Service(new Reference(ref), description, data);
                    tempService = createService(tempService, s, ref, description, tm, j);
                    
                    if(!checkEarlyService(tempService.getEventFromIndex(0), tempService.getRef())) {
                        timetable.addService(tempService);
                    } else {
                        earlyRefs.add(tempService.getRef().toString());
                        if(warnEarlyService) {
                            logger.warn("Instance {} starts before timetable start time, it will not be included.", tempService.getRef());
                        }
                    }
                }
                logger.debug("");
            }
        }
        try {
            return timetable.getTextTimetable();
        } catch (ServiceInvalidException e) {
            logger.error(String.format("Unexpected error in timetable ({}): {}", e.getRef(), e.getMessage()));
            System.exit(0);
            return null;
        }
    }

    public Time getStartTime() {
        return this.startTime;
    }

    private Service createService(Service tempService, JSONService s, String ref, String description, Time tm, int j) {
        ArrayList<StaticEvent> staticEvents = new ArrayList<>();

        Template template = createTemplate(s.events, ref, description, staticEvents, s.suppressWarnings);
        logger.debug("      Template created successfully.");

        if(s.linksForward) {
            FnsEvent fns = (FnsEvent) template.getEvents().get(template.getEventCount() - 1);
            links.get(s.ref).add(ref, new Time(fns.getTime()).addMinutes(tm.getMinutes()));
            logger.debug("      Instance links forward, storing the Fns time: {}", fns.getTime().addMinutes(tm.getMinutes()).toString()); 
        }

        tempService.addTemplate(template, tm, s.increment * j);
        logger.debug("      Added template to service with time offset {}.", tm.toString());

        for(StaticEvent evt : staticEvents) {
            tempService.setEventAtIndex(evt.index, evt.e);
            logger.debug("      Setting static event {}.", evt.e.toString());
        }

        if(s.linksBackward) {
            SnsEvent sns = links.get(s.from).removeSnsEventAfterTime(tm, tempService.getRef());
            tempService.setEventAtIndex(0, sns);
            logger.debug("      Instance links backward, setting Sns event to match incoming time: {}", sns.toString());
        }
        
        return tempService;
    }
    
    private Template createTemplate(JSONArray events, String reference, String description, ArrayList<StaticEvent> staticEvents, boolean suppressWarnings) {
        Template template = new Template(description);
        for(int i = 0; i < events.size(); i++) {
            Object evt = events.get(i);
            if(evt instanceof JSONArray) {
                logger.debug("Checking regex for event in array {}", evt.toString());
                JSONArray evts = (JSONArray) evt;
                boolean matches = false;
                eventcheck:
                for(int j = 0; j < evts.size(); j++) {
                    JSONObject obj = (JSONObject) evts.get(j);
                    Set<String> set = castStringSet((obj).keySet());
                    for(String regex : set) {
                        if(Pattern.matches(regex, reference)) {
                            template.addEvent(createEvent(((JSONObject) obj).get(regex).toString(), staticEvents, i));
                            matches = true;
                            break eventcheck;
                        }
                    }
                }
                if(!matches && !suppressWarnings) {
                    logger.info("Service with ref {} does not match any regex for an event.", reference);
                }
            } else {
                template.addEvent(createEvent(events.get(i).toString(), staticEvents, i));
            }
        }
        return template;
    }

    private Event createEvent(String evt, ArrayList<StaticEvent> staticEvents, int index) {
        ParseEvent parse = new ParseEvent();
        if(evt.startsWith("`")) {
            evt = evt.substring(1);
            staticEvents.add(new StaticEvent(index, parse.getEventFromString(evt)));
        }
        return parse.getEventFromString(evt);
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
        while(old.contains("%r")) {
            int index = old.indexOf("%r");
            String timeUpd = old.substring(index, index + 8);
            Time descTime = new Time(old.substring(index + 2, index + 7));
            descTime = descTime.getNewAddMinutes(tm.getMinutes());
            old = old.replace(timeUpd, descTime.toString().replace(":", ""));
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
