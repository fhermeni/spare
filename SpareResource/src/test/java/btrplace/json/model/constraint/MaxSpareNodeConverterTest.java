package btrplace.json.model.constraint;

import btrplace.json.JSONConverterException;
import btrplace.model.Node;
import btrplace.model.constraint.MaxSpareNode;
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
 * Time: 9:32 AM
 */
public class MaxSpareNodeConverterTest extends PremadeTest {

    @Test
    public void test() {

        Set<Node> nodes = new HashSet<Node>(Arrays.asList(n1, n2, n3, n4));
        MaxSpareNode msn = new MaxSpareNode(nodes, 1);

        MaxSpareNodeConverter converter = new MaxSpareNodeConverter();
        converter.setModel(model);
        try {
            File file = new File("MaxSpareNode.json");
            converter.toJSON(msn, file);
            MaxSpareNode msn_from_file = converter.fromJSON(file);
            Assert.assertEquals(msn, msn_from_file);
            file.delete();
        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
