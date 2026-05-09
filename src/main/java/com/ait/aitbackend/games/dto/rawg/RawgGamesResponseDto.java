package com.ait.aitbackend.games.dto.rawg;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RawgGamesResponseDto {
    private Integer count;
    private String next;
    private String previous;
    private List<RawgGameDto> results;
    private final Map<String, Object> additionalProperties = new HashMap<>();

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public List<RawgGameDto> getResults() {
        return results;
    }

    public void setResults(List<RawgGameDto> results) {
        this.results = results;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    @JsonAnySetter
    public void putAdditionalProperty(String key, Object value) {
        this.additionalProperties.put(key, value);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawgGameDto {
        private Integer id;
        private String slug;
        private String name;
        private String released;
        private String backgroundImage;
        private Double rating;
        private Integer metacritic;
        private List<RawgStoreWrapperDto> stores;
        private final Map<String, Object> additionalProperties = new HashMap<>();

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getReleased() {
            return released;
        }

        public void setReleased(String released) {
            this.released = released;
        }

        @JsonProperty("background_image")
        public String getBackgroundImage() {
            return backgroundImage;
        }

        @JsonProperty("background_image")
        public void setBackgroundImage(String backgroundImage) {
            this.backgroundImage = backgroundImage;
        }

        public Double getRating() {
            return rating;
        }

        public void setRating(Double rating) {
            this.rating = rating;
        }

        public Integer getMetacritic() {
            return metacritic;
        }

        public void setMetacritic(Integer metacritic) {
            this.metacritic = metacritic;
        }

        public List<RawgStoreWrapperDto> getStores() {
            return stores;
        }

        public void setStores(List<RawgStoreWrapperDto> stores) {
            this.stores = stores;
        }

        public Map<String, Object> getAdditionalProperties() {
            return additionalProperties;
        }

        @JsonAnySetter
        public void putAdditionalProperty(String key, Object value) {
            this.additionalProperties.put(key, value);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawgStoreWrapperDto {
        private RawgStoreDto store;

        public RawgStoreDto getStore() {
            return store;
        }

        public void setStore(RawgStoreDto store) {
            this.store = store;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawgStoreDto {
        private Integer id;
        private String name;
        private String slug;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
        }
    }
}
