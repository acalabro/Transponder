package it.cnr.isti.labsedc.transponder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.fazecast.jSerialComm.SerialPort;

/**
 * Sends a short-burst data (SBD) message through an Iridium RockBLOCK 9603 modem.
 * <p>
 * Improvements over the original version:
 * <ul>
 *   <li>Reads and validates every AT response (OK / ERROR)</li>
 *   <li>Parses the +SBDIX response to report MO status</li>
 *   <li>Uses proper per-command timeouts (long for SBDIX)</li>
 *   <li>Instance fields instead of static – safe to run concurrently</li>
 *   <li>Validates message length (≤ 340 bytes) and content</li>
 *   <li>Fixed stty invocation</li>
 * </ul>
 */
public class SatelliteSender extends Thread {

    private static final String AT_CGMI       = "AT+CGMI\r";
    private static final String AT_CGMM       = "AT+CGMM\r";
    private static final String AT_SBDWT      = "AT+SBDWT=\"";
    private static final String AT_SBDIX      = "AT+SBDIX\r";

    /** Max outbound SBD payload for the 9603. */
    private static final int    MAX_SBD_BYTES = 340;

    /** Timeout for informational commands (CGMI, CGMM, SBDWT). */
    private static final long   SHORT_TIMEOUT_MS = 5_000;

    /** Timeout for SBDIX – network acquisition + transfer can take a while. */
    private static final long   SBDIX_TIMEOUT_MS = 60_000;

    private final String  port;
    private final int     baudRate;
    private final String  message;
    private SerialPort    serialPort;

    /**
     * @param port     system port name, e.g. {@code /dev/ttyUSB0} or {@code COM3}
     * @param baudRate baud rate – 19 200 is the RockBLOCK 9603 default
     * @param message  text payload (≤ 340 bytes UTF-8, no double-quote chars)
     */
    
    
    public SatelliteSender(String port, int baudRate, String message) {
        this.port     = port;
        this.baudRate = baudRate;
        this.message  = message;
        validate();
    }

    public SatelliteSender(String satelliteDevicePort, int satelliteDeviceSpeed) {
		this.port = satelliteDevicePort;
		this.baudRate = satelliteDeviceSpeed;
		this.message = "test";
	}

	private void validate() {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Message must not be empty.");
        }
        if (message.contains("\"")) {
            throw new IllegalArgumentException(
                "Message must not contain double-quote characters (breaks AT+SBDWT framing).");
        }
        int byteLen = message.getBytes(StandardCharsets.UTF_8).length;
        if (byteLen > MAX_SBD_BYTES) {
            throw new IllegalArgumentException(
                "Message too long: " + byteLen + " bytes (max " + MAX_SBD_BYTES + ").");
        }
    }

    /* ------------------------------------------------------------------ */
    /*  Serial port setup                                                 */
    /* ------------------------------------------------------------------ */

    private void setup() {
        // Optional OS-level port init (Linux only, best-effort)
        try {
            String cmd = "stty -F " + port + " " + baudRate;
            System.out.println("[setup] " + cmd);
            Process pr = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
            int rc = pr.waitFor();
            if (rc != 0) {
                System.err.println("[setup] stty exited with code " + rc + " (non-fatal)");
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("[setup] stty failed: " + e.getMessage() + " (non-fatal)");
        }

        serialPort = SerialPort.getCommPort(port);
        serialPort.setComPortParameters(baudRate, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_SEMI_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING,
                1000,   // read timeout – we poll in a loop, so 1 s granularity is fine
                0);
    }

    /* ------------------------------------------------------------------ */
    /*  Main thread body                                                  */
    /* ------------------------------------------------------------------ */

    @Override
    public void run() {
        setup();

        if (!serialPort.openPort()) {
            System.err.println("[error] Unable to open port " + port);
            return;
        }
        System.out.println("[info] Port " + port + " opened");

        try (OutputStream out = serialPort.getOutputStream();
             InputStream  in  = serialPort.getInputStream()) {

            // 1. Manufacturer identification
            String cgmiResp = sendAndRead(out, in, AT_CGMI, SHORT_TIMEOUT_MS);
            System.out.println("[CGMI] " + cgmiResp.trim());

            // 2. Model identification
            String cgmmResp = sendAndRead(out, in, AT_CGMM, SHORT_TIMEOUT_MS);
            System.out.println("[CGMM] " + cgmmResp.trim());

            // 3. Write message to MO buffer
            String sbdwtCmd  = AT_SBDWT + message + "\"\r";
            String sbdwtResp = sendAndRead(out, in, sbdwtCmd, SHORT_TIMEOUT_MS);
            System.out.println("[SBDWT] " + sbdwtResp.trim());
            if (!sbdwtResp.contains("OK")) {
                System.err.println("[error] SBDWT failed – aborting. Response: " + sbdwtResp);
                return;
            }

            // 4. Initiate SBD session (this talks to the satellite)
            System.out.println("[info] Initiating SBD session – this may take up to 60 s …");
            String sbdixResp = sendAndRead(out, in, AT_SBDIX, SBDIX_TIMEOUT_MS);
            System.out.println("[SBDIX] " + sbdixResp.trim());
            parseSbdixResponse(sbdixResp);

        } catch (Exception e) {
            System.err.println("[error] " + e.getMessage());
            e.printStackTrace();
        } finally {
            serialPort.closePort();
            System.out.println("[info] Port " + port + " closed");
        }
    }

    /* ------------------------------------------------------------------ */
    /*  AT command helpers                                                 */
    /* ------------------------------------------------------------------ */

    /**
     * Sends an AT command and reads the response until {@code OK}, {@code ERROR},
     * or the timeout expires.
     */
    private String sendAndRead(OutputStream out, InputStream in,
                               String command, long timeoutMs) throws IOException {

        // Drain any stale data
        while (in.available() > 0) { in.read(); }

        out.write(command.getBytes(StandardCharsets.US_ASCII));
        out.flush();

        StringBuilder sb = new StringBuilder();
        long deadline = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < deadline) {
            if (in.available() > 0) {
                int b = in.read();
                if (b == -1) break;
                sb.append((char) b);

                String soFar = sb.toString();
                if (soFar.contains("OK") || soFar.contains("ERROR")) {
                    // Give the modem a moment to flush any trailing \r\n
                    try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
                    while (in.available() > 0) { sb.append((char) in.read()); }
                    break;
                }
            } else {
                try { Thread.sleep(100); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return sb.toString();
    }

    /**
     * Parses the {@code +SBDIX} response and prints a human-readable status.
     * <p>
     * Format: {@code +SBDIX: <MO status>, <MOMSN>, <MT status>, <MTMSN>, <MT length>, <MT queued>}
     */
    private void parseSbdixResponse(String response) {
        int idx = response.indexOf("+SBDIX:");
        if (idx < 0) {
            System.err.println("[warn] Could not find +SBDIX in response");
            return;
        }
        String payload = response.substring(idx + 7).trim();
        // Strip everything after a possible \r or \n or "OK"
        payload = payload.split("[\\r\\n]")[0].trim();

        String[] fields = payload.split(",");
        if (fields.length < 6) {
            System.err.println("[warn] Unexpected SBDIX field count: " + payload);
            return;
        }

        try {
            int moStatus  = Integer.parseInt(fields[0].trim());
            int momsn     = Integer.parseInt(fields[1].trim());
            int mtStatus  = Integer.parseInt(fields[2].trim());
            int mtmsn     = Integer.parseInt(fields[3].trim());
            int mtLen     = Integer.parseInt(fields[4].trim());
            int mtQueued  = Integer.parseInt(fields[5].trim());

            System.out.println("[result] MO status : " + moStatus + " – " + moStatusText(moStatus));
            System.out.println("[result] MOMSN     : " + momsn);
            System.out.println("[result] MT status : " + mtStatus + " – " + mtStatusText(mtStatus));
            System.out.println("[result] MTMSN     : " + mtmsn);
            System.out.println("[result] MT length : " + mtLen + " bytes");
            System.out.println("[result] MT queued : " + mtQueued);
        } catch (NumberFormatException e) {
            System.err.println("[warn] Failed to parse SBDIX fields: " + payload);
        }
    }

    private static String moStatusText(int code) {
        switch (code) {
            case 0:  return "MO message transferred successfully";
            case 1:  return "MO message transferred (too large for MT)";
            case 2:  return "MO message transferred (location update rejected)";
            case 10: return "Gateway timeout (try again)";
            case 11: return "MO message queue at gateway is full";
            case 12: return "MO message has too many segments";
            case 13: return "Incomplete session";
            case 14: return "Invalid segment size";
            case 15: return "Access denied";
            case 16: return "Transceiver locked – unlock before retry";
            case 17: return "Gateway not responding (try again)";
            case 18: return "Antenna fault";
            case 32: return "No network service";
            case 33: return "Antenna fault";
            case 34: return "RF drop (try again)";
            case 35: return "SBD already in progress";
            default: return code <= 4 ? "Transferred with status " + code : "Unknown (" + code + ")";
        }
    }

    private static String mtStatusText(int code) {
        switch (code) {
            case 0: return "No MT message pending";
            case 1: return "MT message received";
            case 2: return "MT error during reception";
            default: return "Unknown (" + code + ")";
        }
    }
}
