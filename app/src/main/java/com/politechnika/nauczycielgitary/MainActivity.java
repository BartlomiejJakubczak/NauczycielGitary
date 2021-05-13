package com.politechnika.nauczycielgitary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.politechnika.nauczycielgitary.databinding.ActivityMainBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity implements ThreadCompletionListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String FILE_PATH_TO_WAVE_FILE = Environment.getExternalStorageDirectory().getPath() + "/sample.wav";
    private static final String FILE_PATH_TO_TFLITE_MODEL= "my_samples_augmented.tflite";
    private static final int REQUEST_ALL_APP_PERMISSIONS = 200;
    private static final String AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO;
    private static final String DATA_WRITE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String DATA_READ_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;
    private final String [] permissions = {AUDIO_PERMISSION, DATA_WRITE_PERMISSION, DATA_READ_PERMISSION};

    private AudioThread audioThread;
    private SoundPoolPlayer soundPoolPlayer;
    private TensorFlowInterpreter tensorFlowInterpreter;

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
        tensorFlowInterpreter = new TensorFlowInterpreter(
                getApplicationContext(),
                FILE_PATH_TO_TFLITE_MODEL,
                new LibrosaService(FILE_PATH_TO_WAVE_FILE));
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setViews();
    }

    private void setUpAudioTools() {
        soundPoolPlayer = new SoundPoolPlayer();
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

    private void setViews() {
        binding.textViewChord.setText("Press Start Recording.");
        binding.buttonPlayRecording.setEnabled(false);
        binding.buttonClassify.setEnabled(false);
        binding.buttonStartRecording.setOnClickListener(v -> {
            binding.textViewChord.setText("Wait...");
            binding.buttonPlayRecording.setEnabled(false);
            binding.buttonClassify.setEnabled(false);
            binding.buttonStartRecording.setEnabled(false);
            soundPoolPlayer.resetPiResource();
            audioThread = new AudioThread(Environment.getExternalStorageDirectory().getPath());
            new Handler().postDelayed(() -> {
                binding.textViewChord.setText("Play!");
                audioThread.start();
            }, 2000);
            new Handler().postDelayed(() -> {
                binding.textViewChord.setText("Press Classify to find your chord.");
                audioThread.stopRecording();
            }, 4150); // TA WARTOSC DAJE RAMKE 128, 173
        });
        binding.buttonPlayRecording.setOnClickListener(v -> {
            soundPoolPlayer.playShortResource();
        });
        binding.buttonClassify.setOnClickListener(v -> {
            String chord = tensorFlowInterpreter.classify();
            binding.textViewChord.setText("Your chord is: " + chord);
        });
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ThreadCompletionEvent event) {
        Log.d(LOG_TAG, "Event received: " + event.getClass().getSimpleName());
        Toast.makeText(this, "Audio file saved.", Toast.LENGTH_SHORT).show();
        binding.buttonPlayRecording.setEnabled(true);
        binding.buttonStartRecording.setEnabled(true);
        binding.buttonClassify.setEnabled(true);
        if (!soundPoolPlayer.isResourceSet()) {
            soundPoolPlayer.setPiResource(FILE_PATH_TO_WAVE_FILE);
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
        tensorFlowInterpreter.close();
    }

}