@echo off
echo Dutch...
"C:\Program Files (x86)\Java\jdk1.6.0_17\bin\native2ascii.exe" -encoding utf-8 names_nl_utf8.properties names_nl.properties
echo Now delete "\ufeff" at the beginning of the .properties file.
@pause
exit