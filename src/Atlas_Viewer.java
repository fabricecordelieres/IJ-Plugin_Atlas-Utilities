import dataViewer.dataViewer;
import ij.plugin.PlugIn;

/**
 * Atlas_Viewer.java
 * 
 * Created on 9 août 2023
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
 * This plugin allows reviewing data extracted using the Atlas_to_Roi plugin. 
 * It provides ways to filter anatomical regions, quantify them and perform
 * exports.
 */
public class Atlas_Viewer implements PlugIn{

	@Override
	public void run(String arg) {
		new dataViewer().setVisible(true);	
	}

}
