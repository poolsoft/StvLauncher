
package com.xstv.desktop.app.db;

import java.util.List;

public interface DaoHelperInterface<T> {
    public long insert(T insert);

    public long insertOrReplace(T insert);

    public boolean update(T update);

    public boolean delete(Long id);

    public T getById(Long id);

    public List<T> getAll();

    public boolean hasKey(Long id);

    public long getTotalCount();

    public boolean deleteAll();

}
