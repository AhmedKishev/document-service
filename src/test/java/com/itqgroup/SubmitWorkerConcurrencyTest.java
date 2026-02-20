package com.itqgroup;

import com.itqgroup.dto.DocumentStatus;
import com.itqgroup.model.Document;
import com.itqgroup.repository.DocumentRepository;
import com.itqgroup.worker.SubmitWorker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@SpringBootTest
@Testcontainers
class SubmitWorkerConcurrencyTest {

    @Autowired
    private SubmitWorker worker;

    @Autowired
    private DocumentRepository repository;


    @BeforeEach
    void clearBefore() {
        repository.deleteAll();
    }

    @AfterEach
    void clearAfter() {
        repository.deleteAll();
    }

    @Test
    void shouldNotProcessSameDocumentsTwice() throws Exception {
        for (int i = 0; i < 20; i++) {
            Document d = new Document();
            d.setDocumentNumber("TEST-" + i);
            d.setAuthor("tester");
            d.setTitle("Test Doc " + i);
            d.setStatus(DocumentStatus.DRAFT);
            repository.save(d);
        }

        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(() -> worker.processSubmitDocuments());
        executor.submit(() -> worker.processSubmitDocuments());

        executor.shutdown();
        boolean finished = executor.awaitTermination(10, TimeUnit.SECONDS);

        if (!finished) {
            fail("Workers didn't finish in time");
        }

        List<Document> all = repository.findAllWithHistory();

        long draftCount = all.stream()
                .filter(d -> d.getStatus() == DocumentStatus.DRAFT)
                .count();

        long submittedCount = all.stream()
                .filter(d -> d.getStatus() == DocumentStatus.SUBMITTED)
                .count();


        assertEquals(20, submittedCount);
        assertEquals(0, draftCount);
        all.stream().forEach(obj -> assertEquals(1, obj.getHistory().size()));
    }
}