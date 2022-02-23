#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WebServer.h>
#include <ESP8266mDNS.h>
#include <ESP8266HTTPUpdateServer.h>
#include <ESP8266HTTPClient.h>
#include <EmonLib.h>
#include "AppConfig.cpp"

const char *ssid = "Sakun's Google Pixel 5";
const char *password = "sakun921118";
const String serverName = "http://192.168.0.57/security_home/device/";
const double voltage = 115;

String deviceID;
char deviceID_c_str[5];
unsigned long long post_timer, read_energy_timer;
int number_of_sample;

ESP8266WebServer httpServer(80); // port
ESP8266HTTPUpdateServer httpUpdater;
EnergyMonitor emon1;
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
    return payload[10] == '1';
  }
  http.end();
  return -1;
}

void read_energy(int param)
{
  double current = emon1.calcIrms(1480); // Calculate Irms only
  file.setenergy(file.getenergy() + current * voltage * param);
  file.configSave();
  EEPROM.commit();
  Serial.println("I = " + String(current) + "A P = " + String(current * voltage) + "W E = " + String(file.getenergy()) + "J");
}

bool upload_energy()
{
  HTTPClient http;
  http.begin(serverName + "sendPowerUsed");
  http.addHeader("Content-Type", "application/json");
  int httpCode = http.POST("{\"deviceId\":\"" + deviceID + "\",\"power\":" + String(file.getenergy() / 3600.0) + "}");
  Serial.println(httpCode);
  http.end();
  if (httpCode == 200)
  {
    file.setenergy(0.0);
    file.configSave();
    EEPROM.commit();
    Serial.println("upload_success");
    return true;
  }
  return false;
}

void setup()
{
  //set pin and load eeprom
  pinMode(LED_BUILTIN, OUTPUT);
  pinMode(D7, OUTPUT);//D7 GPIO 12
  Serial.begin(115200);
  emon1.current(A0, 17);
  EEPROM.begin(512);
  file.configLoad();
  file.getdeviceID(deviceID_c_str);
  deviceID = deviceID_c_str;
  Serial.println("Before energy used:" + String(file.getenergy()) + "J");
  
  //WiFi and OTA
  WiFi.mode(WIFI_STA);
  WiFiSetup();
  MDNS.begin("security_home_" + deviceID);
  httpUpdater.setup(&httpServer);
  httpServer.begin();
  MDNS.addService("http", "tcp", 80);

  //surge current
  for (int i = 0; i < 5; i++)
    emon1.calcIrms(1480); // Calculate Irms only
  
  //initial
  post_timer = millis();
  read_energy_timer = millis();
  number_of_sample = 0;
  digitalWrite(LED_BUILTIN, 1);
  digitalWrite(D7, 0);
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
      digitalWrite(LED_BUILTIN, 0);
      digitalWrite(D7, 1);
      Serial.println("switch has been opened");
    }
    else if (switch_status == 0)
    {
      digitalWrite(LED_BUILTIN, 1);
      digitalWrite(D7, 0);
      Serial.println("switch has been closed");
    }
    else
      Serial.println("switch status got failed");
  }

  //read_energy
  if (millis() - read_energy_timer > 999)
  {
    int param = (millis() - read_energy_timer) / 1000;
    read_energy_timer += param * 1000;
    read_energy(param);
    number_of_sample += param;
  }

  //upload_energy
  if (number_of_sample > 59)
  {
    if (WiFi.status() != WL_CONNECTED)
      WiFiSetup();
    upload_energy();
    number_of_sample = 0;
  }
  delay(10);
  yield();
}
