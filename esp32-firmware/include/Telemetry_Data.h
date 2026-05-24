# ifndef TELEMETRY_DATA_H
# define TELEMETRY_DATA_H

// The packed attribute forces the compiler to not pad between any bytes
struct __attribute__((packed)) TelemetryData 
{
  long time;
  float accelX, accelY, accelZ;
  float latitude, longitude, speed;
};

# endif