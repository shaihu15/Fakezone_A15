package DomainLayer.Model.helpers;

public class Tree{
    private Node root;
    public Tree(int rootID){
        this.root = new Node(rootID);
    }
    public Node getNode(int id){
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
    }
}
