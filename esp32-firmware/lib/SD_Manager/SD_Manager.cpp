# include "SD_Manager.h"

SDManager::SDManager(int csPin)
{
    _csPin = csPin;
}

bool SDManager::begin()
{
    pinMode(_csPin, OUTPUT);
    digitalWrite(_csPin, HIGH);

    // Give the card time to power up
    delay(100);

    for (int i = 0; i < 3; i++)
    {
        // Reset SPI state before each attempt (important on ESP32-S3)
        SPI.end();
        delay(50);

        SPI.begin(12, 13, 11, _csPin); // SCK, MISO, MOSI, CS

        // Lower frequency for stability (VERY important in real systems)
        if (SD.begin(_csPin, SPI, 4000000))
        {
            Serial.println("SD initialization successful!");
            return true;
        }

        Serial.println("SD init retry...");
        delay(200);
    }

    Serial.println("SD initialization failed");
    return false;
}

bool SDManager::writeFile(const char* path, const String header)
{
    File file = SD.open(path, FILE_WRITE);

    if (!file)
    {
        return false;
    }

    if (file.print(header))
    {
        file.close();
        return true;
    }
    else
    {
        file.close();
        return false;
    }
}

bool SDManager::appendToCSV(const char* path, const String data) {
    
    File file = SD.open(path, FILE_APPEND);
    if (!file) return false;

    if (file.print(data)) {
        file.close();
        return true;
    }
    file.close();
    return false;
}