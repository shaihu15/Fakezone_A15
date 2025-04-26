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
    public List<Node> getAllDescendants(){
        List<Node> descendants = new ArrayList<>();
        for (Node child : this.children){
            descendants.add(child);
            descendants.addAll(child.getAllDescendants());
        }
        return descendants;
    }

    public void removeChild(Node toRemove){
        if(!children.contains(toRemove)){
            throw new IllegalArgumentException("Node id: " + getId() + " has no child with id: " + toRemove.getId());
        }
        children.remove(toRemove);
    }

    public boolean isChild(Node child){
        return children.contains(child);
    }
}