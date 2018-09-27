package com.codemo.www.iroads.Fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.codemo.www.iroads.Database.SensorData;
import com.codemo.www.iroads.GraphController;
import com.codemo.www.iroads.MainActivity;
import com.codemo.www.iroads.NavigationHandler;
import com.codemo.www.iroads.R;
import com.codemo.www.iroads.Reorientation.ReorientationType;
import com.codemo.www.iroads.SensorDataProcessor;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    private Switch saving;
    private SeekBar frequencyBar;
    private RadioButton nericelMechanism, wolverineMechanism;
    private static MainActivity mainActivity;
    private TextView sampleRate;


    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        saving = (Switch) view.findViewById(R.id.savingSwitch);
        sampleRate = (TextView) view.findViewById(R.id.sampleRate);
        frequencyBar = (SeekBar) view.findViewById(R.id.frequencyBar);
        nericelMechanism = (RadioButton) view.findViewById(R.id.nericel);
        wolverineMechanism = (RadioButton) view.findViewById(R.id.wolverine);

        RadioGroup reorientaationTypeGroup=(RadioGroup) view.findViewById(R.id.reorientationType);

        reorientaationTypeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId==R.id.nericel){
                    SensorDataProcessor.setReorientation(ReorientationType.Nericel);
                }

                else if (checkedId==R.id.wolverine){
                    SensorDataProcessor.setReorientation(ReorientationType.Wolverine);
                }

            }
        });

        saving.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                Log.d(TAG, "settings auto save changed");
                if(b){
                    Log.d(TAG, "settings auto save enabled");
                    HomeFragment.setAutoSaveON(true);
                    saving.getThumbDrawable().setColorFilter(ContextCompat.getColor(getMainActivity().getApplicationContext(), R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
                }else{
                    Log.d(TAG, "settings auto save disabled");
                    HomeFragment.setAutoSaveON(false);
                    saving.getThumbDrawable().setColorFilter(ContextCompat.getColor(getMainActivity().getApplicationContext(), R.color.colorDisabledThumb), PorterDuff.Mode.MULTIPLY);
                }
            }
        });

        frequencyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int time = 1000/((i+1)*10);
                sampleRate.setText((i+1)*10 +" Hz");
                GraphController.setSleepTime(time);
                Log.d(TAG, "current state: " + time);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



//        saving.setColorFilter(ContextCompat.getColor(mainActivity.getApplicationContext(), R.color.colorPrimary));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavigationHandler.navigateTo("homeFragment");
    }

    public static MainActivity getMainActivity() {
        return mainActivity;
    }

    public static void setMainActivity(MainActivity mainActivity) {
        SettingsFragment.mainActivity = mainActivity;
    }


}
