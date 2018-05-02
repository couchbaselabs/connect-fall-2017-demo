package com.couchbase.mobile.collectors.temperature;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.couchbase.mobile.collectors.Collector;
import com.couchbase.mobile.hardware.HardwareManager;
import com.couchbase.mobile.hardware.HardwareManagerFactory;
import com.couchbase.mobile.hardware.nfc.iso15693.Iso15693;
import com.couchbase.mobile.hardware.nfc.iso15693.NfcVHardwareManager;
import com.couchbase.mobile.utils.Conversion;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// Texas Instruments Temperature Patch Reference Device RF430-TMPSNS-EVM

public class RF430_TMPSNS_EVM implements Collector, Runnable {
    private static final String TAG = RF430_TMPSNS_EVM.class.getCanonicalName();

    private NfcVHardwareManager hardwareManager;
    private OnSampleReadyListener sampleReadyListener;

    private HandlerThread backgroundThread;
    private Handler backgroundHandler;

    // See Texas Instruments publication slau603a - RF430FRL15xH Firmware User's Guide, Section 7
    private static byte RegisterBlock0[] = new byte[] {
            (byte) 0x01, // General Control Register
            (byte) 0x00, // Firmware Status Register
            (byte) 0x07, // Sensor Control Register
            (byte) 0x03, // Frequency Register
            (byte) 0x01, // Number of Passes Register
            (byte) 0x01, // Averaging Register
            (byte) 0x00, // Interrupt Control Register
            (byte) 0x40  // Error Control Register
    };
    private static byte RegisterBlock1[] = new byte[] {
            (byte) 0x00, // Reference-ADC1 Sensor Skip Count Register
            (byte) 0x00, // Thermistor-ADC2 Sensor Skip Count Register
            (byte) 0x00, // ADC0 Sensor Skip Count Register
            (byte) 0x00, // Internal Sensor Skip Count Register
            (byte) 0x00, // Digital Sensor 1 Skip Count Register
            (byte) 0x00, // Digital Sensor 2 Skip Count Register
            (byte) 0x00, // Digital Sensor 3 Skip Count Register
            (byte) 0x00  // Number of Blocks Received Register
    };
    private static byte RegisterBlock2[] = new byte[] {
            (byte) 0x19, // Reference-ADC1 Configuration Register
            (byte) 0x19, // Thermistor-ADC2 Sensor Configuration Register
            (byte) 0x18, // ADC0 Sensor Configuration Register
            (byte) 0x00, // Internal Sensor Configuration Register
            (byte) 0x00, // Initial Delay Period Setup Register
            (byte) 0x00, // JTAG Enable Password Register
            (byte) 0x00, (byte) 0x00 // Initial Delay Period Register
    };
    private static byte RegisterBlock3[] = new byte[] {
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Custom Timer Value Register
            (byte) 0x00, (byte) 0x00, // High Threshold Thermistor-ADC2 Sensor Register
            (byte) 0x00, (byte) 0x00  // Low Threshold ADC0 Sensor Register
    };
    private static byte RegisterBlock4[] = new byte[] {
            (byte) 0x00, (byte) 0x00, // Low Threshold Thermistor-ADC2 Sensor Register
            (byte) 0x00, (byte) 0x00, // High Threshold Thermistor-ADC2 Sensor Register
            (byte) 0x00, (byte) 0x00, // Low Threshold ADC0 Sensor Register
            (byte) 0x00, (byte) 0x00  // High Threshold ADC0 Sensor Register
    };
    private static byte RegisterBlock5[] = new byte[] {
            (byte) 0x00, (byte) 0x00, // Low Threshold Internal Temperature Sensor Register
            (byte) 0x00, (byte) 0x00, // High Threshold Internal Temperature Sensor Register
            (byte) 0x00, (byte) 0x00, // Low Threshold Digital1 Sensor Register
            (byte) 0x00, (byte) 0x00 // High Threshold Digital1 Sensor Register
    };
    private static byte RegisterBlock6[] = new byte[] {
            (byte) 0x00, (byte) 0x00, // Low Threshold Digital2 Sensor Register
            (byte) 0x00, (byte) 0x00, // High Threshold Digital2 Sensor Register
            (byte) 0x00, (byte) 0x00, // Low Threshold Digital3 Sensor Register
            (byte) 0x00, (byte) 0x00 // High Threshold Digital3 Sensor Register
    };
    private static byte RegisterBlock7[] = new byte[] {
            (byte) 0x00, // Reference or ADC1 Alarm Configuration Register
            (byte) 0x00, // Thermistor and ADC2 Alarm Configuration Register
            (byte) 0x00, // ADC0 Alarm Configuration Register
            (byte) 0x00, // Internal Alarm Configuration Register
            (byte) 0x00, // Digital 1 Alarm Configuration Register
            (byte) 0x00, // Digital 2 Alarm Configuration Register
            (byte) 0x00, // Digital 3 Alarm Configuration Register
            (byte) 0x00  // Logging Memory Size Register
    };

    @Override
    public int getType() {
        return COLLECTOR_TYPE_TEMPERATURE;
    }

    @Override
    public void initialize(Context ctx, Intent parameters) throws IllegalArgumentException {
        if (!(ctx instanceof Activity)) {
            throw new IllegalArgumentException(TAG + " requires an Activity Context");
        }

        hardwareManager = (NfcVHardwareManager) HardwareManagerFactory.getHardwareManager(ctx,
                HardwareManager.HARDWARE_TYPE_NFCV);
    }

    @Override
    public void enable(Context ctx, Intent parameters) {
        startBackgroundThread();
        hardwareManager.enable(ctx, parameters);
    }

    @Override
    public void disable(Context ctx) {
        hardwareManager.disable(ctx);
        stopBackgroundThread();
    }

    @Override
    public void requestSample(OnSampleReadyListener listener) {
        sampleReadyListener = listener;

        backgroundHandler.post(this);
    }

    @Override
    public void run() {
        Tag tag = hardwareManager.getTag();

        if (null == tag || !Iso15693.getManufacturer(tag).equals(Iso15693.MANUFACTURER_TEXAS_INSTRUMENTS)) {
            sampleReadyListener.sample(null);

            return;
        }

        Iso15693 iso15693 = new Iso15693(hardwareManager.getTag());

        byte[][] commandBlock = new byte[9][];

        // Prepare sampling parameters
        commandBlock[0] = iso15693.WriteSingleBlockCommand((byte) 1, RegisterBlock1);
        commandBlock[1] = iso15693.WriteSingleBlockCommand((byte) 2, RegisterBlock2);
        commandBlock[2] = iso15693.WriteSingleBlockCommand((byte) 3, RegisterBlock3);
        commandBlock[3] = iso15693.WriteSingleBlockCommand((byte) 4, RegisterBlock4);
        commandBlock[4] = iso15693.WriteSingleBlockCommand((byte) 5, RegisterBlock5);
        commandBlock[5] = iso15693.WriteSingleBlockCommand((byte) 6, RegisterBlock6);
        commandBlock[6] = iso15693.WriteSingleBlockCommand((byte) 7, RegisterBlock7);

        // Trigger sampling
        commandBlock[7] = iso15693.WriteSingleBlockCommand((byte) 0, RegisterBlock0);

        // Read sample value
        commandBlock[8] = iso15693.ReadSingleBlockCommand((byte) 9);

        byte[] result = iso15693.executeCommandBlock(commandBlock);

        if (null == result || (result[0] & Iso15693.ISO15693_ERROR_FLAG) != 0) {
            sampleReadyListener.sample(null);

            return;
        }

        String dataFromTag = Conversion.bytesToHexString(result);

        Log.i(TAG, dataFromTag + ' ' + dataFromTag.length());

        if (20 > dataFromTag.length())  {
            sampleReadyListener.sample(null);

            return;
        }

        double B_Value = 4330.0;
        double R0_Value = 100000.0;
        double T0_Value = 298.15;
        double K0_Temp = 273.15;

        long refValue = Long.parseLong(dataFromTag.substring(6,8).concat(dataFromTag.substring(4,6)),16);
        long thermValue = Long.parseLong(dataFromTag.substring(10,12).concat(dataFromTag.substring(8,10)),16);

        double temperature = (((((thermValue * 0.9) / 16384.0) / 2.0) / 0.0000024) * 8738.13) / refValue;

        temperature = (B_Value / (Math.log10(temperature / (R0_Value * Math.exp((-B_Value) /T0_Value))) / Math.log10(2.718))) - K0_Temp;

        temperature = (temperature * 9 / 5 + 32);

        Map<String, Object> sample = new HashMap<>();

        sample.put("value", temperature);
        sample.put("unit", "degrees F");
        sample.put("issued", new Date());

        sampleReadyListener.sample(sample);
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("RF430BackgroundThread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        backgroundThread.quit();
        backgroundThread = null;
        backgroundHandler = null;
    }
}