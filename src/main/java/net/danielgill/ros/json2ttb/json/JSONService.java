package net.danielgill.ros.json2ttb.json;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONService {
	public final JSONArray events;
	public final String ref;
	public final String description;

	public final JSONArray times;

	public final int startSpeed;
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

		startSpeed = Integer.parseInt(service.get("startSpeed").toString());
		maxSpeed = Integer.parseInt(service.get("maxSpeed").toString());
		mass = Integer.parseInt(service.get("mass").toString());
		maxBrake = Integer.parseInt(service.get("maxBrake").toString());
		power = Integer.parseInt(service.get("power").toString());

		increment = Integer.parseInt(service.get("increment").toString());
	}
}
