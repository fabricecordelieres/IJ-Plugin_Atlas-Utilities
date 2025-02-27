/**
 * dataViewer.java
 * 
 * Created on 7 août 2023
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
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.TreePath;

import dataModel.AtlasDataContainer;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Overlay;
import ij.gui.Roi;
import utilities.pluginsInfo;
import utilities.tools;

import javax.swing.SpringLayout;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JLabel;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.event.TreeSelectionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.awt.event.ActionEvent;
import javax.swing.JTextPane;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JCheckBox;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class dataViewer extends JFrame {
	AtlasDataContainer adc;
	String insertInTitle="";
	boolean hasChanged=false;
	String adcPath="";
	String adcName="";

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JButton openZONButton;
	private JButton selectNoneButton;
	private JButton selectAllButton;
	private JButton measureButton;
	private JComboBox<String> levelComboBox;
	private JButton getImageButton;
	private JButton exportToRoiManagerButton;
	private JButton exportDataButton;
	private JButton saveZONButton;
	private JTree structureTree;
	private JTextPane infosTxtPane;

	private KeyAdapter keyAdapter;


	/** A file filter for ZON files */
	private FileFilter ZONFilter=new FileFilter() {

		@Override
		public String getDescription() {
			return "ZON file (*.zon, *.ZON)";
		}

		@Override
		public boolean accept(File f) {
			return f.getName().toLowerCase().endsWith(".zon");
		}
	};
	private JLabel searchLabel;
	private JTextPane searchOutputTxtPane;
	private JTextField searchTextField;
	private JSlider opacitySlider;
	private JCheckBox fillSelectionsCheckBox;
	private JTabbedPane actionsTabbedPane;
	private JCheckBox whiteOutlinesCheckBox;
	private JLabel lineWidthLabel;
	private JSlider lineWidthSlider;
	private JPanel selectPanel;
	private JPanel outputPanel;
	private JPanel exportPanel;
	private JPanel displayPanel;
	private JComboBox<String> profileComboBox;
	private JButton newProfileButton;
	private JButton delProfileButton;
	private JButton addButton;
	private JButton loadFromZONButton;
	private JButton renameButton;
	private JButton getTableButton;
	private JComboBox<String> normComboBox;
	private JComboBox<String> measureComboBox;
	private JTabbedPane navigationTabbedPane;
	private JScrollPane ontologyPane;
	private JTree ontologyTree;


	private AtlasDataTreeModel ontologyModel;
	private AtlasDataTreeModel structureModel;
	private JScrollPane structurePanel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					dataViewer frame = new dataViewer();//AtlasDataContainer.openJSON("/Users/fab/Desktop/AtlasDataContainer.json"));
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Creates a new Atlas Viewer using the input Atlas Data Container, and add-on to be displayed in the title.
	 */
	public dataViewer() {
		this(null, "");
	}

	/**
	 * Creates a new Atlas Viewer using the input Atlas Data Container, and add-on to be displayed in the title.
	 * @param adc a AtlasDataContainer containing all the information
	 */
	public dataViewer(AtlasDataContainer adc) {
		this(adc, "");
	}

	/**
	 * Creates a new Atlas Viewer using the input Atlas Data Container, and add-on to be displayed in the title.
	 * @param adc a AtlasDataContainer containing all the information
	 * @param insertInTitle a String to be inserted into the window's title
	 */
	public dataViewer(AtlasDataContainer adc, String insertInTitle) {
		this.adc=adc;
		this.insertInTitle=insertInTitle;

		setTitle(buildTitle());

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 631, 512);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);

		navigationTabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		sl_contentPane.putConstraint(SpringLayout.NORTH, navigationTabbedPane, 0, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, navigationTabbedPane, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, navigationTabbedPane, 0, SpringLayout.SOUTH, contentPane);
		contentPane.add(navigationTabbedPane);

		ontologyPane = new JScrollPane();
		navigationTabbedPane.addTab("Ontology", null, ontologyPane, null);

		ontologyTree = new JTree();
		ontologyTree.setName("Ontology");
		ontologyTree.setCellRenderer(new dataViewerTreeCellRenderer(adc));
		ontologyTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent arg0) {
				if(ontologyPane.isShowing()) selectStructureTreeFromOntologyTree();
				showSelectedStructures(true);
				resetSearchText("User defined selection");
			}
		});
		ontologyPane.setViewportView(ontologyTree);
		if(adc==null) {
			ontologyTree.setModel(null);
		}else {
			ontologyModel=new AtlasDataTreeModel(adc, AtlasDataTreeModel.ONTOLOGY_TREEMODEL);
			ontologyTree.setModel(ontologyModel);
		}

		structurePanel = new JScrollPane();
		navigationTabbedPane.addTab("Structures", null, structurePanel, null);

		structureTree = new JTree();
		structureTree.setName("Structures");
		structureTree.setCellRenderer(new dataViewerTreeCellRenderer(adc));

		structureTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent arg0) {
				if(structurePanel.isShowing()) selectOntologyTreeFromStructureTree();
				showSelectedStructures(true);
				resetSearchText("User defined selection");
			}
		});
		structurePanel.setViewportView(structureTree);

		sl_contentPane.putConstraint(SpringLayout.NORTH, structurePanel, 0, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, structurePanel, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, structurePanel, 0, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, structurePanel, 0, SpringLayout.WEST, actionsTabbedPane);
		if(adc==null) {
			structureTree.setModel(null);
		}else {
			structureModel=new AtlasDataTreeModel(adc, AtlasDataTreeModel.STRUCTURE_TREEMODEL);
			structureTree.setModel(structureModel);
		}

		actionsTabbedPane = new JTabbedPane(JTabbedPane.RIGHT);
		sl_contentPane.putConstraint(SpringLayout.EAST, navigationTabbedPane, 0, SpringLayout.WEST, actionsTabbedPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, actionsTabbedPane, -50, SpringLayout.SOUTH, contentPane);
		contentPane.add(actionsTabbedPane);

		sl_contentPane.putConstraint(SpringLayout.NORTH, actionsTabbedPane, 0, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, actionsTabbedPane, -200, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, actionsTabbedPane, 0, SpringLayout.EAST, contentPane);

		//------------------------- OPEN TAB -------------------------
		JPanel openPanel = new JPanel();
		actionsTabbedPane.addTab("Open", null, openPanel, null);
		SpringLayout sl_openPanel = new SpringLayout();
		openPanel.setLayout(sl_openPanel);

		openZONButton = new JButton("Open ZON");
		sl_openPanel.putConstraint(SpringLayout.NORTH, openZONButton, 6, SpringLayout.NORTH, openPanel);
		sl_openPanel.putConstraint(SpringLayout.WEST, openZONButton, 0, SpringLayout.WEST, openPanel);
		sl_openPanel.putConstraint(SpringLayout.EAST, openZONButton, 0, SpringLayout.EAST, openPanel);
		openPanel.add(openZONButton);
		openZONButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openZON();
			}
		});

		//------------------------- SELECT TAB -------------------------
		selectPanel = new JPanel();
		actionsTabbedPane.addTab("Select", null, selectPanel, null);
		SpringLayout sl_selectPanel = new SpringLayout();
		selectPanel.setLayout(sl_selectPanel);

		searchLabel = new JLabel("Find");
		sl_selectPanel.putConstraint(SpringLayout.NORTH, searchLabel, 6, SpringLayout.NORTH, selectPanel);
		searchLabel.setHorizontalAlignment(SwingConstants.CENTER);
		sl_selectPanel.putConstraint(SpringLayout.WEST, searchLabel, 0, SpringLayout.WEST, selectPanel);
		sl_selectPanel.putConstraint(SpringLayout.EAST, searchLabel, 0, SpringLayout.EAST, selectPanel);
		selectPanel.add(searchLabel);

		searchTextField = new JTextField();
		sl_selectPanel.putConstraint(SpringLayout.NORTH, searchTextField, 22, SpringLayout.NORTH, selectPanel);
		sl_selectPanel.putConstraint(SpringLayout.WEST, searchTextField, 6, SpringLayout.WEST, selectPanel);
		sl_selectPanel.putConstraint(SpringLayout.EAST, searchTextField, -6, SpringLayout.EAST, selectPanel);
		searchTextField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				searchOutputTxtPane.setText(textBasedSearch(searchTextField.getText()));
			}
		});
		keyAdapter=new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				searchOutputTxtPane.setText(textBasedSearch(searchTextField.getText()));
			}
		};
		searchTextField.addKeyListener(keyAdapter);

		selectPanel.add(searchTextField);
		searchTextField.setColumns(10);

		searchOutputTxtPane = new JTextPane();
		sl_selectPanel.putConstraint(SpringLayout.SOUTH, searchOutputTxtPane, 64, SpringLayout.SOUTH, searchTextField);
		searchOutputTxtPane.setEditable(false);
		searchOutputTxtPane.setText("No result");
		sl_selectPanel.putConstraint(SpringLayout.NORTH, searchOutputTxtPane, 0, SpringLayout.SOUTH, searchTextField);
		sl_selectPanel.putConstraint(SpringLayout.WEST, searchOutputTxtPane, 0, SpringLayout.WEST, searchTextField);
		sl_selectPanel.putConstraint(SpringLayout.EAST, searchOutputTxtPane, 0, SpringLayout.EAST, searchTextField);
		searchOutputTxtPane.setFont(new Font("Lucida Grande", Font.ITALIC, 11));
		searchOutputTxtPane.setBackground(actionsTabbedPane.getBackground());
		searchOutputTxtPane.setForeground(actionsTabbedPane.getForeground());
		selectPanel.add(searchOutputTxtPane);

		selectNoneButton = new JButton("Select None");
		sl_selectPanel.putConstraint(SpringLayout.NORTH, selectNoneButton, 24, SpringLayout.SOUTH, searchOutputTxtPane);
		sl_selectPanel.putConstraint(SpringLayout.WEST, selectNoneButton, 0, SpringLayout.WEST, selectPanel);
		sl_selectPanel.putConstraint(SpringLayout.EAST, selectNoneButton, 0, SpringLayout.EAST, selectPanel);
		selectPanel.add(selectNoneButton);
		selectNoneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				selectNone();
			}
		});

		selectAllButton = new JButton("Select All");
		sl_selectPanel.putConstraint(SpringLayout.NORTH, selectAllButton, 0, SpringLayout.SOUTH, selectNoneButton);
		sl_selectPanel.putConstraint(SpringLayout.WEST, selectAllButton, 0, SpringLayout.WEST, selectPanel);
		sl_selectPanel.putConstraint(SpringLayout.EAST, selectAllButton, 0, SpringLayout.EAST, selectPanel);
		selectAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectAll();
			}
		});
		selectPanel.add(selectAllButton);



		JLabel profileLabel = new JLabel("Selection profile(s)");
		sl_selectPanel.putConstraint(SpringLayout.NORTH, profileLabel, 24, SpringLayout.SOUTH, selectAllButton);
		profileLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		profileLabel.setHorizontalAlignment(SwingConstants.CENTER);
		sl_selectPanel.putConstraint(SpringLayout.WEST, profileLabel, 0, SpringLayout.WEST, selectPanel);
		sl_selectPanel.putConstraint(SpringLayout.EAST, profileLabel, 0, SpringLayout.EAST, selectPanel);
		selectPanel.add(profileLabel);

		profileComboBox = new JComboBox<String>();
		profileComboBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		sl_selectPanel.putConstraint(SpringLayout.NORTH, profileComboBox, 0, SpringLayout.SOUTH, profileLabel);
		profileComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean isNoneSelected=profileComboBox.getSelectedIndex()==0;
				addButton.setEnabled(!isNoneSelected);
				delProfileButton.setEnabled(!isNoneSelected);
				renameButton.setEnabled(!isNoneSelected);

				if(!isNoneSelected) selectRoisFromProfile();
			}
		});
		updateProfileList();
		sl_selectPanel.putConstraint(SpringLayout.WEST, profileComboBox, 0, SpringLayout.WEST, selectPanel);
		sl_selectPanel.putConstraint(SpringLayout.EAST, profileComboBox, 0, SpringLayout.EAST, selectPanel);
		selectPanel.add(profileComboBox);

		newProfileButton = new JButton("New");
		sl_selectPanel.putConstraint(SpringLayout.NORTH, newProfileButton, 0, SpringLayout.SOUTH, profileComboBox);
		sl_selectPanel.putConstraint(SpringLayout.WEST, newProfileButton, 0, SpringLayout.WEST, selectPanel);
		sl_selectPanel.putConstraint(SpringLayout.EAST, newProfileButton, 55, SpringLayout.WEST, selectPanel);
		newProfileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				createProfile();
			}
		});
		selectPanel.add(newProfileButton);

		addButton = new JButton("Add");
		sl_selectPanel.putConstraint(SpringLayout.WEST, addButton, 47, SpringLayout.WEST, selectPanel);
		sl_selectPanel.putConstraint(SpringLayout.EAST, addButton, 106, SpringLayout.WEST, selectPanel);
		addButton.setEnabled(false);
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addToProfile();
			}
		});
		sl_selectPanel.putConstraint(SpringLayout.NORTH, addButton, 0, SpringLayout.SOUTH, profileComboBox);
		selectPanel.add(addButton);

		delProfileButton = new JButton("Del");
		sl_selectPanel.putConstraint(SpringLayout.NORTH, delProfileButton, 0, SpringLayout.SOUTH, profileComboBox);
		sl_selectPanel.putConstraint(SpringLayout.WEST, delProfileButton, 98, SpringLayout.WEST, selectPanel);
		sl_selectPanel.putConstraint(SpringLayout.EAST, delProfileButton, 0, SpringLayout.EAST, selectPanel);
		delProfileButton.setEnabled(false);
		delProfileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteProfile();
			}
		});
		selectPanel.add(delProfileButton);

		renameButton = new JButton("Rename");
		renameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				renameProfile();
			}
		});
		renameButton.setEnabled(false);
		sl_selectPanel.putConstraint(SpringLayout.NORTH, renameButton, 0, SpringLayout.SOUTH, newProfileButton);
		sl_selectPanel.putConstraint(SpringLayout.WEST, renameButton, 0, SpringLayout.WEST, selectPanel);
		sl_selectPanel.putConstraint(SpringLayout.EAST, renameButton, 82, SpringLayout.WEST, selectPanel);
		selectPanel.add(renameButton);

		loadFromZONButton = new JButton("Load");
		sl_selectPanel.putConstraint(SpringLayout.NORTH, loadFromZONButton, 0, SpringLayout.SOUTH, newProfileButton);
		sl_selectPanel.putConstraint(SpringLayout.WEST, loadFromZONButton, 72, SpringLayout.WEST, selectPanel);
		sl_selectPanel.putConstraint(SpringLayout.EAST, loadFromZONButton, 0, SpringLayout.EAST, selectPanel);
		loadFromZONButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadProfileFromZON();
			}
		});
		selectPanel.add(loadFromZONButton);

		//------------------------- DISPLAY TAB -------------------------
		displayPanel = new JPanel();
		actionsTabbedPane.addTab("Display", null, displayPanel, null);
		SpringLayout sl_displayPanel = new SpringLayout();
		displayPanel.setLayout(sl_displayPanel);

		lineWidthLabel = new JLabel("Line width");
		sl_displayPanel.putConstraint(SpringLayout.NORTH, lineWidthLabel, 6, SpringLayout.NORTH, displayPanel);
		lineWidthLabel.setHorizontalAlignment(SwingConstants.CENTER);
		sl_displayPanel.putConstraint(SpringLayout.WEST, lineWidthLabel, 0, SpringLayout.WEST, displayPanel);
		sl_displayPanel.putConstraint(SpringLayout.EAST, lineWidthLabel, 0, SpringLayout.EAST, displayPanel);
		displayPanel.add(lineWidthLabel);

		lineWidthSlider = new JSlider();
		lineWidthSlider.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		sl_displayPanel.putConstraint(SpringLayout.WEST, lineWidthSlider, 0, SpringLayout.WEST, displayPanel);
		sl_displayPanel.putConstraint(SpringLayout.EAST, lineWidthSlider, 0, SpringLayout.EAST, displayPanel);
		lineWidthSlider.setMinimum(1);
		lineWidthSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				showSelectedStructures(false);
				resetSearchText("User defined selection");
			}
		});
		lineWidthSlider.setMinorTickSpacing(1);
		lineWidthSlider.setPaintLabels(true);
		lineWidthSlider.setSnapToTicks(true);
		lineWidthSlider.setMajorTickSpacing(4);
		lineWidthSlider.setPaintTicks(true);
		lineWidthSlider.setValue(1);
		lineWidthSlider.setMaximum(9);
		sl_displayPanel.putConstraint(SpringLayout.NORTH, lineWidthSlider, 0, SpringLayout.SOUTH, lineWidthLabel);
		displayPanel.add(lineWidthSlider);

		JLabel opacityLabel = new JLabel("Opacity");
		sl_displayPanel.putConstraint(SpringLayout.NORTH, opacityLabel, 12, SpringLayout.SOUTH, lineWidthSlider);
		opacityLabel.setHorizontalAlignment(SwingConstants.CENTER);
		sl_displayPanel.putConstraint(SpringLayout.WEST, opacityLabel, 0, SpringLayout.WEST, displayPanel);
		sl_displayPanel.putConstraint(SpringLayout.EAST, opacityLabel, 0, SpringLayout.EAST, displayPanel);
		displayPanel.add(opacityLabel);

		opacitySlider = new JSlider();
		opacitySlider.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		sl_displayPanel.putConstraint(SpringLayout.WEST, opacitySlider, 0, SpringLayout.WEST, displayPanel);
		sl_displayPanel.putConstraint(SpringLayout.EAST, opacitySlider, 0, SpringLayout.EAST, displayPanel);
		opacitySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				showSelectedStructures(false);
				resetSearchText("User defined selection");
			}
		});
		opacitySlider.setSnapToTicks(true);
		sl_displayPanel.putConstraint(SpringLayout.NORTH, opacitySlider, 0, SpringLayout.SOUTH, opacityLabel);
		opacitySlider.setMinorTickSpacing(5);
		opacitySlider.setMajorTickSpacing(25);
		opacitySlider.setPaintTicks(true);
		opacitySlider.setPaintLabels(true);
		opacitySlider.setValue(100);
		displayPanel.add(opacitySlider);

		fillSelectionsCheckBox = new JCheckBox("Fill selection(s)");
		sl_displayPanel.putConstraint(SpringLayout.NORTH, fillSelectionsCheckBox, 24, SpringLayout.SOUTH, opacitySlider);
		sl_displayPanel.putConstraint(SpringLayout.WEST, fillSelectionsCheckBox, 12, SpringLayout.WEST, displayPanel);
		fillSelectionsCheckBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				showSelectedStructures(false);
				resetSearchText("User defined selection");
			}
		});
		sl_displayPanel.putConstraint(SpringLayout.EAST, fillSelectionsCheckBox, 0, SpringLayout.EAST, displayPanel);
		displayPanel.add(fillSelectionsCheckBox);


		whiteOutlinesCheckBox = new JCheckBox("White outline(s)");
		whiteOutlinesCheckBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				showSelectedStructures(false);
				resetSearchText("User defined selection");
			}
		});
		sl_displayPanel.putConstraint(SpringLayout.NORTH, whiteOutlinesCheckBox, 0, SpringLayout.SOUTH, fillSelectionsCheckBox);
		sl_displayPanel.putConstraint(SpringLayout.WEST, whiteOutlinesCheckBox, 0, SpringLayout.WEST, fillSelectionsCheckBox);
		sl_displayPanel.putConstraint(SpringLayout.EAST, whiteOutlinesCheckBox, 0, SpringLayout.EAST, fillSelectionsCheckBox);
		displayPanel.add(whiteOutlinesCheckBox);



		//------------------------- OUTPUT TAB -------------------------
		outputPanel = new JPanel();
		actionsTabbedPane.addTab("New tab", null, outputPanel, null);
		actionsTabbedPane.setTitleAt(3, "Output");

		measureButton = new JButton("Measure");
		measureButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				measure();
			}
		});

		SpringLayout sl_outputPanel = new SpringLayout();
		sl_outputPanel.putConstraint(SpringLayout.NORTH, measureButton, 6, SpringLayout.NORTH, outputPanel);
		sl_outputPanel.putConstraint(SpringLayout.WEST, measureButton, 0, SpringLayout.WEST, outputPanel);
		sl_outputPanel.putConstraint(SpringLayout.EAST, measureButton, 0, SpringLayout.EAST, outputPanel);
		outputPanel.setLayout(sl_outputPanel);
		outputPanel.add(measureButton);

		levelComboBox = new JComboBox<String>();
		levelComboBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		sl_outputPanel.putConstraint(SpringLayout.NORTH, levelComboBox, 24, SpringLayout.SOUTH, measureButton);
		sl_outputPanel.putConstraint(SpringLayout.WEST, levelComboBox, 0, SpringLayout.WEST, outputPanel);
		sl_outputPanel.putConstraint(SpringLayout.EAST, levelComboBox, 0, SpringLayout.EAST, outputPanel);
		levelComboBox.setModel(new DefaultComboBoxModel<String>(AtlasDataContainer.ANALYSIS_LEVEL));
		outputPanel.add(levelComboBox);

		measureComboBox = new JComboBox<String>();
		measureComboBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		sl_outputPanel.putConstraint(SpringLayout.NORTH, measureComboBox, 0, SpringLayout.SOUTH, levelComboBox);
		measureComboBox.setModel(new DefaultComboBoxModel<String>(AtlasDataContainer.ANALYSIS_MEASUREMENT));
		sl_outputPanel.putConstraint(SpringLayout.WEST, measureComboBox, 0, SpringLayout.WEST, outputPanel);
		sl_outputPanel.putConstraint(SpringLayout.EAST, measureComboBox, 0, SpringLayout.EAST, outputPanel);
		outputPanel.add(measureComboBox);

		normComboBox = new JComboBox<String>();
		normComboBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		normComboBox.setModel(new DefaultComboBoxModel<String>(AtlasDataContainer.ANALYSIS_NORM));
		sl_outputPanel.putConstraint(SpringLayout.NORTH, normComboBox, 0, SpringLayout.SOUTH, measureComboBox);
		sl_outputPanel.putConstraint(SpringLayout.WEST, normComboBox, 0, SpringLayout.WEST, outputPanel);
		sl_outputPanel.putConstraint(SpringLayout.EAST, normComboBox, 0, SpringLayout.EAST, outputPanel);
		outputPanel.add(normComboBox);

		getImageButton = new JButton("Get Image");
		getImageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showOutputImage();
			}
		});
		sl_outputPanel.putConstraint(SpringLayout.NORTH, getImageButton, 16, SpringLayout.SOUTH, normComboBox);
		sl_outputPanel.putConstraint(SpringLayout.WEST, getImageButton, 0, SpringLayout.WEST, outputPanel);
		sl_outputPanel.putConstraint(SpringLayout.EAST, getImageButton, 0, SpringLayout.EAST, outputPanel);
		outputPanel.add(getImageButton);

		getTableButton = new JButton("Get Table");
		sl_outputPanel.putConstraint(SpringLayout.NORTH, getTableButton, 0, SpringLayout.SOUTH, getImageButton);
		sl_outputPanel.putConstraint(SpringLayout.WEST, getTableButton, 0, SpringLayout.WEST, outputPanel);
		sl_outputPanel.putConstraint(SpringLayout.EAST, getTableButton, 0, SpringLayout.EAST, outputPanel);
		outputPanel.add(getTableButton);

		//------------------------- EXPORT TAB -------------------------
		exportPanel = new JPanel();
		actionsTabbedPane.addTab("Export", null, exportPanel, null);

		exportToRoiManagerButton = new JButton("Export To Roi Manager");
		exportToRoiManagerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				exportToRoiManager();
			}
		});
		SpringLayout sl_exportPanel = new SpringLayout();
		sl_exportPanel.putConstraint(SpringLayout.NORTH, exportToRoiManagerButton, 6, SpringLayout.NORTH, exportPanel);
		sl_exportPanel.putConstraint(SpringLayout.WEST, exportToRoiManagerButton, 0, SpringLayout.WEST, exportPanel);
		sl_exportPanel.putConstraint(SpringLayout.EAST, exportToRoiManagerButton, 0, SpringLayout.EAST, exportPanel);
		exportPanel.setLayout(sl_exportPanel);
		exportPanel.add(exportToRoiManagerButton);

		exportDataButton = new JButton("Export Data");
		sl_exportPanel.putConstraint(SpringLayout.NORTH, exportDataButton, 0, SpringLayout.SOUTH, exportToRoiManagerButton);
		sl_exportPanel.putConstraint(SpringLayout.WEST, exportDataButton, 0, SpringLayout.WEST, exportPanel);
		sl_exportPanel.putConstraint(SpringLayout.EAST, exportDataButton, 0, SpringLayout.EAST, exportPanel);
		exportPanel.add(exportDataButton);

		saveZONButton = new JButton("Save ZON");
		sl_exportPanel.putConstraint(SpringLayout.NORTH, saveZONButton, 0, SpringLayout.SOUTH, exportDataButton);
		sl_exportPanel.putConstraint(SpringLayout.WEST, saveZONButton, 0, SpringLayout.WEST, exportPanel);
		sl_exportPanel.putConstraint(SpringLayout.EAST, saveZONButton, 0, SpringLayout.EAST, exportPanel);
		saveZONButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				saveAsZON();
			}
		});
		exportPanel.add(saveZONButton);

		infosTxtPane=new JTextPane();
		sl_contentPane.putConstraint(SpringLayout.NORTH, infosTxtPane, -24, SpringLayout.SOUTH, contentPane);
		infosTxtPane.setEditable(false);
		infosTxtPane.setContentType("text/html");
		infosTxtPane.setText(pluginsInfo.CONTACT);
		infosTxtPane.setBackground(actionsTabbedPane.getBackground());
		infosTxtPane.setForeground(actionsTabbedPane.getForeground());
		sl_contentPane.putConstraint(SpringLayout.SOUTH, infosTxtPane, 0, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, infosTxtPane, 0, SpringLayout.EAST, contentPane);
		contentPane.add(infosTxtPane);




		//Plays with tabs' states (except "Open")
		setEnabledOptions(adc!=null);
	}

	//============================================================================================
	
	//****************************************************************************
	//*******************************GENERAL UPDATE*******************************
	//****************************************************************************
	
	
	/**
	 * Builds the title to be used for the JFrame
	 * @return the title to be used for the JFrame
	 */
	private String buildTitle() {
		return "Atlas Viewer "+pluginsInfo.ATLAS_TO_ROI_VERSION+" ("+pluginsInfo.ATLAS_TO_ROI_DATE+")"+(insertInTitle.isEmpty()?"":" - "+insertInTitle)+(hasChanged?"*":"");
	}
	
	/**
	 * Allows modifying all options' states at once (except Open ZON)
	 * @param state the state, true or false, to apply to all GUI elements
	 */
	public void setEnabledOptions(boolean state){
		if(state) actionsTabbedPane.setSelectedIndex(1);
		for(int i=1; i<actionsTabbedPane.getTabCount(); i++) actionsTabbedPane.setEnabledAt(i, state);

	}

	/**
	 * Tags the internal data has changed and notifies it in the frame's title
	 */
	public void notifyAtlasDataContainerHasChanged() {
		hasChanged=true;
		setTitle(buildTitle());
	}
	
	/**
	 * Updates the Jtree in case modifications have been made to the AtlasDataContainer
	 * @param hasChanged true if a tag should state the AtlasDataContainer has been modified
	 */
	public void updateTreeAfterChange(boolean hasChanged) {
		this.hasChanged=hasChanged;

		ontologyModel=new AtlasDataTreeModel(adc, AtlasDataTreeModel.ONTOLOGY_TREEMODEL);
		ontologyTree.setModel(ontologyModel);

		structureModel=new AtlasDataTreeModel(adc, AtlasDataTreeModel.STRUCTURE_TREEMODEL);
		structureTree.setModel(structureModel);

		setTitle(buildTitle());
	}
	
	
	//****************************************************************************
	//*******************************GENERAL UPDATE*******************************
	//****************************************************************************

	//============================================================================================

	//****************************************************************************
	//********************************INPUT/OUTPUT********************************
	//****************************************************************************


	/**
	 * Opens a new ZON file and updates the interface.
	 * Displays an error message in case the file is not in the appropriate format.
	 */
	public void openZON() {
		AtlasDataContainer toBeOpened=getAtlasDataContainerFromZON();
		if(toBeOpened!=null) {
			adc=toBeOpened;
			insertInTitle=adcName;
			updateTreeAfterChange(false);
			updateProfileList();
			setEnabledOptions(true);
			searchOutputTxtPane.setText("No result");
		}
	}

	/**
	 * Opens a new ZON file and returns it as an AtlasDataContainer object.
	 * Displays an error message in case the file is not in the appropriate format.
	 * @return the content of the ZON file as an AtlasDataContainer object
	 */
	public AtlasDataContainer getAtlasDataContainerFromZON() {
		AtlasDataContainer adc=null;
		JFileChooser jfc=new JFileChooser();
		jfc.addChoosableFileFilter(ZONFilter);
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jfc.setAcceptAllFileFilterUsed(false);

		int result=jfc.showOpenDialog(null);

		if(result==JFileChooser.APPROVE_OPTION) {
			try {
				adcPath=jfc.getSelectedFile().getPath();
				adcName=jfc.getSelectedFile().getName();
				adc=AtlasDataContainer.openZON(adcPath);
			}catch (Exception e) {
				System.out.println(e.getLocalizedMessage());
				IJ.error("Error loading ZON file", "The file "+jfc.getSelectedFile().getPath()+" is not formatted as expected.\nPlease try again.");
			}	
		}
		return adc;
	}

	/**
	 * Saves the current AtlasDataContainer to a new ZON file.
	 * Does nothing if the AtlasDataContainer is null.
	 * Displays an error message in case the file is not in the
	 * appropriate format
	 */
	public void saveAsZON() {
		if(adc!=null) {
			JFileChooser jfc=new JFileChooser();
			jfc.addChoosableFileFilter(ZONFilter);
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			jfc.setAcceptAllFileFilterUsed(false);

			int result=jfc.showSaveDialog(null);

			if(result==JFileChooser.APPROVE_OPTION) {
				try {
					adcPath=tools.checkFileExtension(jfc.getSelectedFile().getPath(),".zon");
					adcName=jfc.getSelectedFile().getName();
					adc.saveAsZON(adcPath);
					insertInTitle=adcName;
					updateTreeAfterChange(false);
				}catch (Exception e) {
					IJ.error("Error saving ZON file", "The file "+jfc.getSelectedFile().getPath()+"\ncan not be written.\nPlease try again.");
					IJ.error(e.getMessage());
				}	
			}
		}
	}

	/**
	 * Loads all profiles contained in a ZON file and adds them to the current Atlas Data Container.
	 * In case a profile exists with the same name, the added one is added -1, -2, -3... until the naming is Ok.
	 */
	public void loadProfileFromZON() {
		AtlasDataContainer openForProfiles=getAtlasDataContainerFromZON();
		if(!openForProfiles.selectionProfiles.isEmpty()) {
			for(String name:openForProfiles.selectionProfiles.keySet()) {
				String tmpName=name;
				int index=1;
				//Adds -1, -2, -3... at the end of the name in case of duplicate entry
				while(adc.selectionProfiles.get(tmpName)!=null) {
					tmpName=name+"-"+index++;
				}
				adc.selectionProfiles.put(tmpName, openForProfiles.selectionProfiles.get(name));
			}
			selectNone();
			updateProfileList();
			profileComboBox.setSelectedIndex(0);
			notifyAtlasDataContainerHasChanged();
		}
	}

	//****************************************************************************
	//********************************INPUT/OUTPUT********************************
	//****************************************************************************

	//============================================================================================

	//****************************************************************************
	//*********************************SELECTIONS*********************************
	//****************************************************************************


	/**
	 * Unselects all ROIs in the tree
	 */
	public void selectNone() {
		int slice=tools.getSliceSafe();
		ontologyTree.clearSelection();
		structureTree.clearSelection();
		showNone();
		tools.setSliceSafe(slice);
		resetSearchText("Select None pressed");
	}

	/**
	 * Selects all ROIs in the tree
	 */
	public void selectAll() {
		int slice=tools.getSliceSafe();
		ontologyTree.clearSelection();
		ontologyTree.setSelectionPaths(structureModel.getTreePathsForAllRois());
		structureTree.clearSelection();
		structureTree.setSelectionPaths(structureModel.getTreePathsForAllRois());
		tools.setSliceSafe(slice);
		resetSearchText("Select All pressed");
	}

	/**
	 * Updates the structureTree selection from selection in the ontologyTree
	 */
	public void selectStructureTreeFromOntologyTree() {
		TreePath[] treePaths=ontologyTree.getSelectionPaths();
		if(treePaths!=null) {
			int[] ontologyTreeSelection=ontologyModel.getDescendantIds(treePaths);
			TreePath[] toSelect=structureModel.getTreePathsFromIds(ontologyTreeSelection);

			int slice=tools.getSliceSafe();
			structureTree.clearSelection();
			structureTree.setSelectionPaths(toSelect);
			tools.setSliceSafe(slice);
			resetSearchText("Selected entrie(s)\nFrom Ontology");
		}
	}

	/**
	 * Updates the ontologyTree selection from selection in the structureTree
	 */
	public void selectOntologyTreeFromStructureTree() {
		TreePath[] treePaths=structureTree.getSelectionPaths();
		if(treePaths!=null) {
			int[] structureTreeSelection=structureModel.getIds(treePaths);
			TreePath[] toSelect=ontologyModel.getTreePathsFromIds(structureTreeSelection);

			int slice=tools.getSliceSafe();
			ontologyTree.clearSelection();
			ontologyTree.setSelectionPaths(toSelect);
			tools.setSliceSafe(slice);
			resetSearchText("Selected entrie(s)\nFrom Structure");
		}
	}

	
	//****************************************************************************
	//*********************************SELECTIONS*********************************
	//****************************************************************************

	//============================================================================================

	//****************************************************************************
	//************************************SHOW************************************
	//****************************************************************************

	
	/**
	 * Removes the overlay from the current image. Does nothing if none is present.
	 */
	public void showNone() {
		ImagePlus ip=WindowManager.getCurrentImage();
		if(ip!=null) {
			ip.setOverlay(null);
		}
	}

	/**
	 * Generates an overlay containing all Rois.
	 * In case no image is active, creates a new empty one, based on the metadata extracted from the annotation image.
	 * Optional: Activates the first z slices on which a Roi is positionned.
	 * @param updateSlice if true, the slice is set to the first found roi. It is left untouched otherwise
	 */
	public void showAllRois(boolean updateSlice) {
		ArrayList<Roi> rois=adc.getAllRois();
		showRois(rois, updateSlice);
	}

	/**
	 * Displays the Rois selected in structure Tree on the active ImagePlus.
	 * in case no selection has been made, removes all Rois.
	 * @param updateSlice if true move the slice slider to the plane where the 
	 * first Roi is visible.
	 */
	public void showSelectedStructures(boolean updateSlice) {
		TreePath[] selectionPaths=structureTree.getSelectionPaths();
		if(selectionPaths!=null) {
			showRois(structureModel.getRois(selectionPaths), updateSlice);
		}else {
			showNone();
		}
	}

	/**
	 * Generates an overlay containing all input Rois.
	 * In case no image is active, creates a new empty one, based on the metadata extracted from the annotation image.
	 * Optional: Activates the middle z slices between the first and last slice on which a Roi is positionned.
	 * @param rois the ArrayList of Rois to display
	 * @param updateSlice if true, the slice is set to the middle z slices between the first and last slice on which 
	 * a Roi is found. It is left untouched otherwise
	 */

	public void showRois(ArrayList<Roi> rois, boolean updateSlice) {
		if(!rois.isEmpty()) {
			ImagePlus ip=getActiveImagePlus();
			Overlay ov=new Overlay();

			int minSlice=ip.getNSlices();
			int maxSlice=1;
			for(Roi roi:rois) {
				Roi roiCopy=(Roi) roi.clone(); //Make a copy not to modify the oprifinal values within the Roi

				Color roiColor=roiCopy.getStrokeColor();
				Color transpColor=new Color(roiColor.getRed(), roiColor.getGreen(), roiColor.getBlue(), 255*opacitySlider.getValue()/100);
				roiCopy.setStrokeWidth(lineWidthSlider.getValue());

				if(fillSelectionsCheckBox.isSelected()) {
					roiCopy.setFillColor(transpColor);
				}
				roiCopy.setStrokeColor(whiteOutlinesCheckBox.isSelected()?Color.WHITE:roiColor); //Should be set AFTER fill color to be taken into account
				roiCopy.setStrokeWidth(lineWidthSlider.getValue());
				ov.add(roiCopy);

				minSlice=Math.min(minSlice, roiCopy.getZPosition());
				maxSlice=Math.max(maxSlice, roiCopy.getZPosition());
			}

			ip.setOverlay(ov);
			if(updateSlice) ip.setSlice((minSlice+maxSlice)/2);
		}else {
			showNone();
		}
	}

	/**
	 * Returns the active ImagePlus nor creates one based on the AtlasDataContainer metadata
	 * @return the active ImagePlus nor creates one based on the AtlasDataContainer metadata
	 */
	public ImagePlus getActiveImagePlus() {
		ImagePlus ip=WindowManager.getCurrentImage();
		if(ip==null) {
			ip=adc.getCalibratedImagePlus();
			ip.show();
		}
		return ip;
	}


	//****************************************************************************
	//************************************SHOW************************************
	//****************************************************************************

	//============================================================================================

	//****************************************************************************
	//********************************TEXT SEARCH*********************************
	//****************************************************************************

	
	/**
	 * For the search field: inactivates the listener, removes text, reactivates listener
	 * @param outputLabelMsg a String representing the message to output in the text search output label
	 */
	public void resetSearchText(String outputLabelMsg) {
		if(!searchTextField.hasFocus()) {
			outputLabelMsg=outputLabelMsg==null?"":outputLabelMsg;
			searchTextField.setText("");
			searchOutputTxtPane.setText(outputLabelMsg);
		}
	}

	/**
	 * Selects all anatomical structures which name contains the input String.
	 * Returns a message describing the results of the search
	 * @param findText a part of text to look for in the name of anatomical structures
	 * @return a message describing the results of the search
	 */
	public String textBasedSearch(String findText) {
		int slice=tools.getSliceSafe();
		int inputLength=findText.length();

		if(inputLength<3) {
			return inputLength>0?"Need "+(3-inputLength)+" more character(s)":"Nothing to look for";
		}else{
			TreePath[] paths=structureModel.getTreePathsContaining(findText);
			if(paths!=null) {
				structureTree.setSelectionPaths(paths);
				tools.setSliceSafe(slice);
				return "Found "+paths.length+" structure(s)";
			}else {
				tools.setSliceSafe(slice);
				return "No structure found";
			}
		}
	}


	//****************************************************************************
	//********************************TEXT SEARCH*********************************
	//****************************************************************************

	//============================================================================================

	//****************************************************************************
	//*********************************PROFILES***********************************
	//****************************************************************************


	/**
	 * Updates the list of selection profile(s)
	 */
	public void updateProfileList() {
		String[] profilesForComboBox=new String[] {"<None>"};
		if(adc!=null) {
			if(!adc.selectionProfiles.isEmpty()) {
				Set<String> keys=adc.selectionProfiles.keySet();
				String[] profilesFromADC=(String[]) keys.toArray(new String[keys.size()]);
				profilesForComboBox=new String[profilesFromADC.length+1];
				profilesForComboBox[0]="<None>";
				System.arraycopy(profilesFromADC, 0, profilesForComboBox, 1, profilesFromADC.length);
			}
		}
		profileComboBox.setModel(new DefaultComboBoxModel<String>(profilesForComboBox));
	}

	/**
	 * Creates a new empty selection profile and updates the drop-down list
	 * @return true if everything went well, false otherwise
	 */
	public boolean createProfile() {
		String name=IJ.getString("Profile's name", "New profile");
		boolean allClear=true;

		if(name.equals("")) {
			IJ.error("New Profile", "Error: name can't be empty");
			allClear=false;
		}
		if(adc.selectionProfiles.get(name)!=null) {
			IJ.error("New Profile", "Error: this name already exists");
			allClear=false;
		}

		if(allClear) {
			
			Set<Integer> values=structureTree.getSelectionPaths()!=null?structureModel.getIdsAsSet(structureTree.getSelectionPaths()):new HashSet<Integer>();
			adc.selectionProfiles.put(name, values);

			updateProfileList();
			profileComboBox.setSelectedItem(name);
			notifyAtlasDataContainerHasChanged();
		}

		return allClear;
	}

	/**
	 * Adds the current selection to the selected profile
	 */
	public void addToProfile() {
		String profile=profileComboBox.getSelectedItem().toString();
		Set<Integer> ids=structureModel.getIdsAsSet(structureTree.getSelectionPaths());

		if(ids.isEmpty()) {
			IJ.error("Add Selection(s) to Profile", "Error: nothing to add");
		}else {
			//Required to be able to do addAll
			Set<Integer> tmp=new HashSet<Integer>(adc.selectionProfiles.get(profile));
			tmp.addAll(ids);
			adc.selectionProfiles.put(profile, tmp);

			selectRoisFromProfile();

			notifyAtlasDataContainerHasChanged();
		}
	}

	/**
	 * Delete the currently selected profile
	 */
	public void deleteProfile() {
		String profile=(String) profileComboBox.getSelectedItem();
		if(tools.confirmationDialog("Delete Profile", "Are you sure you want to\n delete profile "+profile)) {
			adc.selectionProfiles.remove(profile);
			selectNone();
			updateProfileList();
			profileComboBox.setSelectedIndex(0);
			notifyAtlasDataContainerHasChanged();
		}

	}

	/**
	 * Rename the currently selected profile
	 */
	public void renameProfile() {
		String profile=(String) profileComboBox.getSelectedItem();
		Set<String> oldList=new HashSet<String>(adc.selectionProfiles.keySet());//KeySet is dynamically modified! Keep track of its current state
		if(createProfile()) {
			adc.selectionProfiles.remove(profile);
			selectNone();
			updateProfileList();
			Set<String> newList=adc.selectionProfiles.keySet();

			String newName="";
			//Look for new item
			for(String item:newList) {
				if(!oldList.contains(item)) {
					newName=item;
					break;
				}
			}
			profileComboBox.setSelectedItem(newName);
			notifyAtlasDataContainerHasChanged();
		}

	}

	/**
	 * Activates selections when a profile is selected
	 */
	public void selectRoisFromProfile() {
		String profile=(String) profileComboBox.getSelectedItem();
		int slice=tools.getSliceSafe();
		structureTree.clearSelection();
		structureTree.setSelectionPaths(structureModel.getTreePathsFromIds(adc.selectionProfiles.get(profile)));
		tools.setSliceSafe(slice);
		resetSearchText(adc.selectionProfiles.get(profile).size()+" selection(s) \nFrom Profile "+profile);
	}
	
	
	//****************************************************************************
	//*********************************PROFILES***********************************
	//****************************************************************************
	
	//============================================================================================
	
	//****************************************************************************
	//*********************************MEASURES***********************************
	//****************************************************************************
	
	/**
	 * Updates all stored measurements with the ones from the active ImagePlus
	 */
	public void measure() {
		adc.measure(getActiveImagePlus());
		//Update the display+updates the AtalsDataContainer status
		updateTreeAfterChange(true);
	}
	
	
	//****************************************************************************
	//*********************************MEASURES***********************************
	//****************************************************************************
	
	//============================================================================================
	
	//****************************************************************************
	//*********************************EXPORTS************************************
	//****************************************************************************
	
	/**
	 * Exports the selected structures to the RoiManager.
	 * Exports all Rois in case none is selected.
	 */
	public void exportToRoiManager() {
		adc.toRoiManager(structureModel.getIdsAsSet(structureTree.getSelectionPaths()));
	}
	
	
	//****************************************************************************
	//*********************************EXPORTS************************************
	//****************************************************************************

	//============================================================================================

	/**
	 * Displays output images, based on the measurements stored in the AtlasDataContainer
	 */
	public void showOutputImage() {
		TreePath[] paths=structureTree.getSelectionPaths();
		Integer[] ids;
		if(paths==null) {
			ids=structureModel.idToTreePath.keySet().toArray(new Integer[structureModel.idToTreePath.size()]); //No selection is select everything
		}else {
			ids=Arrays.stream(structureModel.getIds(paths)) // IntStream
        			.boxed()                // Stream<Integer>
        			.toArray(Integer[]::new);
		}
		
		
		
		ids=ids.length==0?(Integer[]) adc.nameId.values().toArray(new Integer[adc.nameId.values().size()]):ids;

		adc.getImage(0, 0, 0, ids).show();
		adc.getImage(0, 1, 0, ids).show();
		adc.getImage(0, 5, 0, ids).show();
		adc.getImage(1, 0, 0, ids).show();
		adc.getImage(1, 1, 0, ids).show();
		adc.getImage(1, 5, 0, ids).show();

	}



	





	



	

















	

	
}
