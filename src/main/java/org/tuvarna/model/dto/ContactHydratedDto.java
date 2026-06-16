package org.tuvarna.model.dto;

public record ContactHydratedDto(
        Long userId,
        String keyToImage,
        String fullName,
        boolean isFriend,
        int commonFriendCount) {
}
