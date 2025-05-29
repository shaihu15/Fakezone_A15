package UnitTesting;

import DomainLayer.Model.XorDiscount;
import DomainLayer.Interfaces.IDiscountScope;
import DomainLayer.Model.Cart;
import DomainLayer.Model.OrDiscount;
import DomainLayer.Model.ConditionDiscount;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;
import DomainLayer.Model.AndDiscount;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class discountTests {

    @Test
    void testApply_DiscountAppliedWhenExactlyOneConditionTrue() {
        Cart cart = mock(Cart.class);
        Predicate<Cart> cond1 = c -> true;
        Predicate<Cart> cond2 = c -> false;
        IDiscountScope scope = mock(IDiscountScope.class);
        when(scope.getEligibleAmount(cart)).thenReturn(200.0);

        XorDiscount discount = new XorDiscount(42, List.of(cond1, cond2), 10.0, scope);

        double result = discount.apply(cart);

        assertEquals(20.0, result, 0.0001);
    }

    @Test
    void testApply_NoDiscountWhenZeroConditionsTrue() {
        Cart cart = mock(Cart.class);
        Predicate<Cart> cond1 = c -> false;
        Predicate<Cart> cond2 = c -> false;
        IDiscountScope scope = mock(IDiscountScope.class);

        XorDiscount discount = new XorDiscount(7, List.of(cond1, cond2), 50.0, scope);

        double result = discount.apply(cart);

        assertEquals(0.0, result, 0.0001);
        verify(scope, never()).getEligibleAmount(any());
    }

    @Test
    void testApply_NoDiscountWhenMoreThanOneConditionTrue() {
        Cart cart = mock(Cart.class);
        Predicate<Cart> cond1 = c -> true;
        Predicate<Cart> cond2 = c -> true;
        IDiscountScope scope = mock(IDiscountScope.class);

        XorDiscount discount = new XorDiscount(8, List.of(cond1, cond2), 50.0, scope);

        double result = discount.apply(cart);

        assertEquals(0.0, result, 0.0001);
        verify(scope, never()).getEligibleAmount(any());
    }

    @Test
    void testIsApplicable_ExactlyOneTrue() {
        Cart cart = mock(Cart.class);
        Predicate<Cart> cond1 = c -> false;
        Predicate<Cart> cond2 = c -> true;
        Predicate<Cart> cond3 = c -> false;

        XorDiscount discount = new XorDiscount(1, List.of(cond1, cond2, cond3), 15.0, mock(IDiscountScope.class));

        assertTrue(discount.isApplicable(cart));
    }

    @Test
    void testIsApplicable_ZeroTrue() {
        Cart cart = mock(Cart.class);
        Predicate<Cart> cond1 = c -> false;
        Predicate<Cart> cond2 = c -> false;

        XorDiscount discount = new XorDiscount(2, List.of(cond1, cond2), 15.0, mock(IDiscountScope.class));

        assertFalse(discount.isApplicable(cart));
    }

    @Test
    void testIsApplicable_MoreThanOneTrue() {
        Cart cart = mock(Cart.class);
        Predicate<Cart> cond1 = c -> true;
        Predicate<Cart> cond2 = c -> true;

        XorDiscount discount = new XorDiscount(3, List.of(cond1, cond2), 15.0, mock(IDiscountScope.class));

        assertFalse(discount.isApplicable(cart));
    }

    @Test
    void testGetPolicyID() {
        XorDiscount discount = new XorDiscount(123, List.of(), 0.0, mock(IDiscountScope.class));
        assertEquals(123, discount.getPolicyID());
    }
    @Test
    void testAndDiscount_Apply_Success() {
        Cart cart = mock(Cart.class);
        Predicate<Cart> cond1 = c -> true;
        Predicate<Cart> cond2 = c -> true;
        IDiscountScope scope = mock(IDiscountScope.class);
        when(scope.getEligibleAmount(cart)).thenReturn(100.0);

        AndDiscount discount = new AndDiscount(10, List.of(cond1, cond2), 20.0, scope);

        double result = discount.apply(cart);

        assertEquals(20.0, result, 0.0001);
    }

    @Test
    void testAndDiscount_Apply_NotApplicable() {
        Cart cart = mock(Cart.class);
        Predicate<Cart> cond1 = c -> true;
        Predicate<Cart> cond2 = c -> false;
        IDiscountScope scope = mock(IDiscountScope.class);

        AndDiscount discount = new AndDiscount(11, List.of(cond1, cond2), 50.0, scope);

        double result = discount.apply(cart);

        assertEquals(0.0, result, 0.0001);
        verify(scope, never()).getEligibleAmount(any());
    }

    @Test
    void testAndDiscount_IsApplicable_AllTrue() {
        Cart cart = mock(Cart.class);
        Predicate<Cart> cond1 = c -> true;
        Predicate<Cart> cond2 = c -> true;

        AndDiscount discount = new AndDiscount(12, List.of(cond1, cond2), 10.0, mock(IDiscountScope.class));

        assertTrue(discount.isApplicable(cart));
    }

    @Test
    void testAndDiscount_IsApplicable_OneFalse() {
        Cart cart = mock(Cart.class);
        Predicate<Cart> cond1 = c -> true;
        Predicate<Cart> cond2 = c -> false;

        AndDiscount discount = new AndDiscount(13, List.of(cond1, cond2), 10.0, mock(IDiscountScope.class));

        assertFalse(discount.isApplicable(cart));
    }

    @Test
    void testAndDiscount_GetPolicyID() {
        AndDiscount discount = new AndDiscount(99, List.of(), 0.0, mock(IDiscountScope.class));
        assertEquals(99, discount.getPolicyID());
    }
    @Test
    void testOrDiscount_Apply_Success() {
        Cart cart = mock(Cart.class);
        Predicate<Cart> cond1 = c -> false;
        Predicate<Cart> cond2 = c -> true;
        IDiscountScope scope = mock(IDiscountScope.class);
        when(scope.getEligibleAmount(cart)).thenReturn(150.0);

        OrDiscount discount = new OrDiscount(21, List.of(cond1, cond2), 10.0, scope);

        double result = discount.apply(cart);

        assertEquals(15.0, result, 0.0001);
    }

    @Test
    void testOrDiscount_Apply_NotApplicable() {
        Cart cart = mock(Cart.class);
        Predicate<Cart> cond1 = c -> false;
        Predicate<Cart> cond2 = c -> false;
        IDiscountScope scope = mock(IDiscountScope.class);

        OrDiscount discount = new OrDiscount(22, List.of(cond1, cond2), 50.0, scope);

        double result = discount.apply(cart);

        assertEquals(0.0, result, 0.0001);
        verify(scope, never()).getEligibleAmount(any());
    }

    @Test
    void testOrDiscount_IsApplicable_AnyTrue() {
        Cart cart = mock(Cart.class);
        Predicate<Cart> cond1 = c -> false;
        Predicate<Cart> cond2 = c -> true;

        OrDiscount discount = new OrDiscount(23, List.of(cond1, cond2), 10.0, mock(IDiscountScope.class));

        assertTrue(discount.isApplicable(cart));
    }

    @Test
    void testOrDiscount_IsApplicable_AllFalse() {
        Cart cart = mock(Cart.class);
        Predicate<Cart> cond1 = c -> false;
        Predicate<Cart> cond2 = c -> false;

        OrDiscount discount = new OrDiscount(24, List.of(cond1, cond2), 10.0, mock(IDiscountScope.class));

        assertFalse(discount.isApplicable(cart));
    }

    @Test
    void testOrDiscount_GetPolicyID() {
        OrDiscount discount = new OrDiscount(77, List.of(), 0.0, mock(IDiscountScope.class));
        assertEquals(77, discount.getPolicyID());
    }
    @Test
    void testConditionDiscount_Apply_ConditionTrue() {
        Cart cart = mock(Cart.class);
        Predicate<Cart> condition = c -> true;
        IDiscountScope scope = mock(IDiscountScope.class);
        when(scope.getEligibleAmount(cart)).thenReturn(50.0);
    
        ConditionDiscount discount = new ConditionDiscount(5, 10.0, scope, condition);
    
        double result = discount.apply(cart);
    
        assertEquals(5.0, result, 0.0001);
    }
    
    @Test
    void testConditionDiscount_Apply_ConditionFalse() {
        Cart cart = mock(Cart.class);
        Predicate<Cart> condition = c -> false;
        IDiscountScope scope = mock(IDiscountScope.class);
    
        ConditionDiscount discount = new ConditionDiscount(6, 10.0, scope, condition);
    
        double result = discount.apply(cart);
    
        assertEquals(0.0, result, 0.0001);
        verify(scope, never()).getEligibleAmount(any());
    }
    
    @Test
    void testConditionDiscount_IsApplicable_True() {
        Cart cart = mock(Cart.class);
        Predicate<Cart> condition = c -> true;
    
        ConditionDiscount discount = new ConditionDiscount(7, 10.0, mock(IDiscountScope.class), condition);
    
        assertTrue(discount.isApplicable(cart));
    }
    
    @Test
    void testConditionDiscount_IsApplicable_False() {
        Cart cart = mock(Cart.class);
        Predicate<Cart> condition = c -> false;
    
        ConditionDiscount discount = new ConditionDiscount(8, 10.0, mock(IDiscountScope.class), condition);
    
        assertFalse(discount.isApplicable(cart));
    }
    
    @Test
    void testConditionDiscount_GetPolicyID() {
        ConditionDiscount discount = new ConditionDiscount(99, 10.0, mock(IDiscountScope.class), c -> true);
        assertEquals(99, discount.getPolicyID());
    }
}
