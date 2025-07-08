// ChatMessageListResponse.java
package com.pickteam.dto.chat;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
public class ChatMessageListResponse {
    private List<ChatMessageResponse> messages;
    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private long totalElements;
    private boolean isFirst;
    private boolean isLast;

    public static ChatMessageListResponse from(Page<ChatMessageResponse> page) {
        ChatMessageListResponse response = new ChatMessageListResponse();
        response.setMessages(page.getContent());
        response.setPageNumber(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTotalPages(page.getTotalPages());
        response.setTotalElements(page.getTotalElements());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        return response;
    }
}
