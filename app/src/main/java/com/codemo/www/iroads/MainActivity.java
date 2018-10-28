package com.codemo.www.iroads;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codemo.www.iroads.Database.DatabaseHandler;
import com.codemo.www.iroads.Database.SensorData;
import com.codemo.www.iroads.Fragments.GMapFragment;
import com.codemo.www.iroads.Fragments.GraphFragment;
import com.codemo.www.iroads.Fragments.HelpFragment;
import com.codemo.www.iroads.Fragments.HomeFragment;
import com.codemo.www.iroads.Fragments.SettingsFragment;
import com.codemo.www.iroads.Reorientation.ReorientationType;
import com.github.javiersantos.appupdater.AppUpdater;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapFragment;
import com.pathsense.android.sdk.location.PathsenseLocationProviderApi;
import com.vatichub.obd2.OBD2CoreConfiguration;
import com.vatichub.obd2.OBD2CoreConstants;
import com.vatichub.obd2.OBD2EventManager;
import com.vatichub.obd2.api.OBD2EventListener;
import com.vatichub.obd2.bean.OBD2Event;
import com.vatichub.obd2.connect.BTServiceCallback;
import com.vatichub.obd2.connect.bt.BluetoothCommandService;
import com.vatichub.obd2.realtime.OBD2SiddhiAgentManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener, OBD2EventListener {

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_OK = 200;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final String CAR_CONNECTED = "car_connected";
    public static final String CAR_CONNECTED_STATUS = "car_connected";
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT_PLUS_CONNECT_DEVICE = 5;
    private static final String TAG = "MainActivity";
    private static boolean replicationStopped = true;
    private static ProgressBar spinnerObd;
    private static ProgressBar spinnerSave;
    private static boolean autoSaveON = true;
    private static ImageButton saveBtn;
    private static ImageButton bConnectBtn;
    private static ImageButton activeBtn;
    private static MainActivity activity;
    private static int counter;
    private static int checkCounter;
    private FragmentManager manager;
    private FragmentTransaction transaction;
    private MapFragment mapFragment;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    private com.google.android.gms.location.LocationListener listener;
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    private LocationManager locationManager;
    private BluetoothAdapter mBluetoothAdapter;
    private IroadsConfiguration gconfigs;
    private Timer btconnectAttemptScheduler;
    private boolean attemptScheduled = false;
    private int btConnectAttemptsRemaining = 5;
    private boolean proceedAttemptCycle = true;
    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothCommandService.STATE_CONNECTED_ELM:

//                            indicatorELM.setImageResource(R.drawable.elm_ok);
                            if (msg.getData() != null && msg.getData().containsKey(DEVICE_NAME)) {
                                String mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                                Toast.makeText(getApplicationContext(), "Connected to "
                                        + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                            }

                            //resetting bluetooth connection re-attempt tasks
                            String attempts = gconfigs.getSetting("max_bt_attempts", Constants.MAX_BT_CONNECT_ATTEMPTS_DEFAULT + "");
                            btConnectAttemptsRemaining = Integer.parseInt(attempts);
                            proceedAttemptCycle = true;

                            break;
                        case BluetoothCommandService.STATE_CONNECTED_VINLI:
//                            indicatorELM.setImageResource(R.drawable.vinli_ok);
                            break;

                        case BluetoothCommandService.STATE_CONNECTED_CAR:
                            int carConnectedStatus = msg.getData().getInt(CAR_CONNECTED_STATUS);
                            if (carConnectedStatus == MESSAGE_OK) {
//                                indicatorCar.setImageResource(R.drawable.car_ok);
                                // tripLogCalculator.resetTrip();
                            } else {
//                                indicatorCar.setImageResource(R.drawable.car_no);
                            }
                            break;
                        case BluetoothCommandService.STATE_CONNECTING:
                            break;
                        case BluetoothCommandService.STATE_LISTEN:
                        case BluetoothCommandService.STATE_NONE:
                            //tripLogCalculator.endTrip();

                            boolean autoconnect = Boolean.valueOf(gconfigs.getSetting("bt_autoconnect", Constants.BT_AUTOCONNECT_DEFAULT + ""));
                            if (autoconnect && proceedAttemptCycle && !attemptScheduled) {
                                String lastSuccessfulConnectBTAddr = OBD2CoreConfiguration.getInstance().getSetting(OBD2CoreConstants.LAST_CONNECTED_BT_ADDR);
                                btconnectAttemptScheduler.cancel();
                                btconnectAttemptScheduler = new Timer();
                                TimerTask attemptTask = new BTConnectAttemptTask(lastSuccessfulConnectBTAddr, btconnectAttemptScheduler);
                                btconnectAttemptScheduler.schedule(attemptTask, 10000);
                                attemptScheduled = true;
                            }
                            break;
                    }
                    break;
            }
        }
    };
    private BluetoothCommandService mCommandService;
    private Menu mainMenu;
    private Context context;
    private BottomNavigationView navigation;
    private boolean inHome = true;
    private Icon homeIcon;
    private Thread fakethread;
    private DatabaseHandler dbHandler;
    private boolean gpsEnabled;
    private Runnable handlerTask;
    private PathsenseLocationProviderApi api;
    private AlertDialog dialog;
    private AlertDialog.Builder dialogBuilder;
    private boolean updateChecked = false;

    public static boolean isReplicationStopped() {
        return replicationStopped;
    }

    public static void setReplicationStopped(boolean replicationStarteds) {
        replicationStopped = replicationStarteds;
    }

    public static void startSaving() {
        spinnerSave.setVisibility(View.VISIBLE);
        saveBtn.setEnabled(false);
    }

    public static void stopSaving() {
        spinnerSave.setVisibility(View.GONE);
        saveBtn.setEnabled(true);
    }

    @SuppressLint("WrongConstant")
    private void blinkEffect() {
        ObjectAnimator anim = ObjectAnimator.ofInt(activeBtn, "alpha", Color.TRANSPARENT, Color.WHITE, Color.WHITE,
                Color.TRANSPARENT);
        anim.setDuration(2000);
        anim.setEvaluator(new ArgbEvaluator());
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        anim.start();
    }

    public static boolean isAutoSaveON() {
        return autoSaveON;
    }

    public static void setAutoSaveON(boolean autoSaveON) {
        MainActivity.autoSaveON = autoSaveON;
    }

    public static int getCheckCounter() {
        return checkCounter;
    }

    public static void setCheckCounter(int checkCounter) {
        MainActivity.checkCounter = checkCounter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        MainActivity.activity = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        manager = getSupportFragmentManager();
        transaction = manager.beginTransaction();

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
//        initiate fragment objects

//        open home fragment
        transaction.add(R.id.contentLayout, new GMapFragment(), "mapFragment");
        transaction.add(R.id.contentLayout, new GraphFragment(), "graphFragment");
        transaction.add(R.id.contentLayout, new HomeFragment(), "homeFragment");
        transaction.add(R.id.contentLayout, new SettingsFragment(), "settingsFragment");
        transaction.add(R.id.contentLayout, new HelpFragment(), "helpFragment");
        transaction.commit();
//
        GMapFragment.setActivity(this);
        GraphFragment.setActivity(this);
        NavigationHandler.setManager(manager);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        checkLocation();

        gconfigs = IroadsConfiguration.getInstance();
        gconfigs.initApplicationSettings(this, mHandler);
        context = this;

        OBD2CoreConfiguration.init(this);

        //Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
        }

        //Register Event Listeners
        OBD2EventManager obd2EventManager = OBD2EventManager.getInstance();
        obd2EventManager.registerOBD2EventListener(OBD2SiddhiAgentManager.getInstance());
        obd2EventManager.registerOBD2EventListener(this);

        ArrayList<String> pids = gconfigs.getPidsSetting();
        for (int i = 0; i < pids.size(); i++) {
            gconfigs.getDashboardPIDsSet().add(pids.get(i));
        }

        HomeController.setMainActivity(this);
        SettingsController.setMainActivity(this);
        gconfigs.updateQueryPIDsList();

        saveBtn = (ImageButton) findViewById(R.id.saveBtn);
        saveBtn.setColorFilter(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorWhite));
        saveBtn.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAutoSaveON()) {
                    Toast.makeText(getApplicationContext(), "Auto Save is currently enabled", Toast.LENGTH_SHORT).show();
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    dbHandler.startReplication();
                    MainActivity.setReplicationStopped(false);
                    startSaving();
                    Toast.makeText(getApplicationContext(), "Sync up Started", Toast.LENGTH_SHORT).show();
                }
            }
        });

        spinnerObd = (ProgressBar) findViewById(R.id.progressBarLoadingObd);
        spinnerObd.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite), android.graphics.PorterDuff.Mode.MULTIPLY);
        spinnerObd.setVisibility(View.GONE);
        spinnerSave = (ProgressBar) findViewById(R.id.progressBarLoadingSave);
        spinnerSave.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite), android.graphics.PorterDuff.Mode.MULTIPLY);
        spinnerSave.setVisibility(View.GONE);

        bConnectBtn = (ImageButton) findViewById(R.id.obdBtn);
        bConnectBtn.setColorFilter(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorWhite));
        bConnectBtn.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View view) {
//                spinnerObd.setVisibility(View.VISIBLE);
                onConnectBtn();

            }
        });
        activeBtn = (ImageButton) findViewById(R.id.activeBtn);
        blinkEffect();
        dbHandler = new DatabaseHandler(getApplicationContext());

        checkUpdates();

        if (checkAndRequestPermissions()) {
            saveDeviceId();
        }

        api = PathsenseLocationProviderApi.getInstance(getApplicationContext());
        api.requestInVehicleLocationUpdates(PathsenseInVehicleReceiver.class);
        api.requestActivityUpdates(PathsenseInVehicleReceiver.class);

    }

    @Override
    protected void onDestroy() {
        api.destroy();
        super.onDestroy();
    }

    public void saveDeviceId() {
        String serial = Build.SERIAL;
        Log.d("TAG", "inside DeviceID");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }
            serial = Build.getSerial();
        }
        String device_name = Build.BRAND + " " + Build.MODEL;
        Log.d("TAG", "Device properties: " + device_name + " " + serial);
        SensorData.setDeviceId(String.valueOf(serial.hashCode()));
        SensorData.setModel(device_name);
        Log.d(TAG, "--------------- DeviceId --------- /// " + SensorData.getDeviceId());
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
            NavigationHandler.navigateTo("mapFragment");
        } else if (id == R.id.nav_chart) {
            NavigationHandler.navigateTo("graphFragment");
        } else if (id == R.id.nav_obd) {
            NavigationHandler.navigateTo("homeFragment");
        } else if (id == R.id.nav_settings) {
            NavigationHandler.navigateTo("settingsFragment");
        } else if (id == R.id.nav_help) {
            NavigationHandler.navigateTo("helpFragment");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void startTimer() {
        Handler handler = new Handler();
        handlerTask = new Runnable() {
            @Override
            public void run() {
                checkCounter++;
                if (getCheckCounter() > 10) {
                    if (!isGpsEnabled()) {
                        checkLocation();
                    }
                    checkUpdates();
                    setCheckCounter(0);
                }
                if (GraphFragment.isStarted()){
                    activeBtn.setVisibility(View.VISIBLE);
                }else{
                    activeBtn.setVisibility(View.INVISIBLE);
                }
                if (isAutoSaveON() && isInternetAvailable()) {
                    Log.d(TAG, "--------------- internet --------- /// " + isInternetAvailable());
                    if (isReplicationStopped()) {
                        counter++;
                        if (counter > 8) {
                            dbHandler.startReplication();
                            startSaving();
                            setReplicationStopped(false);
                            counter = 0;
                        }
                    } else {
                        Log.d(TAG, "--------------- replicating ............. --------- /// ");
                    }
                }
                if (isReplicationStopped()) {
                    stopSaving();
                }
                handler.postDelayed(handlerTask, 5000);
            }
        };
        handlerTask.run();
    }

    public boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        /**
         * setting reorientation mechanism
         */
        SensorDataProcessor.setReorientation(ReorientationType.Nericel);
        new MobileSensors(this);
        startTimer();
    }

    public MapFragment initMap() {
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        return mapFragment;
    }

    public void onLocationChanged(Location location) {
        HomeController.updateLocation(location);
        MobileSensors.updateLocation(location);
    }

    private boolean checkLocation() {
        setGpsEnabled(isLocationEnabled());
        if (!isGpsEnabled())
            showAlert();
        return isGpsEnabled();
    }

    private void showAlert() {
        if(dialog != null){
            if(dialog.isShowing()){
                return;
            }
        }
        dialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Enable Location")
                .setMessage("To continue, Please turn on device location.")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialogBuilder.setCancelable(false);
        try {
            dialog = dialogBuilder.show();
        }catch (Exception e){
            Log.d(TAG, "Exception occured"+e.getMessage());
        }
    }

    private boolean isLocationEnabled() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }

        startLocationUpdates();
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLocation == null) {
            startLocationUpdates();
        }
        if (mLocation != null) {
            HomeController.updateLocation(mLocation);
        } else {
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        // If BT is not on, request that it be enabled.
        // setupCommand() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
        }
        // otherwise set up the command service
        else {
            if (mCommandService == null) {
                setupCommand();
            }

        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            Log.d("iemi exception", "--->>>> imei no permission granted ------------------------");
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
        Log.d("reque", "--->>>>");
    }

    private void setupCommand() {
        // Initialize the BluetoothChatService to perform bluetooth connections
        mCommandService = new BluetoothCommandService(new BTServiceConnectionHandler());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("ActivityResult=====", "Result received=======================================" + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    boolean manualConnect = data.getExtras().getBoolean(DeviceListActivity.MANUAL_CONNECT);
                    if (manualConnect) { //user manually try to connect the app to a device
                        proceedAttemptCycle = false; //stop auto connecting
                    }

                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    mCommandService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupCommand();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, context.getString(R.string.bluetooth_not_enabled), Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_ENABLE_BT_PLUS_CONNECT_DEVICE:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupCommand();
                    // Launch the DeviceListActivity to see devices and do scan
                    Intent serverIntent = new Intent(this, DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, context.getString(R.string.bluetooth_not_enabled), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void receiveOBD2Event(OBD2Event e) {
        try {
            JSONObject realTimedata = e.getEventData().getJSONObject("obd2_real_time_data");
            JSONObject speedObject = realTimedata.getJSONObject("obd2_speed");
            JSONObject rpmObject = realTimedata.getJSONObject("obd2_engine_rpm");
            Double speed = speedObject.getDouble("value");
            Double rpm = rpmObject.getDouble("value");
            SensorData.setMobdRpm(Double.toString(rpm));
            SensorData.setMobdSpeed(Double.toString(speed));
            HomeController.updateOBD2Data(speed.intValue(), rpm.intValue());
        } catch (JSONException e1) {
            Log.d("OBD2DATA", e1.getMessage());

        }

    }

    public void onConnectBtn() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT_PLUS_CONNECT_DEVICE);
        } else {
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
        }
    }

    public boolean isGpsEnabled() {
        return gpsEnabled;
    }

    public void setGpsEnabled(boolean gpsEnabled) {
        this.gpsEnabled = gpsEnabled;
    }

    private boolean checkAndRequestPermissions() {
        int phonestate = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);
        int location = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (phonestate != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.READ_PHONE_STATE);
        }
        if (location != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 1);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                Map<String, Integer> perms = new HashMap<>();
                perms.put(android.Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
//                perms.put(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);

                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (
                            perms.get(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                                    perms.get(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//                            && perms.get(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            ) {
                        Log.d(TAG, "phone & location services permission granted");
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ");
                        if (
                                ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_PHONE_STATE) ||
                                        ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
//                              ||  ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                ) {
                            showDialogOK("Some Permissions are required to use this application",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    dialog.dismiss();
                                                    break;
                                            }
                                        }
                                    });
                        } else {
                            explain("You need to give some mandatory permissions to continue. Do you want to go to app settings?");
                        }
                    }
                }
            }
        }
        saveDeviceId();
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    private void explain(String msg) {
        final android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(this);
        dialog.setMessage(msg)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        dialog.create().dismiss();
                        finish();
                    }
                });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            showExitAlert();
//            super.onBackPressed();
        }
    }

    private void showExitAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Confirmation")
                .setMessage("Do you want to exit?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        api.destroy();
                        android.os.Process.killProcess(android.os.Process.myPid());
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    public boolean isUpdateChecked() {
        return updateChecked;
    }

    public void setUpdateChecked(boolean updateChecked) {
        this.updateChecked = updateChecked;
    }

    public void checkUpdates() {
        if(!isUpdateChecked()){
            if(isInternetAvailable()) {
                AppUpdater appUpdater = new AppUpdater(this)
                        .setButtonDoNotShowAgain(null);
                appUpdater.start();
                setUpdateChecked(true);
            }
        }
    }

    class BTConnectAttemptTask extends TimerTask {

        private String address;
        private Timer scheduler;

        public BTConnectAttemptTask(String address, Timer scheduler) {
            this.address = address;
            this.scheduler = scheduler;
        }

        @Override
        public void run() {
            attemptScheduled = false;
            scheduler.cancel();

            if (btConnectAttemptsRemaining > 0) {
                btConnectAttemptsRemaining--;
            } else {
                proceedAttemptCycle = false;
            }

            boolean autoconnect = Boolean.valueOf(gconfigs.getSetting("bt_autoconnect", Constants.BT_AUTOCONNECT_DEFAULT + ""));

            if (mCommandService != null && !gconfigs.isExitting()) {
                int mState = mCommandService.getState();
                if (autoconnect && proceedAttemptCycle && (mState == BluetoothCommandService.STATE_NONE || mState == BluetoothCommandService.STATE_LISTEN)) {
                    Intent data = new Intent();
                    data.putExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS, address);
                    data.putExtra(DeviceListActivity.MANUAL_CONNECT, false);
                    MainActivity.this.onActivityResult(REQUEST_CONNECT_DEVICE, Activity.RESULT_OK, data);
                }
            }
        }
    }

    class BTServiceConnectionHandler implements BTServiceCallback {

        @Override
        public void onStateChanged(int oldState, int newState, BluetoothDevice device) {
            //mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, newState, -1).sendToTarget();
        }

        @Override
        public void onConnecting(BluetoothDevice device) {
            Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(MainActivity.TOAST, "Connecting to " + device.getName());
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }

        @Override
        public void onConnectedDevice(BluetoothDevice device) {
            Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE,
                    BluetoothCommandService.STATE_CONNECTED_ELM, -1);
            Bundle bundle = new Bundle();
            bundle.putString(DEVICE_NAME, device.getName());
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }

        @Override
        public void onConnectedCar() {
            Message msg = mHandler
                    .obtainMessage(MainActivity.MESSAGE_STATE_CHANGE,
                            BluetoothCommandService.STATE_CONNECTED_CAR, -1);
            Bundle bundle = new Bundle();
            bundle.putInt(MainActivity.CAR_CONNECTED, MainActivity.MESSAGE_OK);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }

        @Override
        public void onDisconnected(BluetoothDevice device, Map<String, Object> args) {
            mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE,
                    BluetoothCommandService.STATE_NONE, -1).sendToTarget();
            gconfigs.sendToastToUI("Disconnected! Reason  : " + args.get(BluetoothCommandService.ARGS_REASON));
        }

        @Override
        public void onConnectionFailed(BluetoothDevice device) {
            Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(MainActivity.TOAST, "Unable to connect device " + device.getName());
            msg.setData(bundle);
            mHandler.sendMessage(msg);
            mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE,
                    BluetoothCommandService.STATE_NONE, -1).sendToTarget();
        }

        @Override
        public void onConnectionLost(BluetoothDevice device) {
            Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(MainActivity.TOAST, "Connection was lost with device " + device.getName());
            msg.setData(bundle);
            mHandler.sendMessage(msg);
            mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE,
                    BluetoothCommandService.STATE_NONE, -1).sendToTarget();
        }

    }
}
