package com.couchbase.mobile.database;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseOptions;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.auth.Authenticator;
import com.couchbase.lite.auth.AuthenticatorFactory;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;
import com.couchbase.mobile.app.launch.Runtime;

import java.io.IOException;
import java.net.MalformedURLException;

public class CBLite implements Replication.ChangeListener{
    private static final String TAG = CBLite.class.getCanonicalName();

    private Manager manager = null;
    private Database database = null;
    private Replication pull = null;
    private Replication push = null;

    private CBLite() {
        try {
            manager = new Manager(new AndroidContext(Runtime.getApplicationContext()), Manager.DEFAULT_OPTIONS);
        } catch (IOException e) {
            Log.e(TAG, "Failed to initalize Manager", e);
        }
        try {
            DatabaseOptions options = new DatabaseOptions();
            options.setCreate(true);
            database = manager.openDatabase(Runtime.getDatabaseName(), options);
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Failed to open Database", e);
        }
    }

    @Override
    public void changed(Replication.ChangeEvent event) {
        Log.e(TAG, "changed() event -> " + event.toString());
    }

    private static class Holder {
        private static CBLite INSTANCE = new CBLite();
    }

    public static CBLite getInstance() {
        return Holder.INSTANCE;
    }

    public Database getDatabase() {
        return database;
    }

    public void startReplication() {
        try {
            Authenticator auth = AuthenticatorFactory.createBasicAuthenticator(Runtime.getPatientID(), "password");
            if (pull == null) {
                pull = database.createPullReplication(Runtime.getRemoteURI().toURL());
                pull.setAuthenticator(auth);
                pull.setContinuous(true);
                pull.addChangeListener(this);
                pull.start();
            }
            if (push == null) {
                push = database.createPushReplication(Runtime.getRemoteURI().toURL());
                push.setAuthenticator(auth);
                push.setContinuous(true);
                push.addChangeListener(this);
                push.start();
            }
        }catch (MalformedURLException e){
            Log.e(TAG, "Unable to start push/pull replicators: ", e);
        }
    }

    public void stopReplication() {
        if (pull != null) {
            pull.stop();
            pull.removeChangeListener(this);
            pull = null;
        }
        if (push != null) {
            push.stop();
            push.removeChangeListener(this);
            push = null;
        }
    }
}
