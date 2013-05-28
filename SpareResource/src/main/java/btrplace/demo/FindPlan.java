package btrplace.demo;

import btrplace.evaluation.EvaluationTools;
import btrplace.evaluation.IncreasingLoad;
import btrplace.json.JSONConverterException;
import btrplace.json.model.InstanceConverter;
import btrplace.json.model.ModelConverter;
import btrplace.json.model.constraint.SatConstraintsConverter;
import btrplace.json.plan.ReconfigurationPlanConverter;
import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Instance;
import btrplace.model.Model;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: TU HUYNH DANG
 * Date: 5/22/13
 * Time: 3:41 PM
 */
public class FindPlan {

    private static boolean vflag = false;
    private static boolean cont = false;
    private static String model_file = "";
    private static String output_file = "";
    private static String constraints = "";


    public Instance getInstance() {
        Instance instance = new Instance(new DefaultModel(new DefaultMapping()), new ArrayList<SatConstraint>());
        try {
            InstanceConverter instanceConverter = new InstanceConverter();
            instance = instanceConverter.fromJSON(new File(model_file));


        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
        return instance;
    }

    public static Model getModel() {
        ModelConverter modelConverter = new ModelConverter();
        Model m = new DefaultModel(new DefaultMapping());
        try {
            m = modelConverter.fromJSON(new File(model_file));
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(0);
        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
            System.exit(0);
        }
        return m;
    }

    public static Set<SatConstraint> getConstraints() {
        SatConstraintsConverter satConstraintsConverter = new SatConstraintsConverter();
        Set<SatConstraint> ctrs = new HashSet<SatConstraint>();
        try {
            List<SatConstraint> satConstraint = satConstraintsConverter.listFromJSON(new File(constraints));
            ctrs.addAll(satConstraint);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
        return ctrs;
    }

    public static void recordPlan(ReconfigurationPlan plan) {
        if (plan == null) {
            System.out.println("Plan is NULL");
            System.exit(0);
        }
        ReconfigurationPlanConverter rpc = new ReconfigurationPlanConverter();
        try {
            rpc.toJSON(plan, new File(output_file));
        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    private static void parseOptions(String[] args) {
        Options options = new Options();
        options.addOption("c", false, "For continuous restriction");
        options.addOption("d", false, "For discrete restriction");
        options.addOption("m", true, "Model file");
        options.addOption("o", true, "Plan output file");

        CommandLineParser parser = new BasicParser();

        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("c")) {
                cont = true;
            }

            if (line.hasOption("d")) {
                cont = false;
            }

            if (line.hasOption("m")) {
                model_file = line.getOptionValue("m");
            }

            if (line.hasOption("o")) {
                output_file = line.getOptionValue("o");
            } else {
                output_file = "plan.json";
            }

            for (String s : line.getArgs()) {
                constraints = s;
                break;
            }

        } catch (ParseException e) {
            System.err.println("Error: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("generator", options, true);
        }
    }

    public static void main(String[] args) {
        parseOptions(args);
        Model model = getModel();
        Set<SatConstraint> constraints = getConstraints();
        Model fixed_model = EvaluationTools.prepareModel(model, constraints);
        IncreasingLoad incLoad = new IncreasingLoad(fixed_model, constraints);
        ReconfigurationPlan plan = incLoad.run();
        if (plan == null) {
            System.out.println("The constraints are already satisfied or BtrPlace has no solution");
            System.exit(-1);
        }
        recordPlan(plan);
    }
}
