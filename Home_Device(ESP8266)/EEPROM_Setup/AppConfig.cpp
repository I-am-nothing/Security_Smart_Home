#include <EEPROM.h>
#include <Arduino.h>
// Handle the Application configuration parameters stored in EEPROM
// Access to these parameters is through the object properties only
//
typedef struct configData_t
{
 uint8_t signature[2];
 uint8_t version;
 double energy;
 //String deviceID;
 char deviceID[5];
 // application config data starts below
 //uint16_t delaySelect;
 //uint16_t lastSlotID;
};

class AppConfig
{
public:
 //inline String getdeviceID() { return _D.deviceID; };
 inline void getdeviceID(char *n) { for (int i = 0; i < 5; i++) n[i] = _D.deviceID[i]; };
 //inline void setdeviceID(String n) { _D.deviceID = n; };
 inline void setdeviceID(char *n) { for (int i = 0; i < 5; i++) _D.deviceID[i] = n[i]; };
 inline uint8_t getversion() { return _D.version; };
 inline void setversion(uint8_t n) { _D.version = n; };
 inline double getenergy() { return _D.energy; };
 inline void setenergy(double n) { _D.energy = n; };
 //inline uint8_t getversion() { return(_D.version); };
 //inline void setsignature(uint8_t a, uint8_t b) { _D.signature[0] = a, _D.signature[1] = b; };

 void begin()
 {
   if (!configLoad())
   {
     configDefault();
     configSave();
   }
 };

 void configDefault(void)
 {
   _D.signature[0] = EEPROM_SIG[0];
   _D.signature[1] = EEPROM_SIG[1];
   _D.version = CONFIG_VERSION;
   _D.energy = 0.0;
    for (int i = 0; i < 5; i++)
      _D.deviceID[i] = DEVICEID[i];
   
   //_D.delaySelect = SELECT_DELAY_DEFAULT;
   //_D.lastSlotID = LAST_SLOT_DEFAULT; 
 }

 bool configLoad(void)
 {
   EEPROM.get(EEPROM_ADDR, _D);
   if (_D.signature[0] != EEPROM_SIG[0] && 
       _D.signature[1] != EEPROM_SIG[1])   
     return(false);
  // handle any version adjustments here
  if (_D.version != CONFIG_VERSION)
  {
    // do something here
  }

  // update version number to current
  _D.version = CONFIG_VERSION;

  return(true);
 }

 bool configSave(void)
 {
   EEPROM.put(EEPROM_ADDR, _D);
   return(true);
 }

private:
 //const uint16_t SELECT_DELAY_DEFAULT = 1000; // milliseconds
 //const uint16_t LAST_SLOT_DEFAULT = 99;      // number
 const uint16_t EEPROM_ADDR = 0;
 const uint8_t EEPROM_SIG[2] = { 0xc1, 0xc8 };
 const uint8_t CONFIG_VERSION = 0;
 const char DEVICEID[5] = "000";
 configData_t _D;
};
