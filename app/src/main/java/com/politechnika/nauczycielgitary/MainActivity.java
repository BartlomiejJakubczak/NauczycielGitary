package com.politechnika.nauczycielgitary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.politechnika.nauczycielgitary.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_ALL_APP_PERMISSIONS = 200;
    private static final String AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO;
    private static final String DATA_WRITE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String DATA_READ_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;
    private final String [] permissions = {AUDIO_PERMISSION, DATA_WRITE_PERMISSION, DATA_READ_PERMISSION};

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setViews();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean recordAudioPermission = false;
        boolean writeStoragePermission = false;
        boolean readStoragePermission = false;
        if (requestCode == REQUEST_ALL_APP_PERMISSIONS) {
            recordAudioPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            writeStoragePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
            readStoragePermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
        }
        if (!recordAudioPermission || !writeStoragePermission || !readStoragePermission) finish();
    }

    private void checkPermissions() {
        int audioPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), AUDIO_PERMISSION);
        int dataWriteResult = ContextCompat.checkSelfPermission(getApplicationContext(), AUDIO_PERMISSION);
        int dataReadResult = ContextCompat.checkSelfPermission(getApplicationContext(), AUDIO_PERMISSION);
        if (audioPermissionResult + dataWriteResult + dataReadResult != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "Not all permissions are granted. Audio: " + audioPermissionResult + ", data write: " + dataWriteResult + ", data read: " + dataReadResult);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_ALL_APP_PERMISSIONS);
        }
        Log.d(LOG_TAG, "All permissions granted.");
    }

    private void setViews() {
        binding.buttonChordRecognition.setOnClickListener(v -> {
            startActivity(new Intent(this, ChordRecognitionActivity.class));
        });
        binding.buttonChordPractice.setOnClickListener(v -> {

        });
    }

}