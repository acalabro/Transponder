package notUsed;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Launcher {
	private static BufferedReader brTest;
	private static String capturedString;
	private static String gpsFilePos;
	private static String loraDevicePort;
	private static String baudRate;
	private static String logOutputFile;
	private static int LATENCY;
	
	  public static void main(String[] args) throws InterruptedException {
	    	if (args.length<5) {
				System.out.println("USAGE: pathOfGPSFilePos deviceGPS loraDevicePort baudRate LogOutputFile LatencyValueInMS\n\n eg: /home/user/desktop/gpsPos /dev/ttyACM0 /dev/ttyUSB0 115200 /home/pi/Desktop/outputFile 3000");
			} else {
			 gpsFilePos = args[0];
			 loraDevicePort = args[2];
			 baudRate  = args[3];
			 logOutputFile = args[4];
			 LATENCY = Integer.parseInt(args[5]);
			 }
			
			 LoraDispatcherMultiThread serialComm2 = new LoraDispatcherMultiThread(loraDevicePort,Integer.parseInt(baudRate), logOutputFile);
	      	
			 //LoraDispatcherMultiThread.loopThreadGPS(deviceGPS, gpsFilePos);
			 
			 while(true) {
			 try {
					brTest = new BufferedReader(new FileReader(gpsFilePos));		
					capturedString = "#BSSID:"+ LoraDispatcherMultiThread.generateRandomMacAddress()+"&-55&"+ brTest.readLine() +"#";
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 	serialComm2.sendMessage(capturedString);
				
				try {
					brTest.close();
					Thread.sleep(LATENCY);
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	    }
}
