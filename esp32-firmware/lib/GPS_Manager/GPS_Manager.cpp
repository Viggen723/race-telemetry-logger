# include "GPS_Manager.h"

GPSManager::GPSManager(int uart, int rx, int tx) 
    : _GPSSerial(uart), _GPS(&_GPSSerial), _rx(rx), _tx(tx) {}


bool GPSManager::begin(int baud)
{
    // Seeing if changing the GPS buffer size stops slow down in app
    _GPSSerial.setRxBufferSize(2048);
    _GPSSerial.begin(baud, SERIAL_8N1, _rx, _tx);
    delay(500);

    // Set Update Rate to 25Hz (40ms) using UBX-CFG-RATE
    uint8_t changeRate25Hz[] = {
        0xB5, 0x62, 0x06, 0x08, 0x06, 0x00, 0x28, 0x00, 
        0x01, 0x00, 0x01, 0x00, 0x3E, 0xAA
    };

    _GPSSerial.write(changeRate25Hz, sizeof(changeRate25Hz));
    
    Serial.println("-GPS Initialized-");
    return true;
}

void GPSManager::update(TelemetryData &d)
{
    while (_GPSSerial.available())
    {
        char c = _GPS.read();
    
        if (_GPS.newNMEAreceived())
        {
            if (_GPS.parse(_GPS.lastNMEA()))
            {
                if (_GPS.fix)
                {
                    d.latitude = _GPS.latitudeDegrees;
                    d.longitude = _GPS.longitudeDegrees;

                    //This is apparently in knots, not mph nor kph
                    d.speed = _GPS.speed;
                }
            }
        }
    }
}