package it.cnr.isti.labsedc.transponder;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class LoRaAnd4GDispatcher extends Thread {
	
	public static BlockingQueue<String> toSendBuffer = new LinkedBlockingDeque<>();
	
	private static String deviceLoRa;
	private static int deviceLoRaSpeed;
	private static String Mobile4GDevicePort;
	private static int MobileDevicePortSpeed;
	private static int pinCode;
	private static boolean isPinRequired;
	private static String smsRecipientString;
	
	public LoRaAnd4GDispatcher(String deviceLoRa, int deviceLoRaSpeed, String Mobile4GDevicePort, int Mobile4GDevicePortSpeed, int pinCode, boolean isPinRequired, String smsRecipientString) {
		LoRaAnd4GDispatcher.deviceLoRa	= deviceLoRa;
		LoRaAnd4GDispatcher.deviceLoRaSpeed = deviceLoRaSpeed;
		LoRaAnd4GDispatcher.Mobile4GDevicePort = Mobile4GDevicePort;
		LoRaAnd4GDispatcher.MobileDevicePortSpeed = Mobile4GDevicePortSpeed;
		LoRaAnd4GDispatcher.pinCode = pinCode;
		LoRaAnd4GDispatcher.isPinRequired = isPinRequired;
		LoRaAnd4GDispatcher.smsRecipientString = smsRecipientString;

		System.out.println("Setting up LoRaAnd4GDispatcher Daemon");
	}

	public void run() {		
		//setup
		try {		
			System.out.println("SETUP port at " + LoRaAnd4GDispatcher.deviceLoRaSpeed + " baud with: stty -F "+ LoRaAnd4GDispatcher.deviceLoRa);
			String[] cmdline = { "sh", "-c", "stty -F "+ LoRaAnd4GDispatcher.deviceLoRa, " " + LoRaAnd4GDispatcher.deviceLoRaSpeed};
			Process pr = Runtime.getRuntime().exec(cmdline);
			pr.waitFor();					
			} catch(IOException | InterruptedException e) {		
		}
		
		while(true) {
			if (toSendBuffer.size() > 0) {
					try {
						Thread.sleep(1000);
						sendLoraMessage(toSendBuffer.take());
						System.out.println("Loaded on LoraDispatcherBuffer\n");
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	
	private static void sendLoraMessage(String message) {
		try {		
			String[] cmdline = { "sh", "-c", "echo \"" + message + "\n\" > " + LoRaAnd4GDispatcher.deviceLoRa};
			Process pr = Runtime.getRuntime().exec(cmdline);
			System.out.println("Pushed to serial " + LoRaAnd4GDispatcher.deviceLoRa + " " + message);		
			pr.waitFor();					
			} catch(IOException | InterruptedException e) {		
		}
	}


	public static void addToBuffer(String capturedString) {
		try {
			toSendBuffer.put(capturedString);
			
			if (toSendBuffer.size()> 50) {
		    	AT_SMSSender mobile4G = new AT_SMSSender(Mobile4GDevicePort, MobileDevicePortSpeed , pinCode, isPinRequired, smsRecipientString, capturedString);
				mobile4G.start();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
