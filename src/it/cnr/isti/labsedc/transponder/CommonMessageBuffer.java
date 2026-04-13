package it.cnr.isti.labsedc.transponder;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class CommonMessageBuffer {
	
	public static BlockingQueue<String> messageToSendBuffer;
	public static String lastKnownGPSpos = null;
	
	public CommonMessageBuffer() {
		messageToSendBuffer = new LinkedBlockingDeque<>();
	}
	
	public static synchronized void addMessageToSend(String message) {
		try {
			messageToSendBuffer.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized String getMessageToSend() {
		return messageToSendBuffer.poll();
	}
	
	public static synchronized void setLastKnownGPSpos(String gpsPos) {
		lastKnownGPSpos = gpsPos;
	}
	
	public static synchronized String getLastKnownGPSpos() {
			return lastKnownGPSpos;
	}

}
