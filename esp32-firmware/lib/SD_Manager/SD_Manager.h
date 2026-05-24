# ifndef SD_MANAGER_H
# define SD_MANAGER_H

# include <SD.h>
# include <SPI.h>

class SDManager {

    public:
        SDManager(int csPin);

        bool begin();
        bool writeFile(const char* path, const String header);
        bool appendToCSV(const char* path, const String data);

    private:
        int _csPin;
};

# endif