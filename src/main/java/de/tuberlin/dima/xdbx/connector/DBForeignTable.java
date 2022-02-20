package de.tuberlin.dima.xdbx.connector;

public interface DBForeignTable extends DBObject{
    DBConnectionDetails getForeignDBConnection();
}
