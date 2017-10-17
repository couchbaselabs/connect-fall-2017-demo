package com.couchbase.mobile.hardware.nfc.iso15693;

import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.NfcV;
import android.util.Log;

import java.io.IOException;

/* Caution about sending anti-collison/enumeration commands: https://stackoverflow.com/questions/39153783/nfc-v-tag-lost-exception-with-xamarin-and-st-m24lr-tag?noredirect=1&lq=1 */

public class Iso15693 {
    private static final String TAG = Iso15693.class.getCanonicalName();

    public static final byte ISO15693_ERROR_FLAG = 0x01;

    public static final byte ISO15693_SUB_CARRIER_FLAG = 0x01;
    public static final byte ISO15693_DATA_RATE_FLAG = 0x02;
    public static final byte ISO15693_INVENTORY_FLAG = 0x04;
    public static final byte ISO15693_PROTOCOL_EXTENSION_FLAG = 0x08;
    public static final byte ISO15693_SELECT_FLAG = 0x10;
    public static final byte ISO15693_ADDRESS_FLAG = 0x20;
    public static final byte ISO15693_OPTION_FLAG = 0x40;

    public static final byte ISO15693_READ_SINGLE_BLOCK_COMMAND = 0x20;
    public static final byte ISO15693_WRITE_SINGLE_BLOCK_COMMAND = 0x21;
    public static final byte ISO15693_READ_MULTIPLE_BLOCKS_COMMAND = 0x23;

    public static String MANUFACTURER_MOTOROLA                       = "Motorola";
    public static String MANUFACTURER_ST_MICROELECTRONICS            = "ST Microelectronics";
    public static String MANUFACTURER_HITACHI                        = "Hitachi";
    public static String MANUFACTURER_NXP_SEMICONDUCTORS             = "NXP Semiconductors";
    public static String MANUFACTURER_INFINEON_TECHNOLOGIES          = "Infineon Technologies";
    public static String MANUFACTURER_CYLINC                         = "Cylinc";
    public static String MANUFACTURER_TEXAS_INSTRUMENTS              = "Texas Instruments";
    public static String MANUFACTURER_FUJITSU_LIMITED                = "Fujitsu Limited";
    public static String MANUFACTURER_MATSUSHITA_ELECTRIC_INDUSTRIAL = "Matsushita Electric Industrial";
    public static String MANUFACTURER_NEC                            = "NEC";
    public static String MANUFACTURER_OKI_ELECTRIC                   = "Oki Electric";
    public static String MANUFACTURER_TOSHIBA                        = "Toshiba";
    public static String MANUFACTURER_MITSUBISHI_ELECTRIC            = "Mitsubishi Electric";
    public static String MANUFACTURER_SAMSUNG_ELECTRONICS            = "Samsung Electronics";
    public static String MANUFACTURER_HYUNDAI_ELECTRONICS            = "Hyundai Electronics";
    public static String MANUFACTURER_LG_SEMICONDUCTORS              = "LG Semiconductors";
    public static String MANUFACTURER_EM_MICROELECTRONIC_MARIN       = "EM Microelectronic-Marin";

    private NfcV nfcV;
    private Tag tag;
    private byte[] tagUid;

    public Iso15693(Tag tag) {
        this.tag = tag;
        tagUid = tag.getId();
        nfcV = NfcV.get(tag);
    }

    public byte[] ReadSingleBlockCommand(byte BlockId) {
        return new byte[]{ ISO15693_ADDRESS_FLAG|ISO15693_DATA_RATE_FLAG,
                ISO15693_READ_SINGLE_BLOCK_COMMAND,
                tagUid[0], tagUid[1], tagUid[2], tagUid[3], tagUid[4], tagUid[5], tagUid[6], tagUid[7],
                BlockId };
    }

    public byte[] WriteSingleBlockCommand(byte BlockId, byte[] data) {
        return new byte[]{ ISO15693_OPTION_FLAG|ISO15693_ADDRESS_FLAG|ISO15693_DATA_RATE_FLAG,
                ISO15693_WRITE_SINGLE_BLOCK_COMMAND,
                tagUid[0], tagUid[1], tagUid[2], tagUid[3], tagUid[4], tagUid[5], tagUid[6], tagUid[7],
                BlockId, data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7] };
    }

    public byte[] ReadMultipleBlocksCommand(byte BlockOffset, byte NumberOfBlocks) {
        return new byte[]{ ISO15693_OPTION_FLAG|ISO15693_ADDRESS_FLAG|ISO15693_DATA_RATE_FLAG,
                ISO15693_READ_MULTIPLE_BLOCKS_COMMAND,
                tagUid[0], tagUid[1], tagUid[2], tagUid[3], tagUid[4], tagUid[5], tagUid[6], tagUid[7],
                BlockOffset, NumberOfBlocks };
    }

    public byte[] ReadSystemInfoCommand() {
        return new byte[]{ 0x00, 0x2B };
    }

    public byte[] ReadSingleSlotInventoryCommand() {
        return new byte[]{ 0x26, 0x01, 0x00 };
    }

    public byte[] ReadSecurityStatusCommand(){
        return new byte[]{ 0x02, 0x2C };
    }

    public byte[] executeCommandBlock(byte[][] commandBlock) {
        byte[] response = null;

        try {
            nfcV.connect();

            for (byte[] command : commandBlock) {
                try {
                    response = nfcV.transceive(command);
                } catch (TagLostException ex) {
                    // Many implementations incorrectly respond with tag lost exceptions
                    // especially during writes
                    // Ignore it and continue
                }

                if (null != response && (response[0] & ISO15693_ERROR_FLAG) != 0) {
                    return response;
                }
            }
        } catch (IOException ex) {
            Log.e(TAG, "Error communicating with card: " + ex.toString());
            ex.printStackTrace();
            response = null;
        } finally {
            close(nfcV);
        }

        return response;
    }

    private void close(NfcV nfcV) {
        try {
            nfcV.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e(TAG, ex.toString());
        }
    }

    public byte[] getIso15693Uid() {
        if (null == tag) return null;

        return tag.getId();
    }

    public byte getDsfid() {
        if (null == nfcV) return 0;

        return nfcV.getDsfId();
    }

    public static byte getAfi(byte[] systemInfo) {
        return systemInfo[11];
    }

    public static int getBlockSize(byte[] systemInfo) {
        return ((int) systemInfo[11] + 1);
    }

    public static int getNumberOfBlocks(byte[] systemInfo) {
        return ((int) systemInfo[10] + 1);
    }

    public static byte getIcReference(byte[] systemInfo) {
        return systemInfo[14];
    }

    public static String getManufacturer(Tag tag) {
        byte[] uid = tag.getId();
        String manufacturer = null;

        switch (uid[6]) {
            case 0x01:
                manufacturer = MANUFACTURER_MOTOROLA;
                break;
            case 0x02:
                manufacturer = MANUFACTURER_ST_MICROELECTRONICS;
                break;
            case 0x03:
                manufacturer = MANUFACTURER_HITACHI;
                break;
            case 0x04:
                manufacturer = MANUFACTURER_NXP_SEMICONDUCTORS;
                break;
            case 0x05:
                manufacturer = MANUFACTURER_INFINEON_TECHNOLOGIES;
                break;
            case 0x06:
                manufacturer = MANUFACTURER_CYLINC;
                break;
            case 0x07:
                manufacturer = MANUFACTURER_TEXAS_INSTRUMENTS;
                break;
            case 0x08:
                manufacturer = MANUFACTURER_FUJITSU_LIMITED;
                break;
            case 0x09:
                manufacturer = MANUFACTURER_MATSUSHITA_ELECTRIC_INDUSTRIAL;
                break;
            case 0x0A:
                manufacturer = MANUFACTURER_NEC;
                break;
            case 0x0B:
                manufacturer = MANUFACTURER_OKI_ELECTRIC;
                break;
            case 0x0C:
                manufacturer = MANUFACTURER_TOSHIBA;
                break;
            case 0x0D:
                manufacturer = MANUFACTURER_MITSUBISHI_ELECTRIC;
                break;
            case 0x0E:
                manufacturer = MANUFACTURER_SAMSUNG_ELECTRONICS;
                break;
            case 0x0F:
                manufacturer = MANUFACTURER_HYUNDAI_ELECTRONICS;
                break;
            case 0x10:
                manufacturer = MANUFACTURER_LG_SEMICONDUCTORS;
                break;
            case 0x16:
                manufacturer = MANUFACTURER_EM_MICROELECTRONIC_MARIN;
                break;
        }

        return manufacturer;
    }
}
