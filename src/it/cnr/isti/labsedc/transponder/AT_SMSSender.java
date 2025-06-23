package it.cnr.isti.labsedc.transponder;

import java.io.InputStream;
import java.io.OutputStream;

import com.fazecast.jSerialComm.SerialPort;

public class AT_SMSSender extends Thread {

	private static final String AT = "AT\r";
	private static final String SENDSIMPIN = "AT+CPIN=\"";
    private static final String ENABLESMSMODE = "AT+CMGF=1\r";
    private static final String SETRECIPIENT = "AT+CMGS=\"";

	private final String portName;
	private boolean isPinRequired = false;
	private int pinCode = 0000;
	private String recipientNumber = "+39";
	private String smsText = "nonlofa";
	private int mobileDevicePortSpeed = 9600;

    public AT_SMSSender(String portName, int mobileDevicePortSpeed, int pinCode, boolean isPinRequired, String recipientNumber, String smsText) {
        this.portName = portName;
        this.pinCode = pinCode;
        this.isPinRequired = isPinRequired;
        this.recipientNumber = recipientNumber;
        this.mobileDevicePortSpeed  = mobileDevicePortSpeed;
        this.smsText = smsText;
    }

    @Override
    public void run() {
        SerialPort serialPort = SerialPort.getCommPort(portName);

        serialPort.setComPortParameters(this.mobileDevicePortSpeed, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

        if (serialPort.openPort()) {
            System.out.println("Porta " + portName + " aperta");

            try (OutputStream outputStream = serialPort.getOutputStream();
                 InputStream inputStream = serialPort.getInputStream()) {

                sendCommand(outputStream, AT);
                if (isPinRequired ) {
                	sendCommand(outputStream, SENDSIMPIN + pinCode + "\"\r");	//send pin
                }
                sendCommand(outputStream, ENABLESMSMODE); //config card in sms mode
                sendCommand(outputStream, SETRECIPIENT + recipientNumber + "\"\r"); //set recipient
                sendCommand(outputStream, smsText + (char) 26); // messageText + CTRL+Z 

                System.out.println("Message sent");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                serialPort.closePort();
                System.out.println("Port " + portName + " closed");
            }

        } else {
            System.out.println("Unable to open port " + portName);
        }
    }

    private void sendCommand(OutputStream outputStream, String command) throws Exception {
        outputStream.write(command.getBytes());
        outputStream.flush();
        Thread.sleep(100); //piggybacking
    }

    public static void main(String[] args) {
        AT_SMSSender thread = new AT_SMSSender("COM2", 9600, 0000, false, "+393939393939", "Tampe Finocchio");
        thread.start();
    }
}
