/**
 * decodeLabelImage.java
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

package dataReader;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import dataModel.AtlasDataContainer;
import dataModel.AtlasEntry;
import dataModel.RoiEntry;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.ImageProcessor;
import utilities.tools;

/**
 * This class takes as an input an ImagePlus containing labels and stores
 * as a Map of Map Rois containing the decoded ROIs per index and per slice.
 * The outer-most Map is indexed by labels, the inner Map being indexed by slice.
 * The operation is performed as a multi-threaded process.
 * @author fab
 *
 */
public class decodeLabelImage {
	/** Stores the ImagePlus to decode */
	ImagePlus ip=null;

	/** Stores a reference to the AtlasDataContainer in which to store extracted Rois */
	AtlasDataContainer adc=null;

	/** Handles outputting debug infos into the console */
	public boolean debug=false;

	/**
	 * Constructs a new decodeLabelImage, based on the input ImagePlus
	 * @param ip the input ImagePlus
	 */
	public decodeLabelImage(ImagePlus ip, AtlasDataContainer adc) {
		this.ip=ip;
		this.adc=adc;
		decode();
	}

	/**
	 * Builds the Rois Map, as a multi-threaded process
	 */
	public void decode() {
		long start=System.currentTimeMillis();
		final AtomicInteger ai = new AtomicInteger(1);
		final Thread[] threads=new Thread[Runtime.getRuntime().availableProcessors()];

		//Prepare threads with what needs to be done
		for (int ithread = 0; ithread < threads.length; ithread++){
			final int ithreadNb=ithread; // To be able to pass it in the run
			threads[ithread] = new Thread(){
				public void run() {
					for (int slice = ai.getAndIncrement(); slice <=ip.getStackSize(); slice = ai.getAndIncrement()) {
						ImageProcessor iproc=ip.getStack().getProcessor(slice);
						List<Integer> uniqueIndexes=getUniqueIndexes(iproc, true);

						for(int index : uniqueIndexes){
							//Threshold to ROIs
							iproc.setThreshold(index, index);
							ThresholdToSelection tts=new ThresholdToSelection();
							Roi roi=tts.convert(iproc);
							roi.setPosition(slice);


							//Unpack
							AtlasEntry ae=adc.getOrDefaultFromAtlas(index); //Get the AtlasEntry from the AtlasContainer
							
							//Add infos to the Roi
							Color color=Color.decode("#"+(ae!=null?ae.color_hex_triplet:"D3D3D3"));
							roi.setStrokeColor(color);
							String name=ae.name;
							roi.setName(name+"-Slice_"+IJ.pad(slice, 4));

							if(name=="Unknown") {
								ae.id=index;
								ae.name="Unknown_id_"+index;
								name=ae.name;
							}

							TreeMap<Integer, RoiEntry> rc=ae.ROIs; // Get the RoiDataContainer from the Atlas entry
							RoiEntry re=new RoiEntry(slice, roi); //Get the RoiEntry from the RoiDataContainer

							//Re-pack
							try {
								rc.put(slice, re); //Put the new ROI in the RoiDataContainer
							} catch (Exception e) {
								e.printStackTrace();
								System.out.println("rc: "+rc);
								System.out.println("Key: "+slice);
								System.out.println("Value: "+re);
							}
							

							ae.ROIs=rc; //Update the RoiDataContainer in the Atlas entry
							adc.putInAtlas(index, ae); //Pack everything back in the AtlasContainer

							IJ.showStatus("!Decoding image - Thread "+(ithreadNb+1)+"/"+threads.length+" returned results for slice "+slice+", index "+index);
							if(debug) System.out.println("Thread "+(ithreadNb+1)+" returned: "+index+"/"+slice+"/"+roi);
							iproc.resetThreshold();
							iproc.resetRoi();
						}
					}
				}
			};
			if(debug) System.out.println("Initialized Thread "+(ithread+1)+"/"+threads.length);
		}

		//Initialize threads
		for (int ithread = 0; ithread < threads.length; ++ithread){
			threads[ithread].setPriority(Thread.MAX_PRIORITY);//NORM_PRIORITY);
			threads[ithread].start();
			if(debug) System.out.println("Started Thread "+(ithread+1)+"/"+threads.length);
		}

		try{
			for (int ithread = 0; ithread < threads.length; ++ithread) {
				threads[ithread].join();
				if(debug) System.out.println("Ended Thread "+(ithread+1)+"/"+threads.length);
			}
		} catch (InterruptedException ie){
			throw new RuntimeException(ie);
		}

		long end=System.currentTimeMillis();
		IJ.showStatus("!Decoding image - Done in "+tools.formatInterval(end-start));
	}

	/**
	 * From the input ImageProcessor, extracts the unique pixels values
	 * @param iproc the input ImageProcessor
	 * @param excludeZero true to exclude the zero label
	 * @return the unique pixels values as a list of Integers
	 */
	public List<Integer> getUniqueIndexes(ImageProcessor iproc, boolean excludeZero) {
		List<Integer> indexes = new ArrayList<>();

		//Transforms the ImageProcessor into an array of intensities
		for(int y=0; y<iproc.getHeight(); y++) {
			for(int x=0; x<iproc.getWidth(); x++) {
				int value=(int) iproc.getValue(x, y);
				if(!(excludeZero && value==0)) indexes.add(value);
			}
		}

		//Collects the unique indexes into a List
		List<Integer> out=indexes.stream().distinct().collect(Collectors.toList());
		out.sort(null);

		return out;
	}
}
