package com.shareptiles.app.controller;

import com.shareptiles.app.model.Species;
import com.shareptiles.app.model.SpeciesVariety;
import com.shareptiles.app.repository.SpeciesRepository;
import com.shareptiles.app.repository.SpeciesVarietyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/species-varieties")
public class SpeciesVarietyController {

    @Autowired
    private SpeciesVarietyRepository varietyRepository;

    @Autowired
    private SpeciesRepository speciesRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("varieties", varietyRepository.findAll());
        return "admin/species-varieties/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("variety", new SpeciesVariety());
        model.addAttribute("speciesList", speciesRepository.findAll());
        return "admin/species-varieties/create";
    }

    @PostMapping
    public String store(@RequestParam Long speciesId,
                        @RequestParam String name,
                        @RequestParam(defaultValue = "true") boolean status) {
        Species species = speciesRepository.findById(speciesId)
                .orElseThrow(() -> new IllegalArgumentException("Species not found: " + speciesId));
        varietyRepository.save(new SpeciesVariety(species, name, status));
        return "redirect:/admin/species-varieties";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        SpeciesVariety variety = varietyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Variety not found: " + id));
        model.addAttribute("variety", variety);
        model.addAttribute("speciesList", speciesRepository.findAll());
        return "admin/species-varieties/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam Long speciesId,
                         @RequestParam String name,
                         @RequestParam(defaultValue = "false") boolean status) {
        SpeciesVariety variety = varietyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Variety not found: " + id));
        Species species = speciesRepository.findById(speciesId)
                .orElseThrow(() -> new IllegalArgumentException("Species not found: " + speciesId));
        variety.setSpecies(species);
        variety.setName(name);
        variety.setStatus(status);
        varietyRepository.save(variety);
        return "redirect:/admin/species-varieties";
    }
}
