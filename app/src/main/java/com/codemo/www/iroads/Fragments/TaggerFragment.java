package com.codemo.www.iroads.Fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.codemo.www.iroads.Database.SensorData;
import com.codemo.www.iroads.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class TaggerFragment extends Fragment {
    private String journeyId = "";
    private ArrayList<String> timeArray = new ArrayList<>();

    public TaggerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_tagger, container, false);
        Button idButton = (Button) view.findViewById(R.id.idButton);
        idButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                askJourneyName();
            }
        });
        final Animation animAlpha = AnimationUtils.loadAnimation(getContext(), R.anim.ripple);
        Button addPotholeButton = (Button) view.findViewById(R.id.addPotholeButton);
        addPotholeButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(animAlpha);
                if(getJourneyId() == ""){
                    askJourneyName();
                }else{
                    getTimeArray().add("P , " + String.valueOf(System.currentTimeMillis())+" , "+ SensorData.getMlat()+" , "+ SensorData.getMlon());
                    Toast.makeText( getContext(),"Pothole Added", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Button addBumpButton = (Button) view.findViewById(R.id.addBumpButton);
        addBumpButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(animAlpha);
                if(getJourneyId() == ""){
                    askJourneyName();
                }else{
                    getTimeArray().add("B , " + String.valueOf(System.currentTimeMillis())+" , "+ SensorData.getMlat()+" , "+ SensorData.getMlon());
                    Toast.makeText( getContext(),"Bump Added", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Button saveButton = (Button) view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getTimeArray().isEmpty()){
                    Toast.makeText( getContext(),"Nothing to write", Toast.LENGTH_SHORT).show();
                }else{
                    StringBuilder string = new StringBuilder();
                    for (String str : getTimeArray() ) {
                        string.append(str.toString()+"\n");
                    }
                    writeToFile(String.valueOf(string));
                }
            }
        });
        return view;
    }

    public void writeToFile(String text){
        Log.d("DJourneyID=====", getJourneyId());
        String folder_main = "iRoads";

        File f = new File("sdcard/", folder_main);
        if (!f.exists()) {
            f.mkdirs();
        }
        File logFile = new File("sdcard/iRoads/log_"+getJourneyId()+".txt");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
            setJourneyId("");
            getTimeArray().clear();
            Toast.makeText( getContext(),"Write successful!", Toast.LENGTH_SHORT).show();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void askJourneyName(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Journey Name");

        // Set up the input
        final EditText input = new EditText(getContext());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = input.getText().toString();
                if(text != null && text.trim().length() == 0){
                    Toast.makeText( getContext(),"Journey Name can not be Empty", Toast.LENGTH_SHORT).show();
                }else{
                    setJourneyId(text);
                    Toast.makeText( getContext(),"JourneyId Saved", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public String getJourneyId() {
        return journeyId;
    }

    public void setJourneyId(String journeyId) {
        this.journeyId = journeyId;
    }

    public ArrayList<String> getTimeArray() {
        return timeArray;
    }

}
