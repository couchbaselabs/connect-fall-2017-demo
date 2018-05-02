package com.couchbase.mobile.app.patient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import com.couchbase.lite.Document;
import com.couchbase.mobile.R;
import com.couchbase.mobile.app.common.Item;
import com.couchbase.mobile.app.common.ItemsAdapter;
import com.couchbase.mobile.app.launch.Runtime;
import com.couchbase.mobile.database.CBLite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PatientActivity extends AppCompatActivity implements Document.ChangeListener {
    public static final String TAG = PatientActivity.class.getSimpleName();

    ItemsAdapter adapter = null;
    Document doc = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Patient Profile");

        adapter = new ItemsAdapter(this, new ArrayList<Item>());
        ListView listView = findViewById(R.id.lvItems);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        doc = CBLite.getInstance().getDatabase().getDocument(Runtime.getPatientID());
        if (doc != null) {
            doc.addChangeListener(this);
            adapter.addAll(getPatientProfileList(doc));
        }
    }

    @Override
    protected void onStop() {
        if (doc != null)
            doc.removeChangeListener(this);
        super.onStop();
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
        List<Object> names = (List<Object>) doc.getProperty("name");
        if (names != null && names.size() > 0) {
            Map<String, Object> name = (Map<String, Object>) names.get(0);
            String family = (String) name.get("family");
            List<Object> given = (List<Object>) name.get("given");
            return given.get(0) + " " + family;
        } else {
            return "";
        }
    }

    String getGender(Document doc) {
        Map<String, Object> gender = (Map<String, Object>) doc.getProperty("gender");
        if (gender != null)
            return (String) gender.get("text");
        else
            return "";
    }

    String getBirthDate(Document doc) {
        return (String) doc.getProperty("birthDate");
    }

    String getAddress(Document doc) {
        List<Object> address = (List<Object>) doc.getProperty("address");

        if (address != null  && address.get(0) != null)
            return (String) ((Map<String, Object>)address.get(0)).get("text");
        else
            return "";
    }

    List<Item> getExtension(Document doc) {
        List<Item> list = new ArrayList<>();
        List<Object> telecom = (List<Object>) doc.getProperty("telecom");
        if (telecom != null && telecom.size() > 0) {
            Map<String, Object> tele = (Map<String, Object>) telecom.get(0);
            if (tele != null) {
                for (String key : tele.keySet()) {
                    list.add(new Item(key, tele.get(key).toString()));
                }
            }
        }
        return list;
    }

    @Override
    public void changed(Document.ChangeEvent event) {
        final Document doc = event.getSource();
        if (doc != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PatientActivity.this.adapter.clear();
                    PatientActivity.this.adapter.addAll(getPatientProfileList(doc));
                }
            });
        }
    }
}
