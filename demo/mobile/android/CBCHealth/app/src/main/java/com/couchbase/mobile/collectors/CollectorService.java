package com.couchbase.mobile.collectors;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public class CollectorService {
    private static final String TAG = CollectorService.class.getCanonicalName();

    private static ServiceLoader<Collector> loader = ServiceLoader.load(Collector.class);;

    private CollectorService() { throw new AssertionError("No instances."); }

    public static Collector getCollectorByType(int type) {
        try {
            Iterator<Collector> collectors = loader.iterator();

            while (collectors.hasNext()) {
                Collector collector = collectors.next();

                if (collector.getType() == type) {
                    return collector;
                }
            }
        } catch (ServiceConfigurationError serviceError) {
            serviceError.printStackTrace();
        }

        return null;
    }
}
