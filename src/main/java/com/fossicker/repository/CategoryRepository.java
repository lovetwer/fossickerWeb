package com.fossicker.repository;

import com.fossicker.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    
    List<Category> findAllByOrderBySortOrderAsc();
    
    boolean existsByName(String name);
}
