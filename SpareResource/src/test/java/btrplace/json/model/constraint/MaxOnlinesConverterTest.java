package btrplace.json.model.constraint;

import btrplace.json.JSONConverterException;
import btrplace.model.Node;
import btrplace.model.constraint.MaxOnline;
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
 * Time: 9:22 AM
 */
public class MaxOnlinesConverterTest extends PremadeTest {

    @Test
    public void main() {
        Set<Node> s = new HashSet<Node>(Arrays.asList(n1, n2, n3));
        MaxOnline mo = new MaxOnline(s, 2);
        MaxOnlinesConverter moc = new MaxOnlinesConverter();
        moc.setModel(model);
        try {
            File file = new File("maxOnlines.json");
            moc.toJSON(mo, file);
            MaxOnline new_max = moc.fromJSON(file);
            Assert.assertEquals(mo, new_max);
            file.delete();
        } catch (JSONConverterException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }
}
