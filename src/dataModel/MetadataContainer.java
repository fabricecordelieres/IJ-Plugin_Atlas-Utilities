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

import java.text.SimpleDateFormat;
import java.util.Date;

import ij.ImagePlus;
import ij.measure.Calibration;
import utilities.pluginsInfo;

/**
 * This class allows creating a representation of the metadata of the dataset on which an
 * AtlasDataContainer has been built.
 */
public class MetadataContainer{
	/** Stores the version of the plugin used for analysis */
	public String version=pluginsInfo.ATLAS_TO_ROI_VERSION;;

	/** The date/time at which analysis was performed */
	public String date=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	
	/** Title of the labeled image used during extraction */
	public String imgAnnotationsTitle="";

	/** Path to the image used during extraction */
	public String imgAnnotationsPath="";

	/** Title of the image used for quantification */
	public String imgQuantificationsTitle="";

	/** Path to the image used for quantification */
	public String imgQuantificationsPath="";

	/** Image dimension: width */
	public int imgWidth;

	/** Image dimension: height */
	public int imgHeight;

	/** Image dimension: number of slices */
	public int imgDepth;

	/** Image dimension: bit depth */
	public int bitDepth;

	/** Image spatial calibration: pixel width */
	public double pixelWidth;

	/** Image spatial calibration: pixel height */
	public double pixelHeight;

	/** Image spatial calibration: slice spacing */
	public double pixelDepth;

	/** Image spatial calibration: unit of length */
	public String unit="";

	/** Option used as source for the structure tree file used during extraction (ResultsTable or Included File) */
	public String structureTreeFileOption="";

	/** Path to the structure tree file used during extraction */
	public String structureTreeFilePath="";

	/**
	 * Collects and stores title and path from the labeled image used during extraction
	 * @param ip the labeled image used during extraction, as an ImagePlus
	 */
	public void getInfosFromAnnotationsImage(ImagePlus ip) {
		imgAnnotationsTitle=ip.getTitle();
		imgAnnotationsPath=ip.getOriginalFileInfo().getFilePath();
	}

	/**
	 * Collects and stores title and path from the image used for quantification
	 * @param ip the image used for quantification, as an ImagePlus
	 */
	public void getInfosFromQuantificationsImage(ImagePlus ip) {
		imgQuantificationsTitle=ip.getTitle();
		imgQuantificationsPath=ip.getOriginalFileInfo()!=null?ip.getOriginalFileInfo().getFilePath():"";
	}

	/**
	 * Collects and stores the image dimensions and spatial calibration from the input ImagePlus
	 * @param ip an ImagePlus from which image dimensions and spatial calibration should be extracted and stored
	 */
	public void getDimensions(ImagePlus ip) {
		imgWidth=ip.getWidth();
		imgHeight=ip.getHeight();
		imgDepth=ip.getNSlices();
		bitDepth=ip.getBitDepth();

		pixelWidth=ip.getCalibration().pixelWidth;
		pixelHeight=ip.getCalibration().pixelHeight;
		pixelDepth=ip.getCalibration().pixelDepth;
		unit=ip.getCalibration().getUnit();
	}
	
	/**
	 * Builds a calibration object from the MetadataContainer
	 * @return a calibration object from the MetadataContainer
	 */
	public Calibration getCalibration() {
		Calibration cal=new Calibration();
		cal.pixelWidth=pixelWidth;
		cal.pixelHeight=pixelHeight;
		cal.pixelDepth=pixelDepth;
		cal.setUnit(unit);
		return cal;
	}

	/**
	 * Stores source and path for the structure tree file used during extraction
	 * @param option a String representing the data source (ResultsTable or Included File)
	 * @param path a String representing the path to the data source 
	 */
	public void setStructureTreeFileInfos(String option, String path) {
		structureTreeFileOption=option;
		structureTreeFilePath=path;
	}
}
