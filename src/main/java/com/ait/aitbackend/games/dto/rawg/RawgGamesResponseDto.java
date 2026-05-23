package com.ait.aitbackend.games.dto.rawg;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO odpowiedzi z API RAWG dla listy gier.
 * Zawiera metadane paginacji oraz listę wyników.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RawgGamesResponseDto {

    private Integer count;
    private String next;
    private String previous;
    private List<RawgGameDto> results;

    // zabezpieczenie na nieznane pola z API
    private final Map<String, Object> additionalProperties = new HashMap<>();

    @JsonAnySetter
    public void putAdditionalProperty(String key, Object value) {
        this.additionalProperties.put(key, value);
    }

    /**
     * Pojedyncza gra z RAWG.
     */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawgGameDto {

        private Integer id;
        private String slug;
        private String name;

        @JsonProperty("description_raw")
        private String descriptionRaw;

        private String website;
        private String released;

        @JsonProperty("background_image")
        private String image;

        private Double rating;
        private Integer metacritic;

        private List<RawgStoreWrapperDto> stores;

        private final Map<String, Object> additionalProperties =
                new HashMap<>();

        @JsonAnySetter
        public void putAdditionalProperty(String key, Object value) {
            this.additionalProperties.put(key, value);
        }
    }

    /**
     * Wrapper na sklep w RAWG (struktura zagnieżdżona).
     */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawgStoreWrapperDto {
        private RawgStoreDto store;
    }

    /**
     * Dane pojedynczego sklepu (RAWG).
     */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawgStoreDto {
        private Integer id;
        private String name;
        private String slug;
    }
}