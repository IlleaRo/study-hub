import gui.MainGUI;
import network.Client;
import network.Server;

import java.io.IOException;

import static java.lang.Integer.parseInt;

public class Main {
    public static void main(String[] args) {
        MainGUI gui;
        int port;
        String host;
        Server server;
        Client client;
        switch (args.length) {
            case 1 -> {
                try {
                    port = parseInt(args[0]);
                    server = new Server(port);
                    System.out.println("Waiting for client");
                    server.start();
                    System.out.println("Client connected");
                    gui = new MainGUI(server.getOutputStream(), server.getInputStream(), "Server");
                    gui.setVisible(true);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
            case 2 -> {
                try {
                    host = args[0];
                    port = parseInt(args[1]);
                    client = new Client(host, port);
                    gui = new MainGUI(client.getOutputStream(), client.getInputStream(), "Client");
                    gui.setVisible(true);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(2);
                }
            }
            default -> {
                System.out.println("Run as server args: <port>");
                System.out.println("Run as client args: <host> <port>");
            }
        }
    }
}