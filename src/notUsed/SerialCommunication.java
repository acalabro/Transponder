package notUsed;
import com.fazecast.jSerialComm.SerialPort;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SerialCommunication {
    private SerialPort serialPort;
    private FileWriter fileWriter;
    private BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private static final long SEND_INTERVAL_MS = 600; // intervallo minimo tra invii
    private volatile boolean transmissionInProgress = false;

    // Costruttore per configurare porta seriale, baud rate e nome del file di output
    public SerialCommunication(String portName, int baudRate, String outputFileName) {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setComPortParameters(baudRate, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);

        // Configura il file di output
        try {
            fileWriter = new FileWriter(outputFileName);
        } catch (IOException e) {
            System.out.println("Errore nell'aprire il file di output.");
            e.printStackTrace();
            return;
        }

        // Avvia i thread per lettura e scrittura
        startReading();
        startWriting();
    }

    // Metodo synchronized per aprire la porta seriale
    private synchronized void openPort() {
        if (!serialPort.isOpen()) {
            serialPort.openPort();
            System.out.println("Porta seriale aperta.");
        }
    }

    // Metodo synchronized per chiudere la porta seriale
    private synchronized void closePort() {
        if (serialPort.isOpen()) {
            serialPort.closePort();
            System.out.println("Porta seriale chiusa.");
        }
    }

    // Metodo per avviare il thread di lettura dalla seriale
    private void startReading() {
        new Thread(() -> {
            openPort(); // Porta aperta all'avvio per la lettura continua
            byte[] readBuffer = new byte[1024];
            StringBuilder sb = new StringBuilder();

            while (true) {
                if (!transmissionInProgress) {
                    int numBytes = serialPort.readBytes(readBuffer, readBuffer.length);
                    if (numBytes > 0) {
                        // Aggiungi i dati ricevuti al StringBuilder e processa le linee
                        sb.append(new String(readBuffer, 0, numBytes));
                        int lineEnd;
                        while ((lineEnd = sb.indexOf("\n")) >= 0) {
                            String line = sb.substring(0, lineEnd).trim();
                            sb.delete(0, lineEnd + 1); // Rimuovi la linea dal buffer
                            System.out.println("Ricevuto: " + line);
                            try {
                                fileWriter.write(line + "\n");
                                fileWriter.flush();
                            } catch (IOException e) {
                                System.out.println("Errore nella scrittura su file.");
                                e.printStackTrace();
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(50); // Breve pausa per evitare un ciclo eccessivamente veloce
                } catch (InterruptedException e) {
                    System.out.println("Errore nella lettura.");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Metodo per avviare il thread di scrittura sulla seriale con frequenza limitata
    private void startWriting() {
        new Thread(() -> {
            while (true) {
                try {
                    String input = messageQueue.take();

                    // Imposta la trasmissione in corso, sospendendo la lettura
                    transmissionInProgress = true;
                    closePort(); // Chiudi la porta per prepararla alla scrittura

                    openPort(); // Riapri la porta per inviare il messaggio
                    serialPort.getOutputStream().write((input + "\n").getBytes());
                    serialPort.getOutputStream().flush();
                    System.out.println("Inviato: " + input);

                    closePort();

                    // Segnala che la trasmissione è terminata e riapri la porta per la lettura
                    transmissionInProgress = false;
                    openPort();

                    Thread.sleep(SEND_INTERVAL_MS);
                } catch (Exception e) {
                    System.out.println("Errore durante l'invio dei dati sulla seriale.");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendMessage(String message) {
        messageQueue.add(message);
    }

    public static void main(String[] args) throws InterruptedException {
        SerialCommunication serialComm = new SerialCommunication("/dev/ttyUSB0", 9600, "/home/acalabro/Desktop/output");
      	
    	for (int i = 0; i<35; i++) { 
            serialComm.sendMessage("#BSWAD:00:87:64:6d:da:00&-72dBm&40.1022,12.1212#");
            Thread.sleep(2000);
        	}
    }
}
