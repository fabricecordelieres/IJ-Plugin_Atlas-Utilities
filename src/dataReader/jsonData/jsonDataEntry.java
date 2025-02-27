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

import java.awt.Color;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ij.measure.ResultsTable;

/**
 * This class is used to store and manipulate data stored in a JSON file.
 * It allows conversion to match the structure provided in the structure_tree_safe csv files
 */
public class jsonDataEntry implements Comparable<jsonDataEntry>{
	/** Variables to store all entries from a single entry of the JSON structure tree file */
	public String acronym;
	public int id;
	public String name;
	public int[] structure_id_path;
	public int[] rgb_triplet;
	
	/**
	 * Variables to adapt to the output in a csv files similar to the provided structure_tree_safe csv files.
	 * Using default values unless for some fields for which the format should be adapted.
	 */
	public String atlas_id="";
	public String st_level="";
	
	public String ontology_id="";
	public String hemisphere_id="";
	public String weight="";
	
	public String parent_structure_id;
	public int depth;
	
	public String graph_id="";
	public int graph_order=0;
	
	public String structure_id_path_String;
	public String color_hex_triplet;
	
	public String neuro_name_structure_id="";
	public String neuro_name_structure_id_path="";
	public String failed="";
	public int sphinx_id=0;
	public String structure_name_facet="";
	public String failed_facet="";
	
	public String safe_name;
	
	/**
	 * Performs the translation from the input JSON data to match the fields provided by the structure_tree_safe csv files
	 */
	public void adaptAllFields() {
		parent_structure_id=name.equals("root")?"":""+structure_id_path[structure_id_path.length-2];
		depth=structure_id_path.length-1;
		structure_id_path_String="/"+IntStream.of(structure_id_path).mapToObj(i -> String.valueOf(i)).collect(Collectors.joining("/"))+"/";
		
		Color c=new Color(rgb_triplet[0], rgb_triplet[1], rgb_triplet[2]);
		color_hex_triplet=(Integer.toHexString(c.getRGB()).substring(2)).toUpperCase();
		
		safe_name=name.replace(",", "").replace("  ", " ");
	}
	
	/**
	 * Pushes content of the current object to the provided ResultsTable
	 * @param rt the ResultsTable that will receive the data
	 */
	public void toResultsTable(ResultsTable rt) {
		adaptAllFields();
		
		int row=rt.size();
		rt.setValue("id", row, id);
		rt.setValue("atlas_id", row, atlas_id);
		rt.setValue("name", row, name);
		rt.setValue("acronym", row, acronym);
		rt.setValue("st_level", row, st_level);
		rt.setValue("ontology_id", row, ontology_id);
		rt.setValue("hemisphere_id", row, hemisphere_id);
		rt.setValue("weight", row, weight);
		rt.setValue("parent_structure_id", row, parent_structure_id);
		rt.setValue("depth", row, depth);
		rt.setValue("graph_id", row, graph_id);
		rt.setValue("graph_order", row, graph_order);
		rt.setValue("structure_id_path", row, structure_id_path_String);
		rt.setValue("color_hex_triplet", row, color_hex_triplet);
		rt.setValue("neuro_name_structure_id", row, neuro_name_structure_id);
		rt.setValue("neuro_name_structure_id_path", row, neuro_name_structure_id_path);
		rt.setValue("failed", row, failed);
		rt.setValue("sphinx_id", row, sphinx_id);
		rt.setValue("structure_name_facet", row, structure_name_facet);
		rt.setValue("failed_facet", row, failed_facet);
		rt.setValue("safe_name", row, safe_name);
	}
	
	
	@Override
	public String toString() {
		String out="acronym: "+acronym
				+"\nid: "+id
				+"\nname: "+name
				+"\nstructure_id_path: /";
		
		
		for(int i:structure_id_path) out+=i+"/";
		
		out+="\nrgb_triplet: /";
		
		for(int i:rgb_triplet) out+=i+"/";
		
		return out;
	}

	@Override
	public int compareTo(jsonDataEntry o) {
		int index=0;
		
		//Compares by element
		while(index<o.structure_id_path.length && index<structure_id_path.length) {
			if(o.structure_id_path[index]!=structure_id_path[index]) {
				if(o.structure_id_path[index]<structure_id_path[index]) return 1;
				if(o.structure_id_path[index]>structure_id_path[index]) return -1;
			}
			index++;
		}
		
		//In case one array is larger than the other, knowing we reach this section only if the common part is matching,
		//the longest one should be the one coming after
		if(o.structure_id_path.length<structure_id_path.length) return 1;
		if(o.structure_id_path.length>structure_id_path.length) return -1;
		
		//If all previous scenario failed, it means both are equal
		return 0;
	}
}
