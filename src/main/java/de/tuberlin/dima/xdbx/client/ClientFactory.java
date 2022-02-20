package de.tuberlin.dima.xdbx.client;

import de.tuberlin.dima.xdbx.node.XDBConnectionDetails;

public interface ClientFactory {
    Client create(XDBConnectionDetails xdbConnectionDetails);
}
