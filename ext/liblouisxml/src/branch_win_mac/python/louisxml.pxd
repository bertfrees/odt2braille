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


cdef extern from "liblouisxml.h":
    ctypedef char widechar
    char * lbx_version()
    void *lbx_initialize(char *configFileList, char *logFileName, char *settingsString)
    int lbx_translateString(char* configFileList, char *inbuf, widechar *outbuf, int *outlen, unsigned int mode)
    int lbx_translateFile(char *configFileList, char *inFileName, char *outFileName, unsigned int mode)
    int lbx_translateTextFile(char *configFileList, char *inFileName, char *outFileName, unsigned int mode)
    int lbx_backTranslateFile(char *configFileList, char *inFileName, char *outFileName, unsigned int mode)
    void lbx_free()

