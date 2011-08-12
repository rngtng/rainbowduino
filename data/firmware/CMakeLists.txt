
set(CMAKE_SOURCE_DIR /Users/tobi/Sites/arduino-cmake)
set(CMAKE_MODULE_PATH    ${CMAKE_SOURCE_DIR}/cmake/modules)  # CMake module search path
set(CMAKE_TOOLCHAIN_FILE ${CMAKE_SOURCE_DIR}/cmake/toolchains/Arduino.cmake) # Arduino Toolchain

cmake_minimum_required(VERSION 2.8)

project(RainbowduinoFirmware C CXX)
find_package(Arduino 22 REQUIRED)

#====================================================================#
#  Settings                                                          #
#====================================================================#
# _SRCS           # Target source files
# _HDRS           # Target Headers files (for project based build systems)
# _SKETCHES       # Target sketch files
# _LIBS           # Libraries to linked against target (sets up dependency tracking)
# _BOARD          # Board name (such as uno, mega2560, ...)
# _PORT           # Serial port, for upload and serial targets [OPTIONAL]
# _SERIAL         # Serial command for serial target           [OPTIONAL]
# _NO_AUTOLIBS    # Disables Arduino library detection (default On)
# _AFLAGS         # Overide global avrdude flags for target

set(FIRMWARE_NAME firmware)

set(${FIRMWARE_NAME}_BOARD atmega168)
#set(${FIRMWARE_NAME}_SKETCHES firmware.pde)
set(${FIRMWARE_NAME}_SRCS firmware.cpp Connection.cpp ConnectionMessage.cpp)
# set(${FIRMWARE_NAME}_HDRS Connection.h ConnectionMessage.h RCodes.h)

# we have libraries in subfolder lib
set(CMAKE_CURRENT_SOURCE_DIR ${CMAKE_CURRENT_SOURCE_DIR} ${CMAKE_CURRENT_SOURCE_DIR}/lib)

set(${FIRMWARE_NAME}_PORT /dev/tty.usbserial-A1004csi)
set(${FIRMWARE_NAME}_SERIAL picocom @INPUT_PORT@ -b 115200 -l )


generate_arduino_firmware(${FIRMWARE_NAME})
