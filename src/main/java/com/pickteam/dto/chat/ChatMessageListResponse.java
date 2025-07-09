// ChatMessageListResponse.java
package com.pickteam.dto.chat;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageListResponse {
    private List<ChatMessageResponse> messages;
    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private long totalElements;
    private boolean isFirst;
    private boolean isLast;

    public static ChatMessageListResponse from(Page<ChatMessageResponse> page) {
        if (page == null) {
            throw new IllegalArgumentException("Page cannot be null");
        }

        return ChatMessageListResponse.builder()
                .messages(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }
}
