package com.couchbase.mobile.notifications;

import com.urbanairship.Autopilot;
import com.urbanairship.UAirship;

public class CBCAutoPilot extends Autopilot {
    @Override
    public void onAirshipReady(UAirship airship) {
        airship.getPushManager().setUserNotificationsEnabled(false);
    }
}
