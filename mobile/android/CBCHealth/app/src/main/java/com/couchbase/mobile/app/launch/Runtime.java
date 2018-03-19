package com.couchbase.mobile.app.launch;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.couchbase.mobile.BuildConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;

public class Runtime extends ContentProvider {
    private static String TAG = Runtime.class.getCanonicalName();

    private static Context context;
    private static Parameters parameters;

    @Override
    public boolean onCreate() {
        context = this.getContext().getApplicationContext();
        parameters = BuildConfig.PARAMETERS;

        return true;
    }

    public static Context getApplicationContext() { return context; }

    public static String getDatabaseName() { return parameters.getDatabaseName(); }

    public static URI getRemoteURI() { return parameters.getRemoteURI(); }

    private static class ObjectMapperHolder {
        private static ObjectMapper objectMapper = new ObjectMapper();
    }

    public static ObjectMapper getObjectMapper() { return  ObjectMapperHolder.objectMapper; }

    public static String getPatientID() { return parameters.getPatientID(); }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
