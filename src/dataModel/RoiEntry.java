/**
 * RoiEntry.java
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

import ij.gui.Roi;

/**
 * This class is aimed at storing individual 2D Rois together within relevant informations and measurements.
 * This class is an accessory class to the AtlasEntry class.
 */
public class RoiEntry{
	/** Stores the slice number on which the Roi was detected */
	public int slice;

	/** Stores the detected Roi */
	public Roi roi;

	/** Stores all extracted measurements relative to the Roi */
	public MeasurementsEntry Measurements;

	/**
	 * Creates a new empty RoiEntry object
	 */
	public RoiEntry() {

	}

	/**
	 * Creates a new RoiEntry object, feeding it with the provided slice number and Roi.
	 * It initiates its MeasurementsEntry field by measuring the Roi's area
	 * @param slice the slice number on which the Roi was detected
	 * @param roi the detected Roi
	 */
	public RoiEntry(int slice, Roi roi) {
		this.slice=slice;
		this.roi=roi;

		Measurements=new MeasurementsEntry();
		Measurements.area=roi.getStatistics().area;
	}
}
