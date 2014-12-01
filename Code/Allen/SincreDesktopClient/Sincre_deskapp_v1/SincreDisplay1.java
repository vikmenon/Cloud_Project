package sincre.dapp.com;


import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.BrowserFactory;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


class SincreDisplay_m extends JFrame {

	
	
	static JTextField text = new JTextField("nothing") ;
	static JFrame frame = new JFrame();
	public static Object DISPOSE_ON_CLOSE;

	public static void main(String[] args) {
        final Browser browser = BrowserFactory.create();

        
        browser.addLoadListener(new LoadAdapter() {
            @Override
            public void onFinishLoadingFrame(FinishLoadingEvent event) {
                if (event.isMainFrame()) {
       
                    System.out.println("URL = " + browser.getURL());
                }
            }
        });
        
       
        
        JButton movie1 = new JButton("1st Match");
        movie1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	browser.loadURL("https://s3.amazonaws.com/starkeallen/Jurassic.mp4");
            	text.setText(browser.getURL());
            }
        });

        JButton movie2 = new JButton("2nd Match");
        movie2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	browser.loadURL("https://s3.amazonaws.com/starkeallen/Purge.mp4");
            	text.setText(browser.getURL());
            }
        });
        
        JButton movie3 = new JButton("3rd Match");
        movie3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	browser.loadURL("https://s3.amazonaws.com/starkeallen/sample.mp4");
            	text.setText(browser.getURL());
            }
        });
        
        JButton movie4 = new JButton("4th Match");
        movie4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				browser.loadURL("https://s3.amazonaws.com/starkeallen/Dumb.mp4");
				text.setText(browser.getURL());
            }
        });
        
        JButton movie5 = new JButton("5th Match");
        movie5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				browser.loadURL("https://s3.amazonaws.com/starkeallen/Dumb.mp4");
				text.setText(browser.getURL());
            }
        });
        
        JButton movie6 = new JButton("6th Match");
        movie6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				browser.loadURL("https://s3.amazonaws.com/starkeallen/Dumb.mp4");
				text.setText(browser.getURL());
            }
        });
        
        JButton movie7 = new JButton("7th Match");
        movie7.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	text.setText(browser.getURL());
            }
        });
        
        JButton movie8 = new JButton("8th Match");
        movie8.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				browser.loadURL("https://s3.amazonaws.com/starkeallen/Dumb.mp4");
				text.setText(browser.getURL());
            }
        });
        
        JButton movie9 = new JButton("9th Match");
        movie9.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				browser.loadURL("https://s3.amazonaws.com/starkeallen/Dumb.mp4");
				text.setText(browser.getURL());
            }
        });
        
        JButton movie0 = new JButton("10th Match");
        movie0.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				browser.loadURL("https://s3.amazonaws.com/starkeallen/Dumb.mp4");
				text.setText(browser.getURL());
            }
        });

        
        
        JPanel toolBar = new JPanel();
        toolBar.add(movie1);
        toolBar.add(movie2);
        toolBar.add(movie3);
        toolBar.add(movie4);
        toolBar.add(movie5);
        toolBar.add(movie6);
        toolBar.add(movie7);
        toolBar.add(movie8);
        toolBar.add(movie9);
        toolBar.add(movie0);
        toolBar.add(text);

        
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(browser.getView().getComponent(), BorderLayout.CENTER);
        frame.add(toolBar, BorderLayout.SOUTH);
        frame.setSize(1500, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

        
    }
