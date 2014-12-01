
/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 * Allen Starke - The above copyright stated that even though this information was modified to fit team Sincre 
 * need that the above statement should still be included. And it allows for redistribution.
 */
 
package sincre.dapp.com;



import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.filechooser.*;



public class SincreFileChooser_m extends JPanel implements ActionListener {
    
	private static final long serialVersionUID = 1L;
	static private final String newline = "\n";
    JButton browseButton, uploadButton;
    JTextArea log;
    JFileChooser fc;
    static JFrame frame = new JFrame("FileChooser");
    static SincreDisplay_m f = new SincreDisplay_m();

    public SincreFileChooser_m() {
        super(new BorderLayout());

        //Create the log
        log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);

        //Create a file chooser
        fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "mp4/png Files", "mp4","png");
        fc.setFileFilter(filter);

     
        //fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
       

      
        browseButton = new JButton("Browse a File...",createImageIcon("Open16.gif"));
        browseButton.addActionListener(this);

        
        uploadButton = new JButton("Upload a File...",createImageIcon("Save16.gif"));
        uploadButton.addActionListener(this);

        
        JPanel buttonPanel = new JPanel(); 
        buttonPanel.add(browseButton);
        buttonPanel.add(uploadButton);

        
        add(buttonPanel, BorderLayout.CENTER);
        add(logScrollPane, BorderLayout.PAGE_START);
    }

    public void actionPerformed(ActionEvent e) {

        //Handle browse button action.
        if (e.getSource() == browseButton) {
            int returnVal = fc.showOpenDialog(SincreFileChooser_m.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                
                log.append("Uploading: " + file.getName() + "." + newline + "File Path:" + file.getPath());
            } else {
                log.append("Browse command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());

      
        } else if (e.getSource() == uploadButton) {
        	
        	//having troubles with getting the Jframe SincreDisplay_m.java to pop up after this button is pushed
        	//I have tried a few different approaches, but in the end I just put both guis together and formed
        	//one whole gui which is the SincreDisplay_alone.java file
        	
        	//new SincreDisplay_m();
        	
        	        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        	        f.add(new SincreDisplay_m(), BorderLayout.SOUTH);
        	        f.setSize(500, 800);
        	        f.setVisible(true);
        	        f.setLocationRelativeTo(null);
        	        frame.setVisible(false);
        }
    }

   
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = SincreFileChooser_m.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    
    private static void GUI() {
        //Create and set up the window.
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(new SincreFileChooser_m(), BorderLayout.SOUTH);
        

        //Display the window.
        frame.pack();
        frame.setSize(800, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        
        SwingUtilities.invokeLater(new Runnable() {
        	
            public void run() {
            	
                
                UIManager.put("swing.boldMetal", Boolean.FALSE); 
                GUI();
            }
        });
    }
}
