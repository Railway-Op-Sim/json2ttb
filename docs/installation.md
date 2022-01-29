# Using the Program

## Prerequisites

**1. Ensure you have Java installed.**

json2ttb uses Java. There is a good chance you already have this installed, if not, you can find more details about how to install [here](https://java.com/en/download/help/download_options.html).

**2. A text editor for writing JSON files.**

Notepad (or similar) will work fine for this. However, you may find it useful to have a text editor with *code* support which should insert closing brackets and such. I use [Atom](https://atom.io/) by GitHub, but there's many out there.

**3. The latest jar of json2ttb.**

You can download the latest version from the json2ttb GitHub repository [here](https://github.com/Railway-Op-Sim/ros-json2ttb/releases). In the latest release, select the assets dropdown, then choose the .jar file.

## Running the Program

In a command prompt or terminal window, enter the following command and run it with relevant details filled in.

    java -jar <path to jar> -f <path to json>

The ttb file will be created in the same directory as the json file, the location of the new ttb file will be printed to the command line.

### Creating Later Starting Timetables

json2ttb allows the user to create any number of timetables that start later than the original start time. This can allow the user to experience more of the timetable by having the option to start later. To do this, you can add two more arguments to the command. For example:

    java -jar <path to jar> -f <path to json> -i 01:00 -t 5

will create 5 **extra** timetables each with a start time 1 hour after the last, the first of which will **start 1 hour after the original.** 

### Command Line Help

There is a command help menu built into the program, which can be found by typing:

    java -jar <path to jar> -h

or

    java -jar <path to jar> --help