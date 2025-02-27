import java.awt.AWTEvent;



/**TODO Export: on a plusieurs fois les mesures de la même ROI **/
/**TODO Faire le distingo droite/gauche. Intercept avec demi-champ ? Ajout à l'ontologie ? **/

import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Panel;
import java.awt.TextField;
import java.util.Vector;

import dataModel.AtlasDataContainer;
import dataReader.decodeLabelImage;
import dataReader.readStructureFile;
import dataViewer.dataViewer;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import utilities.pluginsInfo;
import utilities.tools;

/**
 * Atlas_to_Rois.java
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

/**
 * This plugin is aimed at extracting Rois of anatomical structures from an input
 * labeled map image. Pixels should carry an intensity corresponding to one of the
 * IDs present in the structure tree file either provided as part of the jar or 
 * opened in a ResultsTable. In case the ID is not found, the Roi is names "Unknown"+ID. 
 */
public class Atlas_to_Rois implements PlugIn, DialogListener{
	/** List of all opened images at plugin's startup */
	String[] imagesList=WindowManager.getImageTitles();
	
	/** List of all opened ResultsTable at plugin's startup */
	String[] resultsList=tools.getTablesList();
	
	/** Options for structure tree file type selection */
	static final String[] sourceStructureFiles=new String[]{"ResultsTable", "Included file"}; 
	
	/** Options for included structure tree file type selection */
	static final String[] includedStructureFiles=new String[]{"structure_tree_safe_2017.csv", "structure_tree_safe.csv"};
	
	/** The graphical user interface */
	GenericDialog gd=null;
	
	/** Stores the labeled image to be used */
	ImagePlus atlasImage=null;
	
	/** Stores the structure tree file type*/
	int source=0;
	
	/** Titles of the ResultsTable containing the structure tree description */ 
	String resultsTable="";
	
	/** Stores the index of the selected provided structure tree file */
	int includedFileIndex=(int) Prefs.get("AtlasToRoi.includedFile", 0);
	
	/** Stores the path to the directory where output should be saved */
	String outPath="";
	
	/** Stores the name to give to the output file */
	String outFilename=Prefs.get("AtlasToRoi.outFilename", "AtlasDataContainer.zon");
	
	/** True if ZON file (ZIP file containing the JSON file) should be saved */
	boolean saveZON=Prefs.get("AtlasToRoi.saveZON", false);
	
	/** True if Rois should be sent to the Roi Manager */
	boolean addToRoiManager=Prefs.get("AtlasToRoi.addToRoiManager", true);
	
	/** True if the Atlas Data Container should be sent to the data viewer */
	boolean sendToViewer=Prefs.get("AtlasToRoi.sendToViewer", true);
	
	@Override
	public void run(String arg) {
		if(imagesList.length<1) {
			IJ.error("Atlas to ROIs", "At least one image should be opened");
		}else {
			GUI();
			if(gd.wasOKed()) {
				getUserInput();
				process();
			}
		}
		
	}
	
	/**
	 * Displays the plugin's graphical user interface
	 * @return
	 */
	public void GUI() {
		gd=new GenericDialog("Atlas to ROIs "+pluginsInfo.ATLAS_TO_ROI_VERSION+" ("+pluginsInfo.ATLAS_TO_ROI_DATE+")");
		
		//Modifies the Ok button availability depending on options
		boolean okState=((!outPath.isEmpty() && !outFilename.isEmpty() && saveZON) || (!saveZON && (addToRoiManager || sendToViewer)));
		gd.getButtons()[0].setEnabled(okState);
		
		gd.addMessage("<html><b>Image</b></html>");
		gd.addChoice("Atlas_image", imagesList, imagesList[0]);
		gd.addMessage("");
		
		gd.addMessage("<html><b>Structure_Tree_File</b></html>");
		gd.addChoice("Source", sourceStructureFiles, sourceStructureFiles[source]);
		gd.addChoice("ResultsTable", resultsList, resultsList[0]);
		gd.addChoice("Included_file", includedStructureFiles, includedStructureFiles[includedFileIndex]);
		gd.addMessage("");
		
		gd.addMessage("<html><b>Outputs</b></html>");
		gd.addDirectoryField("Save_outputs_to", outPath);
		gd.addStringField("Output_filename", outFilename, 25);
		gd.addCheckbox("ZON_file", saveZON);
		gd.addCheckbox("Add_to_RoiManager", addToRoiManager);
		gd.addCheckbox("Send_to_Atlas_Viewer", sendToViewer);
		
		
		gd.addMessage("");
		
		gd.addMessage(pluginsInfo.CONTACT);
		
		gd.addDialogListener(this);
		dialogItemChanged(gd, null);
		
		gd.showDialog();
	}
	
	/**
	 * Populates the class' variables using the user's input
	 */
	public void getUserInput() {
		atlasImage=WindowManager.getImage(gd.getNextChoice());
		
		source=gd.getNextChoiceIndex();
		resultsTable=gd.getNextChoice();
		includedFileIndex=gd.getNextChoiceIndex();
		
		outPath=tools.checkDirectoryFormat(gd.getNextString());
		outFilename=tools.checkFileExtension(gd.getNextString(), ".zon");
		saveZON=gd.getNextBoolean();
		addToRoiManager=gd.getNextBoolean();
		sendToViewer=gd.getNextBoolean();
		
		
		Prefs.set("AtlasToRoi.includedFile", includedFileIndex);
		Prefs.set("AtlasToRoi.outFilename", outFilename);
		Prefs.set("AtlasToRoi.saveZON", saveZON);
		Prefs.set("AtlasToRoi.addToRoiManager", addToRoiManager);
		Prefs.set("AtlasToRoi.sendToViewer", sendToViewer);	
	}
	
	/**
	 * Performs the extraction:
	 * 1-Initiates an AtlasDataContainer object
	 * 2-Retrieves metadata from the input image and from the structure tree file 
	 * 3-Reads the structure tree file a
	 * 4-Decodes the image into Rois
	 * 
	 * All data are stored into the  AtlasDataContainer object
	 * Optional:
	 * -Saves the AtlasDataContainer object as a JSON file
	 * -Saves the AtlasDataContainer object as a ZON file (JSON file in a zip)
	 * -Pushes Rois to the Roi Manager
	 */
	public void process() {
		long start=System.currentTimeMillis();
		
		AtlasDataContainer adc=new AtlasDataContainer();
		IJ.showStatus("!Creating Atlas Data Container");
		
		adc.retrieveDimensions(atlasImage);
		adc.retrieveMetadataFromAnnotations(atlasImage);
		adc.setStructureTreeFileInfos(sourceStructureFiles[source], source==0?resultsTable:includedStructureFiles[includedFileIndex]);
		
		readStructureFile rsf=new readStructureFile(adc);
		
		switch (source) {
			case 0:
				rsf.loadFromResultsTable(resultsTable);
				break;

			case 1:
				if(includedFileIndex==0) {
					rsf.loadStructureTreeSafe2017();
				}else {
					rsf.loadStructureTreeSafe();
				}
				break;
		}
		
		decodeLabelImage dli=new decodeLabelImage(atlasImage, adc);
		//dli.debug=true;
		
		dli.decode();
		adc.rebuildNameIdMap();
		adc.rebuildMeasurementsPerStructure();
		
		if(saveZON) adc.saveAsZON(outPath+outFilename);
		
		if(addToRoiManager) adc.toRoiManager();
		
		if(sendToViewer) new dataViewer(adc, "from Atlas to Rois").setVisible(true);
		
		long end=System.currentTimeMillis();
		IJ.showStatus("!Atlas to Roi - Done in "+tools.formatInterval(end-start));
	}
	
	

	@SuppressWarnings("rawtypes")
	@Override
	/**
	 * Handles the user made changes in the GUI
	 */
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		Vector choices= gd.getChoices(); //0: Atlas Img; 1: Source Structure; 2: ResultsTable; 3: Included Files
		Vector txtFields=gd.getStringFields(); //0: Save Folder
		Vector chkBoxes=gd.getCheckboxes(); //0: JSON; 1: ZON; 2: Add to Manager
		Vector<Panel> panels=getPanels(); //0: Save Folder
		
		Choice sourceStructure=((Choice) choices.get(1));
		Choice resultTable=((Choice) choices.get(2));
		Choice includedStructure=((Choice) choices.get(3));
		
		TextField savePath=((TextField) txtFields.get(0));
		TextField saveName=((TextField) txtFields.get(1));
		
		
		Panel savePanel=panels.get(0);
		
		Checkbox ZON=((Checkbox) chkBoxes.get(0));
		Checkbox addToRM=((Checkbox) chkBoxes.get(1));
		Checkbox sendToViewer=((Checkbox) chkBoxes.get(2));
		
		
		
		//In case no ResultsTable exists, the only possibility is to use the provided structure files
		if(resultTable.getSelectedItem()=="<No ResultsTable found>") {
			sourceStructure.select(1);
			sourceStructure.setEnabled(false);
		}
		
		boolean srcIsRT=sourceStructure.getSelectedIndex()==0;
		resultTable.setEnabled(srcIsRT);
		includedStructure.setEnabled(!srcIsRT);
		
		savePanel.setEnabled(ZON.getState());
		saveName.setEnabled(ZON.getState());
		
		/*
		 * Ok button to be displayed if:
		 * - JSON/ZON is selected and output path has been provided
		 * - Only ROI Manager output has been chosen
		 */
		
		return ((!savePath.getText().isEmpty() && !saveName.getText().isEmpty() && ZON.getState()) || (!ZON.getState() && (addToRM.getState() || sendToViewer.getState())));
	}
	
	/**
	 * Retrieves all the panels in the GUI: used to enable/disable the output path choice box
	 * @return a vector containing all the panels in the GUI
	 */
	public Vector<Panel> getPanels() {
		Vector<Panel> out=new Vector<Panel>();
		Component[] cArray=gd.getComponents();
		for(Component c: cArray) {
			if(c.getClass()==Panel.class) {
				out.add((Panel) c);
			}
		}
		
		return out;
	}
}
