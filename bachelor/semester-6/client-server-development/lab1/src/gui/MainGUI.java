package gui;

import raster.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainGUI extends JFrame {
    OutputStream outStream;
    InputStream inStream;
    JTextField pathField;
    JButton sendButton;
    Thread recvThread;
    public MainGUI(OutputStream outStream, InputStream inStream, String title) {
        super();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(300,300);
        this.setTitle(title);
        this.setLayout(new GridLayout());
        this.pathField = new JTextField();
        this.sendButton = new JButton("Send");
        this.outStream = outStream;
        this.inStream = inStream;
        this.recvThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    PNM image;
                    String name;
                    byte[] nameBytes = new byte[PNM.class.getName().length()];
                    Viewer viewer;
                    try {
                        if (inStream.read(nameBytes) != nameBytes.length) {
                            throw new IOException("Name reading error");
                        }
                        if (inStream.read() != '\0') {
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
                        image.streamRead(inStream);
                        viewer = new Viewer(image, title);
                        viewer.setVisible(true);
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        this.recvThread.start();
        this.sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String filePath = pathField.getText();
                String[] extensionSplit = filePath.split("\\.");
                String extension = extensionSplit[extensionSplit.length-1];
                PNM image = null;
                final Path path = Paths.get(filePath);
                switch (extension) {
                    case "pbm" -> {
                        try {
                            image = new PBM(path);
                        } catch (IOException | PNM.UnknownFileException e) {
                            e.printStackTrace();
                        }
                    }
                    case "pgm" -> {
                        try {
                            image = new PGM(path);
                        } catch (IOException | PNM.UnknownFileException e) {
                            e.printStackTrace();
                        }
                    }
                    case "ppm" -> {
                        try {
                            image = new PPM(path);
                        } catch (IOException | PNM.UnknownFileException e) {
                            e.printStackTrace();
                        }
                    }
                    default -> {
                        return;
                    }
                }
                try {
                    image.streamWrite(outStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        this.getContentPane().add(this.pathField);
        this.getContentPane().add(this.sendButton);
    }
}
