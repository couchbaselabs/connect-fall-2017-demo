package com.couchbase.mobile.app.patient;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.couchbase.lite.Array;
import com.couchbase.lite.DatabaseChange;
import com.couchbase.lite.DatabaseChangeListener;
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

public class PatientActivity extends AppCompatActivity implements DocumentChangeListener , DatabaseChangeListener {
    public static final String TAG = PatientActivity.class.getSimpleName();

    ArrayAdapter<String> itemsAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Document doc = CBLite.getInstance().getDatabase().getDocument(Runtime.getPatientID());
        Log.e(TAG, "doc -> %s", doc.toMap());

        CBLite.getInstance().getDatabase().addChangeListener(doc.getId(), this);
        //CBLite.getInstance().getDatabase().addChangeListener(this);

        List<String> items = getPatientProfileList(doc);
        itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        ListView listView = findViewById(R.id.lvItems);
        listView.setAdapter(itemsAdapter);
    }

    List<String> getPatientProfileList(Document doc){
        List<String> items = new ArrayList<>();
        items.add(getName(doc));
        items.add(getGender(doc));
        items.add(getBirthDate(doc));
        items.add(getAddress(doc));
        items.addAll(getExtension(doc));
        return items;
    }

    String getName(Document doc){
        Array names = doc.getArray("name");
        Dictionary name = names.getDictionary(0);
        Array family = name.getArray("family");
        Array given = name.getArray("given");
        return given.getString(0) + " " + family.getString(0);
    }

    String getGender(Document doc){
        Dictionary gender = doc.getDictionary("gender");
        return gender.getString("text");
    }

    String getBirthDate(Document doc){
        return doc.getString("birthDate");
    }

    String getAddress(Document doc){
        Dictionary gender = doc.getDictionary("address");
        return gender.getString("text");
    }

    List<String> getExtension(Document doc){
        List<String> list = new ArrayList<>();
        Dictionary extension = doc.getDictionary("extension");
        if(extension!=null){
            for(String key: extension.getKeys()){
                list.add(key + ": " + extension.getObject(key));
            }
        }
        return list;
    }

    @Override
    public void changed(DocumentChange change) {
        Log.e(TAG, "DocumentChange -> %s", change.getDocumentID());
        Document doc = CBLite.getInstance().getDatabase().getDocument(Runtime.getPatientID());
        Log.e(TAG, "doc -> %s", doc.toMap());

        this.itemsAdapter.clear();
        this.itemsAdapter.addAll(getPatientProfileList(doc));
    }

    @Override
    public void changed(DatabaseChange change) {
        Log.e(TAG, "DatabaseChange -> %s", change.getDocumentIDs());
    }
}
