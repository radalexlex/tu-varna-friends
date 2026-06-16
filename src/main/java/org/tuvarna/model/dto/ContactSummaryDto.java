package org.tuvarna.model.dto;

public record ContactSummaryDto (
            Long userId,
            String keyToImage,
            String fullName
) {}
