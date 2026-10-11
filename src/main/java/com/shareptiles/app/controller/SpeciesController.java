package com.shareptiles.app.controller;

import com.shareptiles.app.model.*;
import com.shareptiles.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/species")
public class SpeciesController {

    @Autowired
    private SpeciesRepository speciesRepository;

    @Autowired
    private SpeciesCategoryRepository categoryRepository;

    @Autowired
    private SpeciesVarietyRepository varietyRepository;

    @Autowired
    private ReptileRepository reptileRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("speciesList", speciesRepository.findAll());
        return "admin/species/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("species", new Species());
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/species/create";
    }

    @PostMapping
    public String store(@RequestParam String name,
                        @RequestParam(required = false) String scientificName,
                        @RequestParam(required = false) String description,
                        @RequestParam(required = false) Long categoryId,
                        @RequestParam(defaultValue = "true") boolean status) {
        Species species = new Species();
        species.setName(name);
        species.setScientificName(scientificName);
        species.setDescription(description);
        species.setStatus(status);
        if (categoryId != null) {
            categoryRepository.findById(categoryId).ifPresent(species::setCategory);
        }
        speciesRepository.save(species);
        return "redirect:/admin/species";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Species species = speciesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Species not found: " + id));
        model.addAttribute("species", species);
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/species/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam String name,
                         @RequestParam(required = false) String scientificName,
                         @RequestParam(required = false) String description,
                         @RequestParam(required = false) Long categoryId,
                         @RequestParam(defaultValue = "false") boolean status) {
        Species species = speciesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Species not found: " + id));
        species.setName(name);
        species.setScientificName(scientificName);
        species.setDescription(description);
        species.setStatus(status);
        if (categoryId != null) {
            categoryRepository.findById(categoryId).ifPresent(species::setCategory);
        } else {
            species.setCategory(null);
        }
        speciesRepository.save(species);
        return "redirect:/admin/species";
    }

    // --- Browse Hierarchy ---
    @GetMapping("/browse")
    public String browse(Model model) {
        List<SpeciesCategory> categories = categoryRepository.findAllByOrderByStatusDescNameAsc();
        List<Species> speciesList = speciesRepository.findAll();
        List<SpeciesVariety> varieties = varietyRepository.findAll();
        long activeReptileCount = reptileRepository.countByIsArchivedFalse();

        model.addAttribute("categories", categories);
        model.addAttribute("speciesList", speciesList);
        model.addAttribute("varieties", varieties);
        model.addAttribute("speciesCount", speciesList.size());
        model.addAttribute("varietyCount", varieties.size());
        model.addAttribute("categoryCount", categories.size());
        model.addAttribute("activeReptileCount", activeReptileCount);
        return "admin/species/browse";
    }

    @GetMapping("/directory")
    public String speciesDirectory(Model model) {
        SpeciesCategory snakeCategory = categoryRepository.findByName("Snake")
                .orElseThrow(() -> new IllegalStateException("Snake category has not been initialized."));
        model.addAttribute("category", snakeCategory);
        model.addAttribute("speciesList", speciesRepository.findByCategoryId(snakeCategory.getId()));
        return "admin/species/browse-category";
    }

    @GetMapping("/browse/{categoryId}")
    public String browseCategory(@PathVariable Long categoryId, Model model) {
        SpeciesCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
        model.addAttribute("category", category);
        model.addAttribute("speciesList", speciesRepository.findByCategoryId(categoryId));
        return "admin/species/browse-category";
    }

    @GetMapping("/browse/type/{speciesId}")
    public String browseType(@PathVariable Long speciesId, Model model) {
        Species species = speciesRepository.findById(speciesId)
                .orElseThrow(() -> new IllegalArgumentException("Species not found: " + speciesId));
        model.addAttribute("species", species);
        model.addAttribute("varieties", varietyRepository.findBySpeciesId(speciesId));
        return "admin/species/browse-type";
    }

    @GetMapping("/browse/type/{speciesId}/{varietyId}")
    public String browseVariety(@PathVariable Long speciesId, @PathVariable Long varietyId, Model model) {
        Species species = speciesRepository.findById(speciesId)
                .orElseThrow(() -> new IllegalArgumentException("Species not found: " + speciesId));
        SpeciesVariety variety = varietyRepository.findById(varietyId)
                .orElseThrow(() -> new IllegalArgumentException("Variety not found: " + varietyId));
        List<Reptile> reptiles = reptileRepository.findBySpeciesVarietyIdAndIsArchivedFalse(varietyId);

        model.addAttribute("species", species);
        model.addAttribute("variety", variety);
        model.addAttribute("reptiles", reptiles);
        return "admin/species/browse-variety";
    }
}
