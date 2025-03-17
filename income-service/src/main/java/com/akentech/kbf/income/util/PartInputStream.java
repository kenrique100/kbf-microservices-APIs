package com.akentech.kbf.income.util;

import org.springframework.core.io.buffer.DataBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * A custom {@link InputStream} implementation that wraps a list of {@link DataBuffer} objects.
 * <p>
 * This class enables reading data from a list of Spring WebFlux {@link DataBuffer}s as a sequential stream.
 * It is primarily used for handling file uploads in a reactive environment, where files are received as
 * multiple buffers rather than a single continuous stream.
 */
public class PartInputStream extends InputStream {

    private final List<DataBuffer> dataBuffers; // List of data buffers containing file data
    private int currentBufferIndex = 0; // Index of the current DataBuffer being read
    private int currentBufferPosition = 0; // Position within the current DataBuffer

    /**
     * Constructs a new {@code PartInputStream} using the given list of {@link DataBuffer}s.
     *
     * @param dataBuffers The list of {@link DataBuffer} objects to be read as an input stream.
     */
    public PartInputStream(List<DataBuffer> dataBuffers) {
        this.dataBuffers = dataBuffers;
    }

    /**
     * Reads the next byte of data from the stream.
     * <p>
     * This method sequentially reads bytes from the list of {@link DataBuffer}s, switching to the next buffer
     * when the current one is fully read. When all buffers are exhausted, it returns {@code -1}, indicating the
     * end of the stream.
     *
     * @return The next byte of data as an integer (0 to 255), or {@code -1} if end of stream is reached.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        // If all buffers have been read, return end-of-stream signal (-1)
        if (currentBufferIndex >= dataBuffers.size()) return -1;

        DataBuffer currentBuffer = dataBuffers.get(currentBufferIndex);

        // If we reached the end of the current buffer, move to the next buffer
        if (currentBufferPosition >= currentBuffer.readableByteCount()) {
            currentBufferIndex++;
            currentBufferPosition = 0;
            return read(); // Recursively call read() to continue reading from the next buffer
        }

        // Read the current byte and move the position forward
        return currentBuffer.getByte(currentBufferPosition++) & 0xFF;
    }
}
