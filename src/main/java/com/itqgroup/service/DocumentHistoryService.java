package com.itqgroup.service;

import com.itqgroup.mapper.DocumentHistoryMapper;
import com.itqgroup.model.Document;
import com.itqgroup.model.DocumentHistory;
import com.itqgroup.repository.DocumentHistoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DocumentHistoryService {

    DocumentHistoryRepository documentHistoryRepository;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createHistory(Document document, String initiator) {
        DocumentHistory documentHistory = DocumentHistoryMapper.toDocumentHistory(document, initiator);
        documentHistoryRepository.save(documentHistory);
        log.info("Create document for approval by initiator: {}", initiator);
    }
}
