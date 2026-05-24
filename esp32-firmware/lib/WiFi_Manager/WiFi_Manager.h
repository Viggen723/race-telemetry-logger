# ifndef WIFI_SETUP_H
# define WIFI_SETUP_H

# include <WiFi.h>
# include <WiFiUdp.h>
# include "Telemetry_Data.h"

class WiFiManager
{
  public:

    WiFiManager(const char* ssid, const char* password, const char* remoteIP, int port);

    void begin();
    void sendTelemetry(TelemetryData &d);
    void printTelemetry(TelemetryData &d);

  private:

    WiFiUDP _udp;

    const char* _ssid;
    const char* _password;
    const char* _remoteIP;
    int _port;

};

# endif