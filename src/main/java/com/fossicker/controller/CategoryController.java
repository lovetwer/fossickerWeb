package com.fossicker.controller;

import com.fossicker.common.Result;
import com.fossicker.entity.Category;
import com.fossicker.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public Result<List<Category>> getAllCategories() {
        List<Category> categories = categoryRepository.findAllByOrderBySortOrderAsc();
        return Result.success(categories);
    }

    @GetMapping("/{id}")
    public Result<Category> getCategoryById(@PathVariable String id) {
        return categoryRepository.findById(id)
                .map(Result::success)
                .orElseGet(() -> Result.error("Category not found"));
    }

    @PostMapping
    public Result<Category> createCategory(@RequestBody Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            return Result.error(409, "分类名称已存在");
        }
        Category savedCategory = categoryRepository.save(category);
        return Result.success("创建成功", savedCategory);
    }

    @PutMapping("/{id}")
    public Result<Category> updateCategory(@PathVariable String id, @RequestBody Category category) {
        return categoryRepository.findById(id)
                .map(existingCategory -> {
                    existingCategory.setName(category.getName());
                    existingCategory.setIcon(category.getIcon());
                    existingCategory.setSortOrder(category.getSortOrder());
                    Category updatedCategory = categoryRepository.save(existingCategory);
                    return Result.success("更新成功", updatedCategory);
                })
                .orElseGet(() -> Result.error("Category not found"));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable String id) {
        if (!categoryRepository.existsById(id)) {
            return Result.error("Category not found");
        }
        categoryRepository.deleteById(id);
        return Result.success("删除成功", null);
    }
}
