# Example for iWedia's TV software stack (aka Teatro 3.5) integration with Google's TV Input Framework (TIF) on Android based TV/STB

This example is designed to show how to use some basic functionalists of iWedia's Android4TV API to implement TV Input Service for Android based TV/STB device. It provides the content that comes from live broadcast over-the-air stream data provided by DVB Tuner.

## Introduction

This example features following TV functionalities:

- Channels setup (scan)
- TV and radio services
- Electronic Programming Guide (services and programs)
- Parental control
- Audio and subtitle tracks switching

Example provides TV content which is consumed and presented by TIF compliant TV application. Note that this example doesn’t provide any UI/UX (except for services setup) for video playback or content browsing. That is under responsibility of TV application provided by Android TV platform.

## Pre-requisites

- Android SDK 5.0 (API level 21), or greater
- Android4TV compliant platform
- TIF compliant TV application

## Getting Started

### Getting the code

`git clone https://github.com/iWedia/iWediaSimpleTvInputService.git`

### Compiling and installing

Under Linux:<br />
`gradlew build`<br />
`gradlew installDebug`

Under Windows:<br />
`gradlew.bat build`<br />
`gradlew.bat installDebug`

Note that before app is installed, you must be connected to a device using ADB.

### LiveTv application

Google is providing a simple TIF compliant TV application with basic TV functionality (LiveTv). This example is tested and approved to work against LiveTv application.

To get and build LiveTv application, run the following command:<br />
`repo init -u https://android.googlesource.com/platform/manifest -b android-live-tv`<br />
`repo sync -c -j4`<br />
`. build/envsetup.sh `<br />
`tapas LiveTv`<br />
`make`<br />

## License

License under the Apache 2.0 license. See the LICENSE file for details.

## Change List

Initial version 1.3.51
