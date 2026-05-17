package com.ait.aitbackend.user.dto;

import jakarta.validation.constraints.NotNull;

public record AddWatchedGameRequest(
        @NotNull(message = "rawgId is required")
        Integer rawgId
) {}

