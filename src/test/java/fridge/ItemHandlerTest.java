package fridge;

import fridge.config.ItemRouterConfig;
import fridge.domain.Item;
import fridge.handler.ItemHandler;
import fridge.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static fridge.domain.ItemType.SODA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@WebFluxTest
@ContextConfiguration(classes = {ItemHandler.class, ItemRouterConfig.class})
public class ItemHandlerTest {

    public static final String ITEM_ID = "itemId";
    public static final String ITEM_ID_2 = "itemId2";

    public static final String ITEM_NAME = "Coke";
    public static final String ITEM_NAME_2 = "Pepsi";

    @Autowired
    private ApplicationContext applicationContext;

    @MockBean
    private ItemRepository itemRepository;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient
                .bindToApplicationContext(applicationContext)
                .apply(springSecurity())
                .configureClient()
                .build();
    }

    @Test
    public void create() {
        Item item = new Item(ITEM_ID, ITEM_NAME, SODA);
        Mono<Item> itemMono = Mono.just(item);

        when(itemRepository.save(any(Item.class))).thenReturn(itemMono);

        webTestClient.post()
                .uri("/item")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(item))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Item.class)
                .value(i-> {
                    assertEquals(ITEM_ID, i.getId());
                    assertEquals(ITEM_NAME, i.getName());
                    assertEquals(SODA, i.getItemType());
                });

        ArgumentCaptor<Item> argumentCaptor = ArgumentCaptor.forClass(Item.class);

        verify(itemRepository).save(argumentCaptor.capture());

        assertEquals(ITEM_NAME, argumentCaptor.getValue().getName());
        assertEquals(SODA, argumentCaptor.getValue().getItemType());
    }


    @Test
    @WithMockUser
    public void getById() {
        Item foundItem = new Item(ITEM_ID, ITEM_NAME, SODA);
        Mono<Item> resultMono = Mono.just(foundItem);

        when(itemRepository.findById(ITEM_ID)).thenReturn(resultMono);

        webTestClient.get()
                .uri("/item/"+ITEM_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Item.class)
                .value(response -> {
                    assertEquals(ITEM_ID, response.getId());
                    assertEquals(ITEM_NAME, response.getName());
                    assertEquals(SODA, response.getItemType());
                });
    }

    @Test
    @WithMockUser
    public void getAll() {
        Item foundItem1 = new Item(ITEM_ID, ITEM_NAME, SODA);
        Item foundItem2 = new Item(ITEM_ID_2, ITEM_NAME_2, SODA);
        Flux<Item> resultMono = Flux.just(foundItem1, foundItem2);
        when(itemRepository.findAll()).thenReturn(resultMono);

        webTestClient.get()
                .uri("/item")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Item.class)
                .value(rl -> {
                    assertEquals(ITEM_NAME, rl.get(0).getName());
                    assertEquals(ITEM_NAME_2, rl.get(1).getName());
                });
    }


    @Test
    @WithMockUser
    public void getItemsByType() {
        Item foundItem1 = new Item(ITEM_ID, ITEM_NAME, SODA);
        Item foundItem2 = new Item(ITEM_ID_2, ITEM_NAME_2, SODA);
        Flux<Item> resultMono = Flux.just(foundItem1, foundItem2);

        when(itemRepository.findByItemType(SODA.name())).thenReturn(resultMono);

        webTestClient.get()
                .uri("/item/itemType/SODA")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Item.class)
                .value(rl -> {
                    assertEquals(ITEM_NAME, rl.get(0).getName());
                    assertEquals(ITEM_NAME_2, rl.get(1).getName());
                });
    }
}
