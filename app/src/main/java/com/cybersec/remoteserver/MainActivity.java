package com.cybersec.remoteserver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.format.Formatter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private TextView statusText;
    private TextView ipText;
    private Button startButton;
    private Button stopButton;
    private PowerManager.WakeLock wakeLock;

    private static final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_SMS,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        ipText = findViewById(R.id.ipText);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);

        // Wake lock pour empêcher la mise en veille
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RemoteServer::WakeLock");

        // Afficher l'IP
        displayIpAddress();

        // Vérifier et demander les permissions
        checkAndRequestPermissions();

        startButton.setOnClickListener(v -> startServer());
        stopButton.setOnClickListener(v -> stopServer());

        updateUI();
    }

    private void displayIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = Formatter.formatIpAddress(ipAddress);
        ipText.setText("IP: " + ip + "\nPort: 4444");
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean allGranted = true;
            for (String permission : REQUIRED_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                Toast.makeText(this, "Toutes les permissions accordées", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Certaines permissions sont refusées. L'app peut ne pas fonctionner correctement.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startServer() {
        if (!wakeLock.isHeld()) {
            wakeLock.acquire();
        }

        Intent serviceIntent = new Intent(this, RemoteServerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        Toast.makeText(this, "Serveur démarré", Toast.LENGTH_SHORT).show();
        updateUI();
    }

    private void stopServer() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }

        Intent serviceIntent = new Intent(this, RemoteServerService.class);
        stopService(serviceIntent);

        Toast.makeText(this, "Serveur arrêté", Toast.LENGTH_SHORT).show();
        updateUI();
    }

    private void updateUI() {
        boolean isRunning = RemoteServerService.isRunning();
        if (isRunning) {
            statusText.setText("État: EN LIGNE");
            statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        } else {
            statusText.setText("État: HORS LIGNE");
            statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayIpAddress();
        updateUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
}
