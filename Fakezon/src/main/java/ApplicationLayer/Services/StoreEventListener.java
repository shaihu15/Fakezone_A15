package ApplicationLayer.Services;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.Model.Store;
import DomainLayer.Model.helpers.AssignmentEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionSaveEvent;
import InfrastructureLayer.Adapters.NotificationWebSocketHandler;
import jakarta.transaction.Transactional;

@Component
public class StoreEventListener {

    private final IStoreRepository storeRepository;
    private final NotificationWebSocketHandler wsHandler; //web socket

    public StoreEventListener(IStoreRepository storeRepository, NotificationWebSocketHandler wsHandler) {
        this.storeRepository = storeRepository;
        this.wsHandler = wsHandler;
    }

    @Async
    @EventListener
    @Transactional
    public void handleSaveAuction(AuctionSaveEvent event) {
        Store store = storeRepository.findById(event.getStoreId());
        if (store == null) {
            // Handle the case where the store is not found
            return;
        }
        storeRepository.save(store);
    }
}