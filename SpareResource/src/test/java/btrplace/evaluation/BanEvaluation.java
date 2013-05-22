package btrplace.evaluation;

import btrplace.model.Model;
import btrplace.model.constraint.Ban;
import org.testng.annotations.Test;

/**
 * User: TU HUYNH DANG
 * Date: 5/22/13
 * Time: 10:43 AM
 */
public class BanEvaluation {

    @Test
    public void test1() {
        ModelGenerator mogen = new ModelGenerator();
        Model model = mogen.generateModel(9, 30);

        Ban ban = new Ban(mogen.getRandomVMs(5), mogen.getRandomNodes(5));
        IncreasingLoad incLoad = new IncreasingLoad(model, ban);
        incLoad.run();
    }

}
