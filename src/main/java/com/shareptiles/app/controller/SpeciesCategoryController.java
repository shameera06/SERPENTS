package com.shareptiles.app.controller;

import com.shareptiles.app.model.SpeciesCategory;
import com.shareptiles.app.repository.SpeciesCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/species-categories")
public class SpeciesCategoryController {

    @Autowired
    private SpeciesCategoryRepository categoryRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("categories", categoryRepository.findAllByOrderByStatusDescNameAsc());
        return "admin/species-categories/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("category", new SpeciesCategory("", true));
        return "admin/species-categories/create";
    }

    @PostMapping
    public String store(@RequestParam String name,
                        @RequestParam(defaultValue = "true") boolean status) {
        categoryRepository.save(new SpeciesCategory(name, status));
        return "redirect:/admin/species-categories";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        SpeciesCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
        model.addAttribute("category", category);
        return "admin/species-categories/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam String name,
                         @RequestParam(defaultValue = "false") boolean status) {
        SpeciesCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
        category.setName(name);
        category.setStatus(status);
        categoryRepository.save(category);
        return "redirect:/admin/species-categories";
    }
}
