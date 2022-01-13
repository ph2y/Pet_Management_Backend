package com.sju18.petmanagement.domain.pet.pet.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findAllByOwnername(String username);
    Optional<Pet> findByOwnernameAndId(String username, Long id);
    Optional<Pet> findById(Long Id);
}
