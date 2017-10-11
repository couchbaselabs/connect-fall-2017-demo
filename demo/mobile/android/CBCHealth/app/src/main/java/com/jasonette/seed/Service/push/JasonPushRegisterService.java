package com.jasonette.seed.Service.push;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.jasonette.seed.Core.JasonViewActivity;
import com.jasonette.seed.Launcher.Launcher;

import org.json.JSONObject;

public class JasonPushRegisterService extends FirebaseInstanceIdService{
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if (refreshedToken != null) {
            try {
                JSONObject payload = new JSONObject();
                payload.put("token", refreshedToken);
                ((JasonViewActivity) Launcher.getCurrentContext()).simple_trigger("$push.onregister", payload, Launcher.getCurrentContext());
            } catch (Exception e) {

            }
        }
    }
}
