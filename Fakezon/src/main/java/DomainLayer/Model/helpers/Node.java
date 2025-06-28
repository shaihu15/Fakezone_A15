package DomainLayer.Model.helpers;
import java.util.List;
import java.util.ArrayList;

import jakarta.persistence.*;

@Entity
@Table(name = "tree_nodes")
public class Node {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "node_id")
    private Long nodeId; // JPA entity ID
    
    @Column(name = "user_id", nullable = false)
    private int id; // The actual user/role ID
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Node parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Node> children = new ArrayList<>();

    // Default constructor for JPA
    public Node() {
        this.children = new ArrayList<>();
    }

    public Node(int id){
        this();
        this.id = id;
    }
    
    public int getId(){
        return this.id;
    }
    
    public void addChild(Node child){
        this.children.add(child);
        child.parent = this;
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
        toRemove.parent = null;
    }

    public boolean isChild(Node child){
        return children.contains(child);
    }
    
    // Additional getters/setters for JPA
    public Long getNodeId() {
        return nodeId;
    }
    
    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public Node getParent() {
        return parent;
    }
    
    public void setParent(Node parent) {
        this.parent = parent;
    }
    
    public void setChildren(List<Node> children) {
        this.children = children;
    }
}