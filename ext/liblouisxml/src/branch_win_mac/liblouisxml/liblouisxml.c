/* liblouisxml Braille Transcription Library

   This file may contain code borrowed from the Linux screenreader
   BRLTTY, copyright (C) 1999-2006 by
   the BRLTTY Team

   Copyright (C) 2004, 2005, 2006
   ViewPlus Technologies, Inc. www.viewplus.com
   and
   JJB Software, Inc. www.jjb-software.com
   All rights reserved

   This file is free software; you can redistribute it and/or modify it
   under the terms of the Lesser or Library GNU General Public License
   as published by the
   Free Software Foundation; either version 3, or (at your option) any
   later version.

   This file is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   Library GNU General Public License for more details.

   You should have received a copy of the Library GNU General Public
   License along with this program; see the file COPYING.  If not, write to
   the Free Software Foundation, 51 Franklin Street, Fifth Floor,
   Boston, MA 02110-1301, USA.

   Maintained by John J. Boyer john.boyer@jjb-software.com
   */

#include "config.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "louisxml.h"
#include <libxml/HTMLparser.h>

UserData *ud = NULL;

char *EXPORT_CALL
lbx_version ()
{
  static char *version = PACKAGE_VERSION;
  return version;
}

static void
liblouisxmlErrors (void *ctx ATTRIBUTE_UNUSED, const char *msg, ...)
{
  va_list args;
  char buffer[MAXNAMELEN];
  va_start (args, msg);
  memset (buffer, 0, sizeof (buffer));
  vsnprintf (buffer, sizeof (buffer) - 4, msg, args);
  va_end (args);
  lou_logPrint ("%s", buffer);
}

static int
processXmlDocument (const char *inputDoc, int length, int mode)
{
  /*This function does all processing of xml documents as such.
   * If length is 0 the document is assumed to be a file.
   * If length is not 0 it is assumed to be in memory.
   * Sort of hackish, but only hackers will see it. */

  xmlDoc *doc = NULL;
  xmlNode *rootElement = NULL;
  int haveSemanticFile;
  xmlParserCtxt *ctxt;
  static int initialized = 0;
  if (!initialized)
    {
      initialized = 1;
      LIBXML_TEST_VERSION xmlKeepBlanksDefault (0);
      xmlSubstituteEntitiesDefault (1);
    }
  ctxt = xmlNewParserCtxt ();
  xmlSetGenericErrorFunc (ctxt, liblouisxmlErrors);
  if (length == 0)
    {
      if ((mode & htmlDoc))
	doc = htmlParseFile (inputDoc, NULL);
      else
	{
	  if (ud->internet_access)
	    doc = xmlCtxtReadFile (ctxt, inputDoc, NULL,
				   XML_PARSE_DTDVALID | XML_PARSE_NOENT);
	  else
	    doc = xmlParseFile (inputDoc);
	  if (doc == NULL)
	    doc = htmlParseFile (inputDoc, NULL);
	}
    }
  else
    doc = xmlParseMemory (inputDoc, length);
  if (doc == NULL)
    {
      lou_logPrint ("Document could not be processed");
      return 0;
    }
  rootElement = xmlDocGetRootElement (doc);
  if (rootElement == NULL)
    {
      lou_logPrint ("Document is empty");
      return 0;
    }
  haveSemanticFile = compile_semantic_table (rootElement);
  do_xpath_expr ();
  examine_document (rootElement);
  append_new_entries ();
  if (!haveSemanticFile)
    return 0;
  transcribe_document (rootElement);
  xmlFreeDoc (doc);
  xmlCleanupParser ();
  initGenericErrorDefaultFunc (NULL);
  xmlFreeParserCtxt (ctxt);
  return 1;
}

void *EXPORT_CALL
lbx_initialize (const char *configFileName,
		const char *logFileName, const char *settingsString)
{
  if (!read_configuration_file (configFileName, logFileName,
				settingsString, 0))
    return NULL;
  return (void *) ud;
}

int EXPORT_CALL
lbx_translateString (const char *const configFileName,
		     char *inbuf, widechar * outbuf,
		     int *outlen, unsigned int mode)
{
/* Translate the well-formed xml expression in inbuf into braille
* according to the specifications in configFileName. If the expression
* is not well-formed or there are oteer errors, print an error
* message and return 0.*/
  int k;
  char *xmlInbuf;
  int inlen = strlen (inbuf);
  if (!read_configuration_file (configFileName, NULL, NULL, mode))
    return 0;
  ud->inbuf = inbuf;
  ud->inlen = inlen;
  ud->outbuf1 = outbuf;
  ud->outbuf1_len = *outlen;
  for (k = 0; k < inlen; k++)
    if (inbuf[k] > ' ')
      break;
  if (inbuf[k] != '<')
    {
      transcribe_text_string ();
      *outlen = ud->outbuf1_len_so_far;
      return 1;
    }
  if (inbuf[k + 1] == '?')
    xmlInbuf = inbuf;
  else
    {
      inlen += strlen (ud->xml_header);
      if (!(xmlInbuf = malloc (inlen + 4)))
	{
	  lou_logPrint ("Net enough memory");
	  return 0;
	}
      strcpy (xmlInbuf, ud->xml_header);
      strcat (xmlInbuf, "\n");
      strcat (xmlInbuf, inbuf);
    }
  if (!processXmlDocument (xmlInbuf, inlen, mode))
    return 0;
  *outlen = ud->outbuf1_len_so_far;
  if (xmlInbuf != inbuf)
    free (xmlInbuf);
  return 1;
}

int
  EXPORT_CALL lbx_translateFile
  (char *configFileName, char *inFileName, char *outFileName, unsigned
   int mode)
{
/* Translate the well-formed xml expression in inFileName into
* braille according to the specifications in configFileName. If the
* expression is not well-formed or there are other errors, print
* an error message and return 0.*/

  widechar outbuf[2 * BUFSIZE];
  widechar outbuf2[2 * BUFSIZE];
  widechar outbuf3[2 * BUFSIZE];
  xmlParserCtxtPtr ctxt = NULL;
  xmlDoc *doc;
  if (!read_configuration_file (configFileName, NULL, NULL, mode))
    return 0;
  ud->outbuf1 = outbuf;
  ud->outbuf2 = outbuf2;
  ud->outbuf3 = outbuf3;
  ud->outbuf1_len = (sizeof (outbuf) / CHARSIZE) - 4;
  ud->outbuf2_len = (sizeof (outbuf2) / CHARSIZE) - 4;
  ud->outbuf3_len = (sizeof (outbuf3) / CHARSIZE) - 4;

  if (strcmp (outFileName, "stdout"))
    {
      if (!(ud->outFile = fopen (outFileName, "w")))
	{
	  lou_logPrint ("Can't open file %s.", outFileName);
	  return 0;
	}
    }
  else
    ud->outFile = stdout;
  if (!processXmlDocument (inFileName, 0, mode))
    return 0;
  if (ud->outFile != stdout)
    fclose (ud->outFile);
  return 1;
}

int
  EXPORT_CALL lbx_translateTextFile
  (char *configFileName, char *inFileName, char *outFileName, unsigned
   int mode)
{
/* Translate the text file in inFileName into braille according to
* the specifications in configFileName. If there are errors, print
* an error message and return 0.*/
  if (!read_configuration_file (configFileName, NULL, NULL, mode))
    return 0;
  if (strcmp (inFileName, "stdin"))
    {
      if (!(ud->inFile = fopen (inFileName, "r")))
	{
	  lou_logPrint ("Can't open file %s.\n", inFileName);
	  return 0;
	}
    }
  else
    ud->inFile = stdin;
  if (strcmp (outFileName, "stdout"))
    {
      if (!(ud->outFile = fopen (outFileName, "w")))
	{
	  lou_logPrint ("Can't open file %s.\n", outFileName);
	  return 0;
	}
    }
  else
    ud->outFile = stdout;
  transcribe_text_file ();
  if (ud->inFile != stdin)
    fclose (ud->inFile);
  if (ud->outFile != stdout)
    fclose (ud->outFile);
  return 1;
}

int
  EXPORT_CALL lbx_backTranslateFile
  (char *configFileName, char *inFileName, char *outFileName, unsigned
   int mode)
{
/* Back translate the braille file in inFileName into either an
* xml file or a text file according to
* the specifications in configFileName. If there are errors, print an
* error message and return 0.*/
  if (!read_configuration_file (configFileName, NULL, NULL, mode))
    return 0;
  if (strcmp (inFileName, "stdin"))
    {
      if (!(ud->inFile = fopen (inFileName, "r")))
	{
	  lou_logPrint ("Can't open file %s.\n", inFileName);
	  return 0;
	}
    }
  else
    ud->inFile = stdin;
  if (strcmp (outFileName, "stdout"))
    {
      if (!(ud->outFile = fopen (outFileName, "w")))
	{
	  lou_logPrint ("Can't open file %s.\n", outFileName);
	  return 0;
	}
    }
  else
    ud->outFile = stdout;
  back_translate_file ();
  if (ud->inFile != stdin)
    fclose (ud->inFile);
  if (ud->outFile != stdout)
    fclose (ud->outFile);
  return 1;
}

void EXPORT_CALL
lbx_free (void)
{
/* Free all memory used by liblouisxml. You MUST call this function at
* the END of your application.*/
  lou_free ();
  destroy_semantic_table ();
  if (ud != NULL)
    free (ud);
  ud = NULL;
}
