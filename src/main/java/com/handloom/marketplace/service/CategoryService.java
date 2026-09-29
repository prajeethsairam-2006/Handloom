package com.handloom.marketplace.service;

import com.handloom.marketplace.exception.ResourceNotFoundException;
import com.handloom.marketplace.model.Category;
import com.handloom.marketplace.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
    }

    public Long save(Category category) {
        return categoryRepository.save(category);
    }

    public void update(Category category) {
        categoryRepository.update(category);
    }

    public void deleteById(Long id) {
        categoryRepository.deleteById(id);
    }

    public int countAll() {
        return categoryRepository.countAll();
    }
}
