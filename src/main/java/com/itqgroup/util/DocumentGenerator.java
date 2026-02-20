package com.itqgroup.util;

import com.itqgroup.dto.DocumentRequestDto;
import com.itqgroup.exception.DocumentCanNotCreateException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentGenerator implements CommandLineRunner {

    @Value("${generator.n}")
    int n;

    @Value("${generator.api-url}")
    String apiUrl;

    @Value("${generator.default-author}")
    String defaultAuthor;

    @Value("${generator.title-prefix}")
    String titlePrefix;

    @Value("${generator.initiator}")
    String initiator;

    @Value("${generator.batch-size}")
    int batchSize;

    @Value("${generator.generate}")
    boolean isGenerate;

    @Override
    public void run(String... args) {
        if (isGenerate) {
            log.info("Create document count by {}", n);
            log.info("Initiator: {}", initiator);

            RestTemplate restTemplate = new RestTemplate();


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Initiator", initiator);

            long totalStartTime = System.currentTimeMillis();

            int totalBatches = (int) Math.ceil((double) n / batchSize);


            for (int batch = 0; batch < totalBatches; batch++) {
                int batchStart = batch * batchSize;
                int batchEnd = Math.min(batchStart + batchSize, n);
                long batchStartTime = System.currentTimeMillis();

                for (int i = batchStart; i < batchEnd; i++) {
                    try {
                        DocumentRequestDto doc = new DocumentRequestDto();
                        doc.setAuthor(defaultAuthor);
                        doc.setTitle(titlePrefix + " " + (i + 1));

                        HttpEntity<DocumentRequestDto> request = new HttpEntity<>(doc, headers);

                        ResponseEntity<String> response = restTemplate.exchange(
                                apiUrl,
                                HttpMethod.POST,
                                request,
                                String.class
                        );

                        if (response.getStatusCode() == HttpStatus.OK ||
                                response.getStatusCode() == HttpStatus.CREATED) {
                        }

                    } catch (Exception e) {
                        log.info("Can not create document {}", i + 1);
                        throw new DocumentCanNotCreateException(String.format(
                                "Document can not create throw %s", e.getMessage())
                        );
                    }
                }

                long batchDuration = System.currentTimeMillis() - batchStartTime;
                log.info("Batch {} success by {} ms",
                        batch + 1, batchDuration);
            }

            long totalDuration = System.currentTimeMillis() - totalStartTime;
            log.info("Total time: {} ms", totalDuration);
        } else log.info("Generator not run");
    }
}
