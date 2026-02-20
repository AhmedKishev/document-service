package com.itqgroup;

import com.itqgroup.dto.*;
import com.itqgroup.model.Document;
import com.itqgroup.repository.ApprovalRegistryRepository;
import com.itqgroup.repository.DocumentHistoryRepository;
import com.itqgroup.repository.DocumentRepository;
import com.itqgroup.service.DocumentService;
import com.itqgroup.util.NumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private ApprovalRegistryRepository approvalRegistryRepository;

    @Mock
    private DocumentHistoryRepository historyRepository;

    @Mock
    private NumberGenerator numberGenerator;

    @InjectMocks
    private DocumentService documentService;

    private Document draftDocument;
    private Document submittedDocument;
    private Document approvedDocument;

    @BeforeEach
    void setUp() {
        draftDocument = new Document();
        draftDocument.setId(1L);
        draftDocument.setStatus(DocumentStatus.DRAFT);
        draftDocument.setAuthor("Test Author");
        draftDocument.setTitle("Test Title");

        submittedDocument = new Document();
        submittedDocument.setId(2L);
        submittedDocument.setStatus(DocumentStatus.SUBMITTED);

        approvedDocument = new Document();
        approvedDocument.setId(3L);
        approvedDocument.setStatus(DocumentStatus.APPROVED);
    }

    @Test
    void shouldSuccessfullyCreateSubmitAndApproveSingleDocument() {
        DocumentRequestDto createRequest = new DocumentRequestDto();
        createRequest.setAuthor("Test Author");
        createRequest.setTitle("Test Title");
        String initiator = "test-user";

        when(numberGenerator.generateCreateDocumentNumber()).thenReturn("DOC-TEST-123");
        when(documentRepository.save(any(Document.class))).thenAnswer(i -> i.getArgument(0));

        DocumentResponseDto createdDoc = documentService.createDocument(createRequest, initiator);

        assertThat(createdDoc).isNotNull();
        assertThat(createdDoc.getStatus()).isEqualTo(DocumentStatus.DRAFT);
        verify(documentRepository, times(1)).save(any(Document.class));

        StatusChangeRequestDto submitRequest = new StatusChangeRequestDto();
        submitRequest.setIds(List.of(1L));
        submitRequest.setInitiator(initiator);


        when(documentRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftDocument));

        List<StatusChangeResultDto> submitResults = documentService.submit(submitRequest);

        assertThat(submitResults).hasSize(1);
        assertThat(submitResults.get(0).getStatus()).isEqualTo(StatusChangeResultDto.StatusChangeStatus.SUCCESS);
        assertThat(draftDocument.getStatus()).isEqualTo(DocumentStatus.SUBMITTED);
        verify(documentRepository, times(2)).save(any(Document.class));

        StatusChangeRequestDto approveRequest = new StatusChangeRequestDto();
        approveRequest.setIds(List.of(1L));
        approveRequest.setInitiator(initiator);

        when(numberGenerator.generateApprovalDocumentNumber()).thenReturn("APR-TEST-123");
        when(documentRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftDocument));

        List<StatusChangeResultDto> approveResults = documentService.approve(approveRequest);

        assertThat(approveResults).hasSize(1);
        assertThat(approveResults.get(0).getStatus()).isEqualTo(StatusChangeResultDto.StatusChangeStatus.SUCCESS);
        assertThat(draftDocument.getStatus()).isEqualTo(DocumentStatus.APPROVED);
        assertThat(draftDocument.getApprovalRegistry()).isNotNull();
        verify(documentRepository, times(3)).save(any(Document.class));
    }

    @Test
    void shouldProcessBatchSubmitWithPartialResults() {
        StatusChangeRequestDto request = new StatusChangeRequestDto();
        request.setIds(List.of(1L, 2L, 3L, 999L));
        request.setInitiator("test-user");


        when(documentRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftDocument));
        when(documentRepository.findByIdWithDetails(2L)).thenReturn(Optional.of(submittedDocument));
        when(documentRepository.findByIdWithDetails(3L)).thenReturn(Optional.of(approvedDocument));
        when(documentRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        List<StatusChangeResultDto> results = documentService.submit(request);

        assertThat(results).hasSize(4);

        assertThat(results.get(0).getId()).isEqualTo(1L);
        assertThat(results.get(0).getStatus()).isEqualTo(StatusChangeResultDto.StatusChangeStatus.SUCCESS);

        assertThat(results.get(1).getId()).isEqualTo(2L);
        assertThat(results.get(1).getStatus()).isEqualTo(StatusChangeResultDto.StatusChangeStatus.CONFLICT);

        assertThat(results.get(2).getId()).isEqualTo(3L);
        assertThat(results.get(2).getStatus()).isEqualTo(StatusChangeResultDto.StatusChangeStatus.CONFLICT);

        assertThat(results.get(3).getId()).isEqualTo(999L);
        assertThat(results.get(3).getStatus()).isEqualTo(StatusChangeResultDto.StatusChangeStatus.NOT_FOUND);

        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void shouldProcessBatchApproveWithPartialResults() {
        StatusChangeRequestDto request = new StatusChangeRequestDto();
        request.setIds(List.of(2L, 1L, 3L, 999L));
        request.setInitiator("test-user");

        when(numberGenerator.generateApprovalDocumentNumber()).thenReturn("APR-TEST-123");


        when(documentRepository.findByIdWithDetails(2L)).thenReturn(Optional.of(submittedDocument));
        when(documentRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftDocument));
        when(documentRepository.findByIdWithDetails(3L)).thenReturn(Optional.of(approvedDocument));
        when(documentRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        List<StatusChangeResultDto> results = documentService.approve(request);

        assertThat(results).hasSize(4);

        assertThat(results.get(0).getId()).isEqualTo(2L);
        assertThat(results.get(0).getStatus()).isEqualTo(StatusChangeResultDto.StatusChangeStatus.SUCCESS);

        assertThat(results.get(1).getId()).isEqualTo(1L);
        assertThat(results.get(1).getStatus()).isEqualTo(StatusChangeResultDto.StatusChangeStatus.CONFLICT);

        assertThat(results.get(2).getId()).isEqualTo(3L);
        assertThat(results.get(2).getStatus()).isEqualTo(StatusChangeResultDto.StatusChangeStatus.CONFLICT);

        assertThat(results.get(3).getId()).isEqualTo(999L);
        assertThat(results.get(3).getStatus()).isEqualTo(StatusChangeResultDto.StatusChangeStatus.NOT_FOUND);

        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void shouldRollbackApproveWhenRegistrySaveFails() {
        StatusChangeRequestDto request = new StatusChangeRequestDto();
        request.setIds(List.of(2L));
        request.setInitiator("test-user");

        when(documentRepository.findByIdWithDetails(2L)).thenReturn(Optional.of(submittedDocument));
        when(numberGenerator.generateApprovalDocumentNumber()).thenReturn("APR-TEST-123");

        doThrow(new DataAccessException("DB error") {})
                .when(documentRepository).save(any(Document.class));

        List<StatusChangeResultDto> results = documentService.approve(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(2L);
        assertThat(results.get(0).getStatus()).isEqualTo(StatusChangeResultDto.StatusChangeStatus.REGISTRY_ERROR);

        verify(documentRepository, times(1)).save(any(Document.class));
    }
}