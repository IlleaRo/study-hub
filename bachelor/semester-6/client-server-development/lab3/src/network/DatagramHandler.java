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

public class DatagramHandler {
    public static void sendImage(PNM image, DatagramSocket socket, InetAddress address, int port, boolean fromClient) throws IOException {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(buffer.length);
        DatagramPacket sendPacket;
        byte[] payload;

        if (fromClient)
        {
            outStream.write(1);
        }

        image.streamWrite(outStream);
        payload = outStream.toByteArray();

        sendPacket = new DatagramPacket(payload, payload.length, address, port);
        socket.send(sendPacket);
    }
    public static PNM getImage(byte[] className, byte[] data) throws IOException, ClassNotFoundException {
        PNM image = new PBM();
        String imageClass = new String(className);
        ByteArrayInputStream inStream;

        if (imageClass.equals(PBM.class.getName())) {
            image = new PBM();
        } else if (imageClass.equals(PGM.class.getName())) {
            image = new PGM();
        } else if (imageClass.equals(PPM.class.getName())) {
            image = new PPM();
        } else {
            throw new ClassNotFoundException("Unknown class");
        }

        inStream = new ByteArrayInputStream(data);
        image.streamRead(inStream);

        return image;
    }
}
