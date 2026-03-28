package com.fossicker.repository;

import com.fossicker.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, String> {

    List<Feedback> findByUserIdOrderByCreateTimeDesc(String userId);

    Page<Feedback> findByStatusOrderByCreateTimeDesc(Integer status, Pageable pageable);

    Page<Feedback> findAllByOrderByCreateTimeDesc(Pageable pageable);

    Page<Feedback> findByTypeOrderByCreateTimeDesc(String type, Pageable pageable);
}
