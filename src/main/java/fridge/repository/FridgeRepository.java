package fridge.repository;

import fridge.domain.Fridge;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface FridgeRepository extends ReactiveMongoRepository<Fridge, String> {
}
