package notUsed;

import com.fazecast.jSerialComm.SerialPort;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SerialCommunication2 {
    private SerialPort serialPort;
    private FileWriter fileWriter;
    private BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private static final long SEND_INTERVAL_MS = 1000;

    public SerialCommunication2(String portName, int baudRate, String outputFileName) {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setComPortParameters(baudRate, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 300, 0);

        if (serialPort.openPort()) {
            System.out.println("Porta seriale aperta: " + portName);
        } else {
            System.out.println("Impossibile aprire la porta seriale.");
            return;
        }

        try {
            fileWriter = new FileWriter(outputFileName);
        } catch (IOException e) {
            System.out.println("Errore nell'aprire il file di output.");
            e.printStackTrace();
            return;
        }

        startReading();
        startWriting();
    }

    private void startReading() {
        new Thread(() -> {
            try {
                System.out.println("Inizio lettura seriale...");
                while (true) {
                    int bytesAvailable = serialPort.bytesAvailable();
                    if (bytesAvailable > 0) {
                        byte[] readBuffer = new byte[bytesAvailable];
                        int numRead = serialPort.readBytes(readBuffer, readBuffer.length);
                        System.out.println("Bytes Read: " + numRead);
                        String line = new String(readBuffer);
                        System.out.println("Ricevuto (grezzo): " + line);
                        fileWriter.write(line + "\n");
                        fileWriter.flush();
                    } else {
                        System.out.println("Nessun dato disponibile.");
                    }
                    // Attendi brevemente per non sovraccaricare la CPU
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                System.out.println("Errore nella lettura dalla porta seriale.");
                e.printStackTrace();
            }
        }).start();
    }

    // Metodo per avviare il thread di scrittura sulla seriale con frequenza limitata
    private void startWriting() {
        new Thread(() -> {
            try {
                while (true) {
                    String input = messageQueue.take();
                    serialPort.getOutputStream().write((input + "\n").getBytes());
                    serialPort.getOutputStream().flush();
                    System.out.println("Inviato: " + input);
                    Thread.sleep(SEND_INTERVAL_MS);
                }
            } catch (Exception e) {
                System.out.println("Errore durante l'invio dei dati sulla seriale.");
                e.printStackTrace();
            }
        }).start();
    }

    // Metodo per aggiungere messaggi alla coda di invio
    public void sendMessage(String message) {
        messageQueue.add(message);
    }

    public static void main(String[] args) {
        // Crea una nuova istanza con porta seriale, baud rate e nome del file di output specificati
        SerialCommunication2 serialComm = new SerialCommunication2("/dev/ttyUSB0", 9600, "/home/acalabro/Desktop/output");

        // Aggiungi un esempio di messaggio alla coda
        
        
        serialComm.sendMessage("#BSWAD:00:87:64:6d:da:00&-72dBm&40.1022,12.1212#");
        serialComm.sendMessage("#BSWAD:00:87:64:6d:da:00&-72dBm&40.1022,12.1212#");
        serialComm.sendMessage("#BSWAD:00:87:64:6d:da:00&-72dBm&40.1022,12.1212#");
        serialComm.sendMessage("#BSWAD:00:87:64:6d:da:00&-72dBm&40.1022,12.1212#");
        serialComm.sendMessage("#BSWAD:00:87:64:6d:da:00&-72dBm&40.1022,12.1212#");
        serialComm.sendMessage("#BSWAD:00:87:64:6d:da:00&-72dBm&40.1022,12.1212#");
        serialComm.sendMessage("#BSWAD:00:87:64:6d:da:00&-72dBm&40.1022,12.1212#");
        serialComm.sendMessage("#BSWAD:00:87:64:6d:da:00&-72dBm&40.1022,12.1212#");
        serialComm.sendMessage("#BSWAD:00:87:64:6d:da:00&-72dBm&40.1022,12.1212#");
        serialComm.sendMessage("#BSWAD:00:87:64:6d:da:00&-72dBm&40.1022,12.1212#");
        serialComm.sendMessage("#BSWAD:00:87:64:6d:da:00&-72dBm&40.1022,12.1212#");
        serialComm.sendMessage("#BSWAD:00:87:64:6d:da:00&-72dBm&40.1022,12.1212#");        
        serialComm.sendMessage("#BSWAD:00:87:64:6d:da:00&-72dBm&40.1022,12.1212#");
        serialComm.sendMessage("#BSWAD:00:87:64:6d:da:00&-72dBm&40.1022,12.1212#");
        serialComm.sendMessage("#BSWAD:00:87:64:6d:da:00&-72dBm&40.1022,12.1212#");
    }
}
