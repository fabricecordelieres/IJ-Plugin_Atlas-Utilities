/**
 * jsonDataContainer.java
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

package dataReader.jsonData;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import ij.IJ;
import ij.measure.ResultsTable;

/**
 * This class is aimed at handling json files and opening them as a results table, where
 * information is formatted as the provided structure_tree_safe csv files
 */
public class jsonDataContainer {
	/** Stores all information, sorted by IDs */
	public jsonDataEntry[] jsonData=null;
	
	
	/**
	 * Performs de-serialization of the input JSON file 
	 * @param path path to the input JSON file
	 */
	public static jsonDataContainer openJSON(String path) {
		Gson gson = new GsonBuilder()
				//.registerTypeAdapter(RoiEntry.class, new jsonRoiEntry())
				.create();

		jsonDataContainer jdc=new jsonDataContainer();
		boolean success=true;

		try {
			FileInputStream fis = new FileInputStream(path);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			
			jdc.jsonData=gson.fromJson(br, jsonDataEntry[].class);
			fis.close();
		} catch (IOException | JsonSyntaxException | JsonIOException e) {
			e.printStackTrace();
			success=false;
		} 

		if(success) {
			IJ.showStatus("!JSON file opened successfully");
		}else {
			IJ.showStatus("!Could not open the JSON file");
		}

		//Sort the data
		Arrays.sort(jdc.jsonData);
		for(int i=0; i<jdc.jsonData.length; i++) {
			//Add graph_order & sphinx_id
			jdc.jsonData[i].graph_order=i;
			jdc.jsonData[i].sphinx_id=i+1;
		}
		return jdc;
	}
	
	/**
	 * Exports the content of the JSON file to a ResultsTable
	 * @param name name of the ResultsTable, as a String
	 */
	public void toResultsTable(String name) {
		ResultsTable rt=new ResultsTable();
		
		for(jsonDataEntry jde:jsonData) jde.toResultsTable(rt);
		
		rt.show(name);
	}

}
