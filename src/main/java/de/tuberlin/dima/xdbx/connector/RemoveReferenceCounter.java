package de.tuberlin.dima.xdbx.connector;

import java.sql.SQLException;

public interface RemoveReferenceCounter<T> {
    void removeReference(T object) throws Exception;
}
