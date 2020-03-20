## Spring WebFlux Functional RESTful Fridge Example

This app is a simple working example of a RESTful reactive spring boot application. 

It is based on two entities: Fridge & Item

The `\item` CRUD API is Configured in [ItemRouterConfig.java](src/main/java/fridge/config/ItemRouterConfig.java):
```java
        return route().
                path("/item", b1-> b1
                        .nest(accept(MediaType.APPLICATION_JSON), b2-> b2
                                .POST("", itemHandler::createItem))
                        .GET("/{id}", itemHandler::findItem)
                        .GET("", itemHandler::findItems)
                        .GET("/itemType/{itemType}", itemHandler::findItemsByType))
                .build();
```

The `\fridge` CRUD API, plus the insert/remove item are defined in [FridgeItemRouter.java](src/main/java/fridge/config/FridgeRouterConfig.java):
```java
      return route().
                path("/fridge", b1-> b1
                        .nest(accept(MediaType.APPLICATION_JSON), b2-> b2
                                .POST("", fridgeHandler::createFridge))
                        .GET("/{id}", fridgeHandler::findFridge)
                        .GET("", fridgeHandler::findFridges)
                        .PUT("/{id}/item/{itemId}/qty/{qty}", fridgeHandler::addItem))
                .build();
```

Included is a basic set of calls for postman in: `doc\postman\` 

### Tech Used
* Spring Boot WebFlux Functional
* Embedded Mongo
* Java 11
