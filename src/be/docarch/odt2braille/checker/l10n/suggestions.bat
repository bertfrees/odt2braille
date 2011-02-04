@echo off
echo English...
"C:\Program Files (x86)\Java\jdk1.6.0_17\bin\native2ascii.exe" -encoding utf-8 suggestions_utf8.properties suggestions.properties
echo Now delete "\ufeff" at the beginning of the .properties file.
@pause
exit