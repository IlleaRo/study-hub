package network;

import gui.ClientGUI;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static java.lang.Integer.parseInt;

public class Client {
    String host;
    int port;
    private Socket socket;
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Client running args: <host> <port>");
            System.exit(2);
        }

        int port = parseInt(args[1]);
        ClientGUI gui;

        try {
            Client client = new Client(args[0], port);
            gui = new ClientGUI(client.getOutputStream(), client.getInputStream(), "Client");
            gui.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

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
