package com.reviora.repository;

import com.reviora.model.RefreshToken;
import com.reviora.model.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    Page<Submission> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<Submission> findTop10ByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.user.id = :userId AND s.status = 'COMPLETED'")
    long countCompletedByUserId(@Param("userId") UUID userId);
}
