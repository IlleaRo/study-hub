package gui;

import network.Client;
import raster.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Function;

public class ClientGUI extends JFrame {
    String title = "PNM client";
    PNM image;
    public ClientGUI(Client datagramClient) throws ClassNotFoundException {
        super();
        final Viewer viewer = new Viewer(new PGM());
        JButton sendButton = new JButton("Send");
        JButton recvButton = new JButton("Receive");

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(400,100);
        this.setTitle(title);
        this.setLayout(new BorderLayout());

        viewer.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_LINK);
                    for (File file : (java.util.List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)) {
                        image = PNM.getImageByPath(file.getPath());
                        viewer.update(image);
                        break;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        sendButton.addActionListener(event -> {
            try {
                datagramClient.sendImage(image);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        recvButton.addActionListener(event -> {
            try {
                image = datagramClient.getImage();
                viewer.update(image);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        this.getContentPane().add(viewer, BorderLayout.CENTER);
        this.getContentPane().add(sendButton, BorderLayout.EAST);
        this.getContentPane().add(recvButton, BorderLayout.WEST);
    }
}
