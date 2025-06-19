package DomainLayer.Model;

import java.time.LocalDate;

import jakarta.persistence.Id;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "suspended_users")
public class SuspendedUser {
    @Id
    private int userId;

    private LocalDate suspensionEndDate; // null means permanent

    public SuspendedUser() {}

    public SuspendedUser(int userId, LocalDate suspensionEndDate) {
        this.userId = userId;
        this.suspensionEndDate = suspensionEndDate;
    }

    public int getUserId() {
        return userId;
    }

    public LocalDate getSuspensionEndDate() {
        return suspensionEndDate;
    }

    public boolean isPermanent() {
        return suspensionEndDate == null;
    }
}