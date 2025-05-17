package UnitTesting;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import DomainLayer.Model.helpers.Node;

class NodeTest {

    @Test
    void testConstructorAndGetId() {
        Node node = new Node(1);
        assertEquals(1, node.getId());
        assertTrue(node.getChildren().isEmpty());
    }

    @Test
    void testAddChildAndIsChild() {
        Node parent = new Node(1);
        Node child = new Node(2);
        parent.addChild(child);

        assertTrue(parent.isChild(child));
        assertEquals(1, parent.getChildren().size());
        assertEquals(child, parent.getChildren().get(0));
    }

    @Test
    void testGetAllDescendants() {
        Node root = new Node(1);
        Node child1 = new Node(2);
        Node child2 = new Node(3);
        Node grandchild = new Node(4);

        root.addChild(child1);
        root.addChild(child2);
        child1.addChild(grandchild);

        List<Node> descendants = root.getAllDescendants();
        assertEquals(3, descendants.size());
        assertTrue(descendants.contains(child1));
        assertTrue(descendants.contains(child2));
        assertTrue(descendants.contains(grandchild));
    }

    @Test
    void testRemoveChild_Success() {
        Node parent = new Node(1);
        Node child = new Node(2);
        parent.addChild(child);

        parent.removeChild(child);
        assertFalse(parent.isChild(child));
        assertTrue(parent.getChildren().isEmpty());
    }

    @Test
    void testRemoveChild_NotFound() {
        Node parent = new Node(1);
        Node child = new Node(2);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> parent.removeChild(child));
        assertTrue(exception.getMessage().contains("Node id: 1 has no child with id: 2"));
    }
}