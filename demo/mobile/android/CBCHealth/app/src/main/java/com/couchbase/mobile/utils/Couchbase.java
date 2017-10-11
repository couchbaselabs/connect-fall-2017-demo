package com.couchbase.mobile.utils;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.Authenticator;
import com.couchbase.lite.BasicAuthenticator;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.mobile.app.launch.Runtime;

import java.net.URI;

public class Couchbase {
    private static final String TAG = Couchbase.class.getCanonicalName();

    public static Database openDatabase(Context ctx, String name) {
        Database database;
        DatabaseConfiguration config = new DatabaseConfiguration(ctx);

        try {
            database = new Database(name, config);
        } catch (CouchbaseLiteException ex) {
            Log.e(TAG, "Failed to create database instance: " + name + " - " + config);
            throw new RuntimeException(ex.getMessage());
        }

        return database;
    }

    public static Replicator createContinuousReplicator(Database db, URI remote) {
        ReplicatorConfiguration config = new ReplicatorConfiguration(db, remote);
        Authenticator auth = new BasicAuthenticator(Runtime.getPatientID(), "password");
        config.setAuthenticator(auth);
        config.setContinuous(true);

        return new Replicator(config);
    }
}
