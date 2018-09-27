package com.vatichub.obd2.impl;

import android.nfc.FormatException;
import android.util.Log;

import com.vatichub.obd2.OBD2CoreConfiguration;
import com.vatichub.obd2.OBD2CoreConstants;
import com.vatichub.obd2.api.OBD2Client;
import com.vatichub.obd2.connect.StreamCommunicator;
import com.vatichub.obd2.exception.OBD2ClientException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import expr.Expr;
import expr.Parser;
import expr.SyntaxException;
import expr.Variable;

public class ELM327ClientImpl extends OBD2Client {

    private static final String TAG = OBD2CoreConstants.APPTAG+"-ELM327BTClient";

    private static final String AT_RESET[] = {"ATZ","Reset"};
    private static final String AT_OK[] = {"OK","Response OK"};
    private static final String AT_ECHO_ON[] = {"ATE1","ECHO ON"};
    private static final String AT_ECHO_OFF[] = {"ATE0","ECHO OFF"};
    private static final String AT_MEM_DISABLE[] = {"ATM0","MEMORY DISABLE"};
    private static final String AT_MEM_ENABLE[] = {"ATM1","MEMORY ENABLE"};
    private static final String AT_LF_CR_ONLY[] = {"ATL0","LINE FEED CARRIAGE RETURN ONLY"};
    private static final String AT_LF_CR_PLUS[] = {"ATL1","LINE FEED CARRIAGE RETURN +"};
    private static final String AT_NO_SPACE[] = {"ATS0","NO SPACE"};
    private static final String AT_ADD_SPACE[] = {"ATS1","ADD SPACE"};
    private static final String AT_DEV_DESCRIPTION[] = {"AT@1","DEVICE DESCRIPTION"};
    private static final String AT_IDENTIFY_ITSELF[] = {"ATI","IDENTIFY ITSELF"};
    private static final String AT_ADAPTIVE_TIMING_0[] = {"ATAT0","SET ADAPTIVE TIMING = 0"};
    private static final String AT_ADAPTIVE_TIMING_1[] = {"ATAT1","SET ADAPTIVE TIMING = 1"};
    private static final String AT_ADAPTIVE_TIMING_2[] = {"ATAT2","SET ADAPTIVE TIMING = 2"};
    private static final String AT_HEADERS_OFF[] = {"ATH0","TURN OFF HEADERS"};
    private static final String AT_HEADERS_ON[] = {"ATH1","TURN ON HEADERS"};
    private static final String AT_SET_PROTOCOL_AUTOMATIC[] = {"ATSP0","SET AUTOMATIC PROTOCOL SEARCH"};
    private static final String AT_DESCRIBE_PROTOCOL_NUMBER[] = {"ATDPN","DESCRIBE PROTOCOL NUMBER"};

    private static final byte[] TERM_SEND = {0x0d};
    private static final char TERM_RECEIVE = '>';

    private static final String OBD_SUPPORTED_PIDS_1_20[] = {"0100","WHAT ARE SUPPORTED PIDs from 0x01 to 0x20?"};
    private static final String OBD_SUPPORTED_PIDS_21_40[] = {"0120","WHAT ARE SUPPORTED PIDs from 0x21 to 0x40?"};
    private static final String OBD_SUPPORTED_PIDS_41_60[] = {"0140","WHAT ARE SUPPORTED PIDs from 0x41 to 0x60?"};
    private static final String OBD_SUPPORTED_PIDS_61_80[] = {"0160","WHAT ARE SUPPORTED PIDs from 0x61 to 0x80?"};
    private static final String OBD_SUPPORTED_PIDS_81_A0[] = {"0180","WHAT ARE SUPPORTED PIDs from 0x81 to 0xA0?"};

    private StreamCommunicator streamCommunicator;
    private boolean[] supportedPIDsInBinary;
    private List<String> supportedPIDList;

    public ELM327ClientImpl(StreamCommunicator communicator) {
        streamCommunicator = communicator;
    }

    public void init() throws OBD2ClientException {
        Log.i(TAG, "BEGIN ELM327BTClient");

        try {
            String responseAT;

            responseAT = new String(streamCommunicator.sendReceive(AT_LF_CR_ONLY[0]));
            Log.i(OBD2CoreConstants.BTMSGTAG, responseAT);

            responseAT = new String(streamCommunicator.sendReceive(AT_RESET[0]));
            Log.i(OBD2CoreConstants.BTMSGTAG, responseAT);

            responseAT = new String(streamCommunicator.sendReceive(AT_ECHO_OFF[0]));
            Log.i(OBD2CoreConstants.BTMSGTAG, responseAT);

            responseAT = new String(streamCommunicator.sendReceive(AT_MEM_DISABLE[0]));
            Log.i(OBD2CoreConstants.BTMSGTAG, responseAT);

            responseAT = new String(streamCommunicator.sendReceive(AT_LF_CR_ONLY[0]));
            Log.i(OBD2CoreConstants.BTMSGTAG, responseAT);

            responseAT = new String(streamCommunicator.sendReceive(AT_NO_SPACE[0]));
            Log.i(OBD2CoreConstants.BTMSGTAG, responseAT);

            responseAT = new String(streamCommunicator.sendReceive(AT_HEADERS_OFF[0]));
            Log.i(OBD2CoreConstants.BTMSGTAG, responseAT);

            responseAT = new String(streamCommunicator.sendReceive(AT_ADAPTIVE_TIMING_1[0]));
            Log.i(OBD2CoreConstants.BTMSGTAG, responseAT);

            responseAT = new String(streamCommunicator.sendReceive(AT_SET_PROTOCOL_AUTOMATIC[0]));
            Log.i(OBD2CoreConstants.BTMSGTAG, responseAT);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }

            String pid0100response;

            while ((pid0100response = new String(streamCommunicator.sendReceive(OBD_SUPPORTED_PIDS_1_20[0]))).startsWith("SEARCHING")) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e2) {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }
            }

            String supportedpidHex1To20 = "00000000";
            String supportedpidHex21To40 = "00000000";
            String supportedpidHex41To60 = "00000000";
            String supportedpidHex61To80 = "00000000";
            String supportedpidHex81ToA0 = "00000000";

            try {
                supportedpidHex1To20 = sendRequestSupportedPIDsAndValidateResp(OBD_SUPPORTED_PIDS_1_20[0]);
                supportedpidHex21To40 = sendRequestSupportedPIDsAndValidateResp(OBD_SUPPORTED_PIDS_21_40[0]);
                supportedpidHex41To60 = sendRequestSupportedPIDsAndValidateResp(OBD_SUPPORTED_PIDS_41_60[0]);
                supportedpidHex61To80 = sendRequestSupportedPIDsAndValidateResp(OBD_SUPPORTED_PIDS_61_80[0]);
                supportedpidHex81ToA0 = sendRequestSupportedPIDsAndValidateResp(OBD_SUPPORTED_PIDS_81_A0[0]);

            } catch (FormatException e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }

            String supportedAllPIDsInHex = supportedpidHex1To20 + supportedpidHex21To40 + supportedpidHex41To60 + supportedpidHex61To80 +
                    supportedpidHex81ToA0;

            OBD2CoreConfiguration obd2CoreConfigs = OBD2CoreConfiguration.getInstance();
            List<String> allPIDsList = obd2CoreConfigs.getAllPIDsList();
            supportedPIDsInBinary = getSupportedPIDsBinaryFromHEX(supportedAllPIDsInHex);
            supportedPIDList = selectSupportedPIDsFromAllPIDList(allPIDsList, supportedPIDsInBinary);


            /*todo****** REMOVE THESE*******/
            obd2CoreConfigs.setSupportedPIDsBool(supportedPIDsInBinary);
            obd2CoreConfigs.setSupportedPIDsList(supportedPIDList);
            /**************/
        } catch (IOException e) {
            String errorMsg = "Error in initializing ELM327Client";
            Log.e(TAG, errorMsg, e);
            throw new OBD2ClientException(errorMsg, e);
        }
    }

    public double getPIDValue (String pid) throws OBD2ClientException {
        try {
            JSONObject pidConfig = OBD2CoreConfiguration.getInstance().getPidConfig();
            JSONObject pidobj = pidConfig.getJSONObject(pid);
            String tosend = pidobj.getString(OBD2CoreConstants.CONF_MODE)
                    + pidobj.getString(OBD2CoreConstants.CONF_PID);
            int respbytes = pidobj.getInt(OBD2CoreConstants.CONF_BYTES);

            Log.d(OBD2CoreConstants.BTMSGTAG, ">>" + tosend);
            byte[] temp = streamCommunicator.sendReceive(tosend);

            if (temp != null && temp.length != 0) {
                String response = new String(temp);

                Log.d(OBD2CoreConstants.APPTAG + "RESP", response);

                String exprString = pidobj
                        .getString(OBD2CoreConstants.CONF_EXPRESSION);

                Expr expr = Parser.parse(exprString);

                for (int j = 0; j < respbytes; j++) {
                    String letter = Character
                            .toString((char) (j + 65));
                    String b = (response.charAt(response.length()
                            - j * 2 - 1 - 1) + "")
                            + response.charAt(response.length() - j
                            * 2 - 1);
                    int ib = Integer.parseInt(b, 16);
                    Variable.make(letter).setValue(ib);
                }

                double exprValue = expr.value();
                Log.d("EXPR", pid + ":" + exprValue);
                return exprValue;
            } else {
                String errorMsg = "Error getting PID value " + pid;
                Log.e(TAG, errorMsg);
                throw new OBD2ClientException(errorMsg);
            }
        } catch (SyntaxException | IOException | JSONException e) {
            String errorMsg = "Error getting PID value " + pid;
            Log.e(TAG, errorMsg, e);
            throw new OBD2ClientException(errorMsg, e);
        }
    }

    @Override
    public boolean[] getSupportedPIDsInBinary() {
        return supportedPIDsInBinary;
    }

    @Override
    public List<String> getSupportedPIDsList() {
        return supportedPIDList;
    }

    @Override
    public void close() throws OBD2ClientException {
        try {
            super.close();
            streamCommunicator.close();
        } catch (IOException e) {
            String errorMsg = "Unable to close StreamCommunicator";
            Log.e(TAG, errorMsg, e);
            throw new OBD2ClientException(errorMsg, e);
        }
    }

    private String sendRequestSupportedPIDsAndValidateResp(String PID) throws FormatException, IOException {
        String supportedPIDReponse = new String(streamCommunicator.sendReceive(PID));
        try {
            String supportedpidHex = supportedPIDReponse.substring(supportedPIDReponse.length() - OBD2CoreConstants.SUPPORTED_PIDS_RESPONSE_LENGTH);
            Log.i(OBD2CoreConstants.BTMSGTAG, supportedpidHex);
            new BigInteger(supportedpidHex, 16); //just check whether it can be converted to an integer..
            return supportedpidHex;
        } catch (Exception e) {
            throw new FormatException();
        }
    }
}

