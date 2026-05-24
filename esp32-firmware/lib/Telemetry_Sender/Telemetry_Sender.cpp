# include "Telemetry_Sender.h"

// SD HAS BEEN DISABLED DUE TO HARDWARE PROBLEMS, FIX

TelemetrySender::TelemetrySender(GPSManager &gps, AccelerometerManager &accel,
     WiFiManager &wifi, TelemetryData &data) 
    : _gps(gps), _accel(accel), _wifi(wifi), _data(data) //_sd(sd)
    {  
        _lastSend = 0;
    }


void TelemetrySender::send() 
{
  // Continue updating lastSend so that it doing every 20ms UPDATE: FIXED IT
  if (millis() - _lastSend >= SEND_INTERVAL)
  {
    _data.time = millis();

    _gps.update(_data);
    _accel.update(_data);
    _wifi.sendTelemetry(_data);

    //String row = String(_data.time) + "," + String(_data.speed, 2) + "," + String(_data.longitude, 6) + "," + String(_data.latitude, 6) + "," + String(_data.accelX, 3) 
    // + "," + String(_data.accelY, 4) + "," + String(_data.accelZ, 3) + "\n";

    // _sd.appendToCSV("/log.csv", row);

    // Update the last print
    _lastSend += SEND_INTERVAL; 
   }
}
