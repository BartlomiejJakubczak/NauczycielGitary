package com.politechnika.nauczycielgitary;

import android.media.AudioFormat;
import android.media.AudioRecord;

public class AudioSettings {

    public static final int SAMPLE_RATE_IN_HZ = 44100;
    public static final int RECORDER_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    public static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int BUFFER_ELEMENTS = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            RECORDER_CHANNEL, RECORDER_AUDIO_ENCODING);

}
