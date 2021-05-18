package com.politechnika.nauczycielgitary;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.politechnika.nauczycielgitary.databinding.ChordRecognitionBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ChordRecognitionActivity extends AppCompatActivity implements ThreadCompletionListener {

    private static final String LOG_TAG = ChordRecognitionActivity.class.getSimpleName();
    private static final String FILE_PATH_TO_WAVE_FILE = Environment.getExternalStorageDirectory().getPath() + "/sample.wav";
    private static final String FILE_PATH_TO_TFLITE_MODEL= "my_samples_augmented.tflite";

    private AudioThread audioThread;
    private SoundPoolPlayer soundPoolPlayer;
    private TensorFlowInterpreter tensorFlowInterpreter;

    private ChordRecognitionBinding binding;

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpAudioTools();
        tensorFlowInterpreter = new TensorFlowInterpreter(
                getApplicationContext(),
                FILE_PATH_TO_TFLITE_MODEL,
                new LibrosaService(FILE_PATH_TO_WAVE_FILE));
        binding = ChordRecognitionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setViews();
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

    private void setUpAudioTools() {
        soundPoolPlayer = new SoundPoolPlayer();
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

}
