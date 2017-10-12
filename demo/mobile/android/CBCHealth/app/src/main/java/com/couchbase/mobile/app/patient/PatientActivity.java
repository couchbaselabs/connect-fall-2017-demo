package com.couchbase.mobile.app.patient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import com.couchbase.lite.Array;
import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Document;
import com.couchbase.lite.DocumentChange;
import com.couchbase.lite.DocumentChangeListener;
import com.couchbase.lite.Log;
import com.couchbase.mobile.R;
import com.couchbase.mobile.app.launch.Runtime;
import com.couchbase.mobile.database.CBLite;

import java.util.ArrayList;
import java.util.List;

public class PatientActivity extends AppCompatActivity implements DocumentChangeListener {
    public static final String TAG = PatientActivity.class.getSimpleName();

    ItemsAdapter adapter = null;
    String docID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Patient Profile");

        adapter = new ItemsAdapter(this, new ArrayList<Item>());
        ListView listView = findViewById(R.id.lvItems);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Document doc = CBLite.getInstance().getDatabase().getDocument(Runtime.getPatientID());
        if (doc != null) {
            docID = doc.getId();
            CBLite.getInstance().getDatabase().addChangeListener(docID, this);
            Log.e(TAG, "doc -> %s", doc.toMap());
            adapter.addAll(getPatientProfileList(doc));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (docID != null)
            CBLite.getInstance().getDatabase().removeChangeListener(docID, this);
    }

    List<Item> getPatientProfileList(Document doc) {
        List<Item> items = new ArrayList<>();
        items.add(new Item("Name", getName(doc)));
        items.add(new Item("Gender", getGender(doc)));
        items.add(new Item("Birth Date", getBirthDate(doc)));
        items.add(new Item("Address", getAddress(doc)));
        items.addAll(getExtension(doc));
        return items;
    }

    String getName(Document doc) {
        Array names = doc.getArray("name");
        Dictionary name = names.getDictionary(0);
        Array family = name.getArray("family");
        Array given = name.getArray("given");
        return given.getString(0) + " " + family.getString(0);
    }

    String getGender(Document doc) {
        Dictionary gender = doc.getDictionary("gender");
        return gender.getString("text");
    }

    String getBirthDate(Document doc) {
        return doc.getString("birthDate");
    }

    String getAddress(Document doc) {
        Dictionary gender = doc.getDictionary("address");
        return gender.getString("text");
    }

    List<Item> getExtension(Document doc) {
        List<Item> list = new ArrayList<>();
        Dictionary extension = doc.getDictionary("extension");
        if (extension != null) {
            for (String key : extension.getKeys()) {
                list.add(new Item(key, extension.getObject(key).toString()));
            }
        }
        return list;
    }

    @Override
    public void changed(DocumentChange change) {
        Log.e(TAG, "DocumentChange -> %s", change.getDocumentID());
        Document doc = CBLite.getInstance().getDatabase().getDocument(Runtime.getPatientID());
        if (doc != null) {
            Log.e(TAG, "doc -> %s", doc.toMap());
            this.adapter.clear();
            this.adapter.addAll(getPatientProfileList(doc));
        }
    }
}
