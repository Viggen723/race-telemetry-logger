# ifndef GPS_MANAGER_H
# define GPS_MANAGER_H

# include <Adafruit_GPS.h>
# include <HardwareSerial.h>
# include "Telemetry_Data.h"

class GPSManager
{
    public:
        GPSManager(int uart = 2, int rx = 1, int tx = 2);

        bool begin(int baud = 115200);
        void update(TelemetryData &d);

    private:
        HardwareSerial _GPSSerial;
        Adafruit_GPS _GPS;
        int _rx, _tx;
};

# endif