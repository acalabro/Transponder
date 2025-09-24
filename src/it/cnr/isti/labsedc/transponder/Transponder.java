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
	
	
//	public Transponder(String deviceLoRa, int deviceLoRaSpeed, String deviceWiFi, String homePath, String gpsDevice, String Mobile4GDevicePort, int Mobile4GDevicePortSpeed, String smsRecipientString, int pinCode, boolean isPinRequired) {
//			Transponder.deviceLoRa = deviceLoRa;
//			Transponder.deviceLoRaSpeed = deviceLoRaSpeed;
//			Transponder.deviceWiFi = deviceWiFi;
//			Transponder.homePath = homePath;
//			Transponder.gpsDevice = gpsDevice;
//			Transponder.Mobile4GDevicePort = Mobile4GDevicePort;
//			Transponder.Mobile4GDevicePortSpeed = Mobile4GDevicePortSpeed;
//			Transponder.smsRecipientString = smsRecipientString;
//			Transponder.pinCode = pinCode;
//			Transponder.isPinRequired = isPinRequired;			
//	}
//	
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
		}
		else {
			System.out.println("USAGE:\n"
					+ "loraDevicePort loRaPortbaudRate "
					+ "WiFiDevice "
					+ "homePath "
					+ "gpsDevicePort "
					+ "Mobile4gDevicePort Mobile4gDevicePortSpeed smsRecipientNumber pinCode isPinRequired"
					+ "satDevicePort satDeviceSpeed\n\n "
					+ "eg: /dev/ttyUSB0 115200 wlan0 /home/pi/Desktop/ /dev/ttyACM0 /dev/ttyUSB2 115200 +39347347347 1234 false /dev/ttyUSB1 19200");
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
		
		//COMMUNICATION
    	
    	RoutingManager router = new RoutingManager();
    	router.start();
    	
		LoRaDispatcher senderLora = new LoRaDispatcher(
				Transponder.deviceLoRa, Transponder.deviceLoRaSpeed);
    	senderLora.start();
    	
    	AT_SMSSender mobile4G = new AT_SMSSender(Mobile4GDevicePort, Mobile4GDevicePortSpeed, pinCode, isPinRequired, smsRecipientString);
		mobile4G.start();
    	
    	SatelliteSender sat = new SatelliteSender(
    			Transponder.satelliteDevicePort, Transponder.satelliteDeviceSpeed);
    	sat.start();
	}
	
	
//	public void run() {
//		GetGPSPosition gpsData = new GetGPSPosition(
//				Transponder.gpsDevice, Transponder.homePath);
//		gpsData.start();	
//		
//		LoRaAnd4GDispatcher senderLora = new LoRaAnd4GDispatcher(
//				Transponder.deviceLoRa, Transponder.deviceLoRaSpeed,
//				Transponder.Mobile4GDevicePort, Transponder.Mobile4GDevicePortSpeed, 
//				Transponder.pinCode, Transponder.isPinRequired, 
//				Transponder.smsRecipientString);
//    	senderLora.start();
//    	
//    	WiFiScanner scanner = new  WiFiScanner(
//    			Transponder.homePath, Transponder.deviceWiFi);
//    	scanner.start();
//	}
	
	

}