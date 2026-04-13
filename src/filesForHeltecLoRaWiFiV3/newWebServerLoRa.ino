#include "LoRaWan_APP.h"
#include "Arduino.h"
#include <WiFi.h>
#include <NetworkClient.h>
#include <WebServer.h>
#include <ESPmDNS.h>
#include "FS.h"
#include "SPIFFS.h"  // Per ESP32
#include <SPI.h>
#include <Wire.h>  
#include "HT_SSD1306Wire.h"

#define Percentuale_Di_Successo 100  // percentulale di accettazione messaggio (a scopo di test)
#define RANDOM_DELAY_SIMULA_RETE 0
#define Payload_Syze sizeof(Payload_Fields)
#define RF_FREQUENCY 865000000  // Hz
#define TX_OUTPUT_POWER 5  // dBm
#define LORA_BANDWIDTH 0
#define LORA_SPREADING_FACTOR 7  // [SF7..SF12]
#define LORA_CODINGRATE 1
#define LORA_PREAMBLE_LENGTH 8   // Same for Tx and Rx
#define LORA_SYMBOL_TIMEOUT 0    // Symbols
#define LORA_FIX_LENGTH_PAYLOAD_ON false
#define LORA_IQ_INVERSION_ON false
// #define RX_TIMEOUT_VALUE                            1000
#define BUFFER_SIZE 128  // Define the payload size here
#define WINDOW_LENGTH 10
#define LED_PIN 35

const char *ssid = "GAGL_Mobile";
const char *password = "nonfunziona";
const int led = 13;
WebServer server(80);

SSD1306Wire  factory_display(0x3c, 500000, SDA_OLED, SCL_OLED, GEOMETRY_128_64, RST_OLED); // addr , freq , i2c group , resolution , rst

struct Payload_Fields {
  int ID_Sender;
  int16_t txNumber;
  //   int sensor1;
  float latitude;
  float longitude;
};

Payload_Fields Rx_Message;
Payload_Fields TX_Message;

char txpacket[WINDOW_LENGTH][BUFFER_SIZE];
char rxpacket[BUFFER_SIZE];

static RadioEvents_t RadioEvents;
void OnTxDone(void);
void OnTxTimeout(void);
void OnRxDone(uint8_t *payload, uint16_t size, int16_t rssi, int8_t snr);

typedef enum {
  LOWPOWER,
  STATE_RX,
  STATE_TX
} States_t;

int16_t txNumber;
States_t state;
bool sleepMode = false;
int16_t Rssi, rxSize;
unsigned long int Actual_Time = 0;

long int index_Received_Packet = -1;
long int index_Packet_To_Send = 0;

void setup(void) {
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(led, 0);
  Serial.begin(115200);
  Mcu.begin(HELTEC_BOARD, SLOW_CLK_TPYE);

  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  Serial.println("");
  String s = ssid;
  factory_display.init();
 	factory_display.clear();
  factory_display.setTextAlignment(TEXT_ALIGN_CENTER);
  factory_display.drawString(64, 30, "Connecting to:\n" + s);
  factory_display.display();
  // Wait for connection
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  String IPAddress = WiFi.localIP().toString();
	factory_display.clear();
  factory_display.setTextAlignment(TEXT_ALIGN_CENTER);
	factory_display.drawString(64, 0,  "********************************");
	factory_display.drawString(64, 10, "*       TRANSPONDER       *");
	factory_display.drawString(64, 20, "*       LoRa Receiver        *");
	factory_display.drawString(64, 30, "Connected to: " + s);
	factory_display.drawString(64, 40, "IP Address: " + IPAddress);
  factory_display.drawString(64, 50, "http://"+ IPAddress + "/drone");
	factory_display.drawString(64, 60, "********************************");
  factory_display.display();

  if (MDNS.begin("esp32")) {
    Serial.println("MDNS responder started");
  }

   if(!SPIFFS.begin(true)){  // true formatta se fallisce
    Serial.println("Errore nel montare SPIFFS");
    return;
  }
  
  cleanFile("/data.html");
  writeOnFile("/data.html","<html><body><h2>Received messages:</h2><pre>");

  server.on("/", handleRoot);
  server.on("/drone", handleDrone);

  server.onNotFound(handleNotFound);

  server.begin();
  Serial.println("HTTP server started");

  txNumber = 0;
  Rssi = 0;

  RadioEvents.TxDone = OnTxDone;
  RadioEvents.TxTimeout = OnTxTimeout;
  RadioEvents.RxDone = OnRxDone;

  Radio.Init(&RadioEvents);
  Radio.SetChannel(RF_FREQUENCY);
  Radio.SetTxConfig(MODEM_LORA, TX_OUTPUT_POWER, 0, LORA_BANDWIDTH,
                    LORA_SPREADING_FACTOR, LORA_CODINGRATE,
                    LORA_PREAMBLE_LENGTH, LORA_FIX_LENGTH_PAYLOAD_ON,
                    true, 0, 0, LORA_IQ_INVERSION_ON, 3000);

  Radio.SetRxConfig(MODEM_LORA, LORA_BANDWIDTH, LORA_SPREADING_FACTOR,
                    LORA_CODINGRATE, 0, LORA_PREAMBLE_LENGTH,
                    LORA_SYMBOL_TIMEOUT, LORA_FIX_LENGTH_PAYLOAD_ON,
                    0, true, 0, 0, LORA_IQ_INVERSION_ON, true);
  state = STATE_RX;
}

bool Messaggio_OK = true;
int Last_Received_PKT_Number = 0;
int NEW_Packet_received_Number = 0;
int ACK_SENT_Number = 0;


void loop() {
  Actual_Time = millis();
  server.handleClient();
  delay(2);  //allow the cpu to switch to other tasks
  switch (state) {
    case STATE_TX:
      // delay(1000);
      // txNumber++;
      //      if (Messaggio_OK==true) {
      Actual_Time = millis();


      if (index_Packet_To_Send <= index_Received_Packet) {
        Serial.printf(" \tACK SENT\t%d\t%d\tms\n", ACK_SENT_Number, Actual_Time);
        //   delay(random(500)); // -------------------------------

        Radio.Send((uint8_t *)txpacket[index_Packet_To_Send % WINDOW_LENGTH], strlen(txpacket[index_Packet_To_Send % WINDOW_LENGTH]));
        index_Packet_To_Send++;
        state = LOWPOWER;
      }
      //    } else {
      //      Serial.printf(" \tNO ACK SENT\n");
      //     state=STATE_RX;
      //    }

      break;
    case STATE_RX:
      digitalWrite(LED_PIN, HIGH);
      Radio.Rx(0);
      state = LOWPOWER;
      digitalWrite(LED_PIN, LOW); 
      break;
    case LOWPOWER:
      if (index_Packet_To_Send <= index_Received_Packet) {
        state = STATE_TX;
      //  Serial.printf("\nStato Rete POS #1 %d\n",Radio.GetStatus()); 
        //delay((int) (random(100)>70)*RANDOM_DELAY_SIMULA_RETE);
      }
      // Serial.printf("\nStato Rete POS #2 %d\n",Radio.GetStatus()); 
      Radio.IrqProcess();
      break;
    default:
      break;
  }
 
}

void OnTxDone(void) {
  // Serial.print("TX done......");
  state = STATE_RX;
}

void OnTxTimeout(void) {
 // Radio.Sleep();
  //   Serial.print("TX Timeout......");
  state = STATE_TX;
}

void OnRxDone(uint8_t *payload, uint16_t size, int16_t rssi, int8_t snr) {
  int temp_ID_Sender;
  String Messaggio_Letto; 
  Rssi = rssi;
  rxSize = size;

  memcpy(rxpacket, payload, size);
  rxpacket[size] = '\0';
  Radio.Sleep();
  
 int ID;
int pktNum;
float lat, lon;

sscanf(rxpacket, "S %d %d %f %f", &ID, &pktNum, &lat, &lon);

String latStr = String(lat, 6);
String lonStr = String(lon, 6);

String logEntry = "<a href=\"https://www.google.com/maps/search/?api=1&query=" + latStr + "," + lonStr  + "\" target=\"_blank\">" + "Latitudine: " + latStr + "; Longitudine: " + lonStr + "; RSSI: " + String(rssi) + "</a>\n";
writeOnFile("/data.html", logEntry);

  Serial.printf(">>>>>>>>>>>  %s\t Rssi=\t%d %s", rxpacket, Rssi);
  // sprintf(rxpacket,"Rssi = %d\t %s",Rssi,rxpacket );
  Messaggio_OK = (random(100) < Percentuale_Di_Successo);  // Inserire qui CRC e monitor
  if (Messaggio_OK) {
    index_Received_Packet++;
    sprintf(txpacket[index_Received_Packet % WINDOW_LENGTH], "ACK for < %s >\tRssi=\t%d\t%d\t", rxpacket, Rssi, Actual_Time);
    state = STATE_RX;
  } else {
    Serial.printf(" \tNO ACK SENT\n");
    state = STATE_RX;
  }
}

void writeOnFile(const char* path, const String& content) {
    digitalWrite(led, 1);
 File file = SPIFFS.open(path, FILE_APPEND);
  if (!file) {
    Serial.println("Error opening file in append mode");
    return;
  }
  file.println(content);
  file.close();
    digitalWrite(led, 1);
}

void cleanFile(const char* path) {
  File file = SPIFFS.open(path, FILE_WRITE);  // Sovrascrive il file
  if (!file) {
    Serial.println("Error cleaning file");
    return;
  }
  file.close();
}

String readFromFile(const char* path) {
  File file = SPIFFS.open(path, FILE_READ);
  if (!file) {
    Serial.println("Error opening file in reading mode");
    return "";
  }

  String content = "";
  while (file.available()) {
    content += (char)file.read();
  }

  file.close();
  return content;
}

void handleRoot() {
  digitalWrite(led, 1);
  server.send(200, "text/plain", "webPageTest");
  digitalWrite(led, 0);
}

void handleDrone() {
  digitalWrite(led, 1);
  String contentssss = readFromFile("/data.html");
  writeOnFile("data.html", "</pre></body></html>");
  server.send(200, "text/html", contentssss);
  digitalWrite(led, 0);
}
 

void handleNotFound() {
  digitalWrite(led, 1);
  String message = "File Not Found\n\n";
  message += "URI: ";
  message += server.uri();
  message += "\nMethod: ";
  message += (server.method() == HTTP_GET) ? "GET" : "POST";
  message += "\nArguments: ";
  message += server.args();
  message += "\n";
  for (uint8_t i = 0; i < server.args(); i++) {
    message += " " + server.argName(i) + ": " + server.arg(i) + "\n";
  }
  server.send(404, "text/plain", message);
  digitalWrite(led, 0);
}