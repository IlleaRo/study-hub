package network;

import gui.ClientGUI;
import raster.PNM;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.Arrays;
import java.util.function.Function;

import static java.lang.Integer.parseInt;

public class Client {
    static int timeoutS = 5;

    InetAddress host;
    int port;
    private DatagramSocket socket;
    byte[] buffer = new byte[1024];

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Client running args: <host> <port>");
            System.exit(2);
        }

        try {
            Client client = new Client(args[0], parseInt(args[1]));
            ClientGUI gui = new ClientGUI(client);
            gui.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    public Client(String host, int port) throws SocketException, UnknownHostException {
        this.host = InetAddress.getByName(host);
        this.port = port;
        this.socket = new DatagramSocket();
        this.socket.setSoTimeout(5000);
    }

    public void sendImage(PNM image) throws IOException {
        DatagramHandler.sendImage(image, socket, host, port, true);
    }

    public PNM getImage() throws IOException, ClassNotFoundException {
        int delay = Client.timeoutS * 10;
        byte[] recvBuffer = new byte[1024];
        DatagramPacket requestPacket = new DatagramPacket(new byte[]{0}, 1, host, port);
        DatagramPacket recvPacket = new DatagramPacket(buffer, buffer.length);

        socket.send(requestPacket);

        socket.receive(recvPacket);
        recvBuffer = recvPacket.getData();

        return DatagramHandler.getImage(
                Arrays.copyOfRange(recvBuffer, 0, PNM.class.getName().length()),
                Arrays.copyOfRange(recvBuffer, PNM.class.getName().length()+1, recvBuffer.length-1)
        );
    }

}
