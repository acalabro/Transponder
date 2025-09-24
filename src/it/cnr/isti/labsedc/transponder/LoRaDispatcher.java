package it.cnr.isti.labsedc.transponder;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class LoRaDispatcher extends Thread {
		
	private static String deviceLoRa;
	private static int deviceLoRaSpeed;
	
	public LoRaDispatcher(String deviceLoRa, int deviceLoRaSpeed) {
		LoRaDispatcher.deviceLoRa	= deviceLoRa;
		LoRaDispatcher.deviceLoRaSpeed = deviceLoRaSpeed;

		System.out.println("Setting up LoRaDispatcher Daemon with parameters:\n" + "Device LoRa " + deviceLoRa + " at speed " + deviceLoRaSpeed);
		System.out.println("-----------------------------------");
	}

	public void run() {		
		//setup
		try {		
			System.out.println("SETUP port at " + LoRaDispatcher.deviceLoRaSpeed + " baud with: stty -F "+ LoRaDispatcher.deviceLoRa);
			String[] cmdline = { "sh", "-c", "stty -F "+ LoRaDispatcher.deviceLoRa, " " + LoRaDispatcher.deviceLoRaSpeed};
			Process pr = Runtime.getRuntime().exec(cmdline);
			pr.waitFor();					
			} catch(IOException | InterruptedException e) {		
		}
		
		while(true) {
			if (CommonMessageBuffer.messageToSendBuffer.size() > 0) {
					try {
						Thread.sleep(1000);
						sendLoraMessage(CommonMessageBuffer.getMessageToSend());
						System.out.println("Loaded on LoraDispatcherBuffer\n");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	
	private static void sendLoraMessage(String message) {
		try {		
			String[] cmdline = { "sh", "-c", "echo \"" + message + "\n\" > " + LoRaDispatcher.deviceLoRa};
			Process pr = Runtime.getRuntime().exec(cmdline);
			System.out.println("Pushed to serial " + LoRaDispatcher.deviceLoRa + " " + message);		
			pr.waitFor();					
			} catch(IOException | InterruptedException e) {		
		}
	}
}
