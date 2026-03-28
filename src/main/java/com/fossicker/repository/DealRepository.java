package com.fossicker.repository;

import com.fossicker.entity.Deal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DealRepository extends JpaRepository<Deal, String> {

    Page<Deal> findByStatusOrderByPublishTimeDesc(Integer status, Pageable pageable);

    Page<Deal> findByCategoryNameAndStatusOrderByPublishTimeDesc(String categoryName, Integer status, Pageable pageable);

    List<Deal> findByPublisherIdOrderByPublishTimeDesc(String publisherId);

    Page<Deal> findByTitleContainingAndStatus(String title, Integer status, Pageable pageable);

    Page<Deal> findByContentContainingAndStatus(String content, Integer status, Pageable pageable);

    Page<Deal> findByTitleContainingOrContentContainingAndStatus(String title, String content, Integer status, Pageable pageable);

    List<Deal> findByStatusAndExpireTimeBefore(Integer status, Date expireTime);
}
