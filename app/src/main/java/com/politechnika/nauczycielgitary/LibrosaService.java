package com.politechnika.nauczycielgitary;

import com.jlibrosa.audio.JLibrosa;
import com.jlibrosa.audio.exception.FileFormatNotSupportedException;
import com.jlibrosa.audio.wavFile.WavFileException;

import java.io.IOException;

public class LibrosaService {

    private final JLibrosa jLibrosa;
    private final String filePathToWaveFile;

    public LibrosaService(String filePathToWaveFile) {
        this.filePathToWaveFile = filePathToWaveFile;
        jLibrosa = new JLibrosa();
    }

    public float[][] getMelSpectoGram() throws FileFormatNotSupportedException, IOException, WavFileException {
        return jLibrosa.generateMelSpectroGram(
                getPCPVector(),
                AudioSettings.SAMPLE_RATE_IN_HZ,
                jLibrosa.getN_fft(),
                jLibrosa.getN_mels(),
                jLibrosa.getHop_length());
//        return jLibrosa.generateMelSpectroGram(jLibrosa.loadAndRead(filePathToWaveFile, AudioSettings.SAMPLE_RATE_IN_HZ, -1));
    }

    public float[] getPCPVector() throws FileFormatNotSupportedException, IOException, WavFileException {
        return jLibrosa.loadAndRead(filePathToWaveFile, AudioSettings.SAMPLE_RATE_IN_HZ, -1);
    }

}
