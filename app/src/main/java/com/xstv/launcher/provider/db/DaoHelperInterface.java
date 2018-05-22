
package com.xstv.launcher.provider.db;

import java.util.List;

public interface DaoHelperInterface<T> {
    long insert(T insert);

    long insertOrReplace(T insert);

    boolean update(T update);

    boolean delete(Long id);

    T getById(Long id);

    List<T> getAll();

    boolean hasKey(Long id);

    long getTotalCount();

    boolean deleteAll();

}
