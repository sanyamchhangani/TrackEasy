package com.example.akanshugupta.trackapp;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;


public class MyService extends Service {

    long startTime;
    private BluetoothLeScanner mBluetoothleScanner;
    private static final String copey = "74:DA:EA:AF:EB:B2";
    private static final String dopey = "74:DA:EA:B3:B0:42";
    private static final String popey = "04:A3:16:07:56:81";
    private static final int[][] beacon_position = {{13,23},{26,23},{20,31}};
    Stack<Integer>[] rssi_list = new Stack[3];
    private final HashMap<String, ArrayList<Integer>> mac_address = new HashMap<String, ArrayList<Integer>>();
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    public static int[] RSSI;
    public static int i;
    private static final String TAG = "com.example.akanshugupta.trackapp";
    private static final String TAG1 = "check";
    final static String MY_ACTION = "MY_ACTION";
    public ArrayList filters;
    public ScanSettings settings;
    @Override
    public void onCreate() {
        super.onCreate();
        settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        filters = new ArrayList<>();
        String[] filterlist = {
                copey,
                dopey,
                popey//... some 20 more addresses
        };
        for (int i=0; i< filterlist.length ; i++) {
            ScanFilter filter = new ScanFilter.Builder().setDeviceAddress(filterlist[i]).build();
            filters.add(filter);
            //Log.v("Filter: "," "+ filters.get(i).getDeviceAddress());
        }
        mHandler = new Handler();
        ArrayList<Integer>[] position = new ArrayList[3];
        for(int i=0;i<3;i++){
            rssi_list[i] = new Stack<Integer>();
            position[i] = new ArrayList<Integer>();
            for(int j=0;j<2;j++){
                position[i].add(beacon_position[i][j]);
            }
        }

        mac_address.put(copey,position[0]);
        mac_address.put(dopey,position[1]);
        mac_address.put(popey,position[2]);
        startTime = System.currentTimeMillis();

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        mBluetoothleScanner = mBluetoothAdapter.getBluetoothLeScanner();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                mBluetoothleScanner.startScan(filters, settings, mScanCallback);
                //mBluetoothAdapter.startLeScan(mLeScanCallback);
                //scanLeDevice(true);
                Timer t = new Timer();
                //Set the schedule function and rate
                t.scheduleAtFixedRate(new TimerTask() {

                                          @Override
                                          public void run() {
                                              Log.i(TAG1,mLeDeviceListAdapter.mLeDevices.toString());
                                              //Called each time when 1000 milliseconds (1 second) (the period parameter)
                                              int i = 0;
                                              double[][] positions = {{13,23},{26,23},{20,31}};
                                              double[] distances = {0.0,0.0,0.0};

                                              Log.i(TAG1, " inside check" + mLeDeviceListAdapter.mLeDevices.toString());
                                              if (mLeDeviceListAdapter.RSSI.size() == 3) {
                                                  for(int k=0;k<3;k++){
                                                      for(int j=0;j<2;j++) {
                                                          positions[k][j] = mac_address.get(mLeDeviceListAdapter.getDevice(k).getAddress()).get(j);
                                                      }
                                                  }
                                                  for (i = 0; i < 3; i++) {
                                                      int r = (rssi_list[i].pop()+rssi_list[i].pop()+rssi_list[i].pop()+rssi_list[i].pop())/4;
                                                      rssi_list[i].clear();
                                                      distances[i] = -0.367 * r - 19.39;
                                                      Log.i(TAG1," "+r);
                                                  }
                                              }

                                              else{
                                                  Log.i(TAG , mLeDeviceListAdapter.toString());
                                                  Log.i(TAG1," three beacons are not available");
                                              }

                                              NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
                                              LeastSquaresOptimizer.Optimum optimum = solver.solve();
                                              // the answer
                                              double[] centroid = optimum.getPoint().toArray();
                                              Log.i(TAG1," "+centroid[0]+" "+centroid[1]);
                                              Intent intent = new Intent();
                                              intent.setAction(MY_ACTION);
                                              Bundle bundle = new Bundle();
                                              bundle.putDoubleArray("centroid",centroid);
                                              intent.putExtras(bundle);
                                              sendBroadcast(intent);
                                              Log.i(TAG," "+System.currentTimeMillis());
                                              //mLeDeviceListAdapter.clear();
                                              mLeDeviceListAdapter = new LeDeviceListAdapter();
                                              for(int t=0;t<3;t++){
                                                  rssi_list[t] = new Stack<Integer>();
                                               //   position[i] = new ArrayList<Integer>();
                                              }
                                          }


                                      },
                                    //Set how long before to start calling the TimerTask (in milliseconds)
                        0,
                            //Set the amount of time between each execution (in milliseconds)
                        7000);

            }
        };
        Thread t = new Thread(r);
        t.start();
        return START_NOT_STICKY;
    }



    public void onDestroy(){
        Log.i(TAG,"destroy method called");
        mBluetoothleScanner.stopScan(mScanCallback);

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
        public int getPosition(BluetoothDevice device){
            return mLeDevices.indexOf(device);

        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
            RSSI.clear();
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


    private ScanCallback mScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(final int callbacktype, final ScanResult result) {
                    new MainActivity().runOnUiThread(new Runnable()  {
                        @Override
                        public void run() {
                            BluetoothDevice device = result.getDevice();
                            int rssi = result.getRssi();
                            //if(mac_address.containsKey(device.getAddress())) {
                                if(!mLeDeviceListAdapter.mLeDevices.contains(device)) {
                                    mLeDeviceListAdapter.addDevice(device, rssi);
                                    mLeDeviceListAdapter.notifyDataSetChanged();
                                    int p = mLeDeviceListAdapter.getPosition(device);
                                    Log.i(TAG1, mLeDeviceListAdapter.mLeDevices.toString());
                                    if(p<3) {
                                        Log.i(TAG1, "device " + device.getAddress() + "  rssi  " + rssi);
                                        rssi_list[p].push(rssi);
                                    }
                                }
                                else{
                                    int p = mLeDeviceListAdapter.getPosition(device);
                                    if(p<3) {
                                        rssi_list[p].push(rssi);
                                    }
                                }
                            //}

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
