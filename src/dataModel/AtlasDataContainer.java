/**
 * AtlasDataContainer.java
 * 
 * Created on 3 août 2023
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

package dataModel;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.process.StackStatistics;
import utilities.jsonRoiEntry;
import utilities.tools;

/**
 * This class stores all information related to the metadata, the structure tree file,
 * the extracted Rois and measurements.
 */
public class AtlasDataContainer{
	/** Stores all the metadata */
	public MetadataContainer Metadata=new MetadataContainer();

	/** Stores all information, sorted by IDs */
	public TreeMap<Integer, AtlasEntry> Atlas=new TreeMap<Integer, AtlasEntry>();

	/** Stores a table of correspondence name/id */
	public TreeMap<String, Integer> nameId=new TreeMap<String, Integer>();

	/** Stores a table of selection profiles (name of the profile/ArrayList<Roi Indexes>) */
	public TreeMap<String, Set<Integer>> selectionProfiles=new TreeMap<String, Set<Integer>>();
	
	
	/** Analysis level */
	public static final String[]  ANALYSIS_LEVEL=new String[] {"Per Structure", "Per Roi"};
	
	/** Analysis measurement */
	public static final String[]  ANALYSIS_MEASUREMENT=new String[] {"Sum Intensity", "Mean Intensity", "Std Intensity", "Min Intensity", "Max Intensity", "ID"};
	
	/** Analysis normalization */
	public static final String[]  ANALYSIS_NORM=new String[] {"No Normalization", "100%=All Structures", "100%=Selected Structures"};
	

	/**
	 * Populates the metadata related to the provided labeled image
	 * @param ip the input labeled image, provided as an ImagePlus
	 */
	public void retrieveMetadataFromAnnotations(ImagePlus ip) {
		Metadata.getInfosFromAnnotationsImage(ip);
	}

	/**
	 * Populates the metadata related to the provided image on which
	 * quantifications are performed, provided as an ImagePlus
	 * @param ip the input image on which quantifications are performed, 
	 * as an ImagePlus
	 */
	public void retrieveMetadataFromQuantifications(ImagePlus ip) {
		Metadata.getInfosFromQuantificationsImage(ip);
	}

	/**
	 * Populates the metadata related to the dimensions of the input image, 
	 * provided as an ImagePlus
	 * @param ip the input image on which dimensions are extracted, 
	 * as an ImagePlus
	 */
	public void retrieveDimensions(ImagePlus ip) {
		Metadata.getDimensions(ip);
	}

	/**
	 * Populates the metadata related to the structure tree file
	 * @param option a String describing the file source (ResultsTable or Provided file)
	 * @param path a String describing the file's path
	 */
	public void setStructureTreeFileInfos(String option, String path) {
		Metadata.setStructureTreeFileInfos(option, path);
	}

	/**
	 * Pushes an AtlasEntry object to the AtlasDataContainer
	 * @param id index to associate the entry with, as an Integer
	 * @param entry the AtlasEntry object to push to the AtlasDataContainer
	 */
	public void putInAtlas(int id, AtlasEntry entry) {
		Atlas.put(id, entry);
		nameId.put(entry.name, id);
	}

	/**
	 * (Re)builds the table of correspondence name/id
	 */
	public void rebuildNameIdMap() {
		nameId=new TreeMap<String, Integer>();
		for(Integer key: Atlas.keySet()) nameId.put(Atlas.get(key).name, Atlas.get(key).id);
	}

	/**
	 * (Re)calculates the measurements of each structure
	 */
	public void rebuildMeasurementsPerStructure() {
		for(Integer id: Atlas.keySet()) {
			MeasurementsEntry meStruct=new MeasurementsEntry();
			int nIt=0;
			for(Integer roi: Atlas.get(id).ROIs.keySet()){
				MeasurementsEntry meRoi=Atlas.get(id).ROIs.get(roi).Measurements;
				if(meRoi!=null) {
					meStruct.volume+=meRoi.area;
					meStruct.totalIntensity+=meRoi.totalIntensity;
					meStruct.minIntensity=nIt==0?meRoi.minIntensity:Math.min(meRoi.minIntensity, meStruct.minIntensity);
					meStruct.maxIntensity=nIt==0?meRoi.maxIntensity:Math.min(meRoi.maxIntensity, meStruct.maxIntensity);
					meStruct.stdIntensity+=(meRoi.area-1)*meRoi.stdIntensity*meRoi.stdIntensity;
					nIt++;
				}
			}
			meStruct.meanIntensity=meStruct.volume==0?0:meStruct.totalIntensity/meStruct.volume;
			meStruct.stdIntensity=(meStruct.volume-nIt)==0?0:Math.sqrt(meStruct.stdIntensity/(meStruct.volume-nIt));
			Atlas.get(id).Measurements=meStruct;
		}
	}

	//TODO: normalisation à 100% pour la somme de toutes les ROIs sélectionnées
	public void measure(ImagePlus ip) {
		long start=System.currentTimeMillis();
		final AtomicInteger ai = new AtomicInteger(1);
		final Thread[] threads=new Thread[Runtime.getRuntime().availableProcessors()];

		//Get an array with all ids
		int[] ids=nameId.values().stream().mapToInt(Number::intValue).toArray();

		//Prepare threads with what needs to be done
		for (int ithread = 0; ithread < threads.length; ithread++){
			//final int ithreadNb=ithread; // To be able to pass it in the run
			threads[ithread] = new Thread(){
				public void run() {
					for (int id = 0; id <ids.length; id = ai.getAndIncrement()) {
						ArrayList<Roi> rois=getRois(ids[id]);
						for(int roiIndex=0; roiIndex<rois.size(); roiIndex++) {
							Roi roi=rois.get(roiIndex); //Get the n-th roi
							int slice=roi.getZPosition(); //Retrieve slice nb which also is the index in the ROIs objects

							ImageProcessor iproc=ip.getStack().getProcessor(slice);
							iproc.setRoi(roi);

							//Builds new MeasurementsEntry object and pushes data to it
							MeasurementsEntry me=new MeasurementsEntry();
							me.area=iproc.getStatistics().area;
							me.meanIntensity=iproc.getStatistics().umean;
							me.stdIntensity=iproc.getStatistics().stdDev;
							me.totalIntensity=me.meanIntensity*me.area;
							me.minIntensity=iproc.getStatistics().min;
							me.maxIntensity=iproc.getStatistics().max;

							//Replaces the Measurements with new values
							Atlas.get(ids[id]).ROIs.get(slice).Measurements=me;

							iproc.resetRoi();
						}
						//IJ.showStatus("!Measuring image - Thread "+(ithreadNb+1)+"/"+threads.length+" returned results for ID "+ids[id]+"("+rois.size()+" rois(s))");
					}
				}

			};
		}

		//Initialize threads
		for (int ithread = 0; ithread < threads.length; ++ithread){
			threads[ithread].setPriority(Thread.MAX_PRIORITY);//NORM_PRIORITY);
			threads[ithread].start();
		}

		try{
			for (int ithread = 0; ithread < threads.length; ++ithread) {
				threads[ithread].join();
			}
		} catch (InterruptedException ie){
			throw new RuntimeException(ie);
		}

		long end=System.currentTimeMillis();
		IJ.showStatus("!Measuring image - Done in "+tools.formatInterval(end-start));

		//Updates metadata
		retrieveMetadataFromQuantifications(ip);
		retrieveDimensions(ip);

		//Updates all per structure measurements
		rebuildMeasurementsPerStructure(); 
	}

	/**
	 * 
	 * @param level 0: Per Structure, 1: Per Roi
	 * @param type 0: Sum Intensity, 1: Mean Intensity, 2: Std Intensity, 3: Min Intensity, 4: Max Intensity, 5: ID
	 * @param normType 0: No Normalization, 1: 100%=All Structures, 2: 100%=Selected Structures
	 */
	public ImagePlus getImage(int level, int type, int norm, Integer[] ids) {		
		String imgTitle=AtlasDataContainer.ANALYSIS_LEVEL[level]+"_"+AtlasDataContainer.ANALYSIS_MEASUREMENT[type]+"_"+AtlasDataContainer.ANALYSIS_NORM[norm];
		ImagePlus ip=NewImage.createImage(imgTitle, Metadata.imgWidth, Metadata.imgHeight, Metadata.imgDepth, 32, NewImage.FILL_BLACK);


		long start=System.currentTimeMillis();
		final AtomicInteger ai = new AtomicInteger(1);
		final Thread[] threads=new Thread[Runtime.getRuntime().availableProcessors()];
		
		//Prepare threads with what needs to be done
		for (int ithread = 0; ithread < threads.length; ithread++){
			threads[ithread] = new Thread(){
				public void run() {
					for (int id = 0; id <ids.length; id = ai.getAndIncrement()) {
						ArrayList<Roi> rois=getRois(ids[id]);
						MeasurementsEntry me=Atlas.get(ids[id]).Measurements;
						for(int roiIndex=0; roiIndex<rois.size(); roiIndex++) {
							Roi roi=rois.get(roiIndex); //Get the n-th roi
							int slice=roi.getZPosition(); //Retrieve slice nb which also is the index in the ROIs objects
							
							if(level==1) me=Atlas.get(ids[id]).ROIs.get(slice).Measurements;

							//Get the measurement
							double fillValue=me.totalIntensity;
							switch(type) {
								case 0: //Sum Intensity, no need to update already the default
									break;
								case 1: //Mean Intensity
									fillValue=me.meanIntensity;
									break;
								case 2: //Std Intensity
									fillValue=me.stdIntensity;
									break;
								case 3: //Min Intensity
									fillValue=me.minIntensity;
									break;
								case 4: //Max Intensity
									fillValue=me.maxIntensity;
									break;
								case 5: //ID
									fillValue=ids[id];
									break;
							}
							
							//Put it on image
							ImageProcessor iproc=ip.getStack().getProcessor(slice);
							iproc.setValue(fillValue);
							iproc.fill(roi);
							iproc.resetRoi();
						}
					}
				}

			};
		}

		//Initialize threads
		for (int ithread = 0; ithread < threads.length; ++ithread){
			threads[ithread].setPriority(Thread.MAX_PRIORITY);//NORM_PRIORITY);
			threads[ithread].start();
		}

		try{
			for (int ithread = 0; ithread < threads.length; ++ithread) {
				threads[ithread].join();
			}
		} catch (InterruptedException ie){
			throw new RuntimeException(ie);
		}

		ip.setCalibration(Metadata.getCalibration());
		
		StackStatistics stats=new StackStatistics(ip);
		ip.setDisplayRange(stats.min, stats.max);
		
		long end=System.currentTimeMillis();
		IJ.showStatus("!Image creation - Done in "+tools.formatInterval(end-start));
		
		return ip;
	}

	/**
	 * Retrieves an AtlasEntry object from the AtlasDataContainer, 
	 * or null if not found
	 * @param id index to associate the entry with, as an Integer
	 * @return returns the AtlasEntry object from the AtlasDataContainer 
	 * at the provided index, or null if not found
	 */
	public AtlasEntry getFromAtlas(int id) {
		return Atlas.get(id);
	}

	/**
	 * Retrieves an AtlasEntry object from the AtlasDataContainer, 
	 * or an empty AtlasEntry if not found
	 * @param id index to associate the entry with, as an Integer
	 * @return returns the AtlasEntry object from the AtlasDataContainer 
	 * at the provided index, or an empty AtlasEntry if not found
	 */
	public AtlasEntry getOrDefaultFromAtlas(int id) {
		AtlasEntry out= Atlas.get(id);
		if(out==null) out=new AtlasEntry(id);
		
		return out; //Use an empty HashMap to get defaults
	}

	/**
	 * Sends all the Rois in the AtlasDataContainer to the Roi Manager
	 * Rois are named after the anatomical region-Slice_(slice number, 4-digits)
	 * or Unknown-Index_(index)-Slice_(slice number, 4-digits).
	 * NB: Before populating it, the Roi Manager is emptied
	 */
	public void toRoiManager() {
		toRoiManager(null);
	}

	/**
	 * Sends all the Rois from anatomical structure, for the provided indexes to the Roi Manager.
	 * In case the input Set of indexes is either null or empty, sends all the Rois.
	 * Rois are named after the anatomical region-Slice_(slice number, 4-digits)
	 * or Unknown-Index_(index)-Slice_(slice number, 4-digits).
	 * NB: Before populating it, the Roi Manager is emptied
	 */
	public void toRoiManager(Set<Integer> ids) {
		//Get All entries as a TreeMap to get sorted by names
		TreeMap<String, Roi> allRois=new TreeMap<String, Roi>();

		Set<Integer> indexes=(ids==null||ids.isEmpty())?Atlas.keySet():ids;


		for (Integer index : indexes) {
			AtlasEntry ae=Atlas.get(index);
			for (Map.Entry<Integer, RoiEntry> roiEntry : ae.ROIs.entrySet()) {
				allRois.put(roiEntry.getValue().roi.getName(), roiEntry.getValue().roi);
			}
		}

		RoiManager rm= RoiManager.getRoiManager();
		if(rm.getCount()!=0) rm.reset();

		for (Map.Entry<String, Roi> entry : allRois.entrySet()) {
			rm.addRoi(entry.getValue());
		}

		allRois=null;
		IJ.showStatus("!Atlas Data Container sent to RoiManager");
	}
	
	/**
	 * Returns a new empty and calibrated ImagePlus, based on data from Atlas Data Container
	 * @return a new empty and calibrated ImagePlus, based on data from Atlas Data Container
	 */
	public ImagePlus getCalibratedImagePlus() {
		ImagePlus ip=NewImage.createImage(Metadata.imgAnnotationsTitle, Metadata.imgWidth, Metadata.imgHeight, Metadata.imgDepth, Metadata.bitDepth, NewImage.FILL_BLACK);
		Calibration cal=new Calibration();
		cal.pixelWidth=Metadata.pixelWidth;
		cal.pixelHeight=Metadata.pixelHeight;
		cal.pixelDepth=Metadata.pixelDepth;
		cal.setUnit(Metadata.unit);
		ip.setCalibration(cal);

		return ip;
	}

	/**
	 * Performs serialization of the current object and saves it as a JSON file.
	 * The file is named AtlasDataContainer.json and saved at the provided path
	 * NB: a personalized serializer is used for Rois
	 * @param path path of the folder where the AtlasDataContainer.json file will be saved
	 */
	public void saveAsJSON(String path) {
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(RoiEntry.class, new jsonRoiEntry())
				.setPrettyPrinting()
				.create();

		try {
			FileWriter fw=new FileWriter(path);
			gson.toJson(this, fw);
			fw.flush();
			fw.close();
		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		IJ.showStatus("!Atlas Data Container saved as a JSON file");
	}

	/**
	 * Performs serialization of the current object and saves it as a ZON file 
	 * (ZIP file containing the JSON file). The file is named AtlasDataContainer.zon 
	 * and saved at the provided path
	 * NB: a personalized serializer is used for Rois
	 * @param path path of the folder where the AtlasDataContainer.zon file will be saved
	 */
	public void saveAsZON(String path) {
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(RoiEntry.class, new jsonRoiEntry())
				.setPrettyPrinting()
				.create();

		gson.toJson(this);

		try {
			FileOutputStream fos = new FileOutputStream(path);
			ZipOutputStream zos = new ZipOutputStream(fos);
			ZipEntry ze = new ZipEntry("AtlasDataContainer.json");
			zos.putNextEntry(ze);
			byte[] buffer = gson.toJson(this).getBytes();
			zos.write(buffer);
			zos.closeEntry();
			zos.flush();
			zos.close();
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		IJ.showStatus("!Atlas Data Container saved as a ZON file");
	}

	/**
	 * Performs de-serialization of the input JSON file.
	 * NB: a personalized de-serializer is used for Rois
	 * @param path path to the input JSON file
	 */
	public static AtlasDataContainer openJSON(String path) {
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(RoiEntry.class, new jsonRoiEntry())
				.create();

		AtlasDataContainer adc=null;
		boolean success=true;

		try {
			adc=gson.fromJson(new FileReader(path), AtlasDataContainer.class);	
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			success=false;
		} catch (JsonIOException e) {
			e.printStackTrace();
			success=false;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			success=false;
		}

		if(success) {
			IJ.showStatus("!Atlas Data Container opened from JSON file");
		}else {
			IJ.showStatus("!Could not build Atlas Data Container from JSON file");
		}

		adc.rebuildNameIdMap();

		return adc;
	}
	
	/**
	 * Performs de-serialization of the input ZON file 
	 * (ZIP file containing the JSON file).
	 * NB: a personalized serializer is used for Rois
	 * @param path path to the input ZON file
	 */
	public static AtlasDataContainer openZON(String path) {
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(RoiEntry.class, new jsonRoiEntry())
				.create();

		AtlasDataContainer adc=null;
		boolean success=true;

		try {
			FileInputStream fis = new FileInputStream(path);
			ZipInputStream zis = new ZipInputStream(fis);
			ZipEntry ze = zis.getNextEntry();
			while(ze != null){
				String fileName = ze.getName();

				if(fileName.equals("AtlasDataContainer.json") ){
					BufferedReader br = new BufferedReader(new InputStreamReader(zis));
					adc=gson.fromJson(br, AtlasDataContainer.class);
					break;
				}

				//close this ZipEntry
				zis.closeEntry();
				ze = zis.getNextEntry();
			}

			//close last ZipEntry
			zis.closeEntry();
			zis.close();
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
			success=false;
		} 

		if(success) {
			IJ.showStatus("!Atlas Data Container opened from ZON file");
		}else {
			IJ.showStatus("!Could not build Atlas Data Container from ZON file");
		}

		adc.rebuildNameIdMap();

		return adc;
	}

	/**
	 * Returns all Rois as a Roi ArrayList<Roi>, only for ids for which Roi was found
	 * @return all Rois as a Roi ArrayList<Roi>
	 */
	public ArrayList<Roi> getAllRois(){
		ArrayList<Roi> out=new ArrayList<Roi>();

		for(Map.Entry<Integer,AtlasEntry> entry : Atlas.entrySet()) {
			ArrayList<Roi> rois=getRois(entry.getKey());
			if(!rois.isEmpty()) out.addAll(rois);
		}
		return out;
	}

	/**
	 * Returns all Rois associated to an index or null if no Roi was found
	 * @param index
	 * @return all Rois associated to an index as a Roi ArrayList<Roi> or null if no Roi was found
	 */
	public ArrayList<Roi> getRois(int index){
		TreeMap<Integer, RoiEntry> ROIs=Atlas.get(index).ROIs;

		ArrayList<Roi> out=new ArrayList<Roi>();
		for(Map.Entry<Integer,RoiEntry> entry : ROIs.entrySet()) {
			out.add(entry.getValue().roi);
		}

		return out;
	}
}
