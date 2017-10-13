package com.couchbase.mobile.notifications;

import com.urbanairship.UAirship;

public class Autopilot extends com.urbanairship.Autopilot {
    @Override
    public void onAirshipReady(UAirship airship) {
        airship.getPushManager().setUserNotificationsEnabled(true);
        // airship.getInAppMessageManager().setAutoDisplayEnabled(true);

        // Customize notifications here.
        // NotificationFactory factory = UAirship.shared().getPushManager().getNotificationFactory();

        // factory.setTitleId(R.string.app_title);
        // factory.setColor(ContextCompat.getColor(context, R.color.notification_color));
    }
}