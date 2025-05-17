package UnitTesting;
import ApplicationLayer.RequestDataTypes.RequestOrderDataType;
import ApplicationLayer.DTO.BasketDTO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RequestOrderDataTypeTest {

    @Test
    void testConstructorAndGetters() {
        Integer orderId = 1001;
        Integer userId = 42;
        String address = "123 Main St";
        String paymentMethod = "Credit Card";
        BasketDTO basket = new BasketDTO(1, new java.util.HashMap<>()); 


        RequestOrderDataType request = new RequestOrderDataType(orderId, userId, address, paymentMethod, basket);

        assertEquals(orderId, request.getOrderId());
        assertEquals(userId, request.getUserId());
        assertEquals(address, request.getAddress());
        assertEquals(paymentMethod, request.getPaymentMethod());
        assertEquals(basket, request.getBasket());
    }
}