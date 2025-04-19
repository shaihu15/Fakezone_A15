package DomainLayer.Interfaces;

import java.util.List;

public interface IProduct {
    int getId();
    String getName();
    void setId(int id);
    void setName(String name);
    String getDescription();
    void setDescription(String description);
    List<Integer> getStoresIds();
    void removeStore(Integer storesId);
    void addStore(Integer storesId);
    
}