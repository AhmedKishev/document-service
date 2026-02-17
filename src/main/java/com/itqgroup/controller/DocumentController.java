package com.itqgroup.controller;

import com.itqgroup.dto.DocumentRequestDto;
import com.itqgroup.dto.DocumentResponseDto;
import com.itqgroup.service.DocumentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/api/documents")
@Validated
public class DocumentController {
    DocumentService documentService;


    @PostMapping
    public DocumentResponseDto createDocument(@RequestBody @Valid DocumentRequestDto documentRequestDto,
                                              @RequestHeader("X-Initiator") String initiator) {
        log.info("Request for create document by initiator: {}", initiator);
        return documentService.createDocument(documentRequestDto, initiator);
    }


    @GetMapping("/{id}")
    public DocumentResponseDto getDocument(@PathVariable Long id) {
        log.info("Request for receiving document by id: {}", id);
        return documentService.getDocument(id);
    }

    @PostMapping("/batch")
    public List<DocumentResponseDto> getDocumentsBatch(@RequestBody List<Long> ids) {
        return documentService.getDocumentsBatch(ids);
    }


}
