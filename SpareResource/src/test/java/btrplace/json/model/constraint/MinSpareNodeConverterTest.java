package btrplace.json.model.constraint;

import btrplace.json.JSONConverterException;
import btrplace.model.constraint.MinSpareNode;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * User: TU HUYNH DANG
 * Date: 5/24/13
 * Time: 9:45 AM
 */
public class MinSpareNodeConverterTest implements PremadeElements {

    @Test
    public void test() {
        Set<UUID> nodes = new HashSet<UUID>(Arrays.asList(n1, n2, n3, n4));
        MinSpareNode msn = new MinSpareNode(nodes, 1);

        MinSpareNodeConverter converter = new MinSpareNodeConverter();
        try {
            converter.toJSON(msn, new File("MinSpareNode.json"));
            MinSpareNode minSpareNode = converter.fromJSON(new File("MinSpareNode.json"));
            Assert.assertEquals(msn, minSpareNode);

        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }
}
