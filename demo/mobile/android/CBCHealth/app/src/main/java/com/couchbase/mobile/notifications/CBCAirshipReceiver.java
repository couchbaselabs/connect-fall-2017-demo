package com.couchbase.mobile.notifications;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.urbanairship.AirshipReceiver;
import com.urbanairship.push.PushMessage;

public class CBCAirshipReceiver extends AirshipReceiver {
    private static final String TAG = "CBCAirshipReceiver";

    @Override
    protected void onPushReceived(Context context, PushMessage message, boolean notificationPosted) {
        Log.i(TAG, "Received push message. Alert: " + message.getAlert() + ". posted notification: " + notificationPosted);

        switch(message.getAlert()) {
            case "start":
                //CBLHelper.getInstance().startReplication();
                break;
            case "stop":
                //CBLHelper.getInstance().stopReplication();
                break;
            case "reset":
                //CBLHelper.getInstance().reset();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onChannelCreated(@NonNull Context context, @NonNull String channelId) {
        Log.i(TAG, "Channel created. Channel Id:" + channelId + ".");
    }

    @Override
    protected void onChannelUpdated(@NonNull Context context, @NonNull String channelId) {
        Log.i(TAG, "Channel updated. Channel Id:" + channelId + ".");
    }

    @Override
    protected void onChannelRegistrationFailed(Context context) {
        Log.i(TAG, "Channel registration failed.");
    }
}