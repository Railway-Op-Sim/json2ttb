# JSON Structure Guide

Read this section carefully as the program will not work if the format is incorrect in your .json file.

## Key Terminology

* Service: The list of instructions and data that a train will be associated with.
* Instance [of a service]: The actual train which will follow a service (e.g. each repeat of a service.)
* Timetable: The final product, containing all services which can be used with ROS.

## Example JSON Files

Below are a few example files which match the json format, feel free to use these to get a hang of the structure:

* [json2ttb Testing Example](https://github.com/Railway-Op-Sim/ros-json2ttb/blob/master/src/test/java/net/danielgill/ros/json2ttb/test/testJSON.json)
* [South London and Thameslink](https://github.com/Railway-Op-Sim/GB-SouthLondonAndThameslink/blob/master/Program_Timetables/SouthLondonAndThameslink.json)
* [Llandudno Junction](https://github.com/Railway-Op-Sim/GB-LlandudnoJct/blob/master/Program_Timetables/LlandudnoJct_October2021.json)
* [Leeds and Bradford](https://github.com/Railway-Op-Sim/GB-LeedsAndBradford/blob/master/Program_Timetables/leedsbradford.json)
* [Sutton, Epsom and Wimbledon](https://github.com/Railway-Op-Sim/GB-SuttonEpsomWimbledon/blob/master/Program_Timetables/SEW-LondonVictoriaClosure.json)

## Starting the File

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

## Service Information <a name="service"></a>

To create a new service, we start with an object inside the `services` array. This object takes the following information:

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

The `maxSpeed`, `mass`, `maxBrake`, `power` (or `dataTemplate`), and `startSpeed` does not need to be included for services that form from another service. (i.e their first event is a Sns.) If this data is excluded from a service that requires it, it will throw an error message when you run json2ttb, and the resulting file will not contain the offending service, or the `.ttb` may not work at all.

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

### Description Time Updates <a name="desc"></a>

A special syntax within the description can be used to change the description to reflect the time for each service. The description can be changed both in the service description key and, as you will see below, per-instance descriptions.

An example of a description using the syntax is below:

`%t23:30% London Euston to Manchester Piccadilly`

The `t` refers to time, and the area replaced will be between the `%`. This does mean that the `%` **cannot be used elsewhere** in any service description.

If the `times` for this service were `12:03`, `12:43`, `13:23` for example, then the descriptions would be:

`11:33 London Euston to Manchester Piccadilly`, `12:13 London Euston to Manchester Piccadilly` and `12:53 London Euston to Manchester Piccadilly`

## Adding Events and Times <a name="events"></a>

Once we have a service, we can add any number of events within the `events` array. The services that we create use the exact same syntax as those in ROS timetables. They must be as strings separated by commas.

**There are two ways to achieve timings for instances**, both will work and can be used interchangeably within a single timetable.

### Method A: `events` dictate the *actual* times.

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

This will mean that, in our final timetable we will have 4 instances:
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

### Method B: `times` dictate the *actual* times.

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

### Excluding the `times` Array

If you were to exclude the `times` array completely, then the program will assume that the service has a single instance at `00:00`. For example, if a service runs for a single time and the service is written using Method A above, then you can exclude the `times` array.

### Advanced Event Changes <a name="event-changes"></a>

A new feature for version 1.2.0 allows you to change (add/remove/edit) events for a single instance, or a collection of instances for a single service. This could be useful, for example, if one instance of a service stops at an additional station and you wanted to include it without having to write a whole new service.

To achieve this, we replace a event in the list with an array (square brackets), which contains one or more objects, each of which has one key containing the ***service ref*** or any ***regex*** to represent any number of services. For example:

```json
    ...
    "events": [
      "23:58;Snt;6-2 5-2",
      "00:00;00:00;A",
      "00:03;00:03;B",
      [{"1A01":"00:05;00:05;C"}],
      "00:07;Fer;15-2"
    ],
    ...
```

Or another example, where only instances 1A01 and 1A03 stop at C, and only 1A02 stops at B using regex:

```json
    ...
    "events": [
      "23:58;Snt;6-2 5-2",
      "00:00;00:00;A",
      [{"1A02":"00:03;00:03;B"}],
      [{"1A01|1A03":"00:05;00:05;C"}],
      "00:07;Fer;15-2"
    ],
    ...
```

**In the above two cases, you may get warnings as some instances will not have events (e.g. 1A02 in the first example), however this can safley be ignored.**

You can also include several intances in a single event array as below:

```json
      ...
      [{"1A01":"00:05;00:05;C"},{"....":"00:06;00:06;C"}],
      ...
```

In this case, the first valid object for an instance will be chosen, for example, for `1A01`, it will pick the first option, however, for all others it will pick the second option.

## Change Information per Instance <a name="times"></a>

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
    ...
    "times": [
    {"time":"06:11","ref":"1P02","description":"Ramsgate to London Victoria"},
    {"time":"06:26","ref":"2A06","description":"Swanley to London Victoria"},
    {"time":"06:41","ref":"1P06","description":"Dover Priory to London Victoria"},
    {"time":"06:43","ref":"2A08","description":"Ashford International to London Victoria"},
    ...
    ]
    ...
```

## Data Templates <a name="datatemp"></a>

We can generalise `maxSpeed`, `mass`, `maxBrake` and `power` for each service using a data template, which hold all of this information. This is done by using a `dataTemplate` instead of the above keys. This will contain a keyword that refers to a given template.

```json
{
  "startTime":"07:00",
  "services": [
    {
      "ref": "1A00",
      "description": "A to B",
      "startSpeed": 20,
      "dataTemplate": "TestData",
      "increment": 1,
      "events": [
        ...
      ],
      "times": [
        ...
      ]
    }
  ]
}
```

Data templates can also be used per-instance, so that:

```json
      ...
      "times": [
        "12:00",{"time":"12:30","dataTemplate":"TestData"},"13:00"
      ]
      ...
```

We can create a **custom template** by adding a new array between `startTime` and `services`, which contains an object with keys for `keyword`, `maxSpeed`, `mass`, `maxBrake` and `power`.

```json
{
    "startTime": "07:00",
    "dataTemplates": [
        {
            "keyword":"TestData",
            "maxSpeed": 150,
            "mass": 100,
            "maxBrake": 20,
            "power": 25
        }
    ],
    "services": [
      ...
    ]
}
```

There are also a large number of pre-defined templates for a large number of UK trains from Mark's great stock data spreadsheet. When using them as a data template, the `keyword` is generally of the form:

`C<class>_<carriages/cars>` or `C<class>_<subclass>_<carriages/cars>`

For example, a 2-car Class 150/1 is `C150_1_2`.

A list of pre-defined templates can be found in [Appendix A](appendixA.md).

## Linking Services (EXPERIMENTAL)

**Linking services is an experimental feature that is yet to be tested thoroughly, it may not work properly! Please report any issues you experience with it.**

Services that terminate and then depart from a station as a different service can be linked. Linking these services means that json2ttb will look for the next available departure for a service after it arrives at a station and updates the change of service times respectively. To link services, several things must be true:

* The service must terminate at a station, change using `Fns` forming a new service using `Sns`.
* The arrival service must be listed before the departing service in the json file.
* The `Fns` and `Sns` should be at `00:00` time. (This may not be required, more testing to come.)
* There must be exactly the same number of arrival services to departing services.

To add linking, you include a `becomes` key in the arrival service, and a `from` key in the departing service. For example, two services that link are:

```json
{
    "startTime": "06:00",
    "services": [
        {
            "ref":"1A01",
            "description":"Terminating",
            "startSpeed":80,
            "maxSpeed": 121,
            "mass": 200,
            "maxBrake": 149,
            "power": 1500,
            "increment": 2,
            "becomes":"1A02",
            "events": [
                "23:40;Snt;2-3 2-2",
                "23:59;A",
                "00:00;cdt",
                "00:00;Fns;1A02"
            ],
            "times": [
                "07:13","07:30","07:37","07:45"
            ]
        },
        {
            "ref":"1A02",
            "description":"Departing",
            "increment":2,
            "from":"1A01",
            "events": [
                "00:00;Sns;1A01",
                "00:00;A",
                "00:19;Fer;2-3"
            ],
            "times": [
                "07:15","07:35","07:37","07:48"
            ]
        }
    ]
}
```

When the parser reaches a `becomes` service it stores all the arrivals to the station, then when it reads the `from` service, it picks the service that could sensibly form it.

In the above example, `1A02` will be formed from `1A01`. The `Sns;1A01` line will be changed to the same time as the `Fns;1A02`, but the **departure time will not be changed**. So, `1A02` will `Sns` at `07:13`, but not depart until `07:15`. Likewise, `1A04` (the next repeat) will `Sns` at `07:30`, but not depart until `07:35`.