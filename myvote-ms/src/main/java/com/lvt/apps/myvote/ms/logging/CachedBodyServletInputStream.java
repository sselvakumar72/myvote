package com.lvt.apps.myvote.ms.logging;


import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class is a wrapper for the ServletInputStream.
 * It allows us to cache the request body inside our CachedBodyHttpServletRequest wrapper so
 * it can be consumed multiple times.
 */
@Slf4j
public class CachedBodyServletInputStream extends ServletInputStream {
    private final InputStream cachedBodyInputStream;

    public CachedBodyServletInputStream(byte[] cachedBody) {
        this.cachedBodyInputStream = new ByteArrayInputStream(cachedBody);
    }

    @Override
    public int read() throws IOException {
        return cachedBodyInputStream.read();
    }

    @Override
    public boolean isFinished() {
        try {
            return cachedBodyInputStream.available() == 0;
        } catch (IOException e) {
            log.error("Error validating if the input stream is finished", e);
            return false;
        }
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        throw new UnsupportedOperationException();
    }
}
