package notUsed;

import com.fazecast.jSerialComm.SerialPort;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LoraDispatcherMultiThread {
	private SerialPort serialPort;
    private FileWriter fileWriter;
    private static BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private static final long SEND_INTERVAL_MS = 600; 
    private volatile boolean transmissionInProgress = false;
	private static String position = "00.0000,00.0000";


    public LoraDispatcherMultiThread(String portName, int baudRate, String outputFileName) {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setComPortParameters(baudRate, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);

        try {
            fileWriter = new FileWriter(outputFileName);
        } catch (IOException e) {
            System.out.println("Error on creating output file.");
            e.printStackTrace();
            return;
        }

        startReading();
        startWriting();
    }

    private synchronized void openPort() {
        if (!serialPort.isOpen()) {
            serialPort.openPort();
            //System.out.println("Serial port opened.");
        }
    }

    private synchronized void closePort() {
        if (serialPort.isOpen()) {
            serialPort.closePort();
            //System.out.println("Serial port closed.");
        }
    }

    private void startReading() {
        new Thread(() -> {
            openPort(); 
            byte[] readBuffer = new byte[1024];
            StringBuilder sb = new StringBuilder();

            while (true) {
                if (!transmissionInProgress) {
                    int numBytes = serialPort.readBytes(readBuffer, readBuffer.length);
                    if (numBytes > 0) {
                        sb.append(new String(readBuffer, 0, numBytes));
                        int lineEnd;
                        while ((lineEnd = sb.indexOf("\n")) >= 0) {
                            String line = sb.substring(0, lineEnd).trim();
                            sb.delete(0, lineEnd + 1); 
                            System.out.println("Rx: " + line);
                            try {
                                fileWriter.write(line + "\n");
                                fileWriter.flush();
                            } catch (IOException e) {
                                System.out.println("Error on writing output file.");
                                e.printStackTrace();
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    System.out.println("Error in reading from serial port.");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startWriting() {
        new Thread(() -> {
            while (true) {
                try {
                    String input = messageQueue.take();

                    transmissionInProgress = true;
                    closePort();
                    openPort(); 
                    
                    serialPort.getOutputStream().write((input + "\n").getBytes());
                    serialPort.getOutputStream().flush();
                    System.out.println("TX: " + input);

                    closePort();
                    transmissionInProgress = false;
                    openPort();

                    Thread.sleep(SEND_INTERVAL_MS);
                } catch (Exception e) {
                    //System.out.println("Errore on sending data to the serial port.");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendMessage(String message) {
        messageQueue.add(message);
    }

    public static String generateRandomMacAddress() {
        Random random = new Random();
        byte[] macAddr = new byte[6];
        
        random.nextBytes(macAddr);
        
        macAddr[0] = (byte) (macAddr[0] & (byte) 0xFE);  
        macAddr[0] = (byte) (macAddr[0] | (byte) 0x02);  
        
        StringBuilder macAddress = new StringBuilder();
        for (int i = 0; i < macAddr.length; i++) {
            macAddress.append(String.format("%02X", macAddr[i]));
            if (i < macAddr.length - 1) {
                macAddress.append(":");
            }
        }
        
        return macAddress.toString();
    }
    
    
    public static void loopThreadGPS(String device, String gpsFile) {
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
