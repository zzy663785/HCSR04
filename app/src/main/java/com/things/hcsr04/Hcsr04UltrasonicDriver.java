package com.things.hcsr04;

import android.hardware.Sensor;

import com.google.android.things.userdriver.UserDriverManager;
import com.google.android.things.userdriver.sensor.UserSensorDriver;
import com.google.android.things.userdriver.sensor.UserSensorReading;
import com.google.android.things.userdriver.sensor.UserSensor;

import java.io.IOException;

public class Hcsr04UltrasonicDriver implements AutoCloseable {

    private static final String TAG = Hcsr04UltrasonicDriver.class.getSimpleName();
    private static final int DRIVER_VERSION = 1;
    private static final String DRIVER_NAME = "HC-SR04 Ultrasonic Sensor";

    private UserSensor userSensor;
    private Hcsr04 device;

    public Hcsr04UltrasonicDriver(String trigPin, String echoPin) throws IOException {
        device = new Hcsr04(trigPin, echoPin);
    }

    @Override
    public void close() throws Exception {
        unregister();
        if (device != null) {
            try {
                device.close();
            } finally {
                device = null;
            }
        }
    }

    public void register() {
        if (device == null) {
            throw new IllegalStateException("cannot registered closed driver");
        }
        if (userSensor == null) {
            userSensor = build(device);
            UserDriverManager.getInstance().registerSensor(userSensor);
        }
    }

    public void unregister() {
        if (userSensor != null) {
            UserDriverManager.getInstance().unregisterSensor(userSensor);
            userSensor = null;
        }
    }

    private static UserSensor build(final Hcsr04 hcsr04) {
        return new UserSensor.Builder()
                .setName(DRIVER_NAME)
                .setVersion(DRIVER_VERSION)
                .setType(Sensor.TYPE_PROXIMITY)
                .setDriver(new UserSensorDriver() {
                    @Override
                    public UserSensorReading read() throws IOException {
                        float[] distance = hcsr04.getProximityDistance();
                        return new UserSensorReading(distance);
                    }
                })
                .build();
    }
}
