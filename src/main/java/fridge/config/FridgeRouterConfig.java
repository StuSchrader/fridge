package fridge.config;

import fridge.handler.FridgeHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class FridgeRouterConfig {

    @Bean
    RouterFunction<ServerResponse> fridgeRoute(FridgeHandler fridgeHandler){
        return route().
                path("/fridge", b1-> b1
                        .nest(accept(MediaType.APPLICATION_JSON), b2-> b2
                                .POST("", fridgeHandler::createFridge))
                        .GET("/{id}", fridgeHandler::findFridge)
                        .GET("", fridgeHandler::findFridges)
                        .PUT("/{id}/item/{itemId}/qty/{qty}", fridgeHandler::addItem)
                        .DELETE("/{id}/item/{itemId}/qty/{qty}", fridgeHandler::removeItem))
                .build();
    }

}
