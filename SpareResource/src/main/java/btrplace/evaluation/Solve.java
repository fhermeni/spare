package btrplace.evaluation;

import btrplace.json.JSONConverterException;
import btrplace.json.model.InstanceConverter;
import btrplace.json.model.ModelConverter;
import btrplace.json.model.constraint.SatConstraintsConverter;
import btrplace.json.plan.ActionConverter;
import btrplace.json.plan.ReconfigurationPlanConverter;
import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Instance;
import btrplace.model.Model;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * User: TU HUYNH DANG
 * Date: 5/22/13
 * Time: 3:41 PM
 */
class Solve {

    private boolean vflag = false;
    private boolean cont = false;
    String instance_file = "";
    String outputfile = "";


    public void readArguments(String args[]) {

        int i = 0, j;
        String arg;
        char flag;

        while (i < args.length && args[i].startsWith("-")) {
            arg = args[i++];

            // use this type of check for "wordy" arguments
            if (arg.equals("-verbose") || arg.equals("-v")) {
                System.out.println("verbose mode on");
                vflag = true;
            }

            // use this type of check for arguments that require arguments
            else if (arg.equals("-o")) {
                if (i < args.length)
                    outputfile = args[i++];
                else
                    System.err.println("-m requires a filename");
                if (vflag)
                    System.out.println("output file = " + outputfile);
            }

            // use this type of check for a series of flag arguments
            else {
                for (j = 1; j < arg.length(); j++) {
                    flag = arg.charAt(j);
                    switch (flag) {
                        case 'd':
                            cont = false;
                            if (vflag) System.out.println("Continuous:" + cont);
                            break;
                        case 'c':
                            cont = true;
                            if (vflag) System.out.println("Continuous:" + cont);
                            break;
                        default:
                            System.err.println("Solve: illegal option " + flag);
                            break;
                    }
                }
            }
        }

        if (i == args.length) {
            System.err.println("Usage: Solve [-verbose] [-cd] constraint_file instance_file ");
        } else {
            instance_file = args[args.length - 1];

            if (outputfile.equals("")) {
            /*    File file = new File(instance_file);
                String absolutePath = file.getAbsolutePath();
                String filePath = absolutePath.
                        substring(0,absolutePath.lastIndexOf(File.separator));
                outputfile = filePath + File.separator + "plan.json";*/
                outputfile = "plan.json";
            }

        }


    }

    public Model getModelFromFile(String file) {
        Model m = new DefaultModel(new DefaultMapping());
        try {
            ModelConverter modelConverter = new ModelConverter();
            m = modelConverter.fromJSON(new FileReader(file));

        } catch (FileNotFoundException e) {
            System.out.println("File Not Found" + instance_file);
        } catch (JSONConverterException e) {
            System.out.println("JSON exception");
        } catch (IOException e) {
            System.out.println("I/O exception");
        }
        return m;
    }

    public Instance getInstance() {
        Instance instance = new Instance(new DefaultModel(new DefaultMapping()), new ArrayList<SatConstraint>());
        try {
            InstanceConverter instanceConverter = new InstanceConverter();
            instance = instanceConverter.fromJSON(new File(instance_file));


        } catch (FileNotFoundException e) {
            System.out.println("File Not Found" + instance_file);
        } catch (JSONConverterException e) {
            System.out.println("JSON exception");
        } catch (IOException e) {
            System.out.println("I/O exception");
        }
        return instance;
    }

    public Model getModel() {
        return getInstance().getModel();
    }

    public Set<SatConstraint> getConstraints() {
        return new HashSet<SatConstraint>(getInstance().getConstraints());
    }

    public void recordPlan(ReconfigurationPlan plan) {
        recordConstraints();
        ReconfigurationPlanConverter rpc = new ReconfigurationPlanConverter();
        ActionConverter co = new ActionConverter();
        try {
            for (Action action : plan) {
                System.out.println(co.toJSONString(action));
            }
            rpc.toJSON(plan, new File(outputfile));


        } catch (JSONConverterException e) {
            System.out.println("JSON exception");
        } catch (IOException e) {
            System.out.println("I/O exception");
        }
    }

    public void recordConstraints() {
        SatConstraintsConverter converter = new SatConstraintsConverter();
        try {
            converter.toJSON(getConstraints(), new File("constraints.json"));

        } catch (JSONConverterException e) {
            System.out.println("JSON exception");
        } catch (IOException e) {
            System.out.println("I/O exception");
        }
    }
}
