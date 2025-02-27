/**
 * readStructureFile.java
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import dataModel.AtlasDataContainer;
import dataModel.AtlasEntry;
import ij.IJ;
import ij.measure.ResultsTable;

public class readStructureFile {
	/** Path to the structure tree file 2017 within the jar*/
	String structure_tree_safe_2017="/resources/structure_tree_safe_2017.csv";

	/** Path to the structure tree file within the jar */
	String structure_tree_safe="/resources/structure_tree_safe_2017.csv";

	/** Stores the structure tree file headers */
	String[] structureTreeFileHeaders=null;

	/** The ResultsTable in which data are stored */
	ResultsTable rt=null;

	/** Stores a reference to the AtlasDataContainer in which to store extracted information */
	AtlasDataContainer adc=null;

	/**
	 * Creates a new readStructureFile to store the structure tree file information to the provide
	 * AtlasDataContainer object
	 * @param adc the AtlasDataContainer in which to store extracted information
	 */
	public readStructureFile(AtlasDataContainer adc) {
		IJ.showStatus("!Reading structure tree file");
		this.adc=adc;
	}

	/**
	 * Loads the structure tree content from the ResultsTable that carries the provided title
	 * @param title title of the ResultsTable from which the structure tree content should be retrieved
	 */
	public void loadFromResultsTable(String title) {
		rt=ResultsTable.getResultsTable(title);
		parseStructureTreeFile();
		IJ.showStatus("!Reading structure tree file from ResultsTable \""+title+"\": Done");
	}

	/**
	 * Loads the structure tree content from the structure_tree_safe_2017.csv file enclosed in the jar file
	 * and displays it in a ResultsTable
	 */
	public void loadStructureTreeSafe2017() {
		loadInternalStructureFile(structure_tree_safe_2017, "structure_tree_safe_2017");
		IJ.showStatus("!Reading embarqued \"structure_tree_safe_2017.csv\" file: Done");
	}

	/**
	 * Loads the structure tree content from the structure_tree_safe.csv file enclosed in the jar file
	 * and displays it in a ResultsTable
	 */
	public void loadStructureTreeSafe() {
		loadInternalStructureFile(structure_tree_safe, "structure_tree_safe");
		IJ.showStatus("!Reading embarqued \"structure_tree_safe.csv\" file: Done");
	}

	/**
	 * Loads the structure tree content from a provided path within the jar file and displays it in 
	 * a ResultsTable named after the provided title
	 * @param path path to the structure tree file within the jar file
	 * @param title title to be given to the ResultsTable
	 */
	public void loadInternalStructureFile(String path, String title) {
		String content = "";
		try {
			// get the text resource as a stream
			InputStream is = getClass().getResourceAsStream(path);
			InputStreamReader isr = new InputStreamReader(is);
			StringBuffer sb = new StringBuffer();
			char [] b = new char [8192];
			int n;
			//read a block and append any characters
			while ((n = isr.read(b)) > 0)
				sb.append(b,0, n);
			// display the text in a TextWindow
			content = sb.toString();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		rt=new ResultsTable();
		String[] lines=content.split("\n");
		String[] headers=lines[0].split(",");

		for(int i=1; i<lines.length; i++) {
			String[] columns=lines[i].split(",");
			for(int j=0; j<headers.length; j++) {
				rt.setValue(headers[j], i-1, columns[j]);
			}
		}
		rt.show(title);
		parseStructureTreeFile();
	}

	/**
	 * Parses the structure tree content from the ResultsTable and feeds the AtlasDataContainer
	 * by creating in it one AtlasEntry per line
	 */
	public void parseStructureTreeFile() {
		structureTreeFileHeaders=rt.getHeadings();

		for(int i=0; i<rt.size(); i++) {
			String[] values=rt.getRowAsString(i).split("\t");

			//Build a dictionary per line (Key=header, Value=fieldValue)
			Map<String, String> perLineDictionary=new HashMap<String, String>();

			for(int j=0; j<values.length; j++)  perLineDictionary.put(structureTreeFileHeaders[j], values[j]);

			//Build a dictionary per index (Key=index, Value=perLienDictionnary)
			AtlasEntry ae=new AtlasEntry(perLineDictionary);
			adc.putInAtlas(Integer.parseInt(perLineDictionary.get("id")), ae);
		}
	}
}
