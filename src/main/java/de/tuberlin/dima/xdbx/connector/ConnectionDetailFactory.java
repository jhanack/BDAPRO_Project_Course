package de.tuberlin.dima.xdbx.connector;


public interface ConnectionDetailFactory<T> {
    DBConnectionDetails createConnectionDetails(T grpcDetails);
    T convertConnectionDetails(DBConnectionDetails dbConnectionDetails);

    DBConnectionDetails changeHostConnectionDetails(DBConnectionDetails oldConnectionDetails, String newAddress, int newPort);
}
