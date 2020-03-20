package fridge.config;

import fridge.handler.ItemHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ItemRouterConfig {

    @Bean
    RouterFunction<ServerResponse> itemRoute(ItemHandler itemHandler){
        return route().
                path("/item", b1-> b1
                        .nest(accept(MediaType.APPLICATION_JSON), b2-> b2
                                .POST("", itemHandler::createItem))
                        .GET("/{id}", itemHandler::findItem)
                        .GET("", itemHandler::findItems)
                        .GET("/itemType/{itemType}", itemHandler::findItemsByType))
                .build();
    }
}
