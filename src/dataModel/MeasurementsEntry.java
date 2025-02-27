/**
 * MeasurementsEntry.java
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

/**
 * This class stores and handles measurements for each Roi/group of Rois 
 * associated to a single anatomical structure.
 */
public class MeasurementsEntry {
	/** Roi/Group of Rois area */
	public double area;

	/** Roi/Group of Rois volume */
	public double volume;

	/** Roi/Group of Rois total intensity */
	public double totalIntensity;

	/** Roi/Group of Rois mean intensity */
	public double meanIntensity;

	/** Roi/Group of Rois standard deviation of intensities */
	public double stdIntensity;
	
	/** Roi/Group of Rois min intensity */
	public double minIntensity;
	
	/** Roi/Group of Rois max intensity */
	public double maxIntensity;
}
