package com.couchbase.mobile.database;

import com.couchbase.lite.Database;
import com.couchbase.lite.Replicator;
import com.couchbase.mobile.app.launch.Runtime;
import com.couchbase.mobile.utils.Couchbase;

public class CBLite {
    private static final String TAG = CBLite.class.getCanonicalName();

    private Database database = null;
    private Replicator replicator = null;

    private CBLite() {
        database = Couchbase.openDatabase(Runtime.getApplicationContext(), Runtime.getDatabaseName());
    }

    private static class Holder {
        private static CBLite INSTANCE = new CBLite();
    }

    public static CBLite getInstance() { return Holder.INSTANCE; }

    public Database getDatabase() {
        return database;
    }

    public void startReplication() {
        if (null == replicator) {
            replicator = Couchbase.createContinuousReplicator(getInstance().getDatabase(), Runtime.getRemoteURI());
        }

        replicator.start();
    }

    public void stopReplication() {
        if (null == replicator) return;

        replicator.stop();
    }
}
