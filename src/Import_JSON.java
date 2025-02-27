import dataReader.jsonData.jsonDataContainer;
import ij.IJ;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import utilities.pluginsInfo;

/**
 * Import_JSON.java
 * 
 * Created on 18 déc. 2024
 * Fabrice P. Cordelieres, fabrice.cordelieres at gmail.com
 * 
 * Copyright (C) 2024 Fabrice P. Cordelieres
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
 * This class implements the GUI to import a JSON file and display it as a formatted ResultsTable
 */
public class Import_JSON implements PlugIn{

	@Override
	public void run(String arg) {
		OpenDialog od=new OpenDialog("Import JSON "+pluginsInfo.IMPORT_JSON_VERSION+" ("+pluginsInfo.IMPORT_JSON_DATE+")\nSelect the JSON file to import");
		String path=od.getPath();
		if(path!=null) {
			String name=od.getFileName();
			if(name.toLowerCase().endsWith(".json")) {
				jsonDataContainer jdc=jsonDataContainer.openJSON(path);
				jdc.toResultsTable(name);
			}else {
				IJ.error("The file extension should be JSON");
			}
		}
	}
}
