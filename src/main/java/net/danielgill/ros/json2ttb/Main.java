package net.danielgill.ros.json2ttb;

import net.danielgill.ros.json2ttb.json.JSONTimetable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    private static Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IOException, ParseException {
        if(args.length < 1) {
            logger.error("You have not provided a file, please try again.");
            System.exit(0);
        }
        
        File file = new File(args[0]);
        JSONTimetable json = new JSONTimetable(file);
        String ttb = json.createTimetable();
        File outputFile = new File(file.getAbsolutePath().replace(".json", ".ttb"));
        logger.info("Output file will be: " + outputFile.getAbsolutePath());

        FileWriter fw = new FileWriter(outputFile);
        fw.write(ttb);
        fw.close();
    }
}
