package btrplace.json.model.constraint;

import btrplace.json.JSONConverterException;
import btrplace.model.constraint.MaxOnlines;
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
 * Time: 9:22 AM
 */
public class MaxOnlinesConverterTes implements PremadeElements {

    @Test
    public void main() {

        Set<UUID> s = new HashSet<UUID>(Arrays.asList(n1, n2, n3));
        MaxOnlines mo = new MaxOnlines(s, 2);
        MaxOnlinesConverter moc = new MaxOnlinesConverter();
        try {
            moc.toJSON(mo, new File("maxOnlines.json"));
            MaxOnlines new_max = moc.fromJSON(new File("maxOnlines.json"));

            Assert.assertEquals(mo, new_max);

        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }
}
