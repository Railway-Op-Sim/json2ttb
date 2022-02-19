# json2ttb

A Java project designed for simplyify the timetable writing process, whilst also adding several new timetable features.

json2ttb works best for routes with frequent repeating services, especially if they repeat at odd intervals.

## Key Features

* Allows for unusual repeats of services.
* Create templates of service data and use them for several services.
* Change the reference and description of single repeats.
* Allows for creating shuttles without using shuttle syntax.
* In most cases, quicker than writing timetables in ROS.

**What json2ttb cannot do... (yet)**

* Does not simplify writing individual services.
* No current support for the ROS repeats (e.g. R;30;2;2), repeats are done in a different way.
