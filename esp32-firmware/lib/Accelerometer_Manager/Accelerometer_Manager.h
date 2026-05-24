# ifndef ACCELEROMETER_MANAGER_H
# define ACCELEROMETER_MANAGER_H

# include <Adafruit_BNO08x.h>
# include "Telemetry_Data.h"

class AccelerometerManager {
  public:
    AccelerometerManager(uint8_t address = 0x4B, int sda = 4, int scl = 5);

    bool begin();
    void update(TelemetryData &data);

  private:
    Adafruit_BNO08x _bno;
    uint8_t _address;
    int _sda;
    int _scl;
};

# endif