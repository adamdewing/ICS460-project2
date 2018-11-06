package com.metrostate.ics460.project2.receiver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Course: 				ICS 460 Networks And Security
 * Project Description: A project uses UDP protocol to send and a receive a binary file.
 * 						The sender accepts a file as a command line parameter (any binary 
 * 						file on your hard disk), breaks it into smaller chunks.
 * Class Description: 	A class FileSaver implements a FileProcessor interface to read a file from a disk 
 * 						and save a file onto disk
 * Instructor: 			Professor Demodar Chetty
 * @author Natnael Alemayehu
 *
 */
public class FileSaver implements Saver {

	/**
	* A method save a file onto a disk it takes a list of byte array as an argument
	*/
	public void saveData(byte[] bytes) {
		File file = new File("output.jpg");
		FileOutputStream fis = null;
		try {
			fis = new FileOutputStream(file);
			fis.write(bytes);
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	
}
