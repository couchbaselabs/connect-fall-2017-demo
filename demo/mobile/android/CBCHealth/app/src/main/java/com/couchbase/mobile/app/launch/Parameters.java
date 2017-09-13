package com.couchbase.mobile.app.launch;

import java.net.URI;
import java.net.URISyntaxException;

public enum Parameters {
    DEFAULT("db", "blip://localhost:4984/db"),
    EMULATOR("db", "blip://10.0.2.2:4984/db");

    private final String databaseName;
    private final URI remoteURI;

    Parameters(String databaseName, String remote) {
        this.databaseName = databaseName;

        try {
            this.remoteURI = new URI(remote);
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    public String getDatabaseName() { return databaseName; }
    public URI getRemoteURI() { return remoteURI; }
}
