package network;

import raster.PBM;
import raster.PGM;
import raster.PNM;
import raster.PPM;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

import static java.lang.Integer.parseInt;

public class Server {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Server running args: <port> <path_of_image>");
            System.exit(2);
        }

        try {
            new Server(parseInt(args[0]), PNM.getImageByPath(args[1]));
        } catch (IOException | PNM.UnknownFileException e) {
            e.printStackTrace();
        }
    }

    private final DatagramSocket serverSocket;
    PNM image;

    public Server(int port, PNM init_image) throws IOException {
        this.serverSocket = new DatagramSocket(port);
        this.start(init_image);
    }

    private void start(PNM initImage) {
        Thread serverThread = new Thread() {
            @Override
            public void run() {
                byte[] recvData;
                byte[] buffer = new byte[1024];
                DatagramPacket recvPacket = new DatagramPacket(buffer, buffer.length);

                while (!this.isInterrupted()) {
                    try {
                        serverSocket.receive(recvPacket);

                        /* Client -> Server
                        1. Opcode (Put - 1 / Get - 0)
                        2. Class name (only for put)
                        3. Data (only for put)

                        Server -> Client
                        1. Class name
                        2. Data
                         */

                        recvData = recvPacket.getData();

                        if (recvData[0] == 1) { // PUT
                            image = DatagramHandler.getImage(
                                    Arrays.copyOfRange(recvData, 1, PNM.class.getName().length() + 1),
                                    Arrays.copyOfRange(recvData, PNM.class.getName().length() + 2, recvData.length - 1)
                            );
                        } else if (recvData[0] == 0) { // GET
                            DatagramHandler.sendImage(image, serverSocket, recvPacket.getAddress(), recvPacket.getPort(), false);
                        } else {
                            throw new UnsupportedOperationException("Unknown opcode");
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        this.interrupt();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        this.image = initImage;
        serverThread.start();
    }
}
