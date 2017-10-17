package com.couchbase.mobile.app.launch;

import java.net.URI;
import java.net.URISyntaxException;

public enum Parameters {
    DEFAULT("db", "blip://localhost:4984/db", "0f62b5cb-3735-45f6-8ee9-deaeee7f308a"),
    EMULATOR("db", "blip://10.0.2.2:4984/db", "0f62b5cb-3735-45f6-8ee9-deaeee7f308a"),
    CLUSTER("db", "blip://ec2-52-11-217-155.us-west-2.compute.amazonaws.com:4984/db", "0f62b5cb-3735-45f6-8ee9-deaeee7f308a");

    private final String databaseName;
    private final URI remoteURI;
    private final String patientID;

    Parameters(String databaseName, String remote, String patientID) {
        this.databaseName = databaseName;
        this.patientID = patientID;

        try {
            this.remoteURI = new URI(remote);
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    public String getDatabaseName() { return databaseName; }
    public URI getRemoteURI() { return remoteURI; }
    public String getPatientID() { return patientID; }
}
