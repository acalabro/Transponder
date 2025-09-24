package it.cnr.isti.labsedc.transponder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.fazecast.jSerialComm.SerialPort;

public class SatelliteSender extends Thread {
	
	private static final String ATCGMI = "AT+CGMI\r";
	private static final String ATCGMM = "AT+CGMM\r";
    private static final String OPENSATSTREAM = "AT+SBDWT=\"";
    private static final String SENDMESSAGE = "AT+SBDIX\r";
    
    
	public static String satelliteDevicePort;
	public static int satelliteDeviceSpeed;
	public static String messageToSend;

	public SatelliteSender(String satelliteDevicePort, int satelliteDeviceSpeed) {
		SatelliteSender.satelliteDevicePort = satelliteDevicePort;
		SatelliteSender.satelliteDeviceSpeed = satelliteDeviceSpeed;
	}
	
	public void run() {
		
		try {		
			System.out.println("SETUP port at " + SatelliteSender.satelliteDeviceSpeed + " baud with: stty -F "+ SatelliteSender.satelliteDevicePort);
			String[] cmdline = { "sh", "-c", "stty -F "+ SatelliteSender.satelliteDevicePort, " " + SatelliteSender.satelliteDeviceSpeed};
			Process pr = Runtime.getRuntime().exec(cmdline);
			pr.waitFor();					
			} catch(IOException | InterruptedException e) {		
		}
		
		SerialPort serialPort = SerialPort.getCommPort(SatelliteSender.satelliteDevicePort);

        serialPort.setComPortParameters(SatelliteSender.satelliteDeviceSpeed, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

        if (serialPort.openPort()) {
            System.out.println("Port " + SatelliteSender.satelliteDevicePort + " opened");

            try (OutputStream outputStream = serialPort.getOutputStream();
                 InputStream inputStream = serialPort.getInputStream()) {

                sendCommand(outputStream, ATCGMI);
                sendCommand(outputStream, ATCGMM);
                sendCommand(outputStream, OPENSATSTREAM + messageToSend + "\"\r"); 
                sendCommand(outputStream, SENDMESSAGE);
                //AT+SBDWT=

                System.out.println("Message sent");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                serialPort.closePort();
                System.out.println("Port " + SatelliteSender.satelliteDevicePort + " closed");
            }

        } else {
            System.out.println("Unable to open port " + SatelliteSender.satelliteDevicePort);
        }
    }
	
	   private void sendCommand(OutputStream outputStream, String command) throws Exception {
	        outputStream.write(command.getBytes());
	        outputStream.flush();
	        Thread.sleep(100); //piggybacking
	    }
		
	}
