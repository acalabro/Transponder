package notUsed;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

public class SenderRandom extends Thread {

public static String gpsFile;
private BufferedReader brTest;
public SenderRandom(String gpsFilePos, String loraDevicePort, int i, String logOutputFile) {
	SenderRandom.gpsFile = gpsFilePos; 
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

public void run() {
	
	while(true) {
	try {
			brTest = new BufferedReader(new FileReader(gpsFile));		
			//capturedString = "#BSSID:00:87:64:6d:da:00&-55&"+ brTest.readLine() +"#";
			//String capturedString = "#BSSID:"+generateRandomMacAddress()+"&-55&"+ brTest.readLine() +"#";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//LoraDispatcher.addToBuffer(capturedString);
		//sender.sendMessage(capturedString);
		
		try {
			brTest.close();
			Thread.sleep(1000);
			
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
