package network;

import gui.ClientGUI;
import raster.PBM;
import raster.PGM;
import raster.PNM;
import raster.PPM;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.Arrays;
import java.util.function.Function;

import static java.lang.Integer.parseInt;

public class Client {

    String endpoint;
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

    public Client(String host, Integer port) {
        this.endpoint = "http://"+host+":"+port.toString()+"/image";
    }

    public void sendImage(PNM image) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(endpoint).openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("PUT");
        image.streamWrite(con.getOutputStream());
        con.getOutputStream().flush();
        con.getOutputStream().close();
        con.getInputStream();
        con.disconnect();
    }

    public PNM getImage() throws IOException, ClassNotFoundException {
        HttpURLConnection con = (HttpURLConnection) new URL(endpoint).openConnection();
        String name;
        PNM image;
        con.setRequestMethod("GET");
        InputStream inStream = con.getInputStream();

        name = new String(inStream.readNBytes(PNM.class.getName().length()));

        if (name.equals(PGM.class.getName())) {
            image = new PGM();
        } else if (name.equals(PBM.class.getName())) {
            image = new PBM();
        } else if (name.equals(PPM.class.getName())) {
            image = new PPM();
        } else {
            throw new ClassNotFoundException("Unknown inmage");
        }

        inStream.readNBytes(1);
        image.streamRead(inStream);
        inStream.close();
        con.disconnect();

        return image;
    }
}
