package net.danielgill.ros.json2ttb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
        
        System.out.println(file.getAbsolutePath());
        
        File outputFile = new File(file.getAbsolutePath().replace(".json", ".ttb"));
        FileWriter fw = new FileWriter(outputFile);
        fw.write(ttb);
        fw.close();
    }
}
