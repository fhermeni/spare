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
            File file = new File("MinSpareNode.json");
            converter.toJSON(msn, file);
            MinSpareNode minSpareNode = converter.fromJSON(file);
            Assert.assertEquals(msn, minSpareNode);
            file.delete();
        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }
}
