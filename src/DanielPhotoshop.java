import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.*;

public class DanielPhotoshop extends JFrame{
	
	private static JLabel picture = new JLabel("Please load an image!");
	private static BufferedImage img;
	private static BufferedImage original;
	private static File file;
	public static final int RESTORE = 1;
	public static final int FLIP_HORIZONTAL = 2;
	public static final int FLIP_VERTICAL = 3;
	public static final int GRAY_SCALE = 4;
	public static final int SEPIA_TONE = 5;
	public static final int INVERT_COLOUR = 6;
	public static final int GAUSSIAN_BLUR = 7;
	public static final int BULGE_EFFECT = 8;
	public static Stack<BufferedImage> undoStack = new Stack<BufferedImage>();
	public static Stack<BufferedImage> redoStack = new Stack<BufferedImage>();
	
	public static void loadPicture() throws IOException {
		try {
			while (!undoStack.empty()) {
				undoStack.pop();
			}
			while (!redoStack.empty()) {
				redoStack.pop();
			}
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int returnVal = fc.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = fc.getSelectedFile();
				img = ImageIO.read(file);
				picture.setText("");
				original = ImageIO.read(file);
				picture.setIcon(new ImageIcon(img));
			}			
		} catch (Exception e) {
			picture.setIcon(null);
			picture.setText("Load Failed!");
		}
	}
	
	public static void actionToPicture(int direction){
		try {
			int width = img.getWidth();
			int height = img.getHeight();
			BufferedImage actionedOnPicture = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			for(int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int pixelDetail = img.getRGB(x, y);//has pixel values
					//gets pixel values and puts them into alpha, red, green, blue
			        int alpha = (pixelDetail>>24)&0xff;
			        int red = (pixelDetail>>16)&0xff;
			        int green = (pixelDetail>>8)&0xff;
			        int blue = pixelDetail&0xff;
					switch(direction) {
						case FLIP_HORIZONTAL:
							actionedOnPicture.setRGB((width - 1) - x, y, img.getRGB(x, y));
							break;
						case FLIP_VERTICAL:
							actionedOnPicture.setRGB(x, (height - 1) - y, img.getRGB(x, y));
							break;
						case GRAY_SCALE:
					        int avg = (red + green + blue) / 3; // averages out the rgb values
					        pixelDetail = (alpha<<24) | (avg<<16) | (avg<<8) | avg;
					        actionedOnPicture.setRGB(x, y, pixelDetail);
					        break;
					    case SEPIA_TONE:
					    	int tr = (int)(0.393 * red + 0.769 * green + 0.189 * blue);
					        int tg = (int)(0.349 * red + 0.686 * green + 0.168 * blue);
					        int tb = (int)(0.272 * red + 0.534 * green + 0.131 * blue);
					        red = (tr > 255)? 255: tr;
					        green = (tg > 255)? 255: tg;
					        blue = (tb > 255)? 255: tb;
					        pixelDetail = (alpha<<24) | (red<<16) | (green<<8) | blue;
					        actionedOnPicture.setRGB(x, y, pixelDetail);
					    	break;
					    case INVERT_COLOUR:
					    	red = 255 - red;
					        green = 255 - green;
					        blue = 255 - blue;
					        pixelDetail = (alpha<<24) | (red<<16) | (green<<8) | blue;
        					actionedOnPicture.setRGB(x, y, pixelDetail);
					    	break;
					    case BULGE_EFFECT:
						    	int distanceX = x - width / 2;
			    				int distanceY = y - height / 2;
			    				Double distanceRadius = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
			    				Double newRadius = (Math.pow((distanceRadius * 0.0975), 1.7));
			    				Double angle = Math.atan2(distanceY, distanceX);
			    				int newX = (int)(newRadius * Math.cos(angle) + width / 2);
			    				int newY = (int)(newRadius * Math.sin(angle) + height / 2);
			    				if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
			    					int pixelDetails = img.getRGB(newX, newY);
			    					actionedOnPicture.setRGB(x, y, pixelDetails);
			    				}
					    	break;
					}
				}
			}
			img = actionedOnPicture;
			picture.setIcon(new ImageIcon(img));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void gaussianBlur() {
		try {
			int width = img.getWidth();
			int height = img.getHeight();
			BufferedImage actionedOnPicture = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			//removing the black border
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int p = img.getRGB(x, y);
					actionedOnPicture.setRGB(x, y, p);
				}
			}
			//code for gaussian blur
			for (int y = 2; y < height - 2; y++) {
				for (int x = 2; x < width - 2; x++) {
					int p = img.getRGB(x, y);
					double[][] blurValues = new double[5][5];//holds values for blur to be multipled by
					int[][] pixelColour = new int[5][5];// base pixel values
					int pixelTemp = img.getRGB(x, y);
					int alphaValue = (pixelTemp>>24)&0xff;
					int[][] redValue = new int[5][5];
					int[][] greenValue = new int[5][5];
					int[][] blueValue = new int[5][5];
					double[] rgbValues = {0, 0, 0};
					double total = 0;
					for (int pixelY = -2; pixelY <= 2; pixelY++) {
						for (int pixelX = -2; pixelX <= 2; pixelX++) {
							pixelColour[pixelY + 2][pixelX + 2] = img.getRGB(x + pixelX, y + pixelY);
					        redValue[pixelY + 2][pixelX + 2] = (pixelColour[pixelY + 2][pixelX + 2]>>16)&0xff;
					        greenValue[pixelY + 2][pixelX + 2] = (pixelColour[pixelY + 2][pixelX + 2]>>8)&0xff;
					        blueValue[pixelY + 2][pixelX + 2] = pixelColour[pixelY + 2][pixelX + 2]&0xff;
					        blurValues[pixelY + 2][pixelX + 2] = 1 / (2 * Math.PI * 1.5 * 1.5) * Math.pow(Math.E, -(pixelX * pixelX + pixelY * pixelY) / (2 * 1.5 * 1.5));
					        total += blurValues[pixelY + 2][pixelX + 2];
						}
					}
					for (int pixelY = 0; pixelY < 5; pixelY++) {
						for (int pixelX = 0; pixelX < 5; pixelX++) {
							blurValues[pixelY][pixelX] = blurValues[pixelY][pixelX] / total;
							rgbValues[0] += redValue[pixelY][pixelX] * blurValues[pixelY][pixelX];
							rgbValues[1] += greenValue[pixelY][pixelX] * blurValues[pixelY][pixelX];
							rgbValues[2] += blueValue[pixelY][pixelX] * blurValues[pixelY][pixelX];
						}
					}
					p = (alphaValue<<24) | ((int)rgbValues[0]<<16) | ((int)rgbValues[1]<<8) | (int)rgbValues[2];
					actionedOnPicture.setRGB(x, y, p);
				}
			}
			img = actionedOnPicture;
			picture.setIcon(new ImageIcon(img));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
			
    public static void a_e_s_t_h_e_t_i_c() {
	    	try {	
	    		//UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
	    		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    	} catch (Exception e) {
	    		System.out.println("Error: Unsupported Look And Feel");
	    	}
	}
    
    public static void main(String[] args) {
	    	a_e_s_t_h_e_t_i_c();
	    	JFrame frame = new JFrame("ICS Summative - Photoshop");
	    	JPanel panel = new JPanel();
	
	    	//Menu
	    	//Menu1 - File manipulation
	    	JMenuBar menuBar = new JMenuBar();
	    	JMenu menu1 = new JMenu("File");
	    	JMenu menu2 = new JMenu("Options");
	    	menuBar.add(menu1);
	    	menuBar.add(menu2);
	    	
	    	JMenuItem fileOpen = new JMenuItem("Open");
	    	JMenuItem fileSaveAs = new JMenuItem("Save as");
	    	JMenuItem fileExit = new JMenuItem("Exit");
	    	JMenuItem undo = new JMenuItem("Undo");
	    	JMenuItem redo = new JMenuItem("Redo");
	    	menu1.add(fileOpen);
	    	menu1.add(fileSaveAs);
	    	menu1.addSeparator();
	    	menu1.add(fileExit);
	    	menu1.addSeparator();
	    	menu1.add(undo);
	    	menu1.add(redo);
	    	
	    	fileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
	    	fileSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
	    	fileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
	    	
	    	fileOpen.addActionListener(new fileManipulation());
	    	fileSaveAs.addActionListener(new fileManipulation());
	    	fileExit.addActionListener(new fileManipulation());
	    	undo.addActionListener(new fileManipulation());
	    	redo.addActionListener(new fileManipulation());
	    	
	    	fileOpen.setActionCommand("1");
	    	fileSaveAs.setActionCommand("2");
	    	fileExit.setActionCommand("3");
	    	undo.setActionCommand("4");
	    	redo.setActionCommand("5");
	    	
	    	//Menu 2-Picture manipulation
	    	JMenuItem optionsRestore = new JMenuItem("Restore to Original");
	    	JMenuItem optionsHorizontal = new JMenuItem("Horizontal Flip");
	    	JMenuItem optionsVertical = new JMenuItem("Vertical Flip");
	    	JMenuItem optionsGray = new JMenuItem("Gray Scale");
	    	JMenuItem optionsSepia = new JMenuItem("Sepia Tone");
	    	JMenuItem optionsInvert = new JMenuItem("Invert Colour");
	    	JMenuItem optionsGaussian = new JMenuItem("Gaussian Blur");
	    	JMenuItem optionsBulge = new JMenuItem("Bulge Effect");
	    	menu2.add(optionsRestore);
	    	menu2.addSeparator();
	    	menu2.add(optionsHorizontal);
	    	menu2.add(optionsVertical);
	    	menu2.add(optionsGray);
	    	menu2.add(optionsSepia);
	    	menu2.add(optionsInvert);
	    	menu2.add(optionsGaussian);
	    	menu2.add(optionsBulge);
	    
	    	optionsRestore.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
	    	optionsHorizontal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
	    	optionsVertical.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
	    	optionsGray.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
	    	optionsSepia.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
	    	optionsInvert.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
	    	optionsGaussian.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.CTRL_MASK));
	    	optionsBulge.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
	    	
	    optionsRestore.addActionListener(new pictureManipulation());
	    optionsHorizontal.addActionListener(new pictureManipulation());
	    optionsVertical.addActionListener(new pictureManipulation());
	    optionsGray.addActionListener(new pictureManipulation());
	    optionsSepia.addActionListener(new pictureManipulation());
	    optionsInvert.addActionListener(new pictureManipulation());
	    optionsGaussian.addActionListener(new pictureManipulation());
	    optionsBulge.addActionListener(new pictureManipulation());
	    
	    optionsRestore.setActionCommand("1");
	    optionsHorizontal.setActionCommand("2");
	    optionsVertical.setActionCommand("3");
	    optionsGray.setActionCommand("4");
	    optionsSepia.setActionCommand("5");
	    optionsInvert.setActionCommand("6");
	    optionsGaussian.setActionCommand("7");
	    optionsBulge.setActionCommand("8");
	    
	    //Panel
	    panel.add(picture);
	    
	    //Frame
	    frame.add(panel);
	    frame.setSize(1100, 1025);
	   	frame.setResizable(false);
	   	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	   	frame.setJMenuBar(menuBar);
	   	frame.setVisible(true);
    }
 
    private static class fileManipulation implements ActionListener {
    	
		public void actionPerformed (ActionEvent evt) {
			int command = Integer.valueOf(evt.getActionCommand());
			switch(command) {
				case 1: //Load file
					try {
						loadPicture();
					} catch (Exception e) {
						picture = new JLabel("Load failed!");
					}
					break;
				case 2://Save file
					try {
						JFileChooser fc = new JFileChooser();
						int valueSave = fc.showSaveDialog(null);
						if (valueSave == JFileChooser.APPROVE_OPTION) {
							File file2 = fc.getSelectedFile();
							ImageIO.write(img, "png", file2);
						}
					} catch (Exception e) {
						
					}
					break;
				case 3://Close program
					System.exit(0);
					break;
				case 4://Undo
					if (!undoStack.empty()) {
						redoStack.push(img);
						img = undoStack.pop();
						picture.setIcon(new ImageIcon(img));
					}
					break;
				case 5://Redo
					if (!redoStack.empty()) {
						undoStack.push(img);
						img = redoStack.pop();
						picture.setIcon(new ImageIcon(img));
					}
					break;
			}
		}
    }
    
    private static class pictureManipulation implements ActionListener {
    	
		public void actionPerformed (ActionEvent evt) {
			int command = Integer.valueOf(evt.getActionCommand());
			switch(command) {
				case RESTORE:
					img = original;
					picture.setIcon(new ImageIcon(img));
					while (!undoStack.empty()) {
						undoStack.pop();
					}
					while (!redoStack.empty()) {
						redoStack.pop();
					}
					undoStack.push(img);
					break;
				case GAUSSIAN_BLUR:
					undoStack.push(img);
					gaussianBlur();
					break;
				default:
					undoStack.push(img);
					actionToPicture(command);
				break;
			}
		}
	}
}


