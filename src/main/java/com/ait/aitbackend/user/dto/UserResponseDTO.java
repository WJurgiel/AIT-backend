package com.ait.aitbackend.user.dto;

public record UserResponseDTO(
    Long id,
    String username,
    String email
) {}
