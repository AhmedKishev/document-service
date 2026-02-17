package com.itqgroup.repository;

import com.itqgroup.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByIdIn(List<Long> ids);

}
