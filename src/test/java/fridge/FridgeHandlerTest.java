package fridge;

import fridge.config.FridgeRouterConfig;
import fridge.domain.Fridge;
import fridge.domain.Item;
import fridge.domain.ItemType;
import fridge.handler.FridgeHandler;
import fridge.repository.FridgeRepository;
import fridge.repository.ItemRepository;
import fridge.view.CreateFridge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static fridge.handler.FridgeHandler.MAX_CANS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("local")
@SpringBootTest(classes=FridgeApplication.class)
@WithMockUser
public class FridgeHandlerTest {
    public static final String COKE_ID = "cokeid";
    public static final String FRIDGE_ID = "fridgeid";
    public static final String PEPSI_ID = "pepsiid";

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    FridgeRouterConfig fridgeRouterConfig;

    @Autowired
    FridgeHandler fridgeHandler;

    private WebTestClient webTestClient;

    @MockBean
    private FridgeRepository fridgeRepository;

    @MockBean
    private  ItemRepository itemRepository;


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
        CreateFridge createFridge = new CreateFridge("fridge");

        Fridge fridge = new Fridge("fridge");
        Mono<Fridge> fridgeMono = Mono.just(fridge);

        when(fridgeRepository.save(any(Fridge.class))).thenReturn(fridgeMono);

        webTestClient.post()
                .uri("/fridge")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(createFridge))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Fridge.class)
                .value(f-> {
                    assertEquals("fridge", f.getName());
                });

        ArgumentCaptor<Fridge> argumentCaptor = ArgumentCaptor.forClass(Fridge.class);

        verify(fridgeRepository).save(argumentCaptor.capture());

        assertEquals("fridge", argumentCaptor.getValue().getName());
    }

    @Test
    public void getById() {
        Fridge foundFridge = new Fridge(FRIDGE_ID, "name", new ArrayList<>());
        Mono<Fridge> resultMono = Mono.just(foundFridge);

        when(fridgeRepository.findById(FRIDGE_ID)).thenReturn(resultMono);

        webTestClient.get()
                .uri("/fridge/"+FRIDGE_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Fridge.class)
                .value(response -> {
                   assertEquals(FRIDGE_ID, response.getId());
                   assertEquals("name", response.getName());
                   assertTrue(response.getItems().isEmpty());
                });
    }

    @Test
    public void getAll() {
        Fridge foundFridge1 = new Fridge(FRIDGE_ID, "name1", new ArrayList<>());
        Fridge foundFridge2 = new Fridge(FRIDGE_ID, "name2", new ArrayList<>());
        Flux<Fridge> resultMono = Flux.just(foundFridge1, foundFridge2);
        when(fridgeRepository.findAll()).thenReturn(resultMono);

        webTestClient.get()
                .uri("/fridge")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Fridge.class)
                .value(rl -> {
                    assertEquals("name1", rl.get(0).getName());
                    assertEquals("name2", rl.get(1).getName());
                });
    }

    @Test
    public void addItem() {

        Item pepsi = new Item(PEPSI_ID, "pepsi", ItemType.SODA);

        List<Item> items = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            items.add(pepsi);
        }

        Fridge foundFridge = new Fridge(FRIDGE_ID, "name", items);

        Item coke = new Item(COKE_ID, "coke", ItemType.SODA);

        Mono<Fridge> fridgeMono = Mono.just(foundFridge);
        when(fridgeRepository.findById(FRIDGE_ID)).thenReturn(fridgeMono);

        Mono<Item> cokeMono  = Mono.just(coke);
        when(itemRepository.findById(COKE_ID)).thenReturn(cokeMono);

        Mono<Fridge> resultMono = Mono.just(foundFridge);
        when(fridgeRepository.save(any(Fridge.class))).thenReturn(resultMono);

        webTestClient
                .put()
                .uri("/fridge/"+FRIDGE_ID+"/item/"+ COKE_ID +"/qty/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Fridge.class)
                .value(value -> {
                    assertNotNull(value);
                    List<Item> savedItems = value.getItems();
                    assertNotNull(savedItems);
                    assertEquals(3, savedItems.size());
                    assertEquals(1, savedItems.stream().filter(item -> item.getId().equals(COKE_ID)).count() );
                    assertEquals(2, savedItems.stream().filter(item -> item.getId().equals(PEPSI_ID)).count() );
                });
    }

    @Test
    public void removeItem() {

        Item pepsi = new Item(PEPSI_ID, "pepsi", ItemType.SODA);

        List<Item> items = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            items.add(pepsi);
        }

        Fridge foundFridge = new Fridge(FRIDGE_ID, "name", items);

        Mono<Fridge> fridgeMono = Mono.just(foundFridge);
        when(fridgeRepository.findById(FRIDGE_ID)).thenReturn(fridgeMono);

        Mono<Item> pepsiMono  = Mono.just(pepsi);
        when(itemRepository.findById(PEPSI_ID)).thenReturn(pepsiMono);

        Mono<Fridge> resultMono = Mono.just(foundFridge);
        when(fridgeRepository.save(any(Fridge.class))).thenReturn(resultMono);

        webTestClient
                .delete()
                .uri("/fridge/"+FRIDGE_ID+"/item/"+ PEPSI_ID +"/qty/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Fridge.class)
                .value(value -> {
                    assertNotNull(value);
                    List<Item> savedItems = value.getItems();
                    assertNotNull(savedItems);
                    assertEquals(1, savedItems.size());
                    assertEquals(1, savedItems.stream().filter(item -> item.getId().equals(PEPSI_ID)).count() );
                });
    }

    @Test
    @WithMockUser
    public void sodaValidation() {

        Item pepsi = new Item(PEPSI_ID, "pepsi", ItemType.SODA);

        List<Item> items = new ArrayList<>();
        for (int i = 0; i < MAX_CANS; i++) {
            items.add(pepsi);
        }

        Fridge foundFridge = new Fridge(FRIDGE_ID, "name", items);

        Item coke = new Item(COKE_ID, "coke", ItemType.SODA);

        Mono<Fridge> fridgeMono = Mono.just(foundFridge);
        when(fridgeRepository.findById(FRIDGE_ID)).thenReturn(fridgeMono);

        Mono<Item> cokeMono  = Mono.just(coke);
        when(itemRepository.findById(COKE_ID)).thenReturn(cokeMono);

        webTestClient
                .put()
                .uri("/fridge/"+FRIDGE_ID+"/item/"+ COKE_ID +"/qty/1")
                .exchange()
                .expectStatus().isBadRequest();
    }
}
