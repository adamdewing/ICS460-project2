package com.metrostate.ics460.project2.gui;

import javafx.scene.control.TextField;

public class ValidateInput {
	
	public boolean isValidate(TextField client_ip_input, TextField client_port_input, 
			TextField server_ip_input, TextField server_port_input, TextField timeout_input, 
			TextField packet_size_input,  TextField window_size_input) {
		
		if(client_ip_input.getText().isEmpty()) {
			return true;
		}
		
		if(client_port_input.getText().isEmpty()) {
			return true;
		}
		
		if(server_ip_input.getText().isEmpty()) {
			return true;
		}
		
		if(server_port_input.getText().isEmpty()) {
			return true;
		}
		
		if(timeout_input.getText().isEmpty()) {
			return true;
		}
		
		if(packet_size_input.getText().isEmpty()) {
			return true;
		}
		
		if(window_size_input.getText().isEmpty()) {
			return true;
			
		} else {
			
			window_size_input.setStyle("-fx-border-color: red;");
			packet_size_input.setStyle("-fx-border-color: red;");
			timeout_input.setStyle("-fx-border-color: red;");
			server_port_input.setStyle("-fx-border-color: red;");
			server_ip_input.setStyle("-fx-border-color: red;");
			client_port_input.setStyle("-fx-border-color: red;");
			client_ip_input.setStyle("-fx-border-color: red;");
			
			return false;
			
		}
	}

}