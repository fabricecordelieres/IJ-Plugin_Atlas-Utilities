/**
 * tools.java
 * 
 * Created on 2 août 2023
 * Fabrice P. Cordelieres, fabrice.cordelieres at gmail.com
 * 
 * Copyright (C) 2023 Fabrice P. Cordelieres
 *
 * License:
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package utilities;

import java.awt.Button;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;

/**
 * This class contains miscellaneous static helper functions and methods
 */
public class tools {
	/**
	 * Builds an array containing the titles of all ResulsTable.
	 * @return an array containing the titles of all ResulsTable or a singleton with 
	 * the default message (stored in defaultNoRTMsg) if none was found
	 */
	public static String[] getTablesList() {
		ArrayList<String> tables=new ArrayList<String>();

		String[] nonImageList=WindowManager.getNonImageTitles();

		for (int i=0; i<nonImageList.length; i++) {
			if(ResultsTable.getResultsTable(nonImageList[i])!=null) tables.add(nonImageList[i]);
		}

		String[] titles=new String[] {"<No ResultsTable found>"}; //Default: no RT is opened
		if(!tables.isEmpty()) {
			tables.toArray(titles);
		}

		return titles;
	}

	/**
	 * Safely returns the z position of the active ImagePlus (if any)
	 * @return the actual slice if an ImagePlus is present, -1 otherwise
	 */
	public static int getSliceSafe() {
		ImagePlus ip=WindowManager.getCurrentImage();
		if(ip!=null) {
			return ip.getSlice();
		}
		return -1;
	}

	/**
	 * Safely sets the slice of the active ImagePlus' z position (if any, and z value is Ok)
	 * @param z the slice to activate, if an ImagePlus is active and 1<=z<=ip.getNSlices() 
	 * @return the actual slice if an ImagePlus is present, -1 otherwise
	 */
	public static void setSliceSafe(int z) {
		ImagePlus ip=WindowManager.getCurrentImage();
		if(ip!=null) {
			if(1<=z && z<=ip.getNSlices()) ip.setSlice(z);
		}
	}

	/**
	 * Formats an input number of milliseconds expressed as long into a HH:MM:SS.msec String
	 * @param nbMillisec an input number of milliseconds to express as a HH:MM:SS.msec formatted String
	 * @return an input number of milliseconds expressed as long into a HH:MM:SS.msec String
	 */
	public static String formatInterval(long nbMillisec){
		final long hr = TimeUnit.MILLISECONDS.toHours(nbMillisec);
		final long min = TimeUnit.MILLISECONDS.toMinutes(nbMillisec - TimeUnit.HOURS.toMillis(hr));
		final long sec = TimeUnit.MILLISECONDS.toSeconds(nbMillisec - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
		final long ms = TimeUnit.MILLISECONDS.toMillis(nbMillisec - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec));
		return String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
	}

	/**
	 * Capitalizes the input String
	 * @param line the input String to capitalize
	 * @return the capitalized input String
	 */
	public static String capitalize(String line) {
		return Character.toUpperCase(line.charAt(0)) + line.substring(1);
	}

	/**
	 * Sanitizes an input path by checking and adding if necessary the ending File.separator
	 * @param path the input path to sanitize
	 * @return A sanitized version of the input path
	 */
	public static final String checkDirectoryFormat(String path) {
		return path.endsWith(File.separator)?path:path+File.separator;
	}

	/**
	 * Sanitizes an input path by checking if it ends by the extension and adding it if mission
	 * @param path the input path to sanitize
	 * @param extension the extension the path should end with
	 * @return A sanitized version of the input path
	 */
	public static final String checkFileExtension(String path, String extension) {
		extension=(extension.startsWith(".")?"":".")+extension;
		return path+(path.toLowerCase().endsWith(extension.toLowerCase())?"":extension);
	}

	/**
	 * Changes the color scheme of the input icon depending on the input target color
	 * @param imgIcon the input ImageIcon
	 * @param color the targeted color
	 * @return an new ImageIcon, recolored
	 */
	public static ImageIcon changeColor(ImageIcon imgIcon, Color color) {
		//Convert ImageIcon to BufferedImage
		BufferedImage img = new BufferedImage(imgIcon.getIconWidth(), imgIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = img.createGraphics();
		imgIcon.paintIcon(null, graphics, 0, 0);
		graphics.dispose();

		BufferedImage coloredImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);

		//Change color
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int rgb = img.getRGB(x, y);
				Color pixColor = new Color(rgb, true);
				if (pixColor.getAlpha() > 0) {
					Color pixelColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), pixColor.getAlpha());
					coloredImg.setRGB(x, y, pixelColor.getRGB());
				}
			}
		}
		return new ImageIcon(coloredImg);
	}

	/**
	 * Changes the color scheme of the input icon depending on the input target color
	 * @param imgIcon the input ImageIcon
	 * @param color the targeted color
	 * @return an new ImageIcon, recolored
	 */
	public static ImageIcon sumChangeColor(ImageIcon imgIconBase, ImageIcon imgIconToColor, Color color) {
		//Convert ImageIcon to BufferedImage
		BufferedImage imgBase = new BufferedImage(imgIconBase.getIconWidth(), imgIconBase.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphicsBase = imgBase.createGraphics();
		imgIconBase.paintIcon(null, graphicsBase, 0, 0);
		graphicsBase.dispose();

		BufferedImage imgColor = new BufferedImage(imgIconToColor.getIconWidth(), imgIconToColor.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphicsColor = imgColor.createGraphics();
		imgIconToColor.paintIcon(null, graphicsColor, 0, 0);
		graphicsColor.dispose();

		//Change color
		for (int y = 0; y < imgBase.getHeight(); y++) {
			for (int x = 0; x < imgBase.getWidth(); x++) {
				int rgb = imgColor.getRGB(x, y);
				Color pixColor = new Color(rgb, true);
				if (pixColor.getAlpha() > 0) {
					Color pixelColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), pixColor.getAlpha());
					imgBase.setRGB(x, y, pixelColor.getRGB());
				}
			}
		}
		return new ImageIcon(imgBase);
	}

	/**
	 * Creates a simple Yes/Cancel Dialog box using the input title and message
	 * @param title the title of the dialog box
	 * @param msg the message to be displayed
	 * @return true if the box was Oked, false otherwise
	 */
	public static boolean confirmationDialog(String title, String msg) {
		GenericDialog gd = new GenericDialog(title);
		gd.addMessage(msg);
		((Button) gd.getButtons()[0]).setLabel("No");
		((Button) gd.getButtons()[1]).setLabel("Yes");
		gd.showDialog();
		
		return gd.wasCanceled(); //Before renaming the buttons, the Yes button was the Ok one...
	}
}
