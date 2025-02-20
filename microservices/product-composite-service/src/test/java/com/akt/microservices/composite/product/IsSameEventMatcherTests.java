package com.akt.microservices.composite.product;

import com.akt.api.core.product.Product;
import com.akt.api.event.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static com.akt.api.event.Event.Type.CREATE;
import static com.akt.api.event.Event.Type.DELETE;
import static com.akt.microservices.composite.product.IsSameEventMatcher.sameEventExceptCreatedAt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class IsSameEventMatcherTests {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void testCompareEvents() throws JsonProcessingException {
        Event<Integer, Product> expectedEvent = new Event<>(CREATE, 1, new Product(1, "name", 1, null));
        Event<Integer, Product> actualEvent1 = new Event<>(CREATE, 1, new Product(1, "name", 1, null));
        Event<Integer, Product> actualEvent2 = new Event<>(DELETE, 1, null);
        Event<Integer, Product> actualEvent3 = new Event<>(CREATE, 1, new Product(2, "name", 1, null));

        String actualEventAsJson = mapper.writeValueAsString(expectedEvent);

        assertThat(actualEventAsJson, is(sameEventExceptCreatedAt(actualEvent1)));
        assertThat(actualEventAsJson, not(sameEventExceptCreatedAt(actualEvent2)));
        assertThat(actualEventAsJson, not(sameEventExceptCreatedAt(actualEvent3)));
    }
}
