# json2ttb Documentation
#### For version 1.3.0-alpha

## Contents
1. [Introduction](#intro)
2. [Using the Program](#use)
 1. [Prerequisites](#pre)
 2. [Running the Program](#run)
 3. [Possible Error Messages](#errors)
3. [Guide to the JSON Structure](#json)
 1. [Starting the File](#start)
 2. [Service Information](#service)
 3. [Adding Events and Times](#events)
 4. [Change Information per Instance](#times)
4. [Get Help](#help)

## Introduction <a name="intro"></a>

json2ttb is a complementary program for Railway Operation Simulator to help in the creation of timetables.

#### Features

| What json2ttb can do.                                               |                         What json2ttb cannot do. (yet) |
|---------------------------------------------------------------------|-------------------------------------------------------:|
| Creates timetables for ROS from user data.                          |       No current support for joins/splits or shuttles. |
| Allows for "odd" repeats of services.                               |         Does not simplify writing individual services. |
| Create templates of service data and use them for several services. | No current support for the ROS repeats (e.g. R;30;2;2) |
| Change the reference and description of single repeats.             |                                                        |
| In most cases, quicker than writing timetables in ROS.              |                                                        |

## Using the Program <a name="use"></a>

### Prerequisites <a name="pre"></a>

#### 1. Ensure you have Java installed

json2ttb uses Java. There is a good chance you already have this installed, if not, you can find more details about how to install [here](https://java.com/en/download/help/download_options.html).

#### 2. A text editor for writing JSON files.

Notepad (or similar) will work fine for this. However, you may find it useful to have a text editor with *code* support which should insert closing brackets and such. I use [Atom](https://atom.io/) by GitHub, but there's many out there.

#### 3. The latest jar of json2ttb.

You can download the latest version from the json2ttb GitHub repository [here](https://github.com/Railway-Op-Sim/ros-json2ttb/releases). In the latest release, select the assets dropdown, then choose the .jar file.

### Running the Program <a name="run"></a>

In a command prompt window (or PowerShell), enter the following command and run it with relevant details filled in.

`java -jar <path to jar> <path to json>`

The ttb file will be created in the same directory as the json file.

### Possible Error Messages <a name="error"></a>

*To Do*

## Guide to the JSON Structure <a name="json"></a>

Read this section carefully as the program will not work if the format is incorrect in your .json file.

#### Key Terminology

- Service: The list of instructions and data that a train will be associated with.
- Instance [of a service]: The actual train which will follow a service (e.g. each repeat of a service.)
- Timetable: The final product, containing all services which can be used with ROS.

#### Example JSON files
Below are a few example files which match the json format, feel free to use these to get a hang of the structure.
- [json2ttb Testing Example](https://github.com/Railway-Op-Sim/ros-json2ttb/blob/master/src/test/java/net/danielgill/ros/json2ttb/test/testJSON.json)
- [South London and Thameslink](https://github.com/Railway-Op-Sim/GB-SouthLondonAndThameslink/blob/master/Program_Timetables/SouthLondonAndThameslink.json)
- [Llandudno Junction](https://github.com/Railway-Op-Sim/GB-LlandudnoJct/blob/master/Program_Timetables/LlandudnoJct_October2021.json)
- [Leeds and Bradford (WIP)](https://github.com/Railway-Op-Sim/GB-LeedsAndBradford/blob/master/Program_Timetables/leedsbradford.json)

### Starting the File <a name="start"></a>

To start, the file must contain curly brackets (an object) at the top and bottom.

```json
{

}
```

To be contained in here will be the start time of the timetable, and an array which will contain all of the services.

```json
{
  "startTime":"07:00",
  "services": [

  ]
}
```
<div style="page-break-after: always;"></div>

### Service Information <a name="service"></a>

To create a new service, we start with an object inside the `services` array. This object **requires** the following information:

```json
{
  "startTime":"07:00",
  "services": [
    {
      "ref": "1A00",
      "description": "Z to C",
      "startSpeed": 20,
      "maxSpeed": 121,
      "mass": 200,
      "maxBrake": 149,
      "power": 1500,
      "increment": 1
    }
  ]
}
```

We can replace `maxSpeed`, `mass`, `maxBrake` and `power` with a `dataTemplate`. Find more information [here](#datatemp).

Within the service object, we also need to include `events` and `times` arrays:

```json
{
  "startTime":"07:00",
  "services": [
    {
      "ref": "1A00",
      "description": "A to B",
      "startSpeed": 20,
      "maxSpeed": 121,
      "mass": 200,
      "maxBrake": 149,
      "power": 1500,
      "increment": 1,
      "events": [

      ],
      "times": [

      ]
    }
  ]
}
```

### Adding Events and Times <a name="events"></a>

Once we have a service, we can add any number of events within the `events` array. The services that we create use the exact same syntax as those in ROS timetables. They must be as strings separated by commas.

**There are two ways to achieve timings for instances**, both will work and can be used interchangeably within a single timetable.

#### Method A: `events` dictate the *actual* times.

This is probably considered the simpler method. Here we use the actual times for the events.

```json
      ...
      "events": [
        "07:00;Snt;6-2 5-2",
        "07:02;07:02;A",
        "07:05;07:05;B",
        "07:07;Fer;15-2"
      ],
      ...
```

The `times` will contain the time in relation to `00:00` that these services will occur. In the below example, we want our service to occur every 30 mins for 4 instances.

```json
      ...
      "times": [
        "00:00", "00:30", "01:00", "01:30"
      ]
      ...
```

This will mean that, in our final timetable we will have 4 instances.
(Remember that `"ref":"1A00"` and `"increment":"1"`)
- 1A00, which enters at 07:00
- 1A01, which enters at 07:30
- 1A02, which enters at 08:00
- 1A03, which enters at 08:30

**We are not required to pick times every x minutes.** We can mix up the times and have some very odd service patterns.

```json
      ...
      "times": [
        "00:00", "00:24", "00:38", "01:00"
      ]
      ...
```

In this example, we will have 4 instances:
- 1A00, which enters at 07:00
- 1A01, which enters at 07:24
- 1A02, which enters at 07:38
- 1A03, which enters at 08:00

#### Method B: `times` dictate the *actual* times.

This method may be more useful if you're copying directly from an external timetable. Here we can create events which all have times before and after some *pivot* time, which could be a significant departure time. You could achieve this like so:

```json
      ...
      "events": [
        "23:58;Snt;6-2 5-2",
        "00:00;00:00;A",
        "00:03;00:03;B",
        "00:05;Fer;15-2"
      ],
      ...
```

This example has the same time differences as the previous examples, however, we have now chosen the *pivot* to be the departure time from station A. Now in `times`, we can specify the departure times from station A, and all other times will be changed accordingly.

```json
      ...
      "times": [
        "07:02", "07:26", "07:40", "08:02"
      ]
      ...
```

You could, for example, extract departure times from a single principle station from an external timetable, and import these into your json file. This is especially helpful if the services do not have a regular departure time pattern.

### Change Information per Instance <a name="times"></a>

For example, we may have a one-off service that extends beyond the usual destination station. For this, we can change the `description` for an individual instance in the `times` array.

To do this, we change one element of the array to be an object, which has a `time` key which contains the time as before, as well as a `description` key with the updated description.

```json
      ...
      "times": [
        "07:02", {"time":"07:26", "description":"Z to D"}, "07:40", "08:02"
      ]
      ...
```

We can also do a similar thing with the service references. Please note, however, the `increment` will still apply the changed ref, so the next instance will have an increment twice over.

```json
      ...
      "times": [
        "07:02", {"time":"07:26", "ref":"2D01"}, "07:40", "08:02"
      ]
      ...
```

This extract from the [South London and Thameslink timetable](https://github.com/Railway-Op-Sim/GB-SouthLondonAndThameslink/blob/master/Program_Timetables/SouthLondonAndThameslink.json) shows how these can be used together to create instances with data from [RealTime Trains](https://www.realtimetrains.co.uk/).

```json
"times": [
  {"time":"06:11","ref":"1P02","description":"Ramsgate to London Victoria"},
  {"time":"06:26","ref":"2A06","description":"Swanley to London Victoria"},
  {"time":"06:41","ref":"1P06","description":"Dover Priory to London Victoria"},
  {"time":"06:43","ref":"2A08","description":"Ashford International to London Victoria"},
  ...
]
```

### Data Templates
