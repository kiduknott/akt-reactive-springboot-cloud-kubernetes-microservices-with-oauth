package com.akt.api.core.recommendation;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface RecommendationService {
    @GetMapping(
            value = "/recommendation",
            produces = "application/json")
    List<Recommendation> getRecommendation(@RequestParam(value = "productId", required = true) int productId);
}