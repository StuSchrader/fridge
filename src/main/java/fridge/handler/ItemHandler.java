package fridge.handler;

import fridge.domain.Item;
import fridge.repository.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.springframework.web.reactive.function.server.ServerResponse.status;

@Component
public class ItemHandler {
    private final ItemRepository itemRepository;

    public ItemHandler(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }


    public Mono<ServerResponse> findItem(ServerRequest req) {
        String itemId = req.pathVariable("id");
        return itemRepository.findById(itemId)
                .flatMap(item -> ok().contentType(APPLICATION_JSON).bodyValue(item))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> findItems(ServerRequest req) {
        return ok().contentType(APPLICATION_JSON).body(itemRepository.findAll(), Item.class);
    }

    public Mono<ServerResponse> createItem(ServerRequest req) {
        return req
                .bodyToMono(Item.class)
                .flatMap(itemRepository::save)
                .flatMap(saved->
                        status(HttpStatus.CREATED)
                                .contentType(APPLICATION_JSON)
                                .bodyValue(saved));
    }

    public Mono<ServerResponse> findItemsByType(ServerRequest req) {
        String itemType = req.pathVariable("itemType");
        return ok().contentType(APPLICATION_JSON).body(itemRepository.findByItemType(itemType), Item.class);
    }
}
