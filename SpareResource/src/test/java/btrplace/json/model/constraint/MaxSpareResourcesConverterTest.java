package btrplace.json.model.constraint;

import btrplace.json.JSONConverterException;
import btrplace.model.Node;
import btrplace.model.constraint.MaxSpareResources;
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
 * Time: 9:49 AM
 */
public class MaxSpareResourcesConverterTest extends PremadeTest {

    @Test
    public void test() {
        Set<Node> s = new HashSet<Node>(Arrays.asList(n1, n2));
        MaxSpareResources c = new MaxSpareResources(s, "vcpu", 3);
        MaxSpareResourcesConverter converter = new MaxSpareResourcesConverter();
        converter.setModel(model);
        try {
            File file = new File("MSR.json");
            converter.toJSON(c, file);
            MaxSpareResources msr = converter.fromJSON(file);
            Assert.assertEquals(c, msr);
            file.delete();
        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }
}
