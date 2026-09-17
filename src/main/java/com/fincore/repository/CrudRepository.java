package com.fincore.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic Repository interface defining standard CRUD data access contracts.
 * Demonstrates OOP Generics and Interface Segregation.
 *
 * @param <T>  Domain entity type
 * @param <ID> Primary identifier type
 */
public interface CrudRepository<T, ID> {

    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    boolean update(T entity);

    boolean deleteById(ID id);
}
