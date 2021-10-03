package net.danielgill.ros.json2ttb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.json.simple.parser.ParseException;

public class Main {
    public static void main(String[] args) throws IOException, FileNotFoundException, ParseException {
        if(args.length < 1) {
            System.err.println("You have not provided a file, please try again.");
            System.exit(0);
        }
        
        File file = new File(args[0]);
        JSONReader json = new JSONReader(file);
        String ttb = json.createTimetable();
        System.out.println(ttb);
    }
}
