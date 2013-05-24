package btrplace.json.model.constraint;

import btrplace.json.JSONConverterException;
import btrplace.model.constraint.MaxSpareResources;
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
 * Time: 9:49 AM
 */
public class MaxSpareResourcesConverterTest implements PremadeElements {

    @Test
    public void test() {
        Set<UUID> s = new HashSet<UUID>(Arrays.asList(n1, n2));
        MaxSpareResources c = new MaxSpareResources(s, "vcpu", 3);
        MaxSpareResourcesConverter converter = new MaxSpareResourcesConverter();

        try {
            converter.toJSON(c, new File("MSR.json"));
            MaxSpareResources msr = converter.fromJSON(new File("MSR.json"));
            Assert.assertEquals(c, msr);

        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }
}
