package com.itqgroup.repository;

import com.itqgroup.model.DocumentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentHistoryRepository extends JpaRepository<DocumentHistory, Long> {
}
