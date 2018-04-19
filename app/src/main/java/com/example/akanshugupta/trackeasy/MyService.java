package com.example.akanshugupta.trackeasy;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;


public class MyService extends Service {

   /* public interface ServiceCallbacks {
         void showmap(String x,String y);
    }*/


    long startTime;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    public static int[] RSSI;
    public static int i;
    private static final String TAG = "com.example.akanshugupta.trackapp";
    final static String MY_ACTION = "MY_ACTION";
    //private LinkedList<Device> deviceList;

    //private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

   /* private final IBinder binder = new LocalBinder();
    // Registered callbacks
    private ServiceCallbacks serviceCallbacks;


    // Class used for the client Binder.
    public class LocalBinder extends Binder {
        MyService getService() {
            // Return this instance of MyService so clients can call public methods
            return MyService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setCallbacks(ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }*/


    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();

        startTime = System.currentTimeMillis();
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mLeDeviceListAdapter = new LeDeviceListAdapter();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
                Log.i(TAG,mLeDeviceListAdapter.mLeDevices.toString());
                //scanLeDevice(true);


                Timer t = new Timer();
                //Set the schedule function and rate
                t.scheduleAtFixedRate(new TimerTask() {

                                          @Override
                                          public void run() {
                                              //Called each time when 1000 milliseconds (1 second) (the period parameter)

                                              int i = 0;
                                              double[][] positions = new double[][]{{6,25}, {13.0, 25.0}, {9, 20}};
                                              double[] distances = {0.0,0.0,0.0};
                                              if (mLeDeviceListAdapter.RSSI.size()>=3) {
                                                  for (i = 0; i < 3; i++) {
                                                      distances[i] = -0.367 * mLeDeviceListAdapter.RSSI.get(i) - 21.39;
                                                  }
                                              }

                                              NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
                                              LeastSquaresOptimizer.Optimum optimum = solver.solve();
                                              // the answer
                                              double[] centroid = optimum.getPoint().toArray();
                                              Log.i(TAG," "+centroid[0]+" "+centroid[1]);


                                              Intent intent = new Intent();
                                              intent.setAction(MY_ACTION);
                                              Bundle bundle = new Bundle();
                                              bundle.putDoubleArray("centroid",centroid);
                                              intent.putExtras(bundle);
                                              sendBroadcast(intent);
                                              Log.i(TAG," "+System.currentTimeMillis());
                                          }

                                      },
                                    //Set how long before to start calling the TimerTask (in milliseconds)
                        0,
                            //Set the amount of time between each execution (in milliseconds)
                        5000);

            }
        };
        Thread t = new Thread(r);
        t.start();
        return START_NOT_STICKY;
    }

    public void scan(){

    }

    public void onDestroy(){
        Log.i(TAG,"destroy method called");
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    //invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        //invalidateOptionsMenu();
    }

    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;
        public ArrayList<Integer> RSSI;

        public LeDeviceListAdapter() {
            super();
            RSSI = new ArrayList<Integer>();
            mLeDevices = new ArrayList<BluetoothDevice>();
          //  mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device, int rssi) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
                RSSI.add(rssi);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            return view;
        }
    }


    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    new MapActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device, rssi);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                           // Log.i(TAG, mLeDeviceListAdapter.mLeDevices.toString());

                        }
                    });
                }
            };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
