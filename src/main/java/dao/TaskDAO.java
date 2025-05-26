package dao;

import java.util.List;

public interface TaskDAO<T> {
    T save(T entity);
    T findById(Long id);
    List<T> findAll();
    T update(T entity);
    boolean delete(Long id);
}
