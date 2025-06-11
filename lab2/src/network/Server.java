package network;

import gui.ServerGUI;
import raster.PBM;
import raster.PGM;
import raster.PNM;
import raster.PPM;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;

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
            e.fillInStackTrace();
        }
    }

    private final ServerSocket serverSocket;
    final ArrayList<ClientProvider> clientProviders = new ArrayList<>();
    PNM image;
    public Server(int port, PNM init_image) throws IOException {
        this.serverSocket = new ServerSocket(port);

        this.start(init_image);
        new ServerGUI(this, "Server").setVisible(true);
    }
    private void start(PNM initImage) {
        Thread serverThread = new Thread() {
            @Override
            public void run() {
                while (!this.isInterrupted()) {
                    try {
                        clientProviders.add(new ClientProvider(serverSocket.accept()));
                    } catch (IOException e) {
                        e.fillInStackTrace();
                    }
                }
            }
        };
        this.image = initImage;
        serverThread.start();
    }

    private void clientCleanup() {
        for (ClientProvider client : clientProviders) {
            clientProviders.remove(client);
        }
    }

    public void updateImage (PNM newImage) throws IOException {
        this.image = newImage;
        for (ClientProvider client : clientProviders) {
            client.updateImage();
        }
    }

    private void selectiveUpdate(PNM newImage, ClientProvider sender) {
        this.image = newImage;
        for (ClientProvider client : clientProviders) {
            if (client != sender) {
                client.updateImage();
            }
        }
    }

    private class ClientProvider {
        private final Socket clientSocket;
        private final ClientListener clientListener;
        public ClientProvider(Socket socket) {
            super();
            this.clientSocket = socket;
            new ClientSender().start();
            clientListener =  new ClientListener();

            clientListener.start();
        }
        public void updateImage() {
            new ClientSender().start();
        }

        private class ClientListener extends Thread {
            @Override
            public void run() {

                byte[] buf = new byte[1024];

                byte[] nameBytes = new byte[PNM.class.getName().length()];
                PNM image;
                String name;

                try {
                    InputStream inStream = clientSocket.getInputStream();
                    while (true) {
                        if (inStream.read(buf) == -1) {
                            throw new IOException("Connection closed");
                        }

                        byte[] decodedBytes = Base64.getDecoder().decode(new String(buf).replace("\0", ""));

                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decodedBytes);


                        if (byteArrayInputStream.read(nameBytes) != nameBytes.length) {
                            throw new IOException("Name reading error");
                        }
                        if (byteArrayInputStream.read() != '\0') {
                            throw new IOException("Name reading error");
                        }
                        name = new String(nameBytes);

                        if (name.equals(PBM.class.getName())) {
                            image = new PBM();
                        } else if (name.equals(PGM.class.getName())) {
                            image = new PGM();
                        } else if (name.equals(PPM.class.getName())) {
                            image = new PPM();
                        } else {
                            throw new ClassNotFoundException("Unknown class");
                        }

                        image.streamRead(byteArrayInputStream);
                        Server.this.selectiveUpdate(image, ClientProvider.this);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    clientProviders.remove(ClientProvider.this);
                    this.interrupt();
                }
            }
        }

        private class ClientSender extends Thread {
            @Override
            public void run() {
                try {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    image.streamWrite(byteArrayOutputStream);
                    OutputStream outputStream = clientSocket.getOutputStream();
                    outputStream.write(Base64.getEncoder().encode(byteArrayOutputStream.toByteArray()));
                    outputStream.flush();
                } catch (IOException e) {
                    e.fillInStackTrace();
                    clientListener.interrupt();
                    clientProviders.remove(ClientProvider.this);
                    this.interrupt();
                }
            }
        }
    }
}
