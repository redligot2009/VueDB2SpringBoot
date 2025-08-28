package com.redligot.backend;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Photo} entities, providing CRUD operations.
 */
public interface PhotoRepository extends JpaRepository<Photo, Long> {
}


