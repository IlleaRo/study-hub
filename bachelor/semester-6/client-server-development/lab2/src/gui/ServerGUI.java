package gui;

import network.Server;
import raster.PNM;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;

public class ServerGUI extends JFrame {
    public ServerGUI(Server server, String title) {
        super();

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(400,100);
        this.setTitle(title);
        this.setLayout(new BorderLayout());
        JTextArea pathField = new JTextArea();
        JButton sendButton = new JButton("Send");

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

        sendButton.addActionListener(actionEvent -> {
            try {
                server.updateImage(PNM.getImageByPath(pathField.getText()));
            } catch (IOException | PNM.UnknownFileException e) {
                e.printStackTrace();
            }
        });

        this.getContentPane().add(pathField, BorderLayout.CENTER);
        this.getContentPane().add(sendButton, BorderLayout.SOUTH);
    }
}
