package com.itqgroup.service;

import com.itqgroup.dto.*;
import com.itqgroup.exception.DocumentNotFoundException;
import com.itqgroup.exception.InvalidStatusTransitionException;
import com.itqgroup.mapper.DocumentHistoryMapper;
import com.itqgroup.mapper.DocumentMapper;
import com.itqgroup.model.ApprovalRegistry;
import com.itqgroup.model.Document;
import com.itqgroup.model.DocumentHistory;
import com.itqgroup.repository.DocumentRepository;
import com.itqgroup.util.NumberGenerator;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DocumentService {

    NumberGenerator generatorNumber;
    DocumentRepository documentRepository;

    @Transactional
    public DocumentResponseDto createDocument(DocumentRequestDto request, String initiator) {
        Document document = DocumentMapper.toDocument(request);
        document.setDocumentNumber(generatorNumber.generateCreateDocumentNumber());
        DocumentHistory documentHistory = DocumentHistoryMapper.toDocumentHistory(document, initiator);

        document.addHistory(documentHistory);
        documentRepository.save(document);
        log.info("Create document for initiator: {}", initiator);
        return DocumentMapper.toResponseDto(document, true);

    }


    @Transactional(readOnly = true)
    public DocumentResponseDto getDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with id: " + id));
        log.info("Document was successful find by id: {}", id);
        return DocumentMapper.toResponseDto(document, true);
    }


    @Transactional(readOnly = true)
    public List<DocumentResponseDto> getDocumentsBatch(List<Long> ids) {
        List<Document> documents = documentRepository.findByIdIn(ids);

        Map<Long, Document> documentMap = new HashMap<>();
        documents.forEach(doc -> documentMap.put(doc.getId(), doc));

        List<DocumentResponseDto> result = new ArrayList<>();
        for (Long id : ids) {
            Document doc = documentMap.get(id);
            if (doc != null) {
                result.add(DocumentMapper.toResponseDto(doc, true));
            } else {
                log.info("Document with id {} not found", id);
            }
        }

        return result;
    }

    @Transactional
    public List<StatusChangeResultDto> submit(@Valid StatusChangeRequestDto statusChangeRequestDto) {
        log.info("Processing submit for {} documents", statusChangeRequestDto.getIds().size());

        return statusChangeRequestDto.getIds().stream()
                .map(documentId -> processSubmitDocument(documentId, statusChangeRequestDto))
                .collect(Collectors.toList());
    }

    private StatusChangeResultDto processSubmitDocument(Long documentId, StatusChangeRequestDto request) {
        StatusChangeResultDto result = new StatusChangeResultDto();
        result.setId(documentId);

        try {
            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new DocumentNotFoundException("Document not found"));

            if (document.getStatus() != DocumentStatus.DRAFT) {
                throw new InvalidStatusTransitionException(
                        "Cannot submit document in status: " + document.getStatus());
            }

            DocumentHistory history = new DocumentHistory();
            history.setInitiator(request.getInitiator());
            history.setAction(DocumentAction.SUBMIT);
            history.setComment("Document is submitted");

            document.addHistory(history);

            document.setStatus(DocumentStatus.SUBMITTED);
            documentRepository.save(document);

            result.setStatus(StatusChangeResultDto.StatusChangeStatus.SUCCESS);
            result.setMessage("Document submitted successfully");

            log.debug("Document {} submitted successfully", documentId);

        } catch (DocumentNotFoundException e) {
            result.setStatus(StatusChangeResultDto.StatusChangeStatus.NOT_FOUND);
            result.setMessage("Document not found");
        } catch (InvalidStatusTransitionException e) {
            result.setStatus(StatusChangeResultDto.StatusChangeStatus.CONFLICT);
            result.setMessage(e.getMessage());
        } catch (Exception e) {
            result.setStatus(StatusChangeResultDto.StatusChangeStatus.CONFLICT);
            result.setMessage("Error processing document: " + e.getMessage());
        }

        return result;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<StatusChangeResultDto> approve(@Valid StatusChangeRequestDto statusChangeRequestDto) {
        log.info("Processing approve for {} documents", statusChangeRequestDto.getIds().size());


        return statusChangeRequestDto.getIds().stream()
                .map(documentId -> processApproveDocument(documentId, statusChangeRequestDto))
                .collect(Collectors.toList());
    }


    private StatusChangeResultDto processApproveDocument(Long documentId, StatusChangeRequestDto request) {

        StatusChangeResultDto result = new StatusChangeResultDto();
        result.setId(documentId);

        try {
            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new DocumentNotFoundException("Document not found"));

            if (document.getStatus() != DocumentStatus.SUBMITTED) {
                throw new InvalidStatusTransitionException(
                        "Cannot approve document in status: " + document.getStatus());
            }

            ApprovalRegistry registry = new ApprovalRegistry();
            registry.setApprovedBy(request.getInitiator());
            registry.setApprovalNumber(generatorNumber.generateApprovalDocumentNumber());
            registry.setDocument(document);

            DocumentHistory history = new DocumentHistory();
            history.setInitiator(request.getInitiator());
            history.setAction(DocumentAction.APPROVE);
            history.setComment("Document is approve");

            document.setApprovalRegistry(registry);
            document.addHistory(history);
            document.setStatus(DocumentStatus.APPROVED);

            documentRepository.save(document);

            result.setStatus(StatusChangeResultDto.StatusChangeStatus.SUCCESS);
            result.setMessage("Document approved successfully");

            log.info("Document {} approved successfully", documentId);

        } catch (DocumentNotFoundException e) {
            result.setStatus(StatusChangeResultDto.StatusChangeStatus.NOT_FOUND);
            result.setMessage("Document not found");
        } catch (InvalidStatusTransitionException e) {
            result.setStatus(StatusChangeResultDto.StatusChangeStatus.CONFLICT);
            result.setMessage(e.getMessage());
        } catch (Exception e) {
            result.setStatus(StatusChangeResultDto.StatusChangeStatus.REGISTRY_ERROR);
            result.setMessage("Registry error: " + e.getMessage());
        }

        return result;
    }


    @Transactional(readOnly = true)
    public Page<DocumentResponseDto> searchDocuments(DocumentStatus status, String author, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable) {
        return documentRepository.searchDocuments(status, author, dateFrom, dateTo, pageable)
                .map(doc -> DocumentMapper.toResponseDto(doc, true));
    }


    @Transactional
    public ConcurrencyTestResponseDto testConcurrentApproval(@Valid ConcurrencyTestRequestDto request) {

        log.info("Starting concurrency test for document {} with {} threads and {} attempts",
                request.getDocumentId(), request.getThreads(), request.getAttempts());

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        AtomicInteger registryErrorCount = new AtomicInteger(0);
        AtomicInteger notFoundCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(request.getThreads());

        for (int i = 0; i < request.getAttempts(); i++) {
            executorService.submit(() -> {
                try {
                    StatusChangeRequestDto approveRequest = new StatusChangeRequestDto();
                    approveRequest.setIds(List.of(request.getDocumentId()));
                    approveRequest.setInitiator(request.getInitiator() + "-thread");

                    List<StatusChangeResultDto> results = approve(approveRequest);

                    if (!results.isEmpty()) {
                        StatusChangeResultDto result = results.get(0);
                        switch (result.getStatus()) {
                            case SUCCESS -> successCount.incrementAndGet();
                            case CONFLICT -> conflictCount.incrementAndGet();
                            case REGISTRY_ERROR -> registryErrorCount.incrementAndGet();
                            case NOT_FOUND -> notFoundCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    conflictCount.incrementAndGet();
                }
            });
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Document finalDocument = documentRepository.findById(request.getDocumentId()).orElse(null);

        ConcurrencyTestResponseDto response = new ConcurrencyTestResponseDto();
        response.setSuccessfulAttempts(successCount.get());
        response.setConflictAttempts(conflictCount.get());
        response.setRegistryErrorAttempts(registryErrorCount.get());
        response.setNotFoundAttempts(notFoundCount.get());
        response.setFinalStatus(finalDocument != null ? finalDocument.getStatus() : null);

        log.info("Concurrency test completed. Success: {}, Conflict: {}, Registry Error: {}, Final status: {}",
                successCount.get(), conflictCount.get(), registryErrorCount.get(), response.getFinalStatus());

        return response;
    }


}
