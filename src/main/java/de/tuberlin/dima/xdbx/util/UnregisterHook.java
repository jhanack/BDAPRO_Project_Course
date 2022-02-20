package de.tuberlin.dima.xdbx.util;

import de.tuberlin.dima.xdbx.connector.DBConnectionDetails;
import de.tuberlin.dima.xdbx.connector.RemoveReferenceCounter;

public class UnregisterHook<T, R extends RemoveReferenceCounter<T>> implements ThrowingConsumer<DBConnectionDetails, Exception> {
    private T lazyDBConnector;
    private final R owner;

    public UnregisterHook(R owner) {
        this.owner = owner;
    }

    @Override
    public void accept(DBConnectionDetails dbConnectionDetails) throws Exception {
        owner.removeReference(lazyDBConnector);
    }

    public void setLazyMariaDBConnector(T lazyDBConnector) {
        this.lazyDBConnector = lazyDBConnector;
    }
}
