package com.lumina.service;

import com.lumina.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> listAll();
    Category getById(Long id);
}
