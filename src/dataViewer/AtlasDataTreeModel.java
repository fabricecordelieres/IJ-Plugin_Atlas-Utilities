/**
 * AtlasDataTree.java
 * 
 * Created on 15 août 2023
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

package dataViewer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import dataModel.AtlasDataContainer;
import dataModel.AtlasEntry;
import dataModel.MeasurementsEntry;
import dataModel.MetadataContainer;
import dataModel.RoiEntry;
import ij.gui.Roi;

/**
 * This class is aimed at easing the display as a JTree of AtlasDataContainer objects by extending the DefaultTreeModel class.
 */
public class AtlasDataTreeModel extends DefaultTreeModel{

	private static final long serialVersionUID = 1L;
	
	public static final int ONTOLOGY_TREEMODEL=0;
	public static final int STRUCTURE_TREEMODEL=1;
	
	/** The input AtlasDataContainer */
	AtlasDataContainer adc;
	
	/** The AtlasDataModel type: 0=ONTOLOGY_TREEMODEL, 1=STRUCTURE_TREEMODEL */
	public int type;
	
	/** A correspondence table between Atlas id and the deepest TreePath for this id */
	public TreeMap <Integer, TreePath> idToTreePath=new TreeMap<Integer, TreePath>();
	
	/** A correspondence table between TreePath and Atlas ids */
	public TreeMap <TreePath, Integer> treePathToId=new TreeMap<TreePath, Integer>();
	

	/**
	 * Creates a new AtlasDataTreeModel based on the input AtlasDataContainer.
	 * Its type is defined by the input type: could be ONTOLOGY_TREEMODEL(0) to order nodes 
	 * based on the structure_id_path field from AtlasEntries, or STRUCTURE_TREEMODEL (1) to 
	 * have one node per AtlasEntry
	 * @param adc the input AtlasDataContainer from which to build the defaultTreeModel
	 * @param type an integer, equal to 0 (ONTOLOGY_TREEMODEL) to order nodes based on the 
	 * structure_id_path field from AtlasEntries, or 1 (STRUCTURE_TREEMODEL) to have one node per AtlasEntry
	 */
	public AtlasDataTreeModel(AtlasDataContainer adc, int type) {
		super(new DefaultMutableTreeNode(type==ONTOLOGY_TREEMODEL?"Ontology":"Atlas Data Container"));
		this.adc=adc;
		this.type=type;
		
		switch(type) {
			case ONTOLOGY_TREEMODEL:
				buildOntologyTreeModel();
				break;
			case STRUCTURE_TREEMODEL:
				buildStructureTreeModel(false);
				break;
		}
		
	}
	
	/**
	 * Fills the TreeModel with nodes ordered based on the structure_id_path field 
	 * from AtlasEntries in the AtlasDataContainer
	 */
	public void buildOntologyTreeModel() {
		Set<Integer> ids=adc.Atlas.keySet();

		TreeMap<Integer, AtlasEntry> graph=new TreeMap<>();

		//Build a list order by graph order
		for(Integer id:ids) {
			graph.put(adc.Atlas.get(id).graph_order, adc.Atlas.get(id));
		}

		for(Integer g:graph.keySet()) {
			String structure_id_path=graph.get(g).structure_id_path;
			int id=graph.get(g).id;
			DefaultMutableTreeNode whereWeAre=(DefaultMutableTreeNode) getRoot();
			if(!structure_id_path.equals("Unknown") && structure_id_path.length()>2) {
				String[] path=structure_id_path.substring(1, structure_id_path.length() - 1).split("/");
				String name="";
				
				//Goes through the path
				for(String level:path) {
					name=adc.Atlas.get(Integer.parseInt(level)).name;
					int index=0;
					boolean found=false;
					for(int i=0; i<whereWeAre.getChildCount(); i++) {
						if(((DefaultMutableTreeNode) whereWeAre.getChildAt(i)).toString().equals(name)){
							index=i;
							found=true;
							break;
						}
					}
					if(found) {
						whereWeAre=(DefaultMutableTreeNode) whereWeAre.getChildAt(index);
					}else {
						whereWeAre.add(new DefaultMutableTreeNode(name));
					}
				}
				//At the end, prepares the addition to the correspondence table
				whereWeAre=(DefaultMutableTreeNode) whereWeAre.getLastChild();
				TreePath treePath=new TreePath(whereWeAre.getPath()); // Gets the path to the node
				idToTreePath.put(id, treePath);
			}
		}
	}
	
	/**
	 * Fills the TreeModel with one node per entry from AtlasEntries in the AtlasDataContainer
	 * @param excludeRoisNotFound true to avoid adding ROIs section to AtlasEntries no having associated ROIs
	 */
	public void buildStructureTreeModel(boolean excludeRoisNotFound) {
		//Creates the root node
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) getRoot();

		//Creates the Metadata node
		root.add(getDefaultMutableTreeNode(adc.Metadata));
		
		//Creates the Atlas Entries node
		DefaultMutableTreeNode AtlasEntries = new DefaultMutableTreeNode("Atlas");
		root.add(AtlasEntries);
		
		//Push entries to DefaultMutableTreeNode
		for (Integer id:adc.nameId.values()) {
			DefaultMutableTreeNode ae=getDefaultMutableTreeNode(adc.Atlas.get(id), excludeRoisNotFound);
			if(ae!=null) {
				AtlasEntries.add(ae);
				//Add to correspondence tables
				TreePath treePath=new TreePath(ae.getPath());
				idToTreePath.put(id, treePath);
			}
		} 

		
	}
	
	/**
	 * Returns the full content of the input MetadataContainer object as a DefaultMutableTreeNode object.
	 * @param mc the input MetadataContainer
	 * @return the full content of the the input MetadataContainer object as a DefaultMutableTreeNode object.
	 */
	public DefaultMutableTreeNode getDefaultMutableTreeNode(MetadataContainer mc) {
		DefaultMutableTreeNode tree = new DefaultMutableTreeNode("Metadata");
		
		tree.add(new DefaultMutableTreeNode("Version: "+mc.version));
		tree.add(new DefaultMutableTreeNode("Date: "+mc.date));

		tree.add(new DefaultMutableTreeNode("imgAnnotationsTitle: "+mc.imgAnnotationsTitle));
		tree.add(new DefaultMutableTreeNode("imgAnnotationsPath: "+mc.imgAnnotationsPath));
		tree.add(new DefaultMutableTreeNode("imgQuantificationsTitle: "+mc.imgQuantificationsTitle));
		tree.add(new DefaultMutableTreeNode("imgQuantificationsPath: "+mc.imgQuantificationsPath));

		tree.add(new DefaultMutableTreeNode("imgWidth: "+mc.imgWidth));
		tree.add(new DefaultMutableTreeNode("imgHeight: "+mc.imgHeight));
		tree.add(new DefaultMutableTreeNode("imgDepth: "+mc.imgDepth));
		tree.add(new DefaultMutableTreeNode("bitDepth: "+mc.bitDepth));

		tree.add(new DefaultMutableTreeNode("pixelWidth: "+mc.pixelWidth));
		tree.add(new DefaultMutableTreeNode("pixelHeight: "+mc.pixelHeight));
		tree.add(new DefaultMutableTreeNode("pixelDepth: "+mc.pixelDepth));
		tree.add(new DefaultMutableTreeNode("unit: "+mc.unit));

		tree.add(new DefaultMutableTreeNode("structureTreeFileOption: "+mc.structureTreeFileOption));
		tree.add(new DefaultMutableTreeNode("structureTreeFilePath: "+mc.structureTreeFilePath));

		return tree;
	}
	
	/**
	 * Returns the full content of the input AtlasEntry as a DefaultMutableTreeNode object.
	 * Returns null if doNotReturnIfNoRoi is true and no roi is associated to this anatomical region
	 * @param ae the input AtlasEntry
	 * @param nullIfNoRoi true if null should be returned in case no roi is associated 
	 * to this anatomical region
	 * @return the full content of the current object as a DefaultMutableTreeNode object.
	 */
	public DefaultMutableTreeNode getDefaultMutableTreeNode(AtlasEntry ae, boolean nullIfNoRoi) {
		if(nullIfNoRoi && ae.ROIs.isEmpty()) return null;

		DefaultMutableTreeNode tree = new DefaultMutableTreeNode(ae.name);
		DefaultMutableTreeNode structureNode = new DefaultMutableTreeNode("Structure");

		structureNode.add(new DefaultMutableTreeNode("id: " + ae.id));
		structureNode.add(new DefaultMutableTreeNode("atlas_id: " + ae.atlas_id));
		structureNode.add(new DefaultMutableTreeNode("name: " + ae.name));
		structureNode.add(new DefaultMutableTreeNode("acronym: " + ae.acronym));
		structureNode.add(new DefaultMutableTreeNode("st_level: " + ae.st_level));
		structureNode.add(new DefaultMutableTreeNode("ontology_id: " + ae.ontology_id));
		structureNode.add(new DefaultMutableTreeNode("hemisphere_id: " + ae.hemisphere_id));
		structureNode.add(new DefaultMutableTreeNode("weight: " + ae.weight));
		structureNode.add(new DefaultMutableTreeNode("parent_structure_id: " + ae.parent_structure_id));
		structureNode.add(new DefaultMutableTreeNode("depth: " + ae.depth));
		structureNode.add(new DefaultMutableTreeNode("graph_id: " + ae.graph_id));
		structureNode.add(new DefaultMutableTreeNode("graph_order: " + ae.graph_order));
		structureNode.add(new DefaultMutableTreeNode("structure_id_path: " + ae.structure_id_path));
		structureNode.add(new DefaultMutableTreeNode("color_hex_triplet: " + ae.color_hex_triplet));
		structureNode.add(new DefaultMutableTreeNode("neuro_name_structure_id: " + ae.neuro_name_structure_id));
		structureNode.add(new DefaultMutableTreeNode("neuro_name_structure_id_path: " + ae.neuro_name_structure_id_path));
		structureNode.add(new DefaultMutableTreeNode("failed: " + ae.failed));
		structureNode.add(new DefaultMutableTreeNode("sphinx_id: " + ae.sphinx_id));
		structureNode.add(new DefaultMutableTreeNode("structure_name_facet: " + ae.structure_name_facet));
		structureNode.add(new DefaultMutableTreeNode("failed_facet: " + ae.failed_facet));

		tree.add(structureNode);

		tree.add(getDefaultMutableTreeNode(ae.Measurements, true));

		DefaultMutableTreeNode roisNode = new DefaultMutableTreeNode("ROIs");
		for(Integer index:ae.ROIs.keySet()) {
			roisNode.add(getDefaultMutableTreeNode(ae.ROIs.get(index)));
		}
		if(ae.ROIs.isEmpty()) roisNode.add(new DefaultMutableTreeNode("None"));

		tree.add(roisNode);

		return tree;
	}
	
	/**
	 * Returns the full content of the input MeasurementsEntry as a DefaultMutableTreeNode object.
	 * @param me the input MesurementsEntry
	 * @param is3D true to log the volume field, false in case only 2D measurements are to be displayed
	 * @return the full content of the input MeasurementsEntry object as a DefaultMutableTreeNode object.
	 */
	public DefaultMutableTreeNode getDefaultMutableTreeNode(MeasurementsEntry me, boolean is3D) {
		DefaultMutableTreeNode tree = new DefaultMutableTreeNode("Measurements");

		tree.add(new DefaultMutableTreeNode(is3D?"volume: "+me.volume:"area: "+me.area));
		tree.add(new DefaultMutableTreeNode("totalIntensity: "+me.totalIntensity));
		tree.add(new DefaultMutableTreeNode("meanIntensity: "+me.meanIntensity));
		tree.add(new DefaultMutableTreeNode("stdIntensity: "+me.stdIntensity));
		tree.add(new DefaultMutableTreeNode("minIntensity: "+me.minIntensity));
		tree.add(new DefaultMutableTreeNode("maxIntensity: "+me.maxIntensity));

		return tree;
	}
	
	/**
	 * Returns the full content of the input RoiEntry object as a DefaultMutableTreeNode object.
	 * @param re the input RoiEntry
	 * @return the full content of the input RoiEntry object as a DefaultMutableTreeNode object.
	 */
	public DefaultMutableTreeNode getDefaultMutableTreeNode(RoiEntry re) {
		DefaultMutableTreeNode tree = new DefaultMutableTreeNode("Slice "+re.slice);
		tree.add(getDefaultMutableTreeNode(re.Measurements, false));
		
		return tree;
	}
	
	/**
	 * Based on the input array of TreePath, returns the corresponding structures' ids
	 * @param tp the TreePath array for which the structures' ids should be retrieved
	 * @return the ids of the structure corresponding to the input TreePath, 
	 * as an array of Integers
	 */
	public int[] getIds(TreePath[] tp) {
		int[] out=new int[tp.length];
		int index=0;
		
		for(TreePath t:tp) out[index++]=getId(t);
		
		return out;
	}
	
	/**
	 * Based on the input array of TreePath, returns the corresponding structures' ids
	 * @param tp the TreePath array for which the structures' ids should be retrieved
	 * @return the ids of the structure corresponding to the input TreePath, 
	 * as a Set of Integers
	 */
	public Set<Integer> getIdsAsSet(TreePath[] tp) {
		int[] tmp=getIds(tp);
		
		Set<Integer> out=new HashSet<Integer>();
		
		for(int t:tmp) out.add(t);
		
		return out;
	}
	
	/**
	 * Based on the input array of TreePath, returns the corresponding structures' ids, 
	 * including all the descendants
	 * @param tp the TreePath array for which the structure ids should be retrieved
	 * @return the ids of the structures corresponding to the input TreePaths, including 
	 * all descendants, as an array of Integers
	 */
	public int[] getDescendantIds(TreePath[] tp) {
		ArrayList<Integer> out=new ArrayList<Integer>();
		
		for(TreePath t:tp) {
			for(Entry<Integer, TreePath> tt:idToTreePath.entrySet()) {
				if(t.isDescendant(tt.getValue())) {
					out.add(tt.getKey());
				}
			}
		}
		
		return out.stream().mapToInt(Integer::intValue).toArray();
	}
	
	/**
	 * Based on the input TreePath, returns the corresponding structure id
	 * @param tp the TreePath for which the structure id should be retrieved
	 * @return the id of the structure corresponding to the input TreePath, 
	 * or -1 if not found
	 */
	public int getId(TreePath tp) {
		switch(type) {
			case ONTOLOGY_TREEMODEL:
				return getIdFromOntology(tp);
				
			case STRUCTURE_TREEMODEL:
				return getIdFromStructure(tp);
				
			default:
				return -1;
		}
	}
	
	/**
	 * Based on the input TreePath, returns the corresponding structure id,
	 * the DefaultTreeModel being of type "ONTOLOGY_TREEMODEL"
	 * @param tp the TreePath for which the structure id should be retrieved
	 * @return the id of the structure corresponding to the input TreePath, 
	 * or -1 if not found
	 */
	private int getIdFromOntology(TreePath tp) {
		for(Entry<Integer, TreePath> t:idToTreePath.entrySet()) {
			if(tp.equals(t.getValue())) {
				return t.getKey();
			}
		}
		return -1;
	}
	
	/**
	 * Based on the input TreePath, returns the corresponding structure id,
	 * the DefaultTreeModel being of type "STRUCTURE_TREEMODEL"
	 * @param tp the TreePath for which the structure id should be retrieved
	 * @return the id of the structure corresponding to the input TreePath 
	 * or -1 if not found
	 */
	private int getIdFromStructure(TreePath tp) {
		for(Entry<Integer, TreePath> t:idToTreePath.entrySet()) {
			if(t.getValue().isDescendant(tp)) {
				return t.getKey();
			}
		}
		return -1;
	}
	
	/**
	 * Returns a TreePath, based on a structure's id
	 * @param id the id of the structure to look for
	 * @return a TreePath object for the input structure's id
	 */
	public TreePath getTreePathFromId(int id) {
		return idToTreePath.get(id);
	}
	
	/**
	 * Returns an array of TreePath, based on the input structures' ids
	 * @param ids an array of ids of the structures to look for
	 * @return a TreePath array object for the input structures' ids
	 */
	public TreePath[] getTreePathsFromIds(int[] ids) {
		TreePath[] paths=new TreePath[ids.length];
		int index=0;
		for(int id:ids) paths[index++]=idToTreePath.get(id);
		
		return paths;
	}
	
	/**
	 * Returns an array of TreePath, based on the input structures' ids
	 * @param ids a Set of ids of the structures to look for
	 * @return a TreePath array object for the input structures' ids
	 */
	public TreePath[] getTreePathsFromIds(Set<Integer> ids) {
		TreePath[] paths=new TreePath[ids.size()];
		int index=0;
		for(int id:ids) paths[index++]=idToTreePath.get(id);
		
		return paths;
	}
	
	/**
	 * Based on the input array of TreePaths, returns the corresponding ROIs as an ArrayList.
	 * For "ONTOLOGY_TREEMODEL", all Rois for the selected structure are returned.
	 * For "STRUCTURE_TREEMODEL", all Rois are returned except if the path contains
	 * the "Slice" keyword: in that case, only the corresponding Roi for that slice 
	 * is returned
	 * @param tp a TreePath array for which Rois should be retrieved
	 * @return an ArrayList of Roi objects corresponding to the input array of TreePaths
	 */
	public ArrayList<Roi> getRois(TreePath[] tp) {
		ArrayList<Roi> out=new ArrayList<Roi>();
		
		for(TreePath t:tp) {
			switch(type) {
			case ONTOLOGY_TREEMODEL:
				out.addAll(getAllRoisForId(getId(t)));
				break;
				
			case STRUCTURE_TREEMODEL:
				out.addAll(getRoisFromStructure(t));
				break;
			}
		}
		return out;
	}
	
	/**
	 * Based on the input TreePath, returns the corresponding ROIs as an ArrayList.
	 * For "ONTOLOGY_TREEMODEL", all Rois for the selected structure are returned.
	 * For "STRUCTURE_TREEMODEL", all Rois are returned except if the path contains
	 * the "Slice" keyword: in that case, only the corresponding Roi for that slice 
	 * is returned
	 * @param tp the TreePath for which Rois should be retrieved
	 * @return an ArrayList of Roi objects corresponding to the input TreePath
	 */
	public ArrayList<Roi> getRois(TreePath tp) {
		switch(type) {
		case ONTOLOGY_TREEMODEL:
			return getAllRoisForId(getId(tp));
			
		case STRUCTURE_TREEMODEL:
			return getRoisFromStructure(tp);
			
		default:
			return new ArrayList<Roi>();
		}
	}

	/**
	 * Based on the input TreePath, returns the corresponding ROIs as an ArrayList.
	 * All Rois are returned except if the path contains the "Slice" keyword: 
	 * in that case, only the corresponding Roi for that slice is returned
	 * @param tp the TreePath for which the structure id should be retrieved
	 * @return an ArrayList of Roi objects corresponding to the input TreePath
	 */
	private ArrayList<Roi> getRoisFromStructure(TreePath tp) {
		ArrayList<Roi> rois=new ArrayList<Roi>();
		int id=getId(tp);
		if(id!=-1) {
		String pathToString=tp.toString();
			if(pathToString.contains("Slice")) {
				int slice=Integer.parseInt(tp.getPath()[4].toString().replace("Slice ", "")); //Slice keyword should be in the 4th level
				rois.add(adc.Atlas.get(id).ROIs.get(slice).roi);
			}else{
				rois=getAllRoisForId(getId(tp));
			}
		}
		
		return rois;
	}
	
	/**
	 * Based on the input id of a structure, returns the corresponding ROIs as an ArrayList.
	 * @param tp the structure's id from which Rois should be retrieved
	 * @return an ArrayList of Roi objects corresponding to the input id
	 */
	private ArrayList<Roi> getAllRoisForId(int id) {
		ArrayList<Roi> rois=new ArrayList<Roi>();
		
		if(id!=-1) {
			Collection<RoiEntry> re=adc.Atlas.get(id).ROIs.values();
			for(RoiEntry r:re) rois.add(r.roi);
		}
		
		return rois;
	}
	
	/**
	 * Return an Array containing the TreePaths to all structures
	 * @return an Array containing the TreePaths to all structures
	 */
	public TreePath[] getTreePathsForAllRois() {
		return idToTreePath.values().toArray(new TreePath[idToTreePath.size()]);

	}
	
	/**
	 * Return an Array containing the TreePaths to all structures containing the input searchText
	 * @param searchText a String to look for in the all the TreePaths
	 * @return an Array containing the TreePaths to all structures containing the input searchText
	 */
	public TreePath[] getTreePathsContaining(String searchText) {
		ArrayList<TreePath> out=new ArrayList<TreePath>();
		
		for(TreePath t:idToTreePath.values()) {
			if(t.toString().toLowerCase().contains(searchText.toLowerCase())) out.add(t);
		}
		return out.toArray(new TreePath[out.size()]);

	}
	
	/**
	 * Return the Color associated with the input TreePath
	 * @param treePath TreePath of the selection
	 * @return the Color associated with the input TreePath
	 */
	public Color getColor(TreePath treePath) {
		return Color.decode("#"+(getId(treePath)!=-1?adc.Atlas.get(getId(treePath)).color_hex_triplet:"D3D3D3"));
	}
	
	/**
	 * Return true if the structure described in the path has associated Roi(s)
	 * @param treePath TreePath of the selection
	 * @return true if the structure described in the path has associated Roi(s)
	 */
	public boolean hasRois(TreePath treePath) {
		return getId(treePath)!=-1?!adc.Atlas.get(getId(treePath)).ROIs.isEmpty():false;
	}
}
