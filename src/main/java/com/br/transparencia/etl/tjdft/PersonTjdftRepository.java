package com.br.transparencia.etl.tjdft;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface PersonTjdftRepository extends JpaRepository<PersonTjdft, Integer> {
}
