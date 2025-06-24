package DomainLayer.Model.helpers;

import jakarta.persistence.*;

@Entity
@Table(name = "roles_trees")
public class Tree {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tree_id")
    private Long treeId;
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "root_node_id")
    private Node root;
    
    // Default constructor for JPA
    public Tree() {
    }
    
    public Tree(int rootID){
        this.root = new Node(rootID);
    }
    
    public Node getNode(int id){
        if (root == null) {
            return null;
        }
        return findRec(root, id);
    }
    
    public Node getRoot(){
        return this.root;
    }
    
    private Node findRec(Node curr, int id){
        if (curr.getId() == id){
            return curr;
        }
        for(Node child : curr.getChildren()){
            Node found = findRec(child, id);
            if(found != null)
                return found;
        }
        return null;
    }
    
    public void addNode(int appointor, int appointee){
        Node appointorNode = getNode(appointor);
        if(appointorNode != null)
            appointorNode.addChild(new Node(appointee));
        else
            throw new IllegalArgumentException("appointor id: " + appointor + " not in rolesTree"); // should not happend - for debugging purposes
    }
    
    // Additional getters/setters for JPA
    public Long getTreeId() {
        return treeId;
    }
    
    public void setTreeId(Long treeId) {
        this.treeId = treeId;
    }
    
    public void setRoot(Node root) {
        this.root = root;
    }
}
