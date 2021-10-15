# json2ttb
A Java application which can create an ROS timetable from a structured JSON timetable.

## Using the program
- Download the latest `.jar` file from the releases.
- Create/use a `.json` file that is in the structure described below.
- In a command line enter: `java -jar <path to jar> <path to json>`
- The `.ttb` file will be produced in the same directory as the `.json`

## JSON Structure Example

Please note, comments are not supported in json, **these are here for illustrative purposes only!** If you'd like a similar file without comments, you can find it [here](https://github.com/Railway-Op-Sim/ros-json2ttb/blob/master/src/test/java/net/danielgill/ros/json2ttb/test/testJSON.json).

```json lines
{
    "startTime": "07:00",               // The start time of the timetable is provided at the top of the file.
    "services": [                       // This array contains the services, although, as you'll...
        {                               // ...see later a service can contain several instances.
            "ref": "1A01",              // This is the reference for the first instance of a service (if it repeats).
            "description": "A to B",    // The service description.
            "startSpeed": 120,          // The usual values for the service go here.
            "maxSpeed": 150,
            "mass": 100,
            "maxBrake": 20,
            "power": 25,
            "increment": 2,             // This is the increment to the last two digits of the ref for each repeat.
            "events": [                 // This array contains the events for the service, each as it's own string.
                "23:58;Snt;8-7 9-7",    // The zero hour (00:00) will be replaced by each of the times below...
                "00:01;00:02;A",        // ...and any time before or after 00:00 will be changed to fit.
                "00:04;00:04;B",
                "00:07;Fer;21-2"
            ],
            "times": [                  // These times refer to each instance of the service.
                "07:04","07:34","07:54" // Unlike the ROS R;x;x;x feature, you can pick any values for these...
            ]                           // ...and it will change the event times as above
        },
        {
            "ref": "1A02",              // See above service.
            "description": "B to A",
            "startSpeed": 120,
            "maxSpeed": 150,
            "mass": 100,
            "maxBrake": 20,
            "power": 25,
            "increment": 2,             // Despite this being non-repeating, this is still required!
            "events": [                 // For this service, which has only one instance, you can replace...
                "07:14;Snt;21-1 21-2",  // ...the 00:00 times with the actual times.
                "07:16;07:16;B",
                "07:19;07:19;A",
                "07:22;Fer;8-7"
            ],
            "times": [                   // If you use actual times, you should enter 00:00 here.
                "00:00",{"time":"00:30","description":"C to A"}                  
            ]                            // As with the above line, you can insert different...
        }                                // ...information for other instances of the service.
    ]
}
```
