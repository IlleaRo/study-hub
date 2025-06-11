package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
    String host;
    int port;
    private Socket socket;
    public Client(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        this.socket = new Socket(host, port);
    }
    public void disconnect() throws IOException {
        if (this.socket.isClosed()) {
            throw new IOException("Client is down already");
        }
        this.socket.close();
    }
    public void reconnect() throws IOException {
        if (!this.socket.isClosed()) {
            this.socket.close();
        }
        this.socket = new Socket(this.host, this.port);
    }

    public InputStream getInputStream() throws IOException {
        if (this.socket.isClosed()) {
            throw new IOException("Client is down");
        }
        return this.socket.getInputStream();
    }
    public OutputStream getOutputStream() throws IOException {
        if (this.socket.isClosed()) {
            throw new IOException("Client is down");
        }
        return this.socket.getOutputStream();
    }
}
