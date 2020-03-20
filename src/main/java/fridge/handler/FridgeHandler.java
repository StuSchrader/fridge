package fridge.handler;

import fridge.domain.Fridge;
import fridge.domain.Item;
import fridge.domain.ItemType;
import fridge.repository.FridgeRepository;
import fridge.repository.ItemRepository;
import fridge.view.CreateFridge;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.springframework.web.reactive.function.server.ServerResponse.status;

@Component
@Slf4j
public class FridgeHandler {
    public static final int MAX_CANS =12;

    private final FridgeRepository fridgeRepository;
    private final ItemRepository itemRepository;

    public FridgeHandler(FridgeRepository fridgeRepository, ItemRepository itemRepository) {
        this.fridgeRepository = fridgeRepository;
        this.itemRepository = itemRepository;
    }


    public Mono<ServerResponse> findFridge(ServerRequest req) {
        String fridgeId = req.pathVariable("id");
        return fridgeRepository.findById(fridgeId)
                .flatMap(fridge -> ok().contentType(APPLICATION_JSON).bodyValue(fridge))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> findFridges(ServerRequest req) {
        return ok().contentType(APPLICATION_JSON).body(fridgeRepository.findAll(), Fridge.class);
    }

    public Mono<ServerResponse> createFridge(ServerRequest req) {
        return req
                .bodyToMono(CreateFridge.class)
                .map(createFridge -> new Fridge(createFridge.getName()))
                .flatMap(fridgeRepository::save)
                .flatMap(saved->
                        status(HttpStatus.CREATED)
                                .contentType(APPLICATION_JSON)
                                .bodyValue(saved));
    }

    public Mono<ServerResponse> addItem(ServerRequest req) {
        String fridgeId = req.pathVariable("id");
        String itemId = req.pathVariable("itemId");
        int quantity = Integer.parseInt(req.pathVariable("qty"));

        Mono<FridgeAndItem> fridgeAndItemMono = fridgeRepository.findById(fridgeId).zipWith(itemRepository.findById(itemId), (f1, i1) -> new FridgeAndItem(f1, i1, quantity));

        return validateInsert(c -> {
            c.fridge.addItem(c.item, c.qty);
            Mono<Fridge> savedMono = fridgeRepository.save(c.fridge);
            return savedMono.flatMap(saved -> ok().contentType(APPLICATION_JSON).bodyValue(saved));
        }, fridgeAndItemMono);

    }

    public Mono<ServerResponse> removeItem(ServerRequest req) {
        String fridgeId = req.pathVariable("id");
        String itemId = req.pathVariable("itemId");
        int quantity = Integer.parseInt(req.pathVariable("qty"));

        Mono<FridgeAndItem> fridgeAndItemMono = fridgeRepository.findById(fridgeId).zipWith(itemRepository.findById(itemId), (f1, i1) -> new FridgeAndItem(f1, i1, quantity));

        return validateRemove(c -> {
            c.fridge.removeItem(c.item, c.qty);
            Mono<Fridge> savedMono = fridgeRepository.save(c.fridge);
            return savedMono.flatMap(saved -> ok().contentType(APPLICATION_JSON).bodyValue(saved));
        }, fridgeAndItemMono);

    }

    // Validate no more than 12 cans of soda here.
    public Mono<ServerResponse> validateInsert(Function<FridgeAndItem, Mono<ServerResponse>> block,
                                                Mono<FridgeAndItem> fridgeAndItemMono) {

       return fridgeAndItemMono
               .flatMap(combo -> combo.item.getItemType() == ItemType.SODA && combo.fridge.getItemTypeCount(ItemType.SODA) + combo.qty > MAX_CANS
                       ? ServerResponse.badRequest().bodyValue("Cannot exceed 12 cans of soda")
                       : block.apply(combo))
               .switchIfEmpty(ServerResponse.notFound().build());

    }


    // Validate can only remove found items.
    public Mono<ServerResponse> validateRemove(Function<FridgeAndItem, Mono<ServerResponse>> block,
                                               Mono<FridgeAndItem> fridgeAndItemMono) {
        return fridgeAndItemMono
                .flatMap(combo-> combo.fridge.getItemCount(combo.item.getId()) - combo.qty < 0
                ? ServerResponse.badRequest().bodyValue("Cannot remove that many of item")
                : block.apply(combo))
                .switchIfEmpty(ServerResponse.notFound().build());

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FridgeAndItem {
        Fridge fridge;
        Item item;
        int qty;
    }
}
