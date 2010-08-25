@echo off
echo User guide (html)...
makeinfo --html --no-split --no-headers --output=odt2braille-user-doc.html odt2braille-user-doc.texi
echo User guide (pdf)...
texi2dvi --pdf odt2braille-user-doc.texi
@pause
exit