package ApplicationLayer;

import DomainLayer.Model.Registered;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Model.helpers.AssignmentEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.AbstractMap.SimpleEntry;
import java.util.Optional;

@Component
public class UserEventListener {

    private final IUserRepository userRepository;

    public UserEventListener(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @EventListener
    public void handleAssignmentEvent(AssignmentEvent event) {
        Optional<Registered> user = userRepository.findById(event.getUserId());
        if (user != null) {
            user.get().AssignmentMessages(
                new SimpleEntry<>(event.getStoreId(), 
                "Please approve or decline this role: " + event.getRoleName() +
                " for store " + event.getStoreId())
            );
        }
    }

    // כאן אפשר להוסיף מאזינים לאירועים אחרים (AuctionEnded, AuctionDeclined וכו') בעתיד
}
