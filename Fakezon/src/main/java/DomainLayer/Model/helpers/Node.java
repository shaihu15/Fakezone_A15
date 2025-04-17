package DomainLayer.Model.helpers;
import java.util.List;
import java.util.ArrayList;

public class Node {
    int id;
    List<Node> children;

    public Node(int id){
        this.id = id;
        this.children = new ArrayList<>();
    }
    public int getId(){
        return this.id;
    }
    public void addChild(Node child){
        this.children.add(child);
    }
    public List<Node> getChildren(){
        return this.children;
    }
}