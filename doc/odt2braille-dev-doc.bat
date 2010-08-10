@echo off
echo Developer guide (html)...
makeinfo --html --no-split --no-headers --css-include=body.css --output=odt2braille-dev-doc.html odt2braille-dev-doc.texi
echo Developer guide (pdf)...
texi2dvi --pdf odt2braille-dev-doc.texi
@pause
exit