package btrplace.evaluation;

import btrplace.json.JSONConverterException;
import btrplace.json.model.ModelConverter;
import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Model;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * User: TU HUYNH DANG
 * Date: 5/22/13
 * Time: 3:41 PM
 */
class Solve {

    private boolean vflag = false;
    private boolean cont = false;


    public void readArguments(String args[]) {
        String model_file = "";
        String constraint_file = "";
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
            /*else if (arg.equals("-m")) {
                if (i < args.length)
                    outputfile = args[i++];
                else
                    System.err.println("-m requires a filename");
                if (vflag)
                    System.out.println("model file = " + outputfile);
            }

            else if (arg.equals("-s")) {
                if (i < args.length)
                    outputfile = args[i++];
                else
                    System.err.println("-s requires a filename");
                if (vflag)
                    System.out.println("constraint file = " + outputfile);
            }*/

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
            System.err.println("Usage: Solve [-verbose] [-cd] constraint_file model_file ");
        } else {
            constraint_file = args[args.length - 2];
            model_file = args[args.length - 1];
            System.out.println(constraint_file);
            System.out.println(model_file);
        }


    }

    public Model readModel(String model_file) {
        Model m = new DefaultModel(new DefaultMapping());
        try {
            JSONParser parser = new JSONParser(1);
            Object obj = parser.parse(new FileReader(model_file));
            JSONObject jsonObject = (JSONObject) obj;
            ModelConverter modelConverter = new ModelConverter();
            m = modelConverter.fromJSON(jsonObject);

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONConverterException e) {
            e.printStackTrace();
        }
        return m;
    }


}
