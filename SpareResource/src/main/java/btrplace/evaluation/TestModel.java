package btrplace.evaluation;

/**
 * Created with IntelliJ IDEA.
 * User: TU HUYNH DANG
 * Date: 5/21/13
 * Time: 4:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestModel {

    public static void main(String[] args) {
        Instance a = new InstanceBuilder().medium();
        Instance b = new InstanceBuilder().medium();
        Instance c = new InstanceBuilder().medium();
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        Node node = new NodeBuilder().large();
        Node node2 = new NodeBuilder().large();
        System.out.println(node);
        System.out.println(node2);

    }
}
