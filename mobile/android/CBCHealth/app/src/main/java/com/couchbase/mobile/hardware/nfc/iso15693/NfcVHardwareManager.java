package com.couchbase.mobile.hardware.nfc.iso15693;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.widget.Toast;

import com.couchbase.mobile.hardware.HardwareManager;
import com.couchbase.mobile.utils.ToastFactory;

public class NfcVHardwareManager implements HardwareManager {
    private final IntentFilter[] intentFilters;
    private final String[][] techLists;

    private NfcAdapter nfcAdapter;
    private Tag tag = null;

    public NfcVHardwareManager(Context ctx) {
        nfcAdapter = NfcAdapter.getDefaultAdapter(ctx.getApplicationContext());

        IntentFilter nfcTechDiscovered = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        intentFilters = new IntentFilter[] { nfcTechDiscovered };
        techLists = new String[][] { new String[] { NfcV.class.getName() } };
    }

    @Override
    public void enable(Context ctx, Intent parameters) {
        if (!nfcAdapter.isEnabled()) {
            ToastFactory.makeToast(ctx, "Please enable NFC in device settings",
                    ToastFactory.STYLE_FAILURE, Toast.LENGTH_LONG);

            return;
        }

        tag = parameters.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        Activity activity = (Activity)ctx;

        PendingIntent nfcVIntent = PendingIntent.getActivity(activity, 0,
                new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        nfcAdapter.enableForegroundDispatch(activity, nfcVIntent, intentFilters, techLists);
    }

    @Override
    public void disable(Context ctx) {
        nfcAdapter.disableForegroundDispatch((Activity)ctx);
    }

    public void setTag(Tag tag) { this.tag = tag; }

    public Tag getTag() { return tag; }
}
