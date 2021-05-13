package com.politechnika.nauczycielgitary;

import android.content.Context;

import com.jlibrosa.audio.exception.FileFormatNotSupportedException;
import com.jlibrosa.audio.wavFile.WavFileException;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;

public class TensorFlowInterpreter {

    private Interpreter interpreter;
    private final LibrosaService librosaService;
    private final NDManager NDmanager;
    private Map chords;

    public TensorFlowInterpreter(Context context, String pathToTfliteModel, LibrosaService librosaService) {
        this.librosaService = librosaService;
        loadInterpreter(context, pathToTfliteModel);
        NDmanager = NDManager.newBaseManager();
        populateChordsMap();
    }

    private void populateChordsMap() {
        chords = Stream.of(new Object [][] {
                {0, "A"},
                {1, "Am"},
                {2, "B"},
                {3, "C"},
                {4, "D"},
                {5, "Dm"},
                {6, "E"},
                {7, "Em"},
                {8, "F"},
                {9, "G"}
        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> (String) data[1]));
    }

    private void loadInterpreter(Context context, String pathToFIle) {
        try {
            MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(context, pathToFIle);
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(2);
            interpreter = new Interpreter(tfliteModel, options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getPredictedChord(float[][] output) {
        int greatestIndex = -1;
        float greatestProbability = -1.0f;
        for (float[] floats : output) {
            for (int j = 0; j < output[0].length; j++) {
                if (floats[j] > greatestProbability) {
                    greatestIndex = j;
                    greatestProbability = floats[j];
                }
            }
        }
        return (String) chords.get(greatestIndex);
    }

    public String classify() {
        try {
            float[][] melSpectroGram = librosaService.getMelSpectoGram();
            System.out.println(melSpectroGram.length + ", " + melSpectroGram[0].length);
            float[][] output = new float[1][10];
            NDArray input = NDmanager.create(melSpectroGram).reshape(1,128,173,1);
            ByteBuffer buffer = input.toByteBuffer();
            interpreter.run(buffer, output);
            return getPredictedChord(output);
        } catch (FileFormatNotSupportedException | IOException | WavFileException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        interpreter.close();
    }

}
