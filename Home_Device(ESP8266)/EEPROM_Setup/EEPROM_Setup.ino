#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WebServer.h>
#include <ESP8266mDNS.h>
#include <ESP8266HTTPUpdateServer.h>
#include "AppConfig.cpp"

char* deviceId = "205";
String deviceID = "205";
const char* ssid = "Sakun's Google Pixel 5";
const char* password = "sakun921118";

ESP8266WebServer httpServer(80);
ESP8266HTTPUpdateServer httpUpdater;

unsigned long long int past = millis();

void WiFiSetup()
{
  Serial.printf("connecting to %s ", ssid);
  WiFi.begin(ssid, password);
  int count = 0;
  while (WiFi.status() != WL_CONNECTED)
  {
    if (count == 40)
      ESP.restart();
    Serial.print(".");
    delay(250);
    count++;
  }
  Serial.print("connected ");
  Serial.println(WiFi.localIP());
}

void setup() {
  Serial.begin(115200);
  AppConfig file;
  EEPROM.begin(512);
  file.begin();
  file.setdeviceID(deviceId);
  //Serial.println("first deviceID = " + file.getdeviceID());
  file.setenergy(0.0);
  file.configSave();
  EEPROM.commit();
  WiFi.mode(WIFI_STA);
  WiFiSetup();
  //啟動mdns服務
  MDNS.begin("security_home_" + deviceID);
  //配置webserver為更新server
  httpUpdater.setup(&httpServer);
  httpServer.begin();

  MDNS.addService("http", "tcp", 80);
}

void loop() {
  httpServer.handleClient();
  MDNS.update();
  if (millis() - past > 9999)
  {
    AppConfig file;
    file.configLoad();
    char c_str[5];
    file.getdeviceID(c_str);
    String str = c_str;
    Serial.println("deviceID = " + str);
    Serial.println(sizeof(file));
    Serial.println("E = " + String(file.getenergy()));
    Serial.println(WiFi.localIP());
    past = millis();
  }
}
