# include "WiFi_Manager.h"

WiFiManager::WiFiManager(const char* ssid, const char* password, const char* remoteIP, int port)
    : _ssid(ssid), _password(password), _remoteIP(remoteIP), _port(port) {}


void WiFiManager::begin()
{
    WiFi.softAP(_ssid, _password);
    delay(100);
    
    Serial.printf("Wifi initialized -> ssid: %s password: %s-", _ssid, _password);
}

void WiFiManager::sendTelemetry(TelemetryData &data)
{
    _udp.beginPacket(_remoteIP, _port);

    //  Make the send payload in a byte array, sending as if straight out of memory
    _udp.write((uint8_t *)&data, sizeof(data));

    _udp.endPacket();
}

void WiFiManager::printTelemetry(TelemetryData &d)
{
     Serial.print("X: ");
    Serial.println(d.accelX);
    Serial.print("Y: ");
    Serial.println(d.accelY);
    Serial.print("Z: ");
    Serial.println(d.accelZ);

    Serial.print(d.latitude);
    Serial.print(" and ");
    Serial.println(d.longitude);
    Serial.println(d.speed);
}

