package com.shareptiles.app.controller;

import com.shareptiles.app.model.FoodType;
import com.shareptiles.app.repository.FoodTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/food-types")
public class FoodTypeController {

    @Autowired
    private FoodTypeRepository foodTypeRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("foodTypes", foodTypeRepository.findAll());
        return "admin/food-types/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("foodType", new FoodType());
        model.addAttribute("categories", foodTypeRepository.findByParentIsNull());
        return "admin/food-types/create";
    }

    @PostMapping
    public String store(@RequestParam String name,
                        @RequestParam(required = false) Long parentId,
                        @RequestParam(required = false) String defaultUnit,
                        @RequestParam(required = false) String description,
                        @RequestParam(defaultValue = "true") boolean status) {

        FoodType parent = parentId != null ? foodTypeRepository.findById(parentId).orElse(null) : null;
        FoodType foodType = new FoodType(name, parent, defaultUnit != null && !defaultUnit.isBlank() ? defaultUnit : "piece", status);
        foodType.setDescription(description);
        foodTypeRepository.save(foodType);

        return "redirect:/admin/food-types";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        FoodType foodType = foodTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Food type not found: " + id));
        model.addAttribute("foodType", foodType);
        model.addAttribute("categories", foodTypeRepository.findByParentIsNull());
        return "admin/food-types/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam String name,
                         @RequestParam(required = false) Long parentId,
                         @RequestParam(required = false) String defaultUnit,
                         @RequestParam(required = false) String description,
                         @RequestParam(defaultValue = "false") boolean status) {

        FoodType foodType = foodTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Food type not found: " + id));

        foodType.setName(name);
        foodType.setParent(parentId != null ? foodTypeRepository.findById(parentId).orElse(null) : null);
        foodType.setDefaultUnit(defaultUnit);
        foodType.setDescription(description);
        foodType.setStatus(status);

        foodTypeRepository.save(foodType);
        return "redirect:/admin/food-types";
    }
}
