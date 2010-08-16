@echo off
echo German...
"C:\Program Files (x86)\Java\jdk1.6.0_17\bin\native2ascii.exe" -encoding utf-8 Bundle_de_utf8.properties Bundle_de.properties
echo Now delete "\ufeff" at the beginning of the .properties file.
@pause
exit