package com.akentech.kbf.income.utils;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public final class PartInputStream extends InputStream {
    private final List<DataBuffer> dataBuffers;
    private int currentBufferIndex = 0;
    private int currentBufferPosition = 0;
    private boolean closed = false;

    public PartInputStream(List<DataBuffer> dataBuffers) {
        this.dataBuffers = Objects.requireNonNull(dataBuffers, "DataBuffers cannot be null");
    }

    @Override
    public int read() {
        if (closed) {
            throw new IllegalStateException("Stream already closed");
        }

        while (currentBufferIndex < dataBuffers.size()) {
            DataBuffer currentBuffer = dataBuffers.get(currentBufferIndex);
            if (currentBufferPosition < currentBuffer.readableByteCount()) {
                return currentBuffer.getByte(currentBufferPosition++) & 0xFF;
            }
            currentBufferIndex++;
            currentBufferPosition = 0;
        }
        return -1;
    }

    @Override
    public void close() {
        if (!closed) {
            dataBuffers.forEach(DataBufferUtils::release);
            closed = true;
        }
    }
}