package ApplicationLayer.Services;

import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class UserIdInitializer {
    @Autowired
    private IUserRepository userRepository;

    @PostConstruct
    public void init() {
        int nextId = userRepository.getNextAvailableUserId();
        // Set counter to nextId - 1 so the first increment is the correct next available
        User.setIdCounter(nextId - 1);
    }
} 