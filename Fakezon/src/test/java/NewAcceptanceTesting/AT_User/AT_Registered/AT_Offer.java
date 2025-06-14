package NewAcceptanceTesting.AT_User.AT_Registered;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fakezone.fakezone.FakezoneApplication;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.CartItemInfoDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Services.SystemService;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.Model.Offer;
import DomainLayer.Model.helpers.StoreMsg;
import NewAcceptanceTesting.TestHelper;
import UnitTesting.getCartFinalPriceTest;

@SpringBootTest(classes = FakezoneApplication.class)
public class AT_Offer {

    @Autowired
    private SystemService systemService;

    private int founder;
    private int owner1;
    private int owner2;
    private List<Integer> owners;
    private int manager;
    private int registered;
    private int guest;
    private int store;
    private int wrongStore = -1000;
    private int p1;
    private int p2;
    private int wrongProduct = -1000;
    private double b1 = 555.55; // base price p1
    private double b2 = 444.44; // base price p2
    private int q1 = 100; // quantity p1
    private int q2 = 200; // quantity p2
    private String c1 = PCategory.ELECTRONICS.toString();
    private String c2 = PCategory.AUTOMOTIVE.toString();
    
    private TestHelper testHelper;

    @BeforeEach
    void setUp(){
        systemService.clearAllData(); //should be removed when there's a DB and we exclude the tests!!!
        testHelper = new TestHelper(systemService);

        //create users
        Response<UserDTO> founderRes = testHelper.register_and_login();
        assertTrue(founderRes.isSuccess(), "Failed to register and log in founder");

        Response<UserDTO> owner1Res = testHelper.register_and_login2();
        assertTrue(owner1Res.isSuccess(), "Failed to register and log in owner1");

        Response<UserDTO> owner2Res = testHelper.register_and_login3();
        assertTrue(owner2Res.isSuccess(), "Failed to register and log in owner2");

        Response<UserDTO> registeredRes = testHelper.register_and_login4();
        assertTrue(registeredRes.isSuccess(), "Failed to register and log in registered");

        Response<UserDTO> guestRes = systemService.createUnsignedUser();
        assertTrue(guestRes.isSuccess(), "Failed to create guest user");

        Response<UserDTO> managerRes = testHelper.register_and_login5();
        assertTrue(managerRes.isSuccess(), "Failed to register and log in manager");

        //fetch their ids from the responses
        founder = founderRes.getData().getUserId();
        owner1 = owner1Res.getData().getUserId();
        owner2 = owner2Res.getData().getUserId();
        registered = registeredRes.getData().getUserId();
        guest = guestRes.getData().getUserId();
        owners = List.of(founder, owner1, owner2);
        manager = managerRes.getData().getUserId();

        //create store
        Response<Integer> storeRes = systemService.addStore(founder, "AT_Offer");
        assertTrue(storeRes.isSuccess(), "Failed to create store");
        store = storeRes.getData();

        //add owners & manager
        Response<Void> addO1Res = systemService.addStoreOwner(store, founder, owner1);
        assertTrue(addO1Res.isSuccess(), "Failed to add Owner1 by Founder");
        Response<Void> addO2Res = systemService.addStoreOwner(store, founder, owner2);
        assertTrue(addO2Res.isSuccess(), "Failed to add Owner2 by Founder");
        Response<Void> addManagerRes = systemService.addStoreManager(store, founder, manager, List.of(StoreManagerPermission.DISCOUNT_POLICY));
        assertTrue(addManagerRes.isSuccess(), "Failed to add Owner2 by Founder");

        //accept assignment
        Response<String> acceptO1Res = systemService.acceptAssignment(store, owner1);
        assertTrue(acceptO1Res.isSuccess(), "Failed to add Owner1 by Founder");
        Response<String> acceptO2Res = systemService.acceptAssignment(store, owner2);
        assertTrue(acceptO2Res.isSuccess(), "Failed to add Owner2 by Founder");
        Response<String> acceptMRes = systemService.acceptAssignment(store, manager);
        assertTrue(acceptMRes.isSuccess(), "Failed to add Owner2 by Founder");

        //add products
        Response<StoreProductDTO> p1Res = systemService.addProductToStore(store, founder, "P1_AT_Offer", "P1_AT_Offer", b1, q1, c1);
        assertTrue(p1Res.isSuccess(), "Failed to create p1");
        Response<StoreProductDTO> p2Res = systemService.addProductToStore(store, founder, "P2_AT_Offer", "P2_AT_Offer", b2, q2, c2);
        assertTrue(p2Res.isSuccess(), "Failed to create p2");
        p1 = p1Res.getData().getProductId();
        p2 = p2Res.getData().getProductId();
    }

    // ********************
    // **PlaceOffer TESTS**
    // ********************
    @Test
    void testPlaceOffer_byGuest_Fail(){
        Response<Void> offerRes = systemService.placeOfferOnStoreProduct(store, guest, p1, 100.0);
        assertFalse(offerRes.isSuccess(), "FAILURE - Guest successfully placed an Offer");
        assertTrue(offerRes.getMessage() == "Only Registered Users May Place Offers");
        Response<List<Offer>> getOffersRes = systemService.getUserOffers(store, guest);
        List<Offer> offers = getOffersRes.getData();
        assertTrue(offers.isEmpty());
    }

    @Test
    void testPlaceOffer_storeNotFound_Fail(){
        Response<Void> offerRes = systemService.placeOfferOnStoreProduct(wrongStore, registered, p1, 100.0);
        assertFalse(offerRes.isSuccess(), "FAILURE - Success When Store Doesn't Exist");
        assertTrue(offerRes.getMessage() == "Store not found");
        Response<List<Offer>> getOffersRes = systemService.getUserOffers(store, registered);
        List<Offer> offers = getOffersRes.getData();
        assertTrue(offers.isEmpty());
    }

    @Test
    void testPlaceOffer_productNotFound_Fail(){
        Response<Void> offerRes = systemService.placeOfferOnStoreProduct(store, registered, wrongProduct, 100.0);
        assertFalse(offerRes.isSuccess(), "FAILURE - Success When Product Doesn't Exist");
        assertTrue(offerRes.getMessage().contains("Store Product " + wrongProduct));
        Response<List<Offer>> getOffersRes = systemService.getUserOffers(store, registered);
        List<Offer> offers = getOffersRes.getData();
        assertTrue(offers.isEmpty());
    }

    @Test
    void testPlaceOffer_productOutOfStock_Fail(){
        Response<Void> updateP1Res = systemService.updateProductInStore(store, founder, p1, b1, 0);
        assertTrue(updateP1Res.isSuccess(), "Failed to update P1");
        Response<Void> offerRes = systemService.placeOfferOnStoreProduct(store, registered, p1, 100.0);
        assertFalse(offerRes.isSuccess(), "FAILURE - Success When Product Out of Stock");
        assertTrue(offerRes.getMessage().contains("Product " + p1 + " is out of stock"));
        Response<List<Offer>> getOffersRes = systemService.getUserOffers(store, registered);
        List<Offer> offers = getOffersRes.getData();
        assertTrue(offers.isEmpty());
    }

    @Test
    void testPlaceOffer_invalidOffer_Fail(){
        Response<Void> offerRes = systemService.placeOfferOnStoreProduct(store, registered, p1, 0.5);
        assertFalse(offerRes.isSuccess(), "FAILURE - Success When offerAmount < 1");
        assertTrue(offerRes.getMessage().contains("Offer must be at least $1"));
        Response<List<Offer>> getOffersRes = systemService.getUserOffers(store, registered);
        List<Offer> offers = getOffersRes.getData();
        assertTrue(offers.isEmpty());
    }

    @Test
    void testPlaceOffer_Success(){
        Response<Void> offerRes = systemService.placeOfferOnStoreProduct(store, registered, p1, 12);
        assertTrue(offerRes.isSuccess());
        Response<Void> offerRes2 = systemService.placeOfferOnStoreProduct(store, registered, p2, 13);
        assertTrue(offerRes2.isSuccess());
        //sleep because msg sending is async so avoid race condition
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Sleep was interrupted");
        }
        //verify all owners got the 2 msgs
        for(Integer owner : owners){
            Response<Map<Integer, StoreMsg>> msgsRes = systemService.getUserOfferMessages(owner);
            assertTrue(msgsRes.isSuccess(), msgsRes.getMessage());
            Map<Integer, StoreMsg> msgs = msgsRes.getData();
            boolean msg1Found = false;
            boolean msg2Found = false;
            for(Integer msgId : msgs.keySet()){
                if(msgs.get(msgId).getMessage().contains("Received an offer for product " + p1)){
                    msg1Found = true;
                    if(msg1Found && msg2Found){
                        break;
                    }
                }
                if(msgs.get(msgId).getMessage().contains("Received an offer for product " + p2)){
                    msg2Found = true;
                    if(msg1Found && msg2Found){
                        break;
                    }
                }
            }
            assertTrue(msg1Found && msg2Found, "FAILURE - messages not found for owner " + owner);
        }
        Response<List<Offer>> getOffersRes = systemService.getUserOffers(store, registered);
        List<Offer> offers = getOffersRes.getData();
        boolean o1Found = false;
        boolean o2Found = false;
        for(Offer of : offers){
            if(of.getProductId() == p1){
                o1Found = true;
            }
            else if(of.getProductId() == p2){
                o2Found = true;
            }
        }
        assertTrue(o1Found && o2Found, "FAILURE - offers not found");
    }

    @Test
    void testPlaceOffer_offerOnSameProduct_Fail(){
        Response<Void> offerRes = systemService.placeOfferOnStoreProduct(store, registered, p1, 12);
        Response<Void> offerRes2 = systemService.placeOfferOnStoreProduct(store, registered, p1, 16);
        assertTrue(offerRes.isSuccess() && !offerRes2.isSuccess(), "FAILURE - 2nd offer succeeded");
        Response<List<Offer>> getOffersRes = systemService.getUserOffers(store, registered);
        List<Offer> offers = getOffersRes.getData();
        assertTrue(offers.size() == 1 && offers.get(0).getOfferAmount() == 12);
    }

    // *********************
    // **AcceptOffer TESTS**
    // *********************

    @Test
    void testAcceptOffer_storeNotFound_Fail(){
        systemService.placeOfferOnStoreProduct(store, registered, p1, 12);
        Response<Void> acceptRes = systemService.acceptOfferOnStoreProduct(wrongStore, owner1, registered, p1);
        assertTrue(!acceptRes.isSuccess() && acceptRes.getMessage().contains("Store not found"));
        Response<List<Offer>> getOffersRes = systemService.getUserOffers(store, registered);
        List<Offer> offers = getOffersRes.getData();
        assertTrue(offers.get(0).getOwnersAccepted().isEmpty());
    }

    @Test
    void testAcceptOffer_notOwner_Fail(){
        systemService.placeOfferOnStoreProduct(store, registered, p1, 12);
        Response<Void> acceptRes = systemService.acceptOfferOnStoreProduct(store, manager, registered, p1);
        assertTrue(!acceptRes.isSuccess() && acceptRes.getMessage().contains("Only Store Owners can Accept Offers"));
        Response<List<Offer>> getOffersRes = systemService.getUserOffers(store, registered);
        List<Offer> offers = getOffersRes.getData();
        assertTrue(offers.get(0).getOwnersAccepted().isEmpty());
    }

    @Test
    void testAcceptOffer_offerNotFound_Fail(){
        Response<Void> acceptRes = systemService.acceptOfferOnStoreProduct(store, owner1, registered, p1);
        assertTrue(!acceptRes.isSuccess() && acceptRes.getMessage().contains("Did not place an Offer on Product"), acceptRes.getMessage());
    }

    @Test
    void testAcceptOffer_singleOwnerAccept_Success(){
        systemService.placeOfferOnStoreProduct(store, registered, p1, 12);
        systemService.placeOfferOnStoreProduct(store, registered, p2, 16);
        Response<Void> acceptRes = systemService.acceptOfferOnStoreProduct(store, owner1, registered, p1);
        assertTrue(acceptRes.isSuccess());
        Response<List<Offer>> getOffersRes = systemService.getUserOffers(store, registered);
        List<Offer> offers = getOffersRes.getData();
        Offer offer1 = offers.get(0);
        assertTrue(offer1.getUserId() == registered && offer1.getStoreId() == store && offer1.getProductId() == p1); // verify correct offer
        Offer offer2 = offers.get(1);
        assertTrue(offer2.getUserId() == registered && offer2.getStoreId() == store && offer2.getProductId() == p2); // verify correct offer
        assertTrue(offer1.getOwnersAccepted().size() == 1 && offer1.getOwnersAccepted().contains(owner1));
        assertTrue(offer2.getOwnersAccepted().isEmpty());

        //sleep because msg sending is async so avoid race condition
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Sleep was interrupted");
        }

        //verify all OTHER owners got the msg
        for(Integer owner : owners){
            Map<Integer, StoreMsg> msgs = systemService.getMessagesFromStore(owner).getData();
            boolean found = false;
            for(Integer msgId : msgs.keySet()){
                StoreMsg msg = msgs.get(msgId);
                if(msg.getMessage().contains("Owner " + owner1 + " accepted an offer for product " + p1)){
                    found = true;
                    break;
                }
            }
            if(owner == owner1){
                assertFalse(found);
            }
            else{
                assertTrue(found);
            }
        }
        offer1 = systemService.getUserOffers(store, registered).getData().get(0);
        assertFalse(offer1.isApproved());
    }

    @Test
    void testAcceptOffer_allOwnersAccept_Success(){
        Response<Void> removeRes = systemService.removeStoreManager(store, founder, manager);
        assertTrue(removeRes.isSuccess());
        Response<Void> addRes = systemService.addStoreOwner(store, founder, manager); // to make sure removal makes the operation succeed even if others accepted before
        assertTrue(addRes.isSuccess());
        Response<String> acceptMRes = systemService.acceptAssignment(store, manager);
        assertTrue(acceptMRes.isSuccess(), "Failed to add manager by Founder");


        systemService.addToBasket(registered, p1, store, 2); // to make sure the quantity & final price are correct!
        systemService.placeOfferOnStoreProduct(store, registered, p1, 12);
        List<CartItemInfoDTO> cart =  systemService.viewCart(registered).getData();
        assertTrue(cart.size() == 1 && cart.get(0).getQuantityInCart() == 2); // offer should not be added to quantity until accepted
        
        for(Integer owner: owners){
            systemService.acceptOfferOnStoreProduct(store, owner, registered, p1);
        }
        Response<List<Offer>> getOffersRes = systemService.getUserOffers(store, registered);
        List<Offer> offers = getOffersRes.getData();
        Offer offer = offers.get(0);
        assertFalse(offer.isApproved());
        assertFalse(offer.isHandled());
        systemService.removeStoreOwner(store, founder, manager);
        assertTrue(offer.isApproved());
        assertTrue(offer.isHandled());
        //sleep because msg sending is async so avoid race condition
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Sleep was interrupted");
        }
        cart =  systemService.viewCart(registered).getData();
        assertTrue(cart.size() == 1 && cart.get(0).getQuantityInCart() == 3); // verify the quantity increased

        //verify all owners got the success msg
        for(Integer owner : owners){
            Map<Integer, StoreMsg> msgs = systemService.getMessagesFromStore(owner).getData();
            boolean found = false;
            for(Integer msgId : msgs.keySet()){
                StoreMsg msg = msgs.get(msgId);
                if(msg.getMessage().contains("An Offer for product " + p1 + " in Store " + store +" was accepted by all owners")){
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }

        Map<Integer, StoreMsg> registeredMsgs = systemService.getMessagesFromStore(registered).getData();
        boolean found = false;
        for(Integer msgId : registeredMsgs.keySet()){
            StoreMsg msg = registeredMsgs.get(msgId);
            if(msg.getMessage().contains("We are pleased to inform you that your offer on product: " + p1)){
                found = true;
                break;
            }
        }
        assertTrue(found);
        double finalPrice = systemService.getCartFinalPrice(registered, LocalDate.now()).getData();
        assertTrue(finalPrice == b1 * 2 + 12);
        Response<String> purchaseRes = systemService.purchaseCart(registered, "FR", LocalDate.now(), PaymentMethod.CREDIT_CARD, "SHIPPING", "4111111111111111", "John Doe", "12/30", "123","Main St*City*Country*12345",  "John Doe", "Electronics");
        assertTrue(purchaseRes.isSuccess());
        StoreProductDTO prod1 = systemService.getProductFromStore(p1, store).getData();
        assertTrue(prod1.getQuantity() == q1-3);
        assertTrue(systemService.getUserOffers(store, registered).getData().isEmpty());
    }

    // **********************
    // **DeclineOffer TESTS**
    // **********************

    @Test
    void testDeclineOffer_storeNotFound_Fail(){
        systemService.placeOfferOnStoreProduct(store, registered, p1, 12);
        Response<Void> declineRes = systemService.declineOfferOnStoreProduct(wrongStore, owner1, registered, p1);
        assertTrue(!declineRes.isSuccess() && declineRes.getMessage().contains("Store not found"));
        Response<List<Offer>> getOffersRes = systemService.getUserOffers(store, registered);
        List<Offer> offers = getOffersRes.getData();
        assertFalse(offers.get(0).isDeclined());
    }

    @Test
    void testDeclineOffer_notOwner_Fail(){
        systemService.placeOfferOnStoreProduct(store, registered, p1, 12);
        Response<Void> declineRes = systemService.declineOfferOnStoreProduct(store, manager, registered, p1);
        assertTrue(!declineRes.isSuccess() && declineRes.getMessage().contains("Only Store Owners can Decline Offers"));
        Response<List<Offer>> getOffersRes = systemService.getUserOffers(store, registered);
        List<Offer> offers = getOffersRes.getData();
        assertTrue(offers.get(0).getOwnersAccepted().isEmpty());
    }

    @Test
    void testDeclineOffer_offerNotFound_Fail(){
        Response<Void> declineRes = systemService.declineOfferOnStoreProduct(store, owner1, registered, p1);
        assertTrue(!declineRes.isSuccess() && declineRes.getMessage().contains("Did not place an Offer on Product"), declineRes.getMessage());
    }

    @Test
    void testDeclineOffer_Success(){
        systemService.placeOfferOnStoreProduct(store, registered, p1, 12);
        Response<List<Offer>> getOffersRes = systemService.getUserOffers(store, registered);
        List<Offer> offers = getOffersRes.getData();
        Offer offer = offers.get(0);
        assertFalse(offer.isDeclined());
        systemService.acceptOfferOnStoreProduct(store, founder, registered, p1);
        Response<Void> declineRes = systemService.declineOfferOnStoreProduct(store, owner1, registered, p1);
        assertTrue(declineRes.isSuccess());
        assertTrue(offer.isDeclined() && offer.isHandled());
        offers = systemService.getUserOffers(store, registered).getData();
        assertTrue(offers.isEmpty());
        List<CartItemInfoDTO> cart = systemService.viewCart(registered).getData();
        assertTrue(cart.isEmpty());
        StoreProductDTO prod1 = systemService.getProductFromStore(p1, store).getData();
        assertTrue(prod1.getQuantity() == q1);
        //sleep because msg sending is async so avoid race condition
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Sleep was interrupted");
        }
        Map<Integer, StoreMsg> registeredMsgs = systemService.getMessagesFromStore(registered).getData();
        boolean found = false;
        for(Integer msgId : registeredMsgs.keySet()){
            StoreMsg msg = registeredMsgs.get(msgId);
            if(msg.getMessage().contains("We regret to inform you that the offer for product: " + p1)){
                found = true;
                break;
            }
        }
        assertTrue(found);

        for(Integer owner : owners){
            Map<Integer, StoreMsg> msgs = systemService.getMessagesFromStore(owner).getData();
            boolean foundNewMsg = false;
            boolean foundOldMsg = false; // should stay false!
            for(Integer msgId : msgs.keySet()){
                StoreMsg msg = msgs.get(msgId);
                if(msg.getMessage().contains("An Offer for product " + p1 + " in Store " + store +" was Declined by " + owner1)){
                    foundNewMsg = true;
                }
                if(msg.getMessage().contains("Received an offer for product " + p1 + ". Offer is: $" + 12 + " by user " + registered)){
                    foundOldMsg = true;
                }
            }
            assertTrue(foundNewMsg & !foundOldMsg);
        }
        
    }

    // **********************
    // **CounterOffer TESTS**
    // **********************

    @Test
    void testCounterOffer_storeNotFound_Fail(){
        systemService.placeOfferOnStoreProduct(store, registered, p1, 12);
        Response<Void> counterRes = systemService.counterOffer(wrongStore, owner1, registered, p1, 20);
        assertTrue(!counterRes.isSuccess() && counterRes.getMessage().contains("Store not found"));
        Response<List<Offer>> getOffersRes = systemService.getUserOffers(store, registered);
        List<Offer> offers = getOffersRes.getData();
        assertFalse(offers.isEmpty());
    }

    @Test
    void testCounterOffer_notOwner_Fail(){
        systemService.placeOfferOnStoreProduct(store, registered, p1, 12);
        Response<Void> countereRes = systemService.counterOffer(store, manager, registered, p1, 20);
        assertTrue(!countereRes.isSuccess() && countereRes.getMessage().contains("is not a valid Store Owner in store"));
        Response<List<Offer>> getOffersRes = systemService.getUserOffers(store, registered);
        List<Offer> offers = getOffersRes.getData();
        assertFalse(offers.isEmpty());
    }

    @Test
    void testCounterOffer_offerNotFound_Fail(){
        Response<Void> counterRes = systemService.counterOffer(store, owner1, registered, p1, 20);
        assertTrue(!counterRes.isSuccess() && counterRes.getMessage().contains("Did not place an Offer on Product"), counterRes.getMessage());
        Response<Void> bidRes = systemService.placeOfferOnStoreProduct(store, registered, p1, 12);
        assertTrue(bidRes.isSuccess());
    }

    @Test
    void testCounterOffer_invalidOffer_Fail(){
        systemService.placeOfferOnStoreProduct(store, registered, p1, 12);
        Response<Void> counterRes = systemService.counterOffer(store, owner1, registered, p1, 0.5);
        assertTrue(!counterRes.isSuccess() && counterRes.getMessage().contains("Counter offer must be more than $1"));
        Response<List<Offer>> getOffersRes = systemService.getUserOffers(store, registered);
        List<Offer> offers = getOffersRes.getData();
        assertFalse(offers.isEmpty());
        Response<Void> bidRes = systemService.placeOfferOnStoreProduct(store, registered, p1, 12);
        assertTrue(!bidRes.isSuccess() && bidRes.getMessage().contains("Can not Offer on the Same Product Twice"));
    }


    @Test
    void testCounterOffer_Success(){
        systemService.placeOfferOnStoreProduct(store, registered, p1, 12);
        Response<List<Offer>> getOffersRes = systemService.getUserOffers(store, registered);
        List<Offer> offers = getOffersRes.getData();
        Offer offer = offers.get(0);
        Response<Void> counterRes = systemService.counterOffer(store, owner1, registered, p1, 20);
        assertTrue(counterRes.isSuccess());
        assertTrue(offer.isDeclined() && offer.isHandled());
        assertTrue(systemService.getUserOffers(store, registered).getData().isEmpty());
         //sleep because msg sending is async so avoid race condition
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Sleep was interrupted");
        }
        Map<Integer, StoreMsg> registeredMsgs = systemService.getMessagesFromStore(registered).getData();
        boolean found = false;
        for(Integer msgId : registeredMsgs.keySet()){
            StoreMsg msg = registeredMsgs.get(msgId);
            if(msg.getMessage().contains("We regret to inform you that the offer for product: " + p1)){
                found = true;
                break;
            }
        }
        assertTrue(found);
        
        found = false;
        for(Integer msgId : registeredMsgs.keySet()){
            StoreMsg msg = registeredMsgs.get(msgId);
            if(msg.getMessage().contains("Owner sent you a Counter Offer for product " + p1)){
                assertTrue(msg.isCounterOffer());
                found = true;
                break;
            }
        }
        assertTrue(found);

        Response<Void> bidRes = systemService.placeOfferOnStoreProduct(store, registered, p1, 12);
        assertTrue(!bidRes.isSuccess() && bidRes.getMessage().contains("User already has pending counter offer"));

    }

    // ****************************
    // **AcceptCounterOffer TESTS**
    // ****************************
    @Test
    void testAcceptCounterOffer_storeNotFound_Fail(){
        systemService.placeOfferOnStoreProduct(store, registered, p1, 12);
        systemService.counterOffer(store, owner1, registered, p1, 20);
        Response<Void> acceptRes = systemService.acceptCounterOffer(wrongStore, registered, p1);
        assertTrue(!acceptRes.isSuccess() && acceptRes.getMessage().contains("Store not found"));
    }

    @Test
    void testAcceptCounterOffer_offerNotFound_Fail(){
        Response<Void> acceptRes = systemService.acceptCounterOffer(store, registered, p1);
        systemService.counterOffer(store, owner1, registered, p1, 20);
        assertTrue(!acceptRes.isSuccess() && acceptRes.getMessage().contains("User has no Pending Counter Offers"));
    }

    @Test
    void testAcceptCounterOffer_Success(){
        systemService.placeOfferOnStoreProduct(store, registered, p1, 12);
        systemService.counterOffer(store, owner1, registered, p1, 20);
        Response<Void> acceptRes = systemService.acceptCounterOffer(store, registered, p1);
        assertTrue(acceptRes.isSuccess());
         //sleep because msg sending is async so avoid race condition
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Sleep was interrupted");
        }
        for(Integer owner : owners){
            Response<Map<Integer, StoreMsg>> msgsRes = systemService.getUserOfferMessages(owner);
            assertTrue(msgsRes.isSuccess(), msgsRes.getMessage());
            Map<Integer, StoreMsg> msgs = msgsRes.getData();
            boolean found = false;
            for(Integer msgId : msgs.keySet()){
                if(msgs.get(msgId).getMessage().contains("Received an offer for product " + p1)){
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
        List<Offer> offers = systemService.getUserOffers(store, registered).getData();
        assertTrue(offers.size() == 1 && offers.get(0).getOfferAmount() == 20); // from here its the same as a normal offer which got tested already
    }

    // *****************************
    // **DeclineCounterOffer TESTS**
    // *****************************
    @Test
    void testDeclineCounterOffer_storeNotFound_Fail(){
        systemService.placeOfferOnStoreProduct(store, registered, p1, 12);
        systemService.counterOffer(store, owner1, registered, p1, 20);
        Response<Void> declineRes = systemService.declineCounterOffer(wrongStore, registered, p1);
        assertTrue(!declineRes.isSuccess() && declineRes.getMessage().contains("Store not found"));
    }

    @Test
    void testDeclineCounterOffer_offerNotFound_Fail(){
        systemService.placeOfferOnStoreProduct(store, registered, p1, 12);
        Response<Void> declineRes = systemService.declineCounterOffer(store, registered, p1);
        assertTrue(!declineRes.isSuccess() && declineRes.getMessage().contains("User has no Pending Counter Offers"));
    }

    @Test
    void testDeclineCounterOffer_Success(){
        systemService.placeOfferOnStoreProduct(store, registered, p1, 12);
        systemService.counterOffer(store, owner1, registered, p1, 20);
        Response<Void> declineRes = systemService.declineCounterOffer(store, registered, p1);
        assertTrue(declineRes.isSuccess());
        List<Offer> offers = systemService.getUserOffers(store, registered).getData();
        assertTrue(offers.isEmpty()); //make sure original was removed
        Response<Void> newBid = systemService.placeOfferOnStoreProduct(store, registered, p1, 12);
        assertTrue(newBid.isSuccess()); //make sure pending was removed
         //sleep because msg sending is async so avoid race condition
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Sleep was interrupted");
        }
        for(Integer owner : owners){
            Response<Map<Integer, StoreMsg>> msgsRes = systemService.getMessagesFromStore(owner);
            assertTrue(msgsRes.isSuccess(), msgsRes.getMessage());
            Map<Integer, StoreMsg> msgs = msgsRes.getData();
            boolean found = false;
            for(Integer msgId : msgs.keySet()){
                if(msgs.get(msgId).getMessage().contains("User " + registered + " Declined your Store's ")){
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
    }
}
