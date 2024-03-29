package org.Server.RepoInterfaces;

import java.sql.SQLException;
import java.util.List;

public interface Repository<T,ID> {
    ID save(T entity) throws SQLException;

    T findById(ID id) throws SQLException;
    List<T> findAll() throws SQLException;
    void deleteById(ID id) throws SQLException;
}
