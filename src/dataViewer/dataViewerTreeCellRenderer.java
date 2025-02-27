/**
 * dataViewerTreeCellRenderer.java
 * 
 * Created on 8 août 2023
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
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import dataModel.AtlasDataContainer;
import utilities.tools;

/**
 * This class is aimed at personalizing the display of the TreeMap in the dataViewer using adapted icons, 
 * adapting their color for certain categories.
 */
public class dataViewerTreeCellRenderer extends DefaultTreeCellRenderer{

	private static final long serialVersionUID = 1L;
	
	/** Icon to be used for AtlasDataContainer node */
	public final ImageIcon CONTAINER_IMG = new ImageIcon(new ImageIcon(getClass().getResource("resources/container.png")).getImage());
	
	/** Icon to be used for Atlas node */
	public final ImageIcon ATLAS_IMG = new ImageIcon(new ImageIcon(getClass().getResource("resources/atlas.png")).getImage());
	
	/** Icon to be used for anatomical structure node */
	public final ImageIcon BRAIN_IMG = new ImageIcon(new ImageIcon(getClass().getResource("resources/brain.png")).getImage());
	
	/** Icon to be used for metadata node */
	public final ImageIcon METADATA_IMG = new ImageIcon(new ImageIcon(getClass().getResource("resources/metadata.png")).getImage());
	
	/** Icon to be used for information per anatomical structure node */
	public final ImageIcon STRUCTURE_IMG = new ImageIcon(new ImageIcon(getClass().getResource("resources/structure.png")).getImage());
	
	/** Icon to be used for ROIs anatomical structure node */
	public final ImageIcon ROI_IMG = new ImageIcon(new ImageIcon(getClass().getResource("resources/ROI.png")).getImage());
	
	/** Icon to be used for Slices anatomical structure node */
	public final ImageIcon SLICE_IMG = new ImageIcon(new ImageIcon(getClass().getResource("resources/slice.png")).getImage());
	
	/** Icon to be used for Slices anatomical structure node (base) */
	public final ImageIcon SLICE_BASE_IMG = new ImageIcon(new ImageIcon(getClass().getResource("resources/pizza.png")).getImage());
	
	/** Icon to be used for Slices anatomical structure node (variable) */
	public final ImageIcon SLICE_VAR_IMG = new ImageIcon(new ImageIcon(getClass().getResource("resources/pepperoni.png")).getImage());
	
	/** Icon to be used for Measurements node */
	public final ImageIcon MEASUREMENTS_IMG = new ImageIcon(new ImageIcon(getClass().getResource("resources/measurements.png")).getImage());
	
	/** Icon to be used as the default icon */
	public final ImageIcon DEFAULT_IMG = new ImageIcon(new ImageIcon(getClass().getResource("resources/info.png")).getImage());
	
	AtlasDataContainer adc;
	
	public dataViewerTreeCellRenderer(AtlasDataContainer adc) {
		this.adc=adc;
	}
	
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		DefaultMutableTreeNode dmtn=(DefaultMutableTreeNode) value;
		
		TreePath path=new TreePath(dmtn.getPath());
		Color colorAtlas=tree.getModel() instanceof AtlasDataTreeModel?((AtlasDataTreeModel) tree.getModel()).getColor(path):Color.black; //Instance check required to works with Fiji/Java 1.8.0_322
		
		JLabel label=(JLabel)super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

		
		//Default colors
		if (selected) {
			super.setBackground(getBackgroundSelectionColor());
			setForeground(getTextSelectionColor());
		} else {
			super.setBackground(getBackgroundNonSelectionColor());
			setForeground(getTextNonSelectionColor());
		}
		

		//Work on/select the icons
		ImageIcon labelImg=DEFAULT_IMG;

		switch(dmtn.toString()) {
			case "Ontology": case "Atlas Data Container":
				labelImg=CONTAINER_IMG;
				break;
			case "Atlas":
				labelImg=ATLAS_IMG;
				break;
			case "Metadata":
				labelImg=METADATA_IMG;
				break;
			case "Structure":
				labelImg=STRUCTURE_IMG;
				break;
			case "ROIs":
				labelImg=ROI_IMG;
				break;
			case "Measurements":
				labelImg=MEASUREMENTS_IMG;
				break;
				
			default:
				labelImg=tree.getName().equals("Ontology")?tools.changeColor(BRAIN_IMG, colorAtlas):DEFAULT_IMG;
				break;
		}
		
		//Handles cases of sub-categories in Atlas>AtlasEntries>Rois>Slices
		if(dmtn.getParent()!=null) {
			switch(dmtn.getParent().toString()) {
				case "Atlas": //Individual anatomical structures
					labelImg=tools.changeColor(BRAIN_IMG, colorAtlas);
					//Grays out the structures with no associated Roi(s)
					if(!((AtlasDataTreeModel) tree.getModel()).hasRois(path)) {
						setForeground(Color.LIGHT_GRAY);
					}
					break;
				case "ROIs": //Slices within the ROIs section
					//labelImg=SLICE_IMG;
					labelImg=tools.sumChangeColor(SLICE_BASE_IMG, SLICE_VAR_IMG, colorAtlas);
					break;
					
				case "Ontology": //Root section after Ontology section
					labelImg=BRAIN_IMG;
					break;
				}
		}
		
		label.setIcon(labelImg);

		return this;
	}
}