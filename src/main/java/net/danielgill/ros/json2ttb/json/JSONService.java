package net.danielgill.ros.json2ttb.json;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONService {
	public final JSONArray events;
	public final String ref;
	public final String description;

	public final JSONArray times;

	public final int startSpeed;
        public final boolean usesDataTemplate;
        public final String dataTemplate;
	public final int maxSpeed;
	public final int mass;
	public final int maxBrake;
	public final int power;

	public final int increment;

	public JSONService(JSONObject service) {
		events = (JSONArray) service.get("events");
		ref = service.get("ref").toString();
		description = service.get("description").toString();

		times = (JSONArray) service.get("times");
                
                if(service.containsKey("startSpeed")) {
                    startSpeed = Integer.parseInt(service.get("startSpeed").toString());
                } else {
                    startSpeed = 0;
                }
                
                if(service.containsKey("dataTemplate")) {
                    usesDataTemplate = true;
                    dataTemplate = service.get("dataTemplate").toString();
                    maxSpeed = 0;
                    mass = 0;
                    maxBrake = 0;
                    power = 0;
                } else if(service.containsKey("maxSpeed") && service.containsKey("mass") && service.containsKey("maxBrake") && service.containsKey("power")) {
                    usesDataTemplate = false;
                    dataTemplate = "";
                    maxSpeed = Integer.parseInt(service.get("maxSpeed").toString());
                    mass = Integer.parseInt(service.get("mass").toString());
                    maxBrake = Integer.parseInt(service.get("maxBrake").toString());
                    power = Integer.parseInt(service.get("power").toString());
                } else {
                    usesDataTemplate = false;
                    dataTemplate = "";
                    maxSpeed = -1;
                    mass = -1;
                    maxBrake = -1;
                    power = -1;
                }
                
		increment = Integer.parseInt(service.get("increment").toString());
	}
}
