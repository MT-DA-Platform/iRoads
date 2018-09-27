package com.vatichub.obd2.api;

import com.vatichub.obd2.OBD2CoreConfiguration;
import com.vatichub.obd2.OBD2CoreConstants;
import com.vatichub.obd2.exception.OBD2ClientException;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public abstract class OBD2Client {

    private boolean closed = false;

    public abstract void init() throws OBD2ClientException;
    public abstract boolean[] getSupportedPIDsInBinary();
    public abstract List<String> getSupportedPIDsList();
    public abstract double getPIDValue (String pid) throws OBD2ClientException;

    public static final int ALL_PID_LENGTH_HEX = 40;

    public static boolean[] getSupportedPIDsBinaryFromHEX(String hexStr) {
        int numcount = hexStr.length() * 4;
        String binary = new BigInteger(hexStr, 16).toString(2);
        if (binary.length() < numcount) {
            binary = new String(new char[numcount - binary.length()]).replace("\0", "0") + binary;
        }

        boolean[] supportedpids = new boolean[ALL_PID_LENGTH_HEX * 4 + 1]; //+1 - pid 00 is always true
        supportedpids[0] = true;
        for (int i = 0; i < binary.length(); i++) {
            supportedpids[i + 1] = binary.charAt(i) == '1';
        }
        return supportedpids;
    }


    public static List<String> selectSupportedPIDsFromAllPIDList(List<String> allpidslist, boolean[] binaryarr) {
        ArrayList<String> supportedPIDsList = new ArrayList<>();
        JSONObject pidconf = OBD2CoreConfiguration.getInstance().getPidConfig();
        try {
            for (int i = 0; i < allpidslist.size(); i++) {
                String pidKey = allpidslist.get(i);
                JSONObject pidobj = pidconf.getJSONObject(pidKey);
                String pidHex = pidobj.getString(OBD2CoreConstants.CONF_PID);

                int pidNum = Integer.parseInt(pidHex, 16);

                if (pidNum < binaryarr.length && binaryarr[pidNum]) {
                    supportedPIDsList.add(pidKey);
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return supportedPIDsList;
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() throws OBD2ClientException {
        closed = true;
    }
}
