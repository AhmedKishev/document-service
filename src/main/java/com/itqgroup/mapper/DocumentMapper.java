package com.itqgroup.mapper;

import com.itqgroup.dto.DocumentHistoryDto;
import com.itqgroup.dto.DocumentRequestDto;
import com.itqgroup.dto.DocumentResponseDto;
import com.itqgroup.dto.DocumentStatus;
import com.itqgroup.model.Document;
import com.itqgroup.model.DocumentHistory;

import java.util.List;

public class DocumentMapper {

    public static Document toDocument(DocumentRequestDto request) {
        return Document.builder()
                .author(request.getAuthor())
                .title(request.getTitle())
                .status(DocumentStatus.DRAFT)
                .build();

    }

    public static DocumentResponseDto toResponseDto(Document document, boolean includeHistory) {
        DocumentResponseDto response = new DocumentResponseDto();
        if (includeHistory) {
            List<DocumentHistoryDto> historyDtos = document.getHistory().stream()
                    .map(DocumentMapper::toHistoryDto)
                    .toList();
            response.setHistory(historyDtos);
        }
        response.setAuthor(document.getAuthor());
        response.setTitle(document.getTitle());
        response.setStatus(document.getStatus());
        response.setCreatedAt(document.getCreatedAt());
        return response;
    }

    private static DocumentHistoryDto toHistoryDto(DocumentHistory documentHistory) {
        return DocumentHistoryDto.builder()
                .initiator(documentHistory.getInitiator())
                .actionTime(documentHistory.getActionTime())
                .action(documentHistory.getAction())
                .comment(documentHistory.getComment())
                .build();
    }



}
