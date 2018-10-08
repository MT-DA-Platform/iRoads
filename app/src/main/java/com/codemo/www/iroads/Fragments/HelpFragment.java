package com.codemo.www.iroads.Fragments;


import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codemo.www.iroads.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class HelpFragment extends Fragment {


    public HelpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_help, container, false);
        TextView texiew = (TextView) view.findViewById(R.id.texiew);
        texiew.setMovementMethod(LinkMovementMethod.getInstance());
        TextView texView = (TextView) view.findViewById(R.id.texView);
        texView.setMovementMethod(LinkMovementMethod.getInstance());
        TextView tAndC = (TextView) view.findViewById(R.id.tAndC);
        tAndC.setMovementMethod(LinkMovementMethod.getInstance());
        TextView textPrivacy = (TextView) view.findViewById(R.id.textPrivacy);
        textPrivacy.setMovementMethod(LinkMovementMethod.getInstance());
        return view;
    }

}
