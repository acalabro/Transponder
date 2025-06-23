package notUsed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class test {
    public static void main(String[] args) {
        // Assicurati di avere il percorso corretto del file che vuoi leggere
        String[] command = {"/bin/cat", "test.txt"};
        
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        
        try {
            Process process = processBuilder.start();
            
            // Legge l'output del comando
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            
            // Attende il completamento del processo
            int exitCode = process.waitFor();
            System.out.println("Processo terminato con codice di uscita: " + exitCode);
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Processo interrotto.");
        }
    }
}
