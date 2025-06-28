package DomainLayer.Model;

import java.io.Serializable;
import java.util.Objects;

public class StoreProductKey implements Serializable {
    private int sproductId;
    private int storeId;

    public StoreProductKey() {}

    public StoreProductKey(int storeId, int sproductId) {
        this.sproductId = sproductId;
        this.storeId = storeId;
    }

    public int getSproductId() {
        return sproductId;
    }

    public int getStoreId() {
        return storeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoreProductKey that = (StoreProductKey) o;
        return sproductId == that.sproductId && storeId == that.storeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sproductId, storeId);
    }
} 