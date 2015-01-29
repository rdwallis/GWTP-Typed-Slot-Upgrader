package com.arcbees.gwtp.upgrader;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class Upgrader extends JPanel {
	
	private static final Logger LOGGER = Logger.getGlobal();

	private static final long serialVersionUID = 1L;
	
	JTextField fileTextField = new JTextField();

	private File selectedDir;
	
	private JRadioButton v2;

	public Upgrader() {
		super(new BorderLayout(10, 10));
		
		setPreferredSize(new Dimension(600, 400));
		
		initLogger();
		
        addBackupWarning();
        
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        

        add(centerPanel, BorderLayout.CENTER);

		
        addOpenDialog(centerPanel);
		addVersionChoice(centerPanel);
		addRunButton(centerPanel);
		
		
		
	}

	private void initLogger() {
		JTextArea log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        LOGGER.addHandler(new TextComponentLogger(log));
        add(new JScrollPane(log), BorderLayout.PAGE_END);
		
	}

	private void addRunButton(JPanel container) {
		JPanel panel = new JPanel();
		JButton runButton = new JButton("Convert Project");
		runButton.setSize(new Dimension(100, 20));
		panel.add(runButton);
		container.add(panel, BorderLayout.PAGE_END);
		
		runButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				new SlotCollector(selectedDir, v2.isSelected());
				
			}
		});
		
	}

	private void addVersionChoice(JPanel container) {
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder("Choose target version"));
		
		 v2 = new JRadioButton("GWTP 2.x");
	     JRadioButton v1 = new JRadioButton("GWTP 1.x");
	     ButtonGroup bG = new ButtonGroup();
	     
	     bG.add(v2);
	     bG.add(v1);
	     
	     panel.add(v2);
	     panel.add(v1);
	     v2.setSelected(true);
	     container.add(panel, BorderLayout.CENTER);
	     
		
	}

	private void addOpenDialog(JPanel container) {
		JPanel outer = new JPanel();
		outer.setBorder(new TitledBorder("Choose Project Directory"));
		outer.setLayout(new BorderLayout(10, 10));
		
		
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
		JButton openButton = new JButton("Browse");

		openButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				LOGGER.info("Showing file dialog");
				final JFileChooser fileChooser = new JFileChooser();
				
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				//fileChooser.setSelectedFile(Paths.get("c:/ws1/GWTPUpgradeSample/").toFile());

				if (fileChooser.showOpenDialog(Upgrader.this) == JFileChooser.APPROVE_OPTION) {
					setDirectory(fileChooser.getSelectedFile());
				} else {
					LOGGER.info("file selection cancelled");
				}

			}
		});
		
	
		panel.add(fileTextField, BorderLayout.CENTER);
		panel.add(openButton, BorderLayout.EAST);
		
		outer.add(panel);
		container.add(outer, BorderLayout.PAGE_START);
	}

	private void addBackupWarning() {
		JEditorPane warning = new JEditorPane();
		warning.setContentType("text/html");

		warning.setText("<h2 align='center'>This program will overwrite files in your project!</h2><h3 align='center'>Backup your project before running it!</h3>");
		add(warning, BorderLayout.PAGE_START);
	}


	private static void createAndShowGUI() {
		final JFrame frame = new JFrame("GWTP Upgrader");
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JComponent newContentPane = new Upgrader();
		newContentPane.setOpaque(true);
		frame.setContentPane(newContentPane);

		frame.pack();
		frame.setVisible(true);
	}

	private void setDirectory(File selectedFile) {
		LOGGER.info("Selected dir: " + selectedFile.getName());
		fileTextField.setText(selectedFile.getAbsolutePath());
		this.selectedDir = selectedFile;
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});

	}

}
