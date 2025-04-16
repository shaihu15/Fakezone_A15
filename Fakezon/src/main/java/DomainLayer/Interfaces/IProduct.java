package DomainLayer.Interfaces;

public interface IProduct {
    int getId();
    String getName();
    void setId(int id);
    void setName(String name);
    String getDescription();
    void setDescription(String description);
}