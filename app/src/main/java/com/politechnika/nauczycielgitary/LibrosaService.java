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
                jLibrosa.getSampleRate(),
                jLibrosa.getN_fft(),
                jLibrosa.getN_mels(),
                jLibrosa.getHop_length());
    }

    public float[] getPCPVector() throws FileFormatNotSupportedException, IOException, WavFileException {
        return jLibrosa.loadAndRead(filePathToWaveFile, -1, 2);
    }

}
