package it.cnr.isti.labsedc.transponder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class WiFiScanner extends Thread {
	
	private static String homePath;
	private String deviceWiFi;
    private static long lastMessageTime;
	
	public WiFiScanner(String homePath, String deviceWiFi) {
		WiFiScanner.homePath = homePath;
		this.deviceWiFi = deviceWiFi;

		System.out.println("Setting up WiFi Scanner Daemon with parameters:\n" + "Device WiFi " + deviceWiFi +
				"\n" +"and HomePath " + homePath);
		System.out.println("-----------------------------------");
	}
	

	public void run() {
		try {	
			String[] commandStrings = {"sudo", "tcpdump", "-i", deviceWiFi, "-e", "type", "mgt"};
//			Process p = Runtime.getRuntime().exec(
//			"sudo tcpdump -i " + deviceWiFi + " -e type mgt");
			Process p = Runtime.getRuntime().exec(commandStrings);
		new Thread(new Runnable() {
			private String macAddress;
			private String receivedDb;
		    
			public void run() {
		    	System.out.println("Wi-Fi Probe started");
		    	
		    	try {
		    		BufferedReader input = new BufferedReader(
		        		new InputStreamReader(p.getInputStream()));
			        String line, capturedString = "";	
					String[] results;
					System.out.print("Scan running...");
			        
			            while ((line = input.readLine()) != null) {
		            		System.out.println(line);
			            	if (line != null && (
			            			line.contains("Probe Request") || 
			            			line.contains("Beacon")))
		                	{
			            		results = line.split(" ");
			            		if (line.contains("Request")) {
			            			macAddress = results[16];
			            			receivedDb = results[10];
	    				        } else {
			            		macAddress = results[18];
			            		receivedDb = results[10];
			            		}
			            			
			            		//capturedString = "#"+dateFormat.format(date)+"&"+macAddress+"&"+receivedDb+"&"+readGPS()+"#";
			            		capturedString = "#"+macAddress+"&"+receivedDb+"&"+readGPS()+"#";
			            		writeOnFile(capturedString);	
			            		LoRaAnd4GDispatcher.addToBuffer(capturedString);
			            		lastMessageTime = System.currentTimeMillis();
			            		MonitoringConnector.sendEventMessage(capturedString);
		                	}
			        		checkPing();
			            }
		        } catch (IOException e) {
		            e.printStackTrace();
		        }				
			}
			
			private void writeOnFile(String message) {
					File theFile = new File(WiFiScanner.homePath + "wifiDump");
	    			
					FileWriter write;
					BufferedWriter stream;
					try {
						write = new FileWriter(theFile,true);
				        stream = new BufferedWriter(write);
				        
			            stream.append(message+"\n");
			            stream.flush();	
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
			
			void checkPing() {
		    	try {
				new Thread(new Runnable() {

					@Override
					public void run() {
						if ((System.currentTimeMillis() - lastMessageTime) > 5000) {
							if(LoRaAnd4GDispatcher.toSendBuffer.size() == 0) {
								LoRaAnd4GDispatcher.addToBuffer("#BSSID:00:00:00:00:00:00&-00dBm&0000.00000,00000.00000#");
								System.out.println("For checking connection, an empty message will be sent.");
							}
							lastMessageTime = System.currentTimeMillis();							
						}
					}
		    	}).start();
		    		} catch (Exception e) {
		    	}
			}		
		}).start();
		p.waitFor();
	} catch (InterruptedException | IOException e1) {
		e1.printStackTrace();
	}
}
	
	private static String readGPS() {
		FileReader fileReader;
		BufferedReader reader;
		String response ="";
		try {
			fileReader = new FileReader(WiFiScanner.homePath + "gpsPos");
			reader = new BufferedReader(fileReader);
			response = reader.readLine(); 
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}
}
