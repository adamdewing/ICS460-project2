package com.metrostate.ics460.project2.sender;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import javax.swing.JFileChooser;

public class FileLoader implements Loader {

	private ByteArrayOutputStream bytearray_stream;
	private byte[] buffer = new byte[128];
	private FileInputStream file_input_stream;
	private String file_name;
	private Map<Integer, byte[]> bytes_array;

	@Override
	public byte[] loadData() {
		JFileChooser file_Chooser = new JFileChooser();
		file_Chooser.setCurrentDirectory(new File("."));

		if (file_Chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			file_name = file_Chooser.getSelectedFile().getName();

			try {
				// get a file from user
				File file = new File(file_name);
				// read file into bytes[]
				file_input_stream = new FileInputStream(file);
				bytearray_stream = new ByteArrayOutputStream();
				int readNum = 0;
				while ((readNum = file_input_stream.read(buffer)) != -1) {
					bytearray_stream.write(buffer, 0, readNum);
					buffer = bytearray_stream.toByteArray();
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (file_input_stream != null) {
					try {
						file_input_stream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return buffer;

	}

}
