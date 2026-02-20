package com.itqgroup.worker;


import com.itqgroup.dto.DocumentStatus;
import com.itqgroup.dto.StatusChangeRequestDto;
import com.itqgroup.dto.StatusChangeResultDto;
import com.itqgroup.model.Document;
import com.itqgroup.repository.DocumentRepository;
import com.itqgroup.service.DocumentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class SubmitWorker {

    DocumentRepository documentRepository;

    DocumentService documentService;

    @NonFinal
    @Value("${worker.submit.batch-size}")
    int batchSize;

    @Scheduled(fixedDelayString = "${worker.submit.fixed-delay}")
    @Transactional
    public void processSubmitDocuments() {
        log.info("SUBMIT-worker started. Looking for DRAFT documents, batch size: {}", batchSize);

        long startTime = System.currentTimeMillis();
        int totalProcessed = 0;
        int totalSuccess = 0;
        int totalErrors = 0;

        try {
            List<Document> draftDocuments = documentRepository.findByStatusWithLock(
                    DocumentStatus.DRAFT.name(),
                    batchSize
            );

            if (draftDocuments.isEmpty()) {
                log.info("No DRAFT documents found for processing");
                return;
            }

            List<Long> documentIds = draftDocuments.stream()
                    .map(Document::getId)
                    .collect(Collectors.toList());

            log.info("Found {} DRAFT documents to submit", documentIds.size());

            StatusChangeRequestDto request = new StatusChangeRequestDto();
            request.setIds(documentIds);
            request.setInitiator("SUBMIT-WORKER");

            List<StatusChangeResultDto> results = documentService.submitDocuments(request, draftDocuments);

            for (StatusChangeResultDto result : results) {
                totalProcessed++;
                switch (result.getStatus()) {
                    case SUCCESS:
                        totalSuccess++;
                        break;
                    default:
                        totalErrors++;
                        log.warn("Document {} failed: {}", result.getId(), result.getMessage());
                        break;
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("SUBMIT-worker completed. Processed: {}, Success: {}, Errors: {}, Time: {} ms",
                    totalProcessed, totalSuccess, totalErrors, duration);

        } catch (Exception e) {
            log.error("SUBMIT-worker encountered an error: {}", e.getMessage(), e);
        }

    }


}
