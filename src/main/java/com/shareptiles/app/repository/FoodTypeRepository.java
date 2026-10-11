package com.shareptiles.app.repository;

import com.shareptiles.app.model.FoodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodTypeRepository extends JpaRepository<FoodType, Long> {
    List<FoodType> findByParentIsNull();
    List<FoodType> findByParentId(Long parentId);
    List<FoodType> findByStatusTrue();
    Optional<FoodType> findByNameAndParentIsNull(String name);
    Optional<FoodType> findByNameAndParentId(String name, Long parentId);
}
