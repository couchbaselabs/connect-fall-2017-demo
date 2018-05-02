package com.couchbase.mobile.fhir;

import com.couchbase.mobile.app.launch.Runtime;
import com.couchbase.mobile.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Temperature extends HashMap<String, Object> {
    public Temperature(final Object value, final Object unit, final Date issued) {
        //TODO improve type safety
        put("resourceType", "Observation");

        put("code", new HashMap<String, Object>() {{
            put("coding", new ArrayList<Map<String, Object>>(1) {{
                add(new HashMap<String, Object>() {{
                    put("code", "39106-0");
                    put("display", "Temperature");
                    put("system", "http://loinc.org");
                }});
            }});
            put("text", "Temperature");
        }});

        put("issued", DateUtils.toJson(issued));

        put("valueQuantity", new HashMap<String, Object>() {{
            put("value", value);
            put("unit", unit);
        }});

        // Todo pull reference id from login info?
        put("subject", new HashMap<String, Object>() {{
            put("reference", "urn:uuid:" + Runtime.getPatientID());
        }});
    }
}
