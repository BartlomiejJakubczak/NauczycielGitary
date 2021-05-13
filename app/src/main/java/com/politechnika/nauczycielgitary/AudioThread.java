package com.politechnika.nauczycielgitary;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.politechnika.nauczycielgitary.AudioSettings.BUFFER_ELEMENTS;
import static com.politechnika.nauczycielgitary.AudioSettings.RECORDER_CHANNEL;
import static com.politechnika.nauczycielgitary.AudioSettings.SAMPLE_RATE_IN_HZ;

public class AudioThread extends Thread {

    private static final String LOG_TAG = AudioThread.class.getSimpleName();

    private static final String RAW_FILE = "/sample.pcm";
    private static final String WAVE_FILE = "/sample.wav";
    private boolean isRecording;

    private final String filePath;
    private AudioRecord recorder;

    public AudioThread(String filePath) {
        Log.d(LOG_TAG, "The given filePath: " + filePath);
        this.filePath = filePath;
        setRecorder();
        prepareFileForRecording();
    }

    private void prepareFileForRecording() {
        File file = new File(filePath + RAW_FILE);
        if (!file.exists()) {
            boolean isFileCreated = false;
            try {
                isFileCreated = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(LOG_TAG, "File was not in place. File was created successfully: " + isFileCreated);
        } else {
            Log.d(LOG_TAG, "File was already in place.");
        }
    }

    private void setRecorder() {
        Log.d(LOG_TAG, "Setting up AudioRecord.");
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_IN_HZ,
                RECORDER_CHANNEL,
                AudioFormat.ENCODING_PCM_16BIT,
                BUFFER_ELEMENTS);
    }

    @Override
    public void run() {
        Log.d(LOG_TAG, "The thread has started.");
        startRecording();
        try {
            writeAudioDataToFile();
            convertToWaveFile();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            EventBus.getDefault().post(new ThreadCompletionEvent());
        }
    }

    private void startRecording() {
        Log.d(LOG_TAG, "Started recording.");
        recorder.startRecording();
        isRecording = true;
    }

    public void stopRecording() {
        Log.d(LOG_TAG, "Stopped recording.");
        isRecording = false;
    }

    private void writeAudioDataToFile() throws IOException {
        FileOutputStream os = new FileOutputStream(filePath + RAW_FILE);
        Log.d(LOG_TAG, "The file used to write raw data has been opened..");
        while (isRecording) {
            byte[] byteData = new byte[BUFFER_ELEMENTS];
            recorder.read(byteData, 0, BUFFER_ELEMENTS);
            os.write(byteData, 0, BUFFER_ELEMENTS);
        }
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
        Log.d(LOG_TAG, "Recording stopped, closing writing.");
        os.close();
    }

    private void convertToWaveFile() throws IOException {
        Log.d(LOG_TAG, "Converting to Wave file.");
        File rawFile = new File(filePath + RAW_FILE);
        File wavFile = new File(filePath + WAVE_FILE);
        WavConverter.rawToWave(rawFile, wavFile);
        rawFile.delete();
    }

}
