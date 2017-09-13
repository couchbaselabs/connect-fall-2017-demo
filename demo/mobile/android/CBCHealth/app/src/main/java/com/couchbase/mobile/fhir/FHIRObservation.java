package com.couchbase.mobile.fhir;

/* See https://www.hl7.org/fhir/observation.html */

public class FHIRObservation implements FHIRResource {
    private String json;

    @Override
    public String toJson() {
        return json;
    }

    public FHIRObservation fromJson(String json) {
        this.json = json;

        return this;
    }
}
