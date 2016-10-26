package mapgui;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.*;

public class MapApplication {
	
	public static void main(String[] args) {
		//create a frame for the application
        final JFrame frame = new JFrame("KoraMap");
        //make sure to shut down the application, when the frame is closed
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //create a panel for the applet and the button panel
        JPanel panel = new JPanel();
        
        //create an instance of your processing applet
        final MapDisplay applet = new MapDisplay();
        
        applet.init();
        
        //store the applet in panel
        panel.add(applet);
        
        //store the panel in the frame
        frame.add(panel);
        
        //assign a size for the frame
        //reading the size from the applet
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int screenWidth = gd.getDisplayMode().getWidth();
		int screenHeight = gd.getDisplayMode().getHeight();
        frame.setSize(screenWidth, screenHeight);
        
        //display the frame
        frame.setVisible(true);
	}
}
