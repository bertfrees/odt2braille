install_name_tool -change /usr/local/lib/liblouisxml.1.dylib @executable_path/liblouisxml.1.dylib xml2brl
install_name_tool -change /usr/local/lib/liblouis.2.dylib    @executable_path/liblouis.2.dylib    xml2brl
install_name_tool -change /usr/lib/libxml2.2.dylib           @executable_path/libxml2.2.dylib     xml2brl
install_name_tool -change /usr/local/lib/liblouis.2.dylib    @executable_path/liblouis.2.dylib    liblouisxml.1.dylib
install_name_tool -change /usr/lib/libxml2.2.dylib           @executable_path/libxml2.2.dylib     liblouisxml.1.dylib
