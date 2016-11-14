package uri.egr.biosensing.arduinologgingapp;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import java.util.ArrayList;

import uri.egr.biosensing.arduinologgingapp.fragments.DeviceScanFragment;
import uri.egr.biosensing.arduinologgingapp.fragments.DeviceStreamingFragment;
import uri.egr.biosensing.arduinologgingapp.models.BluetoothDeviceModel;
import uri.egr.biosensing.arduinologgingapp.services.BLEConnectionService;

public class MainActivity extends AppCompatActivity {
    private FrameLayout mFragmentContainer;
    private CoordinatorLayout mMessageContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragmentContainer = (FrameLayout) findViewById(R.id.fragment_container);
        mMessageContainer = (CoordinatorLayout) findViewById(R.id.message_container);

        getFragmentManager().beginTransaction().replace(R.id.fragment_container,new DeviceScanFragment()).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }

    public void connect(BluetoothDeviceModel bluetoothDeviceModel) {
        Bundle bundle = new Bundle();
        bundle.putString(DeviceStreamingFragment.BUNDLE_DEVICE_ADDRESS, bluetoothDeviceModel.getDeviceAddress());
        DeviceStreamingFragment deviceStreamingFragment = new DeviceStreamingFragment();
        deviceStreamingFragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.fragment_container,deviceStreamingFragment).commit();


    }

    public void disconnect() {
        getFragmentManager().beginTransaction().replace(R.id.fragment_container,new DeviceScanFragment()).commit();
    }

    public void showMessage(String message) {
        Snackbar.make(mMessageContainer, message, Snackbar.LENGTH_LONG).show();
    }
}
