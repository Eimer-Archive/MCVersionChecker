# MCVersionChecker

![Build Status](https://github.com/Eimer-Archive/MCVersionChecker/actions/workflows/build.yml/badge.svg)
[![Discord Server](https://img.shields.io/discord/979589333524820018?color=7289da&label=DISCORD&style=flat-square&logo=appveyor)](https://discord.gg/k8RcgxpnBS)

MCVersionChecker is a program that checks the version and commit information from old CraftBukkit jar files. This does not work with newer files at the moment.

### Features
- [ ] Insert into spreadsheet file
- [x] Basic info extraction
- [ ] Extract from modern versions
- [x] Add a GUI
- [ ] Native executable compilation
- [ ] Check the Eimer Archive API to see if the file exists there or not (Once the website API exists)


## Usage

### Requirements
- Java 25

To use this download the latest version from the actions tab and run the program with `java -jar MCVersionChecker-1.0-SNAPSHOT.jar`. Then paste the file path to the jar files you want to scan.

You can also use it without the UI by running `java -jar MCVersionChecker-1.0-SNAPSHOT.jar --scan /path/to/jar/file.jar`. To scan multiple files you can separate them with a comma.
