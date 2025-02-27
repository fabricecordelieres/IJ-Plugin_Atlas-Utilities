package dataModel;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import utilities.tools;

/**
 * AtlasEntry.java
 * 
 * Created on 3 août 2023 Fabrice P. Cordelieres, fabrice.cordelieres at
 * gmail.com
 * 
 * Copyright (C) 2023 Fabrice P. Cordelieres
 *
 * License: This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General public License for more details.
 * 
 * You should have received a copy of the GNU General public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This class is aimed at manipulating AtlasEntry. It associates data from of line of
 * the structure tree file to Rois found on a labeled image and measurements made
 * within each Roi.
 */
public class AtlasEntry {
	/** Variables to store all entries in a single line of the structure tree file */
	public int id;
	public int atlas_id;
	public String name;
	public String acronym;
	public String st_level;
	public int ontology_id;
	public int hemisphere_id;
	public int weight;
	public int parent_structure_id;
	public int depth;
	public int graph_id;
	public int graph_order;
	public String structure_id_path;
	public String color_hex_triplet;
	public String neuro_name_structure_id;
	public String neuro_name_structure_id_path;
	public String failed;
	public int sphinx_id;
	public String structure_name_facet;
	public String failed_facet;
	public String safe_name;

	/** Variables to store all extracted ROIs from the image */
	public TreeMap<Integer, RoiEntry> ROIs=new TreeMap<Integer, RoiEntry>();

	/** Variables to store all extracted measurements for the anatomical structure */
	public MeasurementsEntry Measurements=new MeasurementsEntry();;

	/**
	 * Creates a new empty AtlasEntry object. Numerical values are initialized to -1, String values to "Unkwown".
	 */
	public AtlasEntry() {
		setAllDescriptionFields(new HashMap<String, String>()); //Use an empty HashMap to get defaults
	}

	/**
	 * Creates a new empty AtlasEntry object. Numerical values are initialized to -1, String values to Unknown.
	 * The id field is initialized to the input id value, the name field to Unknown-Index_(id).
	 * @param id id to set for the AtlasEntry, as an Integer
	 */
	public AtlasEntry(int id) {
		setAllDescriptionFields(new HashMap<String, String>()); //Use an empty HashMap to get defaults
		this.id=id;
		this.name="Unknown-Index_"+id;
	}
	/**
	 * Creates a new AtlasEntry object, based on the input Map. 
	 * @param description a String, String Map from which the AtlasEntry will be built. The keys should
	 * map the class variables to be used. Values are Strings which will be parsed according to their respective keys.
	 */
	public AtlasEntry(Map<String, String> description) {
		setAllDescriptionFields(description);
	}

	/**
	 * Replaces the class variable content with values found in the input map. In case a field is not found, re-initialises
	 * the field value to its default value.
	 * @param description a String, String Map from which the AtlasEntry will be built. The keys should
	 * map the class variables to be used. Values are Strings which will be parsed according to their respective keys.
	 */
	public void setAllDescriptionFields(Map<String, String> description) {
		id=getInt(description, "id");
		atlas_id=getInt(description, "atlas_id");
		name=tools.capitalize(getString(description, "name")); //Important to capitalize for sorting based on names
		acronym=getString(description, "acronym");
		st_level=getString(description, "st_level");
		ontology_id=getInt(description, "ontology_id");
		hemisphere_id=getInt(description, "hemisphere_id");
		weight=getInt(description, "weight");
		parent_structure_id=getInt(description, "parent_structure_id");
		depth=getInt(description, "depth");
		graph_id=getInt(description, "graph_id");
		graph_order=getInt(description, "graph_order");
		structure_id_path=getString(description, "structure_id_path");
		color_hex_triplet=getString(description, "color_hex_triplet");
		neuro_name_structure_id=getString(description, "neuro_name_structure_id");
		neuro_name_structure_id_path=getString(description, "neuro_name_structure_id_path");
		failed=getString(description, "failed");
		sphinx_id=getInt(description, "sphinx_id");
		structure_name_facet=getString(description, "structure_name_facet");
		failed_facet=getString(description, "failed_facet");
		safe_name=getString(description, "safe_name");	
	}

	/**
	 * Looks for the input key within the input map. If found, returns the associated value, parsed as an Integer.
	 * If not found, returns -1.
	 * @param description a String, String Map where the key will be looked for.
	 * @param key the key to look for.
	 * @return If found, returns the associated value, parsed as an Integer. If not found, returns -1.
	 */
	private int getInt(Map<String, String> description, String key) {
		int out=-1;
		try {
			out=Integer.parseInt(description.getOrDefault(key, "-1"));
		} catch (Exception e) {
			/*
			 * Do nothing, quietly handles the problem of empty 
			 * values by catching any complain and returning -1
			 */
		}

		return out;
	}

	/**
	 * Looks for the input key within the input map. If found, returns the associated value, parsed as a String.
	 * If not found, returns an empty String. In case the key is "color_hex_triplet" and value is "Unknown", 
	 * returns "D3D3D3".
	 * @param description a String, String Map where the key will be looked for.
	 * @param key the key to look for.
	 * @return If found, returns the associated value, parsed as a String. If not found, returns an empty String. 
	 * In case the key is "color_hex_triplet" and value is "Unknown", returns "D3D3D3".
	 */
	private String getString(Map<String, String> description, String key) {
		String out=description.getOrDefault(key, "Unknown");
		out=out.replace("\"", "");

		//Handles case where color item is not found: return light gray (D3D3D3)
		if(key=="color_hex_triplet" && out=="Unknown") {
			return "D3D3D3";
		}

		//Handles case where structure_id_path item is not found: return empty string
		if(key=="structure_id_path" && out=="Unknown") {
			return "";
		}

		//Handles case where graph_order item is not found: return empty string
		if(key=="graph_order" && out=="Unknown") {
			return "";
		}

		return out;
	}
}