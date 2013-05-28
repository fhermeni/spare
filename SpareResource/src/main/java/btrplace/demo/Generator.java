package btrplace.demo;

import btrplace.json.JSONConverterException;
import btrplace.json.model.constraint.SatConstraintsConverter;
import btrplace.model.constraint.SatConstraint;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * User: TU HUYNH DANG
 * Date: 5/28/13
 * Time: 3:38 PM
 */
public class Generator {
    public static void main(String[] args) {
        parseOptions(args);
    }

    private static void parseOptions(String[] args) {
        Options options = new Options();
        options.addOption("c", false, "for continuous restriction");
        options.addOption("d", false, "for discrete restriction");
        options.addOption("p", true, "plan file");
        options.addOption("m", true, "model file");

        CommandLineParser parser = new BasicParser();

        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("c")) {
                boolean c = true;
                System.out.println(c);
            }

            if (line.hasOption("p")) {
                System.out.println(line.getOptionValue("p"));
            }

            if (line.hasOption("m")) {
                System.out.println(line.getOptionValue("m"));
            }

            for (String s : line.getArgs()) {
                System.out.println(s);
            }


        } catch (ParseException e) {
            System.err.println("Error: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("generator", options, true);
        }
    }

    public void recordConstraints(Set<SatConstraint> constraints) {
        SatConstraintsConverter converter = new SatConstraintsConverter();
        try {
            converter.toJSON(constraints, new File("constraints.json"));

        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }
}
