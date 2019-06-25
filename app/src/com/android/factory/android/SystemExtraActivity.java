package com.android.factory.android;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import com.android.factory.R;
import com.android.factory.TTSBaseActivity;

import java.util.Iterator;
import java.util.List;
import android.os.ServiceManager;
import com.android.factory.reset.ResetActivity;
import com.android.internal.telephony.ITelephony;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.telephony.PhoneConstants;
import static android.location.GpsStatus.GPS_EVENT_SATELLITE_STATUS;

/**
 * System Test (wifi,blue,sim,gps)
 */
public class SystemExtraActivity extends TTSBaseActivity {

    private WifiManager mWifiManager;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean isWifiSuccess;
    private boolean isBlueSuccess;
    private boolean isSimSuccess;
    private boolean isGpsSuccess;
    private boolean mSim1Exist = false;
    private boolean mSim2Exist = false;
    @Override
    protected void initData() {
        String mPlayText = getResources().getString(R.string.start_system);
        if (mSystemTTS != null) {
            mSystemTTS.playText(mPlayText);
        }
        initParams();
    }

    private void initParams() {
        initWifiParams();
        initBluetoothParams();
        initSimParams();
        initGPS();
    }

    private void initSimParams() {
        ITelephony iTelephony = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
        try {
            mSim1Exist = iTelephony.hasIccCardUsingSlotIndex(PhoneConstants.SIM_ID_1);
            mSim2Exist = iTelephony.hasIccCardUsingSlotIndex(PhoneConstants.SIM_ID_2);
            Log.d("system_log","mSim1Exist------------>"+mSim1Exist);
            Log.d("system_log","mSim2Exist------------>"+mSim2Exist);
            if (mSim1Exist || mSim2Exist){
                isSimSuccess = true;
            }
        }catch (RemoteException e){
            Log.d("system_log","RemoteException-RemoteException----------->"+e.getMessage());
        }
    }

    private void initBluetoothParams() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) return;
        mBluetoothAdapter.enable();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mBluetoothStateReceiver, intentFilter);
        mBluetoothAdapter.startDiscovery();
    }

    private void initWifiParams() {
        mWifiManager= (WifiManager)this.getSystemService(WIFI_SERVICE);
        if (mWifiManager == null)return;
        mWifiManager.setWifiEnabled(true);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mWifiStateReceiver, intentFilter);
        mWifiManager.startScan();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_system;
    }

    @Override
    public void handleMsg(Message msg) {
    }

    @Override
    protected void systemTTSComplete() {
        super.systemTTSComplete();
        isTTSComplete = true;
        if (mGlobalHandler != null){
            mGlobalHandler.removeCallbacks(startSystemRunnable);
            mGlobalHandler.postDelayed(startSystemRunnable,5*1000);
        }
    }

    private final Runnable startSystemRunnable = new Runnable() {
        @Override
        public void run() {
            speechTestResult();
        }
    };

    private void speechTestResult() {
        Log.d("system_log","isWifiSuccess------------>"+isWifiSuccess);
        Log.d("system_log","isBlueSuccess------------>"+isBlueSuccess);
        Log.d("system_log","isSimSuccess------------>"+isSimSuccess);
        Log.d("system_log","isGpsSuccess------------>"+isGpsSuccess);
        if (isSystemTestComplete()){
            mSystemTTS.playText(getResources().getString(R.string.start_system_success));
        }else{
            //mSystemTTS.playText(getResources().getString(R.string.start_system_fail));
            if (!isWifiSuccess)
                mSystemTTS.playText(getResources().getString(R.string.start_system_wifi_fail));
            if(!isBlueSuccess)
                mSystemTTS.playText(getResources().getString(R.string.start_system_blue_fail));
            if(!isSimSuccess)
                mSystemTTS.playText(getResources().getString(R.string.start_system_sim_fail));
            if(!isGpsSuccess)
                mSystemTTS.playText(getResources().getString(R.string.start_system_gps_fail));
        }
    }

    public boolean isSystemTestComplete(){
        return isWifiSuccess && isBlueSuccess && isSimSuccess;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWifiStateReceiver != null){
            unregisterReceiver(mWifiStateReceiver);
            mWifiStateReceiver = null;
        }
        if (mBluetoothStateReceiver != null){
            unregisterReceiver(mBluetoothStateReceiver);
            mBluetoothStateReceiver = null;
        }
        mGlobalHandler.removeCallbacks(startSystemRunnable);
        closeReceiver();
        mLocationManager.removeUpdates(locationListener);
    }

    private void closeReceiver() {
        if (mBluetoothAdapter != null){
            mBluetoothAdapter.disable();
            mBluetoothAdapter = null;
        }
        if (mWifiManager != null){
            mWifiManager.setWifiEnabled(false);
        }
    }

    private BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("system_log", "onReceive wifi------------>");
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                List<ScanResult> scanResults = mWifiManager.getScanResults();
                for (int i = scanResults.size() - 1; i >= 0; i--) {
                    String mCurrentSSID = scanResults.get(i).SSID;
                    Log.d("system_log", "mCurrentSSID------------>" + mCurrentSSID);
                    if (mCurrentSSID != null && !"".equals(mCurrentSSID)) {
                        isWifiSuccess = true;
                    }
                }
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                Log.d("system_log", "onReceive wifi--WIFI_STATE_CHANGED_ACTION---------->");
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_ENABLED:
                        mWifiManager.startScan();
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        break;
                }
            }
        }
    };


    private BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null){
                    String address = device.getAddress();
                    Log.d("system_log","address------------>"+address);
                    if (address != null && !"".equals(address)){
                        isBlueSuccess = true;
                    }
                }
            }else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                String stateExtra = BluetoothAdapter.EXTRA_STATE;
                int  mBtState = intent.getIntExtra(stateExtra, -1);
                if ((mBtState == BluetoothAdapter.STATE_TURNING_ON) || (mBtState == BluetoothAdapter.STATE_ON)) {
                    mBluetoothAdapter.startDiscovery();
                }
            }
        }
    };

    @Override
    protected void startActivityIntentClass() {
        startActivityIntent(this, ResetActivity.class);
    }


    private LocationManager mLocationManager;

    private void initGPS(){
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "请开启GPS导航", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
            return;
        }
        String bestProvider = mLocationManager.getBestProvider(getCriteria(), true);
        Location location = mLocationManager.getLastKnownLocation(bestProvider);
        mLocationManager.addGpsStatusListener(listener);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
    }


    GpsStatus.Listener listener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int i) {
            switch (i) {
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Toast.makeText(SystemExtraActivity.this, "第一次定位", Toast.LENGTH_SHORT).show();
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                    int count = 0;
                    while (iters.hasNext() && count <= maxSatellites) {
                        GpsSatellite  s = iters.next();
                        count++;
                    }
                    isGpsSuccess = count > 3;
                    Log.i("lx_log","count " + count);
                case GpsStatus.GPS_EVENT_STARTED:
                    Toast.makeText(SystemExtraActivity.this,"定位启动",Toast.LENGTH_SHORT).show();
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    Toast.makeText(SystemExtraActivity.this,"定位结束",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private LocationListener locationListener = new LocationListener() {
        /**
         * GPS状态变化时触发
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
        public void onLocationChanged(Location location){
        }
    };

    private Criteria getCriteria(){
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setAltitudeRequired(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }

}
