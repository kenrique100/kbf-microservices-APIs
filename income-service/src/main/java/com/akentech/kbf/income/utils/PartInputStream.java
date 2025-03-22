package com.akentech.kbf.income.utils;

import org.springframework.core.io.buffer.DataBuffer;

import java.io.InputStream;
import java.util.List;

public class PartInputStream extends InputStream {

    private final List<DataBuffer> dataBuffers;
    private int currentBufferIndex = 0;
    private int currentBufferPosition = 0;

    public PartInputStream(List<DataBuffer> dataBuffers) {
        this.dataBuffers = dataBuffers;
    }

    @Override
    public int read() {
        if (currentBufferIndex >= dataBuffers.size()) return -1;

        DataBuffer currentBuffer = dataBuffers.get(currentBufferIndex);

        if (currentBufferPosition >= currentBuffer.readableByteCount()) {
            currentBufferIndex++;
            currentBufferPosition = 0;
            return read();
        }

        return currentBuffer.getByte(currentBufferPosition++) & 0xFF;
    }
}