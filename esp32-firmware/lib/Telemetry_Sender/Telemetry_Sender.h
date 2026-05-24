# ifndef TELEMETRY_SENDER_H
# define TELEMETRY_SENDER_H

# include "GPS_Manager.h"
# include "Accelerometer_Manager.h"
# include "Telemetry_Data.h"
# include "WiFi_Manager.h"
# include "SD_Manager.h"

class TelemetrySender {

    public:
        TelemetrySender(GPSManager &gps, AccelerometerManager &accel, 
            WiFiManager &wifi, TelemetryData &data);

        void send();
    
    private:
        static unsigned long time;
        const int SEND_INTERVAL = 20; // 50Hz update rate is the goal but overpowers the current GPS 

        GPSManager &_gps;
        AccelerometerManager &_accel;
        WiFiManager &_wifi;
        TelemetryData &_data;
        // SDManager &_sd;

        int _lastSend;
    
};




# endif
