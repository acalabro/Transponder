package it.cnr.isti.labsedc.transponder;


public class Transponder {

	private static String deviceWiFi = "wlan1";
	private static String deviceLoRa = "/dev/ttyUSB0";
	private static String homePath = "/home/acalabro/Desktop/";
	private static int deviceLoRaSpeed = 115200;
	private static String gpsDevice = "/dev/ttyACM0";
	private static String Mobile4GDevicePort= "/dev/ttyUSB2";
	private static int Mobile4GDevicePortSpeed = 9600;
	private static String smsRecipientString = "+39347";
	private static int pinCode = 0000;
	private static boolean isPinRequired = false;
	private static String satelliteDevicePort;
	private static int satelliteDeviceSpeed;
	private static CHANNELMODE transmissionMode; 
		
	public static void main(String[] args) {
		if (args.length > 0) {
			deviceLoRa = args[0];
			deviceLoRaSpeed = Integer.parseInt(args[1]);
			deviceWiFi = args[2];
			homePath = args[3];
			gpsDevice = args[4];
			Mobile4GDevicePort = args[5];
			Mobile4GDevicePortSpeed = Integer.parseInt(args[6]); 
			smsRecipientString = args[7];
			pinCode = Integer.parseInt(args[8]);
			isPinRequired = Boolean.parseBoolean(args[9]);
			satelliteDevicePort = args[10];
			satelliteDeviceSpeed = Integer.parseInt(args[11]);
			transmissionMode = channelModeFromInt(Integer.parseInt(args[12]));
		}
		else {
			System.out.println("USAGE:\n"
					+ "loraDevicePort loRaPortbaudRate\n"
					+ "WiFiDevice\n"
					+ "homePath\n"
					+ "gpsDevicePort\n"
					+ "Mobile4gDevicePort Mobile4gDevicePortSpeed smsRecipientNumber pinCode isPinRequired\n"
					+ "satDevicePort satDeviceSpeed\n"
					+ "channelMode\n\n"
					+ "eg: /dev/ttyUSB0 115200 wlan0 /home/pi/Desktop/ /dev/ttyACM0 /dev/ttyUSB2 115200 +39347347347 1234 false /dev/ttyUSB1 19200 0\n"
					+ "\n"
					+ "The channelMode parameter can be:\n"
					+ "0 = for LoRa only\n"
					+ "1 = for 4GSMS only\n"
					+ "2 = for SAT only\n"
					+ "3 = for Multi-simultaneous\n"
					+ "4 = for Multi-cascade\n");
			System.exit(2);
		}
		
		CommonMessageBuffer buffer = new CommonMessageBuffer();
		
		//POSITIONING
		GetGPSPosition gpsData = new GetGPSPosition(
				Transponder.gpsDevice, Transponder.homePath);
		gpsData.start();	
		
		//SENSING
    	WiFiScanner scanner = new  WiFiScanner(
    			Transponder.homePath, Transponder.deviceWiFi);
    	scanner.start();
		
    	//Start communication components
    	LoRaSender senderLora = new LoRaSender(Transponder.deviceLoRa, Transponder.deviceLoRaSpeed);
    	
    	AT_4G_SMSSender mobile4G = new AT_4G_SMSSender(Mobile4GDevicePort, Mobile4GDevicePortSpeed, pinCode, isPinRequired, smsRecipientString);
    	
    	SatelliteSender sat = new SatelliteSender(Transponder.satelliteDevicePort, Transponder.satelliteDeviceSpeed);
    	
		//COMMUNICATION
    	RoutingManager router = new RoutingManager(
    			senderLora,
    			mobile4G,
    			sat,
    			transmissionMode);
    	router.start();
    	 
//		LoRaSender senderLora = new LoRaSender(
//				Transponder.deviceLoRa, Transponder.deviceLoRaSpeed);
//    	senderLora.start();
//    	
//    	AT_SMSSender mobile4G = new AT_SMSSender(Mobile4GDevicePort, Mobile4GDevicePortSpeed, pinCode, isPinRequired, smsRecipientString);
//		mobile4G.start();
//    	
//    	SatelliteSender sat = new SatelliteSender(
//    			Transponder.satelliteDevicePort, Transponder.satelliteDeviceSpeed);
//    	sat.start();
	}
	
	public static CHANNELMODE channelModeFromInt(int i) {
        CHANNELMODE[] values = CHANNELMODE.values();
        if (i < 0 || i >= values.length) {
            throw new IllegalArgumentException("Invalid int value for CHANNELMODE: " + i);
        }
        return values[i];
    }
}