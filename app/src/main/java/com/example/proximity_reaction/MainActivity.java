package com.example.proximity_reaction;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private TextView display;
    private Button start;
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private SensorEventListener proximitySensorListener;
    long startTime;
    long endTime;
    boolean isMeasuring = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        display = findViewById(R.id.display);
        start = findViewById(R.id.start);
        start.setText("Start");
        start.setOnClickListener(v -> {
            if (!isMeasuring) {
                display.setText("Ready!");
                start.setText("Again");
                int randomTimeOut = (int) (Math.random()*5000);
                Log.d("timeout", String.valueOf(randomTimeOut));
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startTime = System.nanoTime();
                        isMeasuring = true;
                        display.setText("Now!");

                    }
                }, randomTimeOut);
            }
            });

        // Get the SensorManager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Get the proximity sensor
        if (sensorManager != null) {
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }

        // Check if the device has a proximity sensor
        if (proximitySensor == null) {
            Log.e("ProximitySensor", "No proximity sensor found!");
            return;
        }

        // Create a listener
        proximitySensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Log.d("proximity",String.valueOf(event.values[0]));
                if (event.values[0] < 0.1 && isMeasuring) {
                    endTime = System.nanoTime();
                    double duration = (endTime - startTime) * Math.pow(10,-9);
                    Log.d("start time", String.valueOf(startTime));
                    Log.d("end time", String.valueOf(endTime));
                    Log.d("duration", String.valueOf(duration));
                    display.setText(String.format("%.3f",duration));
                    isMeasuring = false;
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (proximitySensor != null) {
            sensorManager.registerListener(proximitySensorListener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (proximitySensor != null) {
            sensorManager.unregisterListener(proximitySensorListener);
        }
    }
}