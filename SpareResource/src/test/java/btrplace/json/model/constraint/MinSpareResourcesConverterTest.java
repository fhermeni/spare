package btrplace.json.model.constraint;

import btrplace.json.JSONConverterException;
import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.constraint.MinSpareResources;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * User: TU HUYNH DANG
 * Date: 5/24/13
 * Time: 9:53 AM
 */
public class MinSpareResourcesConverterTest {

    @Test
    public void test() {
        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();

        Set<Node> s = new HashSet<Node>(Arrays.asList(n1, n2));
        MinSpareResources c = new MinSpareResources(s, "ucpu", 3);
        MinSpareResourcesConverter converter = new MinSpareResourcesConverter();
        converter.setModel(model);
        try {
            File file = new File("mSR.json");
            converter.toJSON(c, file);
            MinSpareResources minSR = converter.fromJSON(file);
            Assert.assertEquals(c, minSR);
            file.delete();
        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
