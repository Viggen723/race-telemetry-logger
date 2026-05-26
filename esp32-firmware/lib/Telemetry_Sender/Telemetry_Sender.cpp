# include "Telemetry_Sender.h"

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

    // Update the last print
    _lastSend += SEND_INTERVAL; 
   }
}
