package it.cnr.isti.labsedc.transponder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class GetGPSPosition extends Thread{

	private static String deviceGPS = "/dev/ttyACM0";
	private static String pathFile = "/"; 
	public static String lastGPSpos = null;
	
	public static void main(String[] args) throws InterruptedException {

	}

	public GetGPSPosition(String deviceGPS, String pathFile) {
		GetGPSPosition.deviceGPS = deviceGPS;
		GetGPSPosition.pathFile = pathFile;
		
		System.out.println("Setting up GPS Daemon\n with parameters: " + deviceGPS + " " + pathFile);
		System.out.println("-----------------------------------");
	}
	
	public void run() {
		try {	
			String[] commandString= {"cat", GetGPSPosition.deviceGPS};
			Process p = Runtime.getRuntime().exec(commandString);
		new Thread(new Runnable() {
		    public void run() {
		    	System.out.println("GPS Probe started");       		
		        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
		        String line = null;
		        BufferedWriter stream;
		        String[] results;
		        try {
		        	File theFile = new File(GetGPSPosition.pathFile + "gpsPos");
			    			
		            while ((line = input.readLine()) != null)
		            	
		            	if (line != null && line.startsWith("$GPGLL")) //  || line.contains("signal:"))
	                	{
		            		results = line.split(",");
		            		if (results[6].compareTo("A") == 0) { //gps signal is valid
		            			CommonMessageBuffer.setLastKnownGPSpos(results[1]+","+results[3]);
		            			theFile.delete();
		            			FileWriter write = new FileWriter(theFile,false);
		    		        	stream = new BufferedWriter(write);
		            			stream.write(results[1]+","+results[3]);
		            			stream.flush();
		            		}
	                	}
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		    }
		}).start();

			p.waitFor();
		} catch (InterruptedException | IOException e1) {
			e1.printStackTrace();
		}
	}	
}
