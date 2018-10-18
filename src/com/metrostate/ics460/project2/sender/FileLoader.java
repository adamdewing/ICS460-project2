package com.metrostate.ics460.project2.sender;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFileChooser;

public class FileLoader implements Loader {

	private String fileName;

	public byte[] loadData() {
		
		byte[] buffer = null;
		
		JFileChooser file_Chooser = new JFileChooser();
		file_Chooser.setCurrentDirectory(new File("."));
		if (file_Chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			fileName = file_Chooser.getSelectedFile().getName();
			FileInputStream fis;
			try {
				fis = new FileInputStream((new File(fileName)));
				buffer = fis.readAllBytes();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return buffer;
	}

}
