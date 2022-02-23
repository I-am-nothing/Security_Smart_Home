#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WebServer.h>
#include <ESP8266mDNS.h>
#include <ESP8266HTTPUpdateServer.h>
#include <ESP8266HTTPClient.h>
#include "AppConfig.cpp"

const char *ssid = "Sakun's Google Pixel 5";
const char *password = "sakun921118";
String serverName = "http://192.168.0.57/security_home/device/";

String deviceID;
char deviceID_c_str[5];
unsigned long long doorlock_timer, doorbell_timer, post_timer;
bool door_opening, cooling, ringing;

ESP8266WebServer httpServer(80);
ESP8266HTTPUpdateServer httpUpdater;
AppConfig file;

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
  
  HTTPClient http;
  http.begin(serverName + "boot");
  http.addHeader("Content-type", "application/json");
  int httpCode = http.POST("{\"deviceId\":\"" + deviceID + "\",\"ip\":\"" + WiFi.localIP().toString() + "\"}");
  Serial.println("deviceID = " + deviceID);
  Serial.println(httpCode);
  http.end();
}

int get_switch_status()
{
  HTTPClient http;
  http.begin(serverName + "onOffStatus/get");
  http.addHeader("Content-type", "application/json");
  int httpCode = http.POST("{\"deviceId\":\"" + deviceID + "\"}");
  Serial.println("deviceID = " + deviceID);
  Serial.println(httpCode);
  if (httpCode == 200)
  {
    String payload = http.getString();
    Serial.println(payload.substring(23, payload.length() - 2));
    http.end();
    return payload[10] - '0';
  }
  http.end();
  return -1;
}

bool set_status()
{
  HTTPClient http;
  http.begin(serverName + "onOffStatus/set");
  http.addHeader("Content-Type", "application/json");
  int httpCode = http.POST("{\"deviceId\":\"" + deviceID + "\",\"status\":0}");
  Serial.println("deviceID = " + deviceID);
  Serial.println(httpCode);
  return httpCode == 200;
}

void setup()
{
  //set pin and load eeprom
  pinMode(LED_BUILTIN, OUTPUT);
  pinMode(D7, OUTPUT);//D7 GPIO 13 doorlock
  pinMode(D8, OUTPUT);//D8 GPIO 15 doorbell
  Serial.begin(115200);
  EEPROM.begin(512);
  file.configLoad();
  file.getdeviceID(deviceID_c_str);
  deviceID = deviceID_c_str;

  //WiFi and OTA
  WiFi.mode(WIFI_STA);
  WiFiSetup();
  MDNS.begin("security_home_" + deviceID);
  httpUpdater.setup(&httpServer);
  httpServer.begin();
  MDNS.addService("http", "tcp", 80);

  //initial
  post_timer = millis();
  doorlock_timer = millis();
  doorbell_timer = millis();
  digitalWrite(LED_BUILTIN, 1);
  digitalWrite(D7, 0);
  digitalWrite(D8, 0);
  door_opening = false;
  cooling = false;
  ringing = false;
}

void loop()
{
  //OTA
  httpServer.handleClient();
  MDNS.update();

  //get_switch_status
  if (millis() - post_timer > 999)
  {
    if (WiFi.status() != WL_CONNECTED)
      WiFiSetup();
    post_timer = millis();
    int switch_status = get_switch_status();
    if (switch_status == 1)
    {
      set_status();
      if (door_opening)
        Serial.println("door had been opened");
      else if (cooling)
        Serial.println("switch just cooling now");
      else
      {
        Serial.println("door is opening now");
        doorlock_timer = millis();
        digitalWrite(LED_BUILTIN, 0);
        digitalWrite(D7, 1);
        door_opening = true;
      }
    }
    else if (switch_status == 2)
    {
      set_status();
      Serial.println("doorbell is ringing");
      doorbell_timer = millis();
      digitalWrite(D8, 1);
      ringing = true;
    }
    else if (switch_status == -1)
      Serial.println("switch status got failed");
  }

  //door_opening
  if (door_opening && millis() - doorlock_timer > 4999)
  {
    Serial.println("door has been closed");
    digitalWrite(LED_BUILTIN, 1);
    digitalWrite(D7, 0);
    door_opening = false;
    cooling = true;
    doorlock_timer = millis();
  }
  
  //cooling
  else if (cooling && millis() - doorlock_timer > 999)
  {
    Serial.println("switch has cooled already");
    cooling = false;
  }

  //ringing
  if (ringing && millis() - doorbell_timer > 1999)
  {
    Serial.println("doorbell has been stoped");
    digitalWrite(D8, 0);
    ringing = false;
  }
  delay(10);
  yield();
}
