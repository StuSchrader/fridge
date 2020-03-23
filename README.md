## Spring WebFlux Functional RESTful Fridge Example

This app is a simple working example of a RESTful reactive spring boot application.
Authentication/Authorization is supported via OKTA OAuth 2. 

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
                        .PUT("/{id}/item/{itemId}/qty/{qty}", fridgeHandler::addItem)
                        .DELETE("/{id}/item/{itemId}/qty/{qty}", fridgeHandler::removeItem))
                .build();
```

Security is configured currently such that authorization is required for any operation in SecurityConfig.java.  If you want to play with roles/authorities based on patterns, you can insert matchers here.  For example, the three commented out lines below would cause all PUT/POST/DELETE methods to require Admin authority.

```java        
        http.csrf().disable()
                .authorizeExchange()
//                .pathMatchers(HttpMethod.POST, "**").hasAnyAuthority("Admin")
//                .pathMatchers(HttpMethod.PUT, "**").hasAnyAuthority("Admin")
//                .pathMatchers(HttpMethod.DELETE, "**").hasAnyAuthority("Admin")
                .anyExchange().authenticated()
                .and()
                .oauth2ResourceServer()
                .jwt();
        return http.build();


Included is a basic set of calls for postman in: `doc\postman\` 

### Tech Used
* Spring Boot WebFlux Functional
* Embedded Mongo
* Java 11
* Oauth2 security
