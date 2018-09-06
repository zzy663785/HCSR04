package com.things.hcsr04;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import android.util.Log;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity2 extends Activity implements SensorEventListener {
    private static final String TAG = "Sensor";

    private TextView textView;
    private Hcsr04UltrasonicDriver hcsr04UltrasonicDriver;
    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.text);
        try {
            hcsr04UltrasonicDriver = new Hcsr04UltrasonicDriver("BCM20", "BCM21");

            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            mSensorManager.registerDynamicSensorCallback(new Hcsr04SensorCallback());

            hcsr04UltrasonicDriver.register();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        textView.setText("distanceInCm = " + sensorEvent.values[0] + "cm");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private class Hcsr04SensorCallback extends SensorManager.DynamicSensorCallback {
        @Override
        public void onDynamicSensorConnected(Sensor sensor) {
            Log.i(TAG, sensor.getName() + " has been connected");
            mSensorManager.registerListener(MainActivity2.this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        public void onDynamicSensorDisconnected(Sensor sensor) {
            Log.i(TAG, sensor.getName() + " has been disconnected");
            mSensorManager.unregisterListener(MainActivity2.this);
        }
    }
}
