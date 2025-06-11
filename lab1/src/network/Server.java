package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final ServerSocket serverSocket;
    private Socket socket;
    public Server(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    public void start() throws IOException {
        if (!(this.socket == null || this.socket.isClosed())) {
            throw new IOException("Server is already started");
        }
        socket = serverSocket.accept();
    }

    public void stop() throws IOException {
        if (this.socket == null || this.socket.isClosed()) {
            throw new IOException("Server is down");
        }
        this.socket.close();
        serverSocket.close();
    }

    public OutputStream getOutputStream() throws IOException {
        if (this.socket == null || this.socket.isClosed()) {
            throw new IOException("Server is down");
        }
        return this.socket.getOutputStream();
    }
    public InputStream getInputStream() throws IOException {
        if (this.socket == null || this.socket.isClosed()) {
            throw new IOException("Server is down");
        }
        return this.socket.getInputStream();
    }
}
