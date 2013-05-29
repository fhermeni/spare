package btrplace.demo;

import btrplace.evaluation.ModelGenerator;
import btrplace.json.JSONConverterException;
import btrplace.json.model.ModelConverter;
import btrplace.json.model.constraint.SatConstraintsConverter;
import btrplace.model.Model;
import btrplace.model.constraint.*;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * User: TU HUYNH DANG
 * Date: 5/28/13
 * Time: 3:38 PM
 */
public class Generator {

    private static boolean continuous;
    private static String constraint_name;
    private static int nNode;
    private static int nVM;

    private enum Constraint {
        among, ban, CReC, CVmC, fence, gather, killed, lonely, offline, online, overbook, preserve, quarantine,
        ready, root, running, SVMT, SReC, SVmC, split, splitAmong, spread
    }

    public static void main(String[] args) {
        parseOptions(args);
        generateConstraint(Constraint.valueOf(constraint_name));
    }

    private static void parseOptions(String[] args) {
        Options options = new Options();
        options.addOption("c", false, "For continuous restriction");
        options.addOption("n", true, "Number of nodes");
        options.addOption("m", true, "Number of VMs");
        options.addOption("t", true, "Constraint Name");
        CommandLineParser parser = new BasicParser();

        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("c")) {
                continuous = true;
            }

            if (line.hasOption("n")) {
                nNode = Integer.parseInt(line.getOptionValue("n"));
            }

            if (line.hasOption("m")) {
                nVM = Integer.parseInt(line.getOptionValue("m"));
            }

            if (line.hasOption("t")) {
                constraint_name = line.getOptionValue("t");
            }

        } catch (ParseException e) {
            System.err.println("Error: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("generator", options, true);
        }
    }

    public static void generateConstraint(Constraint name) {
        Set<SatConstraint> constraintSet = new HashSet<SatConstraint>();
        ModelGenerator gen = new ModelGenerator();
        Model model = gen.generateModel(nNode, nVM);
        switch (name) {
            case among:
                Among among = new Among(gen.getRandomVMs(nNode / 3), gen.getDistinctSet(4), continuous);
                constraintSet.add(among);
                break;

            case spread:
                Spread spread = new Spread(gen.getSpreadVMs(nNode - 3), continuous);
                constraintSet.add(spread);
                break;

            case ban:
                Ban ban = new Ban(gen.getRandomVMs(4), gen.getRandomNodes(nNode / 2));
                constraintSet.add(ban);
                break;

            case fence:
                Fence fence = new Fence(gen.getRandomVMs(4), gen.getRandomNodes(nNode / 2));
                constraintSet.add(fence);
                break;

            case lonely:
                Lonely lonely = new Lonely(gen.getRandomVMs(4), continuous);
                constraintSet.add(lonely);
                break;

            case quarantine:
                Quarantine quarantine = new Quarantine(gen.getRandomNodes(4));
                constraintSet.add(quarantine);
                break;
        }

        recordModel(model);
        recordConstraints(constraintSet);
    }

    public static void recordConstraints(Set<SatConstraint> constraints) {
        SatConstraintsConverter converter = new SatConstraintsConverter();
        try {
            converter.toJSON(constraints, new File(constraint_name + "Constraint.json"));

        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    public static void recordModel(Model model) {
        ModelConverter converter = new ModelConverter();
        try {
            converter.toJSON(model, new File(constraint_name + "Model.json"));
        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }
}
