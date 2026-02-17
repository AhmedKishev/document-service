package com.itqgroup.service;

import com.itqgroup.dto.DocumentRequestDto;
import com.itqgroup.dto.DocumentResponseDto;
import com.itqgroup.exception.DocumentNotFoundException;
import com.itqgroup.mapper.DocumentHistoryMapper;
import com.itqgroup.mapper.DocumentMapper;
import com.itqgroup.model.Document;
import com.itqgroup.model.DocumentHistory;
import com.itqgroup.repository.DocumentRepository;
import com.itqgroup.util.Generator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DocumentService {

    Generator generatorNumber;
    DocumentRepository documentRepository;

    @Transactional
    public DocumentResponseDto createDocument(DocumentRequestDto request, String initiator) {
        Document document = DocumentMapper.toDocument(request);
        document.setDocumentNumber(generatorNumber.generateDocumentNumber());
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
                result.add(DocumentMapper.toResponseDto(doc, false));
            }
        }

        return result;
    }
}
