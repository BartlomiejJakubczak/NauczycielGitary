package com.politechnika.nauczycielgitary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.politechnika.nauczycielgitary.databinding.ActivityMainBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity implements ThreadCompletionListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String FILE_PATH_TO_SD_CARD = Environment.getExternalStorageDirectory().getPath();
    private static final int REQUEST_ALL_APP_PERMISSIONS = 200;
    private static final String audioPermission = Manifest.permission.RECORD_AUDIO;
    private static final String dataWritePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String dataReadPermission = Manifest.permission.READ_EXTERNAL_STORAGE;
    private final String [] permissions = {audioPermission, dataWritePermission, dataReadPermission};
    private AudioService audioService;
    private SoundPoolPlayer soundPoolPlayer;

    private ActivityMainBinding binding;

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
        setUpAudioTools();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setButtons();
    }

    private void setUpAudioTools() {
        soundPoolPlayer = new SoundPoolPlayer();
    }

    private void checkPermissions() {
        int audioPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), audioPermission);
        int dataWriteResult = ContextCompat.checkSelfPermission(getApplicationContext(), audioPermission);
        int dataReadResult = ContextCompat.checkSelfPermission(getApplicationContext(), audioPermission);
        if (audioPermissionResult + dataWriteResult + dataReadResult != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "Not all permissions are granted. Audio: " + audioPermissionResult + ", data write: " + dataWriteResult + ", data read: " + dataReadResult);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_ALL_APP_PERMISSIONS);
        }
        Log.d(LOG_TAG, "All permissions granted.");
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

    private void setButtons() {
        binding.buttonPlayRecording.setEnabled(false);
        binding.buttonStopRecording.setEnabled(false);
        binding.buttonStartRecording.setOnClickListener(v -> {
            soundPoolPlayer.resetPiResource();
            audioService = new AudioService(FILE_PATH_TO_SD_CARD);
            audioService.start();
            binding.buttonStartRecording.setEnabled(false);
            binding.buttonStopRecording.setEnabled(true);
            binding.buttonPlayRecording.setEnabled(false);
        });
        binding.buttonStopRecording.setOnClickListener(v -> {
            audioService.stopRecording();
        });
        binding.buttonPlayRecording.setOnClickListener(v -> {
            soundPoolPlayer.playShortResource();
        });
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ThreadCompletionEvent event) {
        Log.d(LOG_TAG, "Event received: " + event.getClass().getSimpleName());
        Toast.makeText(this, "Audio file saved.", Toast.LENGTH_SHORT).show();
        binding.buttonPlayRecording.setEnabled(true);
        binding.buttonStopRecording.setEnabled(false);
        binding.buttonStartRecording.setEnabled(true);
        if (!soundPoolPlayer.isResourceSet()) {
            soundPoolPlayer.setPiResource(FILE_PATH_TO_SD_CARD + AudioService.WAVE_FILE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPoolPlayer.release();
    }

}