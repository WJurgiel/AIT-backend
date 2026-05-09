package com.ait.aitbackend.user.dto;

import java.time.LocalDateTime;

public record UserAboutMeResponse(
        String username,
        String email,
        LocalDateTime createdAt
) {}
