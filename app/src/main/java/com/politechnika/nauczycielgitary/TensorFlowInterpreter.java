package com.politechnika.nauczycielgitary;

import android.content.Context;

import com.jlibrosa.audio.exception.FileFormatNotSupportedException;
import com.jlibrosa.audio.wavFile.WavFileException;
import com.politechnika.nauczycielgitary.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.util.Arrays;
import java.util.stream.Stream;

public class TensorFlowInterpreter {

    private final Interpreter interpreter;
    private final LibrosaService librosaService;

    public TensorFlowInterpreter(MappedByteBuffer modelFile, LibrosaService librosaService) {
        this.librosaService = librosaService;
        this.interpreter = new Interpreter(modelFile);
    }

    public void classify() {
        try {
            float[][] melSpectoGram = librosaService.getMelSpectoGram();
            System.out.println("Size of melspectogram: " + melSpectoGram.length + ", " + melSpectoGram[0].length);
            float[][] output = new float[1][10];
            interpreter.run(toByteBuffer(melSpectoGram), output);
            printOutput(output);
        } catch (FileFormatNotSupportedException | IOException | WavFileException e) {
            e.printStackTrace();
        }
    }

    private void printOutput(float[][] output) {
        for (float[] floats : output) {
            for (int j = 0; j < output[0].length; j++) {
                System.out.println("Probability at index: " + j + " is: " + floats[j]);
            }
        }
    }

    private ByteBuffer toByteBuffer(float[][] spectogram) {
        float[][] croppedSpectogram = cropSpectogramToDesiredShape(spectogram);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4*croppedSpectogram.length*croppedSpectogram[0].length);
        byteBuffer.order(ByteOrder.nativeOrder());
        float[] flattenedSpectogram = flattenTwoDimensionalArray(croppedSpectogram);
        for (float value: flattenedSpectogram) {
            byteBuffer.putFloat(value);
        }
        return byteBuffer;
    }

    private float[][] cropSpectogramToDesiredShape(float[][] spectogram) {
        float[][] croppedSpectogram = new float[128][87];
        for (int i = 0; i < 128; i++) {
            System.arraycopy(spectogram[i], 0, croppedSpectogram[i], 0, 87);
        }
        return croppedSpectogram;
    }

    private float[] flattenTwoDimensionalArray(float[][] twoDArray) {
        float[] array = new float[twoDArray.length*twoDArray[0].length];
        for (int i = 0; i < twoDArray.length; i++) {
            float[] row = twoDArray[i];
            for (int j = 0; j < row.length; j++) {
                float value = twoDArray[i][j];
                array[i*row.length+j] = value;
            }
        }
        return array;
    }

}
