package notUsed;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class SendGenericMessageAndGPS {

	private static String gpsFilePos;
	private static String deviceGPS;
	private static String loraDevicePort;
	private static String position = "00.0000,00.0000";
	private static String baudRate = "9600";
	private static String logOutputFile;
	
	public static void main(String[] args) {
		if (args.length<5) {
			System.out.println("USAGE: pathOfGPSFilePos deviceGPS loraDevicePort Baudrate LogOutputFile\n\n eg: /home/user/desktop/gpsPos /dev/ttyACM0 /dev/ttyUSB0 115200 /home/pi/Desktop/outputFile");
		} else {
		 gpsFilePos = args[0];
		 deviceGPS = args[1];
		 
		 
		 loraDevicePort = args[2];
		 baudRate  = args[3];
		 logOutputFile = args[4];
		 	 
		 loopThreadGPS(deviceGPS, gpsFilePos);
		 
		 //sender.start();
		 
		 SenderRandom senderRandom = new SenderRandom(gpsFilePos, loraDevicePort, Integer.parseInt(baudRate), logOutputFile);
		 System.out.println("Starting randomPayloadSender");
		 senderRandom.start();
		 
		}
	}
	
	
	private static void loopThreadGPS(String device, String gpsFile) {
		new Thread(new Runnable() {
		    
			public void run() {
		    	System.out.println("Starting GPS Process");       
		    			    	
	        	String[] command = {"/bin/cat", device};
	        	ProcessBuilder builder = new ProcessBuilder(command);
	            Process process = null;
		        BufferedWriter stream;
	        	try {
	        		process = builder.start();
	        	} catch (IOException e) {
	        		// TODO Auto-generated catch block
	        		e.printStackTrace();
	        	}
	            final BufferedReader reader = new BufferedReader(
	                    new InputStreamReader(process.getInputStream()));
	            String line = null;
		        String[] results;
		        try {
		        	File theFile = new File(gpsFile);
		            while (true) {
		            line = reader.readLine();	
		            	if (line != null && line.startsWith("$GPGLL")) //  || line.contains("signal:"))
	                	{
		            		results = line.split(",");
		            		if (results[6].compareTo("A") == 0) { //gps signal is valid
		            			theFile.delete();
		            			FileWriter write = new FileWriter(theFile,false);
		    		        	stream = new BufferedWriter(write);
		    		        	position = results[1]+","+results[3];
		            			stream.write(position);
		            			System.out.println("Updated GPSPOS: " + position + "\n");
		            			stream.flush();
		            		}
	                	}
		            }} catch (IOException e) {
		            	e.printStackTrace();
		            }
		        }
		}).start();
	}
		
}
