"""Python bindings for liblouisxml

This module contains the python bindings for the liblouisxml Braille
translation library. While there are docstrings to help you use this
module, you are advised to view the official liblouisxml documentation.

These bindings intend to be as close to the original calls for liblouisxml
only making changes to the API where it simplifies the use in python.

"""

###############################################################################
# Copyright (C) 2009 Michael Whapples
#
# This module is free software; you can redistribute it and/or modify it under
# the terms of the Artistic License 2.0. For details, see the full text of the
# license in the file LICENSE.
#
# This module is distributed in the hope that it will be useful, but it is
# provided "as is" and without any express or implied warranties. For details,
# see the full text of the license in the file LICENSE.
###############################################################################


from python_version cimport PY_MAJOR_VERSION, PY_MINOR_VERSION
cimport stdlib

from louisxml cimport *

cdef char *lbx_encoding
if sizeof(widechar) == 2:
    lbx_encoding = u"utf_16_le"
elif sizeof(widechar) == 4:
    lbx_encoding = u"utf_32_le"
if PY_MAJOR_VERSION == 2 and PY_MINOR_VERSION < 6:
    import PyICU
cdef object get_unicode(widechar* buftxt, int buflen):
    if PY_MAJOR_VERSION == 2 and PY_MINOR_VERSION < 6:
        tmp_unicode = PyICU.UnicodeString((<char *>buftxt)[:buflen],
                                         lbx_encoding.encode("ascii"))
        return unicode(tmp_unicode)
    else:
        tmp_str = (<char *>buftxt)[:buflen]
        return unicode(tmp_str, lbx_encoding)

class LouisXMLException(Exception):
    pass

def version():
    return lbx_version()

def initialize(configFileName, logFileName, settingsString):
    """Initialise liblouisxml
    
    This function will initialise liblouisxml by calling the lou_initialize
    with the arguments you provide.

    Please note: This function need not be called unless you need to set
    something special such as a log file.
    """
    logFileNameBytes = ""
    cdef char *c_logFileName
    if isinstance(logFileName, unicode):
        logFileNameBytes = logFileName.encode()
        c_logFileName = logFileNameBytes
    elif (PY_MAJOR_VERSION < 3) and isinstance(logFileName, str):
        c_logFileName = configFileName
    elif logFileName is None:
        c_logFileName = NULL
    else:
        raise ValueError("Requires text input but got %s" % type(logFileName))
    lbx_initialize(configFileName, c_logFileName, settingsString)

def translateString(char *configFileName, inbuf, unsigned int mode):
    """Translate a string to Braille
    
    This will translate a string or unicode string to Braille. This
    does the translation by calling lbx_translateString. The call for
    python differs from the C as python handles things differently. This
    means that you only need specify the translation table, string to
    translate and the mode arguments. This function will return the Braille.
    """
    cdef char *c_inbuf = inbuf
    cdef int outlen = len(inbuf) * 2
    cdef int widechar_size = sizeof(widechar)
    cdef widechar *outbuf = <widechar *>stdlib.malloc(outlen * widechar_size)
    unicode_outbuf = None
    try:
        if lbx_translateString(configFileName, c_inbuf, outbuf, &outlen, mode) < 0:
            raise LouisXMLException("Unable to complete translation")
        unicode_outbuf = get_unicode(outbuf, outlen * widechar_size)
    finally:
        stdlib.free(outbuf)
    return unicode_outbuf

def translateFile(char * configFileName, char *infile, char * outfile, unsigned int mode):
    """Translates an XML file
    
    This function will translate an XML file into Braille and output the
    Braille to a file. This does the translation by calling the
    lbx_translateFile function.
    """
    if lbx_translateFile(configFileName, infile, outfile, mode) < 0:
        raise LouisXMLException("Unable to complete translation")

def translateTextFile(char *configFileList, char *infile, char *outfile, unsigned int mode):
    """Translate a text file to Braille
    
    Translate a text file into Braille and output the result to a file.
    This function performs the translation by calling lbx_translateTextFile.
    """
    if lbx_translateTextFile(configFileList, infile, outfile, mode) < 0:
        raise LouisXMLException("Unable to complete translation")

def backTranslateFile(char *configFileList, char *infile, char *outfile, unsigned int mode):
    """Back translate a Braille file
    
    Back translate a Braille file to a file. Does this by calling
    lbx_backTranslateFile.
    """
    if lbx_backTranslateFile(configFileList, infile, outfile, mode) < 0:
        raise LouisXMLException("Unable to complete back translation")

def free():
    """Free resources
    
    Frees the system resources used by liblouisxml by calling lbx_free.
    """
    lbx_free()

