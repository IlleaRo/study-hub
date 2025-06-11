package gui;

import network.Server;
import raster.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Base64;

public class ClientGUI extends JFrame {
    OutputStream outStream;
    InputStream inStream;
    Thread recvThread;
    public ClientGUI(OutputStream outStream, InputStream inStream, String title) {
        super();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(400,100);
        this.setTitle(title);
        this.setLayout(new BorderLayout());
        JTextArea pathField = new JTextArea();
        JButton sendButton = new JButton("Send");
        this.outStream = outStream;
        this.inStream = inStream;
        pathField.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_LINK);
                    for (File file : (java.util.List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)) {
                        pathField.setText(file.getAbsolutePath());
                        break;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        this.recvThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    PNM image;
                    String name;
                    byte[] nameBytes = new byte[PNM.class.getName().length()];
                    Viewer viewer;
                    byte[] buf = new byte[1024];
                    try {
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
                        viewer = new Viewer(image, title);
                        viewer.setVisible(true);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(3);
                    }
                }
            }
        };
        this.recvThread.start();
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    PNM image = PNM.getImageByPath(pathField.getText());
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    image.streamWrite(byteArrayOutputStream);
                    outStream.write(Base64.getEncoder().encode(byteArrayOutputStream.toByteArray()));
                    outStream.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        this.getContentPane().add(pathField, BorderLayout.CENTER);
        this.getContentPane().add(sendButton, BorderLayout.SOUTH);
    }
}
