package com.itqgroup.controller;

import com.itqgroup.dto.*;
import com.itqgroup.service.DocumentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
        log.info("Request for getting batch documents");
        return documentService.getDocumentsBatch(ids);
    }


    @PostMapping("/submit")
    public List<StatusChangeResultDto> submitDocuments(@RequestBody @Valid StatusChangeRequestDto statusChangeRequestDto) {
        log.info("Request for submit documents");
        return documentService.submit(statusChangeRequestDto);
    }

    @PostMapping("/approve")
    public List<StatusChangeResultDto> approveDocuments(@RequestBody @Valid StatusChangeRequestDto statusChangeRequestDto) {
        log.info("Request for approve documents");
        return documentService.approve(statusChangeRequestDto);
    }


    @GetMapping("/search")
    public Page<DocumentResponseDto> searchDocuments(
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(required = false) String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        log.info("Request for search documents throw next params: status {}, author {}, page {}, size {}, dateFrom {}, dateTo {}",
                status, author, page, size, dateFrom, dateTo);
        Sort sortOrder = Sort.by(sort[0].split(",")[0]);
        if (sort[0].split(",").length > 1 && sort[0].split(",")[1].equalsIgnoreCase("desc")) {
            sortOrder = sortOrder.descending();
        } else {
            sortOrder = sortOrder.ascending();
        }

        Pageable pageable = PageRequest.of(page, size, sortOrder);
        return documentService.searchDocuments(status, author, dateFrom, dateTo, pageable);
    }


    @PostMapping("/test/concurrent-approval")
    public  ConcurrencyTestResponseDto testConcurrentApproval(
            @Valid @RequestBody ConcurrencyTestRequestDto request)     {
        return documentService.testConcurrentApproval(request);
    }


}
