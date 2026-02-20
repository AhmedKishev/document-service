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
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApproveWorker {

    DocumentRepository documentRepository;

    DocumentService documentService;

    @NonFinal
    @Value("${worker.approve.batch-size}")
    int batchSize;


    @Scheduled(fixedDelayString = "${worker.approve.fixed-delay}")
    @Transactional
    public void processApprovedDocuments() {
        log.info("APPROVE-worker started. Looking for SUBMITTED documents, batch size: {}", batchSize);

        long startTime = System.currentTimeMillis();
        int totalProcessed = 0;
        int totalSuccess = 0;
        int totalErrors = 0;
        int totalRegistryErrors = 0;

        try {
            List<Document> submittedDocuments = documentRepository.findByStatusWithLock(
                    DocumentStatus.SUBMITTED.name(),
                    batchSize
            );

            if (submittedDocuments.isEmpty()) {
                log.info("No SUBMITTED documents found for processing");
                return;
            }

            List<Long> documentIds = submittedDocuments.stream()
                    .map(Document::getId)
                    .collect(Collectors.toList());

            log.info("Found {} SUBMITTED documents to approve", documentIds.size());

            StatusChangeRequestDto request = new StatusChangeRequestDto();
            request.setIds(documentIds);
            request.setInitiator("APPROVE-WORKER");

            List<StatusChangeResultDto> results = documentService.approveDocuments(request, submittedDocuments);

            for (StatusChangeResultDto result : results) {
                totalProcessed++;
                switch (result.getStatus()) {
                    case SUCCESS:
                        totalSuccess++;
                        break;
                    case REGISTRY_ERROR:
                        totalRegistryErrors++;
                        log.warn("Document {} registry error: {}", result.getId(), result.getMessage());
                        break;
                    default:
                        totalErrors++;
                        log.warn("Document {} failed: {}", result.getId(), result.getMessage());
                        break;
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("APPROVE-worker completed. Processed: {}, Success: {}, RegistryErrors: {}, OtherErrors: {}, Time: {} ms",
                    totalProcessed, totalSuccess, totalRegistryErrors, totalErrors, duration);

        } catch (Exception e) {
            log.error("APPROVE-worker encountered an error: {}", e.getMessage(), e);
        }

        long totalDuration = System.currentTimeMillis() - startTime;
        log.info("Total time success batch by: {} ms", totalDuration);
    }

}
