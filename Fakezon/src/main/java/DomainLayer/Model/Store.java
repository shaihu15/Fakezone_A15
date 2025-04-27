package DomainLayer.Model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.ObjectUtils.Null;

import ApplicationLayer.DTO.StoreProductDTO;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.Interfaces.IStore;
import DomainLayer.Model.helpers.*;
import java.util.AbstractMap.SimpleEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Store implements IStore {

    private String name;
    private int storeID;
    private boolean isOpen = true;
    private int storeFounderID; // store founder ID
    private HashMap<Integer, StoreRating> Sratings; // HASH userID to store rating
    private HashMap<Integer, StoreProduct> storeProducts; // HASH productID to store product
    private HashMap<Integer, AuctionProduct> auctionProducts; // HASH productID to auction product
    private HashMap<Integer, PurchasePolicy> purchasePolicies; // HASH policyID to purchase policy
    private HashMap<Integer, DiscountPolicy> discountPolicies; // HASH policyID to discount policy
    private List<Integer> storeOwners;
    private HashMap<Integer, Integer> pendingOwners; // appointee : appointor
    private HashMap<Integer, List<StoreManagerPermission>> storeManagers; // HASH userID to store manager perms
    private Tree rolesTree;
    private Queue<SimpleEntry<Integer, String>> messagesFromUsers; // HASH userID to message
    private Stack<SimpleEntry<Integer, String>> messagesFromStore; // HASH userID to message
    private static final AtomicInteger idCounter = new AtomicInteger(0);
    private static final Logger logger = LoggerFactory.getLogger(Store.class);

    public Store(String name, int founderID) {
        this.storeFounderID = founderID;
        this.storeOwners = new ArrayList<>();
        // storeOwners.put(founderID, new StoreOwner(founderID, name));
        this.storeManagers = new HashMap<>();
        this.name = name;
        this.storeID = idCounter.incrementAndGet();
        this.Sratings = new HashMap<>();
        this.storeProducts = new HashMap<>();
        this.auctionProducts = new HashMap<>();
        this.purchasePolicies = new HashMap<>();
        this.discountPolicies = new HashMap<>();
        this.rolesTree = new Tree(founderID); // founder = root
        this.storeOwners.add(founderID);
        this.pendingOwners = new HashMap<>(); // appointee : appointor
        this.storeManagers = new HashMap<>(); // HASH userID to store manager
        this.messagesFromUsers = new LinkedList<>();
        this.messagesFromStore = new Stack<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getId() {
        return storeID;
    }

    // precondition: user is logged in and previously made a purchase from the store
    // - cheaked by service layer
    @Override
    public void addRating(int userID, double rating, String comment) {
        if (Sratings.containsKey(userID)) {
            Sratings.get(userID).updateRating(rating, comment);
        } else {
            Sratings.put(userID, new StoreRating(userID, rating, comment));
        }
    }

    @Override
    public void addStoreProductRating(int userID, int productID, double rating, String comment) {
        if (storeProducts.containsKey(productID)) {
            storeProducts.get(productID).addRating(userID, rating, comment);
        } else {
            throw new IllegalArgumentException(
                    "Product with ID: " + productID + " does not exist in store ID: " + storeID);
        }
    }

    @Override
    public void addStoreProduct(int productID, String name, double basePrice, int quantity) {
        storeProducts.put(productID, new StoreProduct(productID, name, basePrice, quantity));
    }

    // To Do: change the paramers of the function and decide on the structure of
    // purchase policy and discount policy
    @Override
    public void addPurchasePolicy(int userID, PurchasePolicy purchasePolicy) {
        if (isOwner(userID)
                || (isManager(userID) && storeManagers.get(userID).contains(StoreManagerPermission.PURCHASE_POLICY))) {
            purchasePolicies.put(purchasePolicy.getPolicyID(), purchasePolicy);
        } else {
            throw new IllegalArgumentException(
                    "User with ID: " + userID + " has insufficient permissions for store ID: " + storeID);
        }
    }

    // To Do: change the paramers of the function and decide on the structure of
    // purchase policy and discount policy
    @Override
    public void addDiscountPolicy(int userID, DiscountPolicy discountPolicy) {
        if (isOwner(userID)
                || (isManager(userID) && storeManagers.get(userID).contains(StoreManagerPermission.DISCOUNT_POLICY))) {
            discountPolicies.put(discountPolicy.getPolicyID(), discountPolicy);
        } else {
            throw new IllegalArgumentException(
                    "User with ID: " + userID + " has insufficient permissions for store ID: " + storeID);
        }
    }

    @Override
    public void addAuctionProduct(int requesterId, int productID, double basePrice, int daysToEnd) {
        if (!isOwner(requesterId) && !(isManager(requesterId)
                && storeManagers.get(requesterId).contains(StoreManagerPermission.INVENTORY))) {
            throw new IllegalArgumentException(
                    "User with ID: " + requesterId + " has insufficient permissions for store ID: " + storeID);
        }
        if (storeProducts.containsKey(productID)) {
            StoreProduct storeProduct = storeProducts.get(productID);
            auctionProducts.put(productID, new AuctionProduct(storeProduct, basePrice, daysToEnd));
        } else {
            throw new IllegalArgumentException(
                    "Product with ID: " + productID + " does not exist in store ID: " + storeID);
        }
    }

    @Override
    public boolean addBidToAuctionProduct(int requesterId, int productID, double bidAmount) {
        if (auctionProducts.containsKey(productID)) {
            return auctionProducts.get(productID).addBid(requesterId, bidAmount);
        } else {
            throw new IllegalArgumentException(
                    "Product with ID: " + productID + " does not exist in store ID: " + storeID);
        }
    }

    @Override
    public void isValidPurchaseAction(int requesterId, int productID) {
        if (auctionProducts.containsKey(productID)) {
            AuctionProduct auctionProduct = auctionProducts.get(productID);
            if (auctionProduct.getDaysToEnd() <= 0) {
                throw new IllegalArgumentException("Auction for product with ID: " + productID + " has ended.");
            }
            if (auctionProduct.getUserIDHighestBid() != requesterId) {
                throw new IllegalArgumentException("User with ID: " + requesterId
                        + " is not the highest bidder for product with ID: " + productID);
            }
        } else {
            throw new IllegalArgumentException("The product with ID: " + productID + " is not an auction product.");
        }
    }

    @Override
    public void receivingMessage(int userID, String message) {
        messagesFromUsers.add(new SimpleEntry<>(userID, message));
    }

    @Override
    public void sendMessage(int managerId, int userID, String message) {
        if (isOwner(managerId) || (isManager(managerId)
                && storeManagers.get(managerId).contains(StoreManagerPermission.REQUESTS_REPLY))) {
            messagesFromStore.push(new SimpleEntry<>(userID, message));
        } else {
            throw new IllegalArgumentException(
                    "User with ID: " + managerId + " has insufficient permissions for store ID: " + storeID);
        }
    }

    @Override
    public Queue<SimpleEntry<Integer, String>> getMessagesFromUsers(int managerId) {
        if (isOwner(managerId) || (isManager(managerId)
                && storeManagers.get(managerId).contains(StoreManagerPermission.REQUESTS_REPLY))) {
            return messagesFromUsers;
        } else {
            throw new IllegalArgumentException(
                    "User with id: " + managerId + " has insufficient permissions for store ID: " + storeID);
        }
    }

    @Override
    public Stack<SimpleEntry<Integer, String>> getMessagesFromStore(int managerId) {
        if (isOwner(managerId) || (isManager(managerId)
                && storeManagers.get(managerId).contains(StoreManagerPermission.REQUESTS_REPLY))) {
            return messagesFromStore;
        } else {
            throw new IllegalArgumentException(
                    "User with id: " + managerId + " has insufficient permissions for store ID: " + storeID);
        }
    }

    @Override
    public HashMap<Integer, StoreProduct> getStoreProducts() {
        return storeProducts;
    }

    @Override
    public HashMap<Integer, StoreRating> getRatings() {
        return Sratings;
    }

    @Override
    public HashMap<Integer, PurchasePolicy> getPurchasePolicies() {
        return purchasePolicies;
    }

    @Override
    public HashMap<Integer, DiscountPolicy> getDiscountPolicies() {
        return discountPolicies;
    }

    @Override
    public List<Integer> getStoreOwners(int requesterId) {
        if (isOwner(requesterId) || (isManager(requesterId)
                && storeManagers.get(requesterId).contains(StoreManagerPermission.VIEW_ROLES))) {
            return new ArrayList<>(storeOwners); // copy of store owners
        } else {
            logger.warn("User {} tried to access store roles without permission for store {}", requesterId, storeID);
            throw new IllegalArgumentException(
                    "User with id: " + requesterId + " has insufficient permissions for store ID: " + storeID);
        }
    }

    // so the list won't change outside of Store and mess with the hashmap
    private HashMap<Integer, List<StoreManagerPermission>> copyStoreManagersMap() {
        HashMap<Integer, List<StoreManagerPermission>> copy = new HashMap<>();
        for (Integer id : storeManagers.keySet()) {
            copy.put(id, new ArrayList<>(storeManagers.get(id)));
        }
        return copy;
    }

    @Override
    public HashMap<Integer, List<StoreManagerPermission>> getStoreManagers(int requesterId) {
        if (isOwner(requesterId) || (isManager(requesterId)
                && storeManagers.get(requesterId).contains(StoreManagerPermission.VIEW_ROLES))) {
            return copyStoreManagersMap();
        } else {
            logger.warn("User {} tried to access store roles without permission for store {}", requesterId, storeID);
            throw new IllegalArgumentException(
                    "User with id: " + requesterId + " has insufficient permissions for store ID: " + storeID);
        }
    }

    // TO DO: Send Approval Request
    @Override
    public void addStoreOwner(int appointor, int appointee) {
        if (!isOwner(appointor)) {
            throw new IllegalArgumentException(
                    "Appointor ID: " + appointor + " is not a valid store owner for store ID: " + storeID);
        }
        if (isOwner(appointee)) {
            throw new IllegalArgumentException(
                    "User with ID: " + appointee + " is already a store owner for store with ID: " + storeID);
        }
        // relevant after notifs
        /*
         * if(pendingOwners.containsKey(appointee)){
         * throw new IllegalArgumentException("Already waiting for User with ID: " +
         * appointee + "'s approval");
         * }
         */
        // pendingOwners.put(appointee, appointor); TO DO WHEN OBSERVER/ABLE IS
        // IMPLEMENTED
        if (isManager(appointee)) {
            Node appointeeNode = rolesTree.getNode(appointee);
            Node appointorNode = rolesTree.getNode(appointor);
            if (!appointorNode.getChildren().contains(appointeeNode)) {
                throw new IllegalArgumentException(
                        "Only the manager with id: " + appointee + "'s appointor can reassign them as Owner");
            }
            storeManagers.remove(appointee); // how should reappointing a manager affect the tree?? right now - only
                                             // their father can re-assign them
        }
        storeOwners.add(appointee);
        rolesTree.addNode(appointor, appointee);
    }

    // ***will be relevant when observer/able is implemented***
    // public void approvalStoreOwner(int appointee){
    // int appointor = pendingOwners.get(appointee);
    // pendingOwners.remove(appointee);
    // storeOwners.add(appointee);
    // rolesTree.addNode(appointor, appointee);
    // }
    // public void declineStoreOwner(int appointee){
    // pendingOwners.remove(appointee);
    // }
    @Override
    public boolean isOwner(int userId) {
        return storeOwners.contains(userId);
    }

    @Override
    public boolean isManager(int userId) {
        return storeManagers.containsKey(userId);
    }

    @Override
    public void addStoreManager(int appointor, int appointee, List<StoreManagerPermission> perms) {
        if (!isOwner(appointor)) {
            throw new IllegalArgumentException(
                    "Appointor ID: " + appointor + " is not a valid store owner for store ID: " + storeID);
        }
        if (isManager(appointee) || isOwner(appointee)) {
            throw new IllegalArgumentException(
                    "User with ID: " + appointee + " is already a store manager/owner for store with ID: " + storeID);
        }
        if (perms == null || perms.isEmpty()) {
            throw new IllegalArgumentException("Permissions list is empty");
        }
        storeManagers.put(appointee, new ArrayList<>(perms));
        rolesTree.addNode(appointor, appointee);
    }

    @Override
    public void addManagerPermissions(int requesterId, int managerId, List<StoreManagerPermission> perms) {
        if (!isOwner(requesterId)) {
            throw new IllegalArgumentException(
                    "Requester ID: " + requesterId + " is not a valid store owner for store ID: " + storeID);
        }
        if (!isManager(managerId)) {
            throw new IllegalArgumentException(
                    "Manager ID: " + requesterId + " is not a valid store manager for store ID: " + storeID);
        }
        checkNodesValidity(requesterId, managerId); // no need to hold the nodes here
        List<StoreManagerPermission> currentPerms = storeManagers.get(managerId);
        for (StoreManagerPermission perm : perms) {
            if (!currentPerms.contains(perm))
                currentPerms.add(perm);
        }
    }

    @Override
    public void removeManagerPermissions(int requesterId, int managerId, List<StoreManagerPermission> toRemove) {
        if (!isOwner(requesterId)) {
            throw new IllegalArgumentException(
                    "Requester ID: " + requesterId + " is not a valid store owner for store ID: " + storeID);
        }
        if (!isManager(managerId)) {
            throw new IllegalArgumentException(
                    "Manager ID: " + requesterId + " is not a valid store manager for store ID: " + storeID);
        }
        checkNodesValidity(requesterId, managerId); // no need to hold the nodes here
        List<StoreManagerPermission> currentPerms = storeManagers.get(managerId);
        List<StoreManagerPermission> copyCurrentPerms = new ArrayList<>(currentPerms);
        for (StoreManagerPermission perm : toRemove) {
            if (!currentPerms.contains(perm)) {
                storeManagers.put(managerId, copyCurrentPerms); // restore initial perms because method failed
                throw new IllegalArgumentException("can not remove permission: " + perm + " because manager: "
                        + managerId + " does not have it. permissions reseted to original");
            }
            currentPerms.remove(perm);
        }
        if (currentPerms.isEmpty()) {
            storeManagers.put(managerId, copyCurrentPerms); // restore initial perms because method failed
            throw new IllegalArgumentException(
                    "permissions can not be empty. reseting manager: " + managerId + " permissions to original");
        }
    }

    @Override
    public int getStoreFounderID() {
        return storeFounderID;
    }

    @Override
    public void closeStore(int requesterId) {
        if (requesterId == this.storeFounderID) {
            if (!this.isOpen) {
                throw new IllegalArgumentException("Store: " + storeID + " is already closed");
            }
            this.isOpen = false;
            // TODO: ADD NOTIFICATIONS SENDING
        } else {
            throw new IllegalArgumentException(
                    "Requester ID: " + requesterId + " is not a Store Founder of store: " + storeID);
        }
    }

    @Override
    public StoreRating getStoreRatingByUser(int userID) {
        if (Sratings.containsKey(userID)) {
            return Sratings.get(userID);
        } else {
            throw new IllegalArgumentException("User with ID: " + userID + " has not rated the store yet.");
        }
    }

    @Override
    public ProductRating getStoreProductRating(int userID, int productID) {
        if (storeProducts.containsKey(productID)) {
            return storeProducts.get(productID).getRatingByUser(userID);
        } else {
            throw new IllegalArgumentException(
                    "Product with ID: " + productID + " does not exist in store ID: " + storeID);
        }
    }

    @Override
    public void openStore() {
        this.isOpen = true;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public double getAverageRating() {
        double sum = 0;
        for (StoreRating rating : Sratings.values()) {
            sum += rating.getRating();
        }
        return sum / Sratings.size();
    }

    @Override
    public StoreProduct getStoreProduct(int productID) {
        if (storeProducts.containsKey(productID)) {
            return storeProducts.get(productID);
        } else {
            throw new IllegalArgumentException(
                    "Product with ID: " + productID + " does not exist in store ID: " + storeID);
        }
    }

    @Override
    public void removeStoreOwner(int requesterId, int toRemoveId) {
        if (!isOwner(requesterId)) {
            throw new IllegalArgumentException("User with id: " + requesterId + " is not a valid store owner");
        }
        if (!isOwner(toRemoveId)) {
            throw new IllegalArgumentException("User with id: " + toRemoveId + " is not a valid store owner");
        }
        Node[] nodesArr = checkNodesValidity(requesterId, toRemoveId);
        Node fatherNode = nodesArr[0];
        Node childNode = nodesArr[1];
        storeOwners.remove(Integer.valueOf(childNode.getId())); // WRAPPED AS INTEGER BECAUSE OTHERWISE JAVA WANTS TO
                                                                // REMOVE AS INDEX - DO NOT CHANGE!!
        removeAllChildrenRoles(childNode); // remove all children from their respective roles list/hashmap
        if (requesterId != toRemoveId)
            fatherNode.removeChild(childNode); // remove child & all descendants from the actual tree
    }

    @Override
    public void removeStoreManager(int requesterId, int toRemoveId) {
        if (!isOwner(requesterId)) {
            throw new IllegalArgumentException("User with id: " + requesterId + " is not a valid store owner");
        }
        if (!isManager(toRemoveId)) {
            throw new IllegalArgumentException("User with id: " + toRemoveId + " is not a valid store manager");
        }
        Node[] nodesArr = checkNodesValidity(requesterId, toRemoveId);
        Node fatherNode = nodesArr[0];
        Node childNode = nodesArr[1];
        if (!childNode.getChildren().isEmpty()) {
            throw new IllegalArgumentException("Manager with id " + toRemoveId + " has children in rolesTree"); // should
                                                                                                                // not
                                                                                                                // happen
                                                                                                                // -
                                                                                                                // just
                                                                                                                // for
                                                                                                                // debugging
                                                                                                                // purposes
        }
        storeManagers.remove(toRemoveId);
        fatherNode.removeChild(childNode);// remove child from the actual tree
    }

    private void removeAllChildrenRoles(Node toRemove) {
        List<Node> children = toRemove.getAllDescendants();
        for (Node child : children) {
            int childId = child.getId();
            if (storeOwners.contains(childId)) {
                storeOwners.remove(Integer.valueOf(childId));// WRAPPED AS INTEGER BECAUSE OTHERWISE JAVA WANTS TO
                                                             // REMOVE AS INDEX - DO NOT CHANGE!!
            } else {
                if (storeManagers.containsKey(childId)) {
                    storeManagers.remove(childId);
                } else {
                    throw new IllegalArgumentException("Node with id " + toRemove.getId() + " has descendant with id "
                            + childId + " with no roles on owners list/managers hashmap"); // should not happen - just
                                                                                           // for debugging purposes
                }
            }
        }
    }

    // returns [father,child]
    // allows requesterId = childId!!!
    private Node[] checkNodesValidity(int requesterId, int childId) {
        Node fatherNode = rolesTree.getNode(requesterId);
        Node childNode = rolesTree.getNode(childId);
        if (fatherNode == null) {
            throw new IllegalArgumentException("Could Not Find fatherNode in rolesTree (id: " + requesterId + ")"); // should
                                                                                                                    // not
                                                                                                                    // happen
                                                                                                                    // -
                                                                                                                    // just
                                                                                                                    // for
                                                                                                                    // debugging
                                                                                                                    // purposes
        }
        if (childNode == null) {
            throw new IllegalArgumentException("Could Not Find childNode in rolesTree (id: " + requesterId + ")"); // should
                                                                                                                   // not
                                                                                                                   // happen
                                                                                                                   // -
                                                                                                                   // just
                                                                                                                   // for
                                                                                                                   // debugging
                                                                                                                   // purposes
        }
        if (requesterId != childId && !fatherNode.isChild(childNode)) {
            throw new IllegalArgumentException("Only " + childId + "'s appointor can change/remove their permissions");
        }
        return new Node[] { fatherNode, childNode };
    }

    @Override
    public synchronized StoreProductDTO decrementProductQuantity(int productId, int quantity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'decrementProductQuantity'");
    }

}
