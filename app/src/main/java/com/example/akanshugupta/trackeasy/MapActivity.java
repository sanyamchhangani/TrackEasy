package com.example.akanshugupta.trackeasy;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


public class MapActivity extends AppCompatActivity implements BottomSectionFragment.BottomSectionListener {


    MyReceiver myReceiver;
    double centroid[] = {0.0,0.0};
    Intent intent;

    /*private MyService myService;
    private boolean bound = false;*/

    private static final String TAG = "check";
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;


    public void setmap(String map){
        TopSectionFragment topFragment = (TopSectionFragment) getFragmentManager().findFragmentById(R.id.fragment2);
        topFragment.setm(map,this);
    }
    public void showmap(String x,String y){
        TopSectionFragment topFragment = (TopSectionFragment) getFragmentManager().findFragmentById(R.id.fragment2);
        /*float xm = 10*Float.parseFloat(x);
        float ym = 10*Float.parseFloat(y);*/
        float xm = Float.parseFloat(x);
        float ym = Float.parseFloat(y);
        topFragment.plot(xm,ym);
    }
    public Double initX(){
        return centroid[0];
    }
    public Double initY(){
        return centroid[1];
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Log.i(TAG,"MapActivity started");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }
        String map_type = getIntent().getStringExtra("map_name");
        setmap(map_type);
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyService.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);
    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            Bundle b = arg1.getExtras();

            centroid = b.getDoubleArray("centroid");
            Log.i(TAG, " inside main " + centroid[0] + " " + centroid[1]);
            showmap(Double.toString(centroid[0]),Double.toString(centroid[1]));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Context context = this;
                    if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                        Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final BluetoothManager bluetoothManager =
                            (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                    mBluetoothAdapter = bluetoothManager.getAdapter();
                    if (mBluetoothAdapter == null) {
                        Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this,MyService.class);

                        return;
                    }

                    if (!mBluetoothAdapter.isEnabled()) {
                        if (!mBluetoothAdapter.isEnabled()) {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        }
                    }
                    Log.i(TAG,"service started");

                    intent = new Intent(this,MyService.class);
                    startService(intent);

                    /*Intent intent = new Intent(this, MyService.class);
                    bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);*/

                    // Permission granted, yay! Start the Bluetooth device scan.
                } else {
                    // Alert the user that this application requires the location permission to perform the scan.
                }
            }
        }
    }


    /*@Override
    protected void onStop() {
        super.onStop();
        // Unbind from service
        if (bound) {
            myService.setCallbacks(null); // unregister
            unbindService(serviceConnection);
            bound = false;
        }
    }

     //Callbacks for service binding, passed to bindService()
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            MyService.LocalBinder binder = (MyService.LocalBinder) service;
            myService = binder.getService();
            bound = true;
            myService.setCallbacks(MainActivity.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };*/


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(intent);
    }
}
