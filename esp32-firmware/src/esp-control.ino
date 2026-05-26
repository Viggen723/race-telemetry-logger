# include "Telemetry_Data.h"
# include "WiFi_Manager.h"
# include "GPS_Manager.h"
# include "Accelerometer_Manager.h"
# include "Telemetry_Sender.h"

TelemetryData currentRun;
WiFiManager wifi = WiFiManager("RoyTelemetrySystems_Logger", "Testing123", "192.168.4.2", 4120);
AccelerometerManager accel = AccelerometerManager();
GPSManager gps = GPSManager();
TelemetrySender sender = TelemetrySender(gps, accel, wifi, currentRun);

void setup(void)
{

  Serial.begin(460800);
  Serial.println("Race Telemetry logger - Booting up");
  delay(1000);
    
  wifi.begin();
  accel.begin();
  gps.begin(115200);

}

void loop(void)
{
  sender.send();
}