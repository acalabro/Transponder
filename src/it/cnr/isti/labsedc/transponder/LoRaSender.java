package it.cnr.isti.labsedc.transponder;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class LoRaSender extends Thread {
		
	private static String deviceLoRa;
	private static int deviceLoRaSpeed;
	
	public LoRaSender(String deviceLoRa, int deviceLoRaSpeed) {
		LoRaSender.deviceLoRa	= deviceLoRa;
		LoRaSender.deviceLoRaSpeed = deviceLoRaSpeed;

		System.out.println("Setting up LoRaDispatcher Daemon with parameters:\n" + "Device LoRa " + deviceLoRa + " at speed " + deviceLoRaSpeed);
		System.out.println("-----------------------------------");
	}

	public void run() {		
		//setup
		try {		
			System.out.println("SETUP port at " + LoRaSender.deviceLoRaSpeed + " baud with: stty -F "+ LoRaSender.deviceLoRa);
			String[] cmdline = { "sh", "-c", "stty -F "+ LoRaSender.deviceLoRa, " " + LoRaSender.deviceLoRaSpeed};
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
			String[] cmdline = { "sh", "-c", "echo \"" + message + "\n\" > " + LoRaSender.deviceLoRa};
			Process pr = Runtime.getRuntime().exec(cmdline);
			System.out.println("Pushed to serial " + LoRaSender.deviceLoRa + " " + message);		
			pr.waitFor();					
			} catch(IOException | InterruptedException e) {		
		}
	}
}
