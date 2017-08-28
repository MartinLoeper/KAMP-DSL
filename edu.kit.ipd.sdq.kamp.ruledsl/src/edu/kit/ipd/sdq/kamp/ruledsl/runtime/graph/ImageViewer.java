package edu.kit.ipd.sdq.kamp.ruledsl.runtime.graph;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

// not needed for the task to work, but the debug feature show graph uses it
public class ImageViewer extends JFrame {
	private static ImageViewer currentApp;
	private String primaryPath;
	
	public ImageViewer(String path) { 
        currentApp = this;	
        primaryPath = path;
	} 
	
	public static void close() {
		if(currentApp != null) {
			try {
				currentApp.dispose();
			} catch (Exception e) { }
		}
	}
	
	public static void init(String path) {
		 EventQueue.invokeLater(() -> {
			 currentApp = new ImageViewer(path);
			 currentApp.setVisible(true);
			 Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		     currentApp.setBounds(0,0, (int) (screenSize.width * 0.8), (int) (screenSize.height * 0.8));
			 currentApp.toFront();
			 currentApp.repaint();
			 
			 try {
				currentApp.showNewImageViewer();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	     });
	}

    private void showNewImageViewer() throws IOException {
    	BufferedImage myPicture = ImageIO.read(new File(primaryPath));
    	JLabel picLabel = new JLabel(new ImageIcon(myPicture));
    	add(picLabel);
	}
}