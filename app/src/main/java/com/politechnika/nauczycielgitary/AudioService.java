package com.politechnika.nauczycielgitary;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioService extends Thread {

    private static final String LOG_TAG = AudioService.class.getSimpleName();

    private static final int SAMPLE_RATE_IN_HZ = 44100;
    private static final int RECORDER_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int BUFFER_ELEMENTS = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            RECORDER_CHANNEL, RECORDER_AUDIO_ENCODING);;
    private static final String RAW_FILE = "/sample.pcm";
    public static final String WAVE_FILE = "/sample.wav";
    private boolean isRecording;

    private final String filePath;
    private AudioRecord recorder;

    public AudioService(String filePath) {
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
            Log.d(LOG_TAG, "Writing some shit");
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
    }

}
