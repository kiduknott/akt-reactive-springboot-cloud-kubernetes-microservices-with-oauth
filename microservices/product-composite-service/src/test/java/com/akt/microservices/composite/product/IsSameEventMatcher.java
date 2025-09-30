package com.akt.microservices.composite.product;

import com.akt.api.event.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class IsSameEventMatcher extends TypeSafeMatcher<String> {

    private static final Logger logger = LoggerFactory.getLogger(IsSameEventMatcher.class);

    private ObjectMapper mapper = new ObjectMapper();
    private Event expectedEvent;

    private IsSameEventMatcher(Event expectedEvent) {
        this.expectedEvent = expectedEvent;
    }

    public static Matcher<String> sameEventExceptCreatedAt(Event expectedEvent) {
        return new IsSameEventMatcher(expectedEvent);
    }

    @Override
    protected boolean matchesSafely(String expectedEventAsJson) {
        if(expectedEvent == null){
            return false;
        }

        logger.trace("Converting the actual event json string to a map: {}", expectedEventAsJson);
        Map actualEventAsMap = convertJsonStringToMap(expectedEventAsJson);
        actualEventAsMap.remove("eventCreatedAt");
        logger.trace("Created map of the actual event: {}", actualEventAsMap);

        Map expectedEventAsMap = getMapWithoutCreatedAt(expectedEvent);
        logger.trace("Created map of the expected event: {}", expectedEventAsMap);

        return actualEventAsMap.equals(expectedEventAsMap);
    }

    @Override
    public void describeTo(Description description) {
        String expectedJson = convertObjectToJsonString(expectedEvent);
        description.appendText("expected to look like " + expectedJson);
    }

    private String convertObjectToJsonString(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Map convertJsonStringToMap(String eventAsJson) {
        try {
            return mapper.readValue(eventAsJson, new TypeReference<HashMap>(){});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Map getMapWithoutCreatedAt(Event event) {
        Map mapEvent = convertObjectToMap(event);
        mapEvent.remove("eventCreatedAt");
        return mapEvent;
    }

    private Map convertObjectToMap(Object object) {
        JsonNode node = mapper.convertValue(object, JsonNode.class);
        return mapper.convertValue(node, Map.class);
    }
}
