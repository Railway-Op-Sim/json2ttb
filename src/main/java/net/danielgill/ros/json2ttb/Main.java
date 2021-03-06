package net.danielgill.ros.json2ttb;

import net.danielgill.ros.json2ttb.json.JSONTimetable;
import net.danielgill.ros.timetable.time.Time;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    private static Logger logger = LogManager.getLogger(Main.class);
    private static FileWriter fw;
    private static boolean debug = false;

    public static void main(String[] args) throws IOException {
        Options options = new Options();
        options.addOption("f", "file", true, "The json file to be parsed.");
        options.addOption("i", "interval", true, "The time interval for generating several timetables.");
        options.addOption("t", "times", true, "The number of extra timetables to generate with the given interval.");
        options.addOption("d", "debug", false, "Runs the program in debug mode.");
        options.addOption("h", "help", false, "Opens this help message.");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        
        try {
            cmd = parser.parse(options, args);

            if(cmd.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("json2ttb.jar", options);
                System.exit(0);
            }

            if(cmd.hasOption("d")) {
                debug = true;
                Configurator.setLevel(logger.getName(), Level.DEBUG);
            }

            File file = new File("");

            if(cmd.hasOption("f")) {
                file = new File(cmd.getOptionValue("f"));
            } else {
                if(args.length == 1) {
                    file = new File(args[0]);
                    logger.warn("Assuming the single argument detected is input file, please use '-f <path to file>' in the future.");
                } else {
                    logger.error("Cannot find the .json file in the program arguments, please use '-f <path to file>'.");
                    System.exit(0);
                }
            }

            logger.debug("JSON file found at {}", file.getAbsolutePath());
            
            JSONTimetable json = new JSONTimetable(file, debug);
            String ttb = json.createTimetable();
            File outputFile = new File(file.getAbsolutePath().replace(".json", ".ttb"));
            logger.info("Output file will be: {}", outputFile.getAbsolutePath());
            fw = new FileWriter(outputFile);
            fw.write(ttb);
            fw.close();
            if(cmd.hasOption("i") && cmd.hasOption("t")) {
                Time interval = new Time(cmd.getOptionValue("i"));
                int intervalMins = interval.getMinutes();
                int times = Integer.parseInt(cmd.getOptionValue("t"));

                for(int i = 0; i < times; i++) {
                    json = new JSONTimetable(file, debug, interval);
                    ttb = json.createTimetable();
                    outputFile = new File(file.getAbsolutePath().replace(".json", "-") + json.getStartTime().toString().replace(":", "") + ".ttb");
                    logger.info("{} file will be: {}", interval, outputFile.getAbsolutePath());
                    fw = new FileWriter(outputFile);
                    fw.write(ttb);
                    fw.close();
                    interval.addMinutes(intervalMins);
                }
            } else if(cmd.hasOption("i")) {
                logger.error("You are missing the -t argument, see --help for more information.");
            } else if(cmd.hasOption("t")) {
                logger.error("You are missing the -i argument, see --help for more information.");
            }
            
        } catch (org.apache.commons.cli.ParseException e) {
            logger.error("Unexpected error in command line arguments.");
            System.exit(0);
        } catch (IOException e) {
            logger.error("Error reading/writing file, make sure the file exists or you have permission to create a file.");
            System.exit(0);
        } catch (ParseException e) {
            logger.error("Error parsing JSON, make sure that the JSON is valid.");
            System.exit(0);
        }
    }
}
