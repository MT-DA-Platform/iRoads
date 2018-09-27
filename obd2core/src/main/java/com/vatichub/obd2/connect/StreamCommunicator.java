package com.vatichub.obd2.connect;

import java.io.IOException;

public interface StreamCommunicator {
    void write(byte[] buffer) throws IOException;
    void write(String str, byte[] term) throws IOException;
    byte[] sendReceive(String command) throws IOException;
    void close() throws IOException;
}
