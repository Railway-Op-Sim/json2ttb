# Using the Program

## Prerequisites

**1. Ensure you have Java installed.**

json2ttb uses Java. There is a good chance you already have this installed, if not, you can find more details about how to install [here](https://java.com/en/download/help/download_options.html).

**2. A text editor for writing JSON files.**

Notepad (or similar) will work fine for this. However, you may find it useful to have a text editor with *code* support which should insert closing brackets and such. I use [Atom](https://atom.io/) by GitHub, but there's many out there.

**3. The latest jar of json2ttb.**

You can download the latest version from the json2ttb GitHub repository [here](https://github.com/Railway-Op-Sim/ros-json2ttb/releases). In the latest release, select the assets dropdown, then choose the .jar file.

## Running the Program

In a command prompt window (or PowerShell), enter the following command and run it with relevant details filled in.

    java -jar <path to jar> <path to json>

The ttb file will be created in the same directory as the json file.