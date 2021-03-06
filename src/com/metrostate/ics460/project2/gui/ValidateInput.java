package com.metrostate.ics460.project2.gui;

import javafx.scene.control.TextField;

public class ValidateInput {
	
	public boolean isValidate(TextField client_ip_input, TextField client_port_input, 
			TextField server_ip_input, TextField server_port_input, TextField timeout_input, 
			TextField packet_size_input,  TextField window_size_input) {
		
		if(client_ip_input.getText().isEmpty()) {
			client_ip_input.setStyle("-fx-border-color: red;");
			return false;
		}
		
		if(client_port_input.getText().isEmpty()) {
			client_port_input.setStyle("-fx-border-color: red;");
			return false;
		}
		
		if(server_ip_input.getText().isEmpty()) {
			server_ip_input.setStyle("-fx-border-color: red;");
			return false;
		}
		
		if(server_port_input.getText().isEmpty()) {
			server_port_input.setStyle("-fx-border-color: red;");
			return false;
		}
		
		if(timeout_input.getText().isEmpty()) {
			timeout_input.setStyle("-fx-border-color: red;");
			return false;
		}
		
		if(packet_size_input.getText().isEmpty()) {
			packet_size_input.setStyle("-fx-border-color: red;");
			return false;
		}
		
		if(window_size_input.getText().isEmpty()) {
			window_size_input.setStyle("-fx-border-color: red;");
			return false;
			
		} else {
			
			return true;
			
		}
	}

}