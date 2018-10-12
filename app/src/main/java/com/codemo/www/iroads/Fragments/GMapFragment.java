package com.codemo.www.iroads.Fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codemo.www.iroads.Database.SensorData;
import com.codemo.www.iroads.MainActivity;
import com.codemo.www.iroads.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * A simple {@link Fragment} subclass.
 */
public class GMapFragment extends Fragment implements OnMapReadyCallback {


    private static final String TAG = "GMapFragment";
    private static MainActivity activity;
    private static GoogleMap gmap;
    private static Marker marker;

    public GMapFragment() {
        // Required empty public constructor
    }

    public static void setActivity(MainActivity Activity) {
        activity = Activity;
    }

    public static void updateLocation() {
        double lat = Double.parseDouble(SensorData.getMlat());
        double lon = Double.parseDouble(SensorData.getMlon());

        if (lat != 0.0) {
            LatLng loc = new LatLng(lat, lon);
            float zoomLevel;

            if (marker == null) {
                marker = gmap.addMarker(new MarkerOptions()
                        .position(loc)
                        .title("You are Here!")
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker)));
//            marker.showInfoWindow();
            } else {
                marker.setPosition(loc);
            }
            zoomLevel = 15.0f;//This goes up to 21

            gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, zoomLevel));
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateLocation();
            }
        });
        FloatingActionButton infoBtn = (FloatingActionButton) view.findViewById(R.id.infoBtn);
        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Visit iroads.projects.mrt.ac.lk for more info.", Snackbar.LENGTH_LONG)
                        .setAction("go !", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent browserIntent = new
                                        Intent(Intent.ACTION_VIEW,
                                        Uri.parse(getString(R.string.page_address)));
                                startActivity(browserIntent);

                            }
                        }).show();
            }
        });
        Log.d("rht", "aaaaaaaaaaaaaaaaaaaa.....map fragment created....aaaaaaaaaaaaaaaaaaaaaa***");
        return view;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.initMap().getMapAsync(this);
//        NavigationHandler.navigateTo("mapFragment");

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        gmap = googleMap;
        Log.d("ssssssssssssssssssssss", "sssssssssssssssssssssssssssssssssss");
        marker = null;
        float zoomLevel = 7.0f;
        LatLng sri_lanka = new LatLng(8.068590, 80.654578);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sri_lanka, zoomLevel));

    }

}
