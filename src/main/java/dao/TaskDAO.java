package dao;

import java.util.List;

/**
 * Generic interface for DAO operations
 * @param <T> The entity type
 */
public interface TaskDAO<T> {
    
    /**
     * Save an entity to database
     * @param entity Entity to save
     * @return The saved entity
     */
    T save(T entity);
    
    /**
     * Find an entity by its ID
     * @param id ID of the entity
     * @return The entity if found, null otherwise
     */
    T findById(Long id);
    
    /**
     * Find all entities
     * @return List of all entities
     */
    List<T> findAll();
    
    /**
     * Update an entity
     * @param entity Entity to update
     * @return Updated entity
     */
    T update(T entity);
    
    /**
     * Delete an entity by its ID
     * @param id ID of the entity to delete
     * @return true if deleted successfully, false otherwise
     */
    boolean delete(Long id);
}
