package network;

import static java.lang.Integer.TYPE;
import static spark.Spark.*;

import com.sun.jdi.ClassNotPreparedException;
import raster.PBM;
import raster.PGM;
import raster.PNM;
import raster.PPM;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

import static java.lang.Integer.parseInt;

public class Server {
    static PNM image = new PBM();
    public static void main(String[] args) throws PNM.UnknownFileException, IOException {
        if (args.length != 2) {
            System.err.println("Server running args: <port> <path_of_image>");
            System.exit(2);
        }

        port(Integer.parseInt(args[0]));
        image = PNM.getImageByPath(args[1]);

        get("/image", (req, res) -> {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            image.streamWrite(outputStream);
            return outputStream.toString();
        });

        put("/image", (req, res) -> {
            try {
                String imageData = req.body();
                ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData.getBytes());
                String name = new String(inputStream.readNBytes(image.getClass().getName().length()));
                if (name.equals(PGM.class.getName())) {
                    image = new PGM();
                } else if (name.equals(PBM.class.getName())) {
                    image = new PBM();
                } else if (name.equals(PPM.class.getName())) {
                    image = new PPM();
                } else {
                    throw new ClassNotFoundException("Unknown inmage");
                }

                inputStream.readNBytes(1);

                image.streamRead(inputStream);

                return "SUCCESS";
            }
            catch (Exception e)
            {
                return "FAILURE";
            }
        });
    }
}
