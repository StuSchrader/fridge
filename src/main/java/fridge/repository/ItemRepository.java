package fridge.repository;

import fridge.domain.Item;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ItemRepository extends ReactiveMongoRepository<Item, String> {
    Flux<Item> findByItemType(String itemType);
}
