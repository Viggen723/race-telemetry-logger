# include "Accelerometer_Manager.h" 
 # include <Wire.h> 

 AccelerometerManager::AccelerometerManager(uint8_t address, int sda, int scl) 
     : _address(address), _sda(sda), _scl(scl) {} 

 bool AccelerometerManager::begin() 
 { 
     Wire.begin(_sda, _scl); 
     Wire.setClock(100000); 
     delay(100); 

     // Need the INT pin t
     if (!_bno.begin_I2C(_address, &Wire, 6))  
     { 
         return false; 
     } 

     // Enable linear acceleration report I guess need to to get the data 
     // 5000 is the report interval 
     if (!_bno.enableReport(SH2_LINEAR_ACCELERATION, 5000)) 
     { 
         return false; 
     } 

     Serial.println("imu initialized"); 
     return true; 
 } 

 void AccelerometerManager::update(TelemetryData &d) 
 { 
     sh2_SensorValue_t sensorValue; 

     while (_bno.getSensorEvent(&sensorValue)) 
     { 
         if (sensorValue.sensorId == SH2_LINEAR_ACCELERATION) 
         { 
             d.accelX = sensorValue.un.linearAcceleration.x; 
             d.accelY = sensorValue.un.linearAcceleration.y; 
             d.accelZ = sensorValue.un.linearAcceleration.z; 
         } 
     } 
 }