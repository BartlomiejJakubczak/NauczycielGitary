package com.politechnika.nauczycielgitary;

import android.media.AudioAttributes;
import android.media.SoundPool;

import org.greenrobot.eventbus.EventBus;

public class SoundPoolPlayer {

    private SoundPool shortPlayer;
    private Integer piResource;

    public SoundPoolPlayer() {
        AudioAttributes.Builder audioAttributesBuilder = new AudioAttributes.Builder();
        AudioAttributes audioAttributes = audioAttributesBuilder
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build();
        SoundPool.Builder soundPoolBuilder = new SoundPool.Builder();
        this.shortPlayer = soundPoolBuilder
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(1)
                .build();
    }

    public void setPiResource(String filePath) {
        piResource = this.shortPlayer.load(filePath, 1);
    }

    public void resetPiResource() {
        piResource = null;
    }

    public void playShortResource() {
        this.shortPlayer.play(piResource, 0.99f, 0.99f, 0, 0, 1);
    }

    public boolean isResourceSet() {
        return piResource != null;
    }

    public void release() {
        if (shortPlayer != null) {
            this.shortPlayer.release();
            this.shortPlayer = null;
            this.piResource = null;
        }
    }

}
