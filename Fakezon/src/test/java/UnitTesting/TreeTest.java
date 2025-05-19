package UnitTesting;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import DomainLayer.Model.helpers.Node;
import DomainLayer.Model.helpers.Tree;

class TreeTest {

    @Test
    void testConstructorAndGetRoot() {
        Tree tree = new Tree(1);
        assertNotNull(tree.getRoot());
        assertEquals(1, tree.getRoot().getId());
    }

    @Test
    void testAddNodeAndGetNode() {
        Tree tree = new Tree(1);
        tree.addNode(1, 2);
        tree.addNode(2, 3);

        Node node2 = tree.getNode(2);
        Node node3 = tree.getNode(3);

        assertNotNull(node2);
        assertEquals(2, node2.getId());

        assertNotNull(node3);
        assertEquals(3, node3.getId());
    }

    @Test
    void testGetNodeNotFound() {
        Tree tree = new Tree(1);
        assertNull(tree.getNode(99));
    }

    @Test
    void testAddNodeThrowsIfAppointorNotFound() {
        Tree tree = new Tree(1);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> tree.addNode(99, 2));
        assertTrue(exception.getMessage().contains("appointor id: 99 not in rolesTree"));
    }
}