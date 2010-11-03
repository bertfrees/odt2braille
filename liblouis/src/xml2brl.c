/* liblouisxml Braille Transcription Library

   This file may contain code borrowed from the Linux screenreader
   BRLTTY, copyright (C) 1999-2006 by the BRLTTY Team

   Copyright (C) 2004, 2005, 2006, 2009
   ViewPlus Technologies, Inc. www.viewplus.com and
   JJB Software, Inc. www.jjb-software.com

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

   Maintained by John J. Boyer john.boyer@jjb-software.com
   */

#include "liblouisxml/config.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "louisxml.h"
#include <getopt.h>
#include "progname.h"
#include "version-etc.h"

static const struct option longopts[] =
{
  { "help", no_argument, NULL, 'h' },
  { "version", no_argument, NULL, 'v' },
  { "config-file", required_argument, NULL, 'f' },
  { "backward", no_argument, NULL, 'b' },
  { "reformat", no_argument, NULL, 'r' },
  { "poorly-formatted", no_argument, NULL, 'p' },
  { "html", no_argument, NULL, 't' },
  { "log-file", no_argument, NULL, 'l' },
  { "config-setting", required_argument, NULL, 'C' },
  { NULL, 0, NULL, 0 }
};

const char version_etc_copyright[] =
  "Copyright %s %d ViewPlus Technologies, Inc. and JJB Software, Inc.";

#define AUTHORS "John J. Boyer"

static void
print_help (void)
{
  printf ("\
Usage: %s [OPTION] [inputFile] [outputFile]\n", program_name);

  fputs ("\
Translate an xml or a text file into an embosser-ready braille file.\n\
This includes translation into grade two, if desired, mathematical \n\
codes, etc. It also includes formatting according to a built-in \n\
style sheet which can be modified by the user.\n\
\n\
If inputFile is not specified or '-' input is taken from stdin. If outputFile\n\
is not specified the output is sent to stdout.\n\n", stdout);

  fputs ("\
  -h, --help          	  display this help and exit\n\
  -v, --version       	  display version information and exit\n\
  -f, --config-file       name a configuration file that specifies\n\
                          how to do the translation\n\
  -b, --backward      	  backward translation\n\
  -r, --reformat      	  reformat a braille file\n\
  -p, --poorly-formatted  translate a poorly formatted file\n\
  -t, --html              html document, not xhtml\n\
  -C, --config-setting    specify particular configuration settings\n\
                          They override any settings that are specified in a\n\
                          config file\n\
  -l, --log-file          write errors to log file instead of stderr\n", stdout);

  printf ("\n");
  printf ("\
Report bugs to <%s>.\n", PACKAGE_BUGREPORT);
}

int
main (int argc, char **argv)
{
  int mode = dontInit;
  char *configFileName = "default.cfg";
  char *inputFileName = "stdin";
  char *outputFileName = "stdout";
  char tempFileName[MAXNAMELEN];
  char *logFileName = NULL;
  char whichProc = 0;
  char *configSettings = NULL;
  FILE *inputFile = NULL;
  FILE *tempFile;
  int ch = 0;
  int pch = 0;
  int nch = 0;
  int charsRead = 0;
  int k;
  UserData *ud;

  int optc;
  set_program_name (argv[0]);

  while ((optc = getopt_long (argc, argv, "hvf:brptlC:", longopts, NULL)) != -1)
    switch (optc)
      {
      /* --help and --version exit immediately, per GNU coding standards.  */
      case 'v':
        version_etc (stdout, program_name, PACKAGE_NAME, VERSION, AUTHORS, (char *) NULL);
        exit (EXIT_SUCCESS);
        break;
      case 'h':
        print_help ();
        exit (EXIT_SUCCESS);
        break;
      case 'l':
	logFileName = "xml2brl.log";
	break;
      case 't':
	mode |= htmlDoc;
	break;
      case 'f':
	configFileName = optarg;
	break;
      case 'b':
      case 'p':
      case 'r':
      case 'x':
	whichProc = optc;
      break;
      case 'C':
	if (configSettings == NULL)
	  {
	    configSettings = malloc (BUFSIZE);
	    configSettings[0] = 0;
	  }
	strcat (configSettings, optarg);
	strcat (configSettings, "\n");
	break;
      default:
	fprintf (stderr, "Try `%s --help' for more information.\n",
		 program_name);
	exit (EXIT_FAILURE);
        break;
      }

  if (optind < argc)
    {
      if (optind == argc - 1)
	{
	  inputFileName = argv[optind];
	}
      else if (optind == argc - 2)
	{
	  if (strcmp (argv[optind], "-") != 0)
	    inputFileName = argv[optind];
	  outputFileName = argv[optind + 1];
	}
      else
	{
	  fprintf (stderr, "%s: extra operand: %s\n",
		   program_name, argv[optind + 2]);
	  fprintf (stderr, "Try `%s --help' for more information.\n",
		   program_name);
	  exit (EXIT_FAILURE);
	}
    }

  if (whichProc == 0)
    whichProc = 'x';
  if (configSettings != NULL)
    for (k = 0; configSettings[k]; k++)
      if (configSettings[k] == '=' && configSettings[k - 1] != ' ')
	configSettings[k] = ' ';
  if ((ud = lbx_initialize (configFileName, logFileName,
			    configSettings)) == NULL)
    exit (EXIT_FAILURE);
  if (strcmp (inputFileName, "stdin") != 0)
    {
      if (!(inputFile = fopen (inputFileName, "r")))
	{
	  lou_logPrint ("Can't open file %s.\n", inputFileName);
	  exit (EXIT_FAILURE);
	}
    }
  else
    inputFile = stdin;
  /*Create somewhat edited temporary file to facilitate use of stdin. */
  strcpy (tempFileName, ud->writeable_path);
  strcat (tempFileName, "xml2brl.temp");
  if (!(tempFile = fopen (tempFileName, "w")))
    {
      lou_logPrint ("Can't open temporary file.\n");
      exit (1);
    }
  if (whichProc == 'p')
    {
      int ppch = 0;
      int firstCh = 0;
      int skipit = 0;
      while ((ch = fgetc (inputFile)) != EOF)
	{
	  if (firstCh == 0)
	    firstCh = ch;
	  if (ch == 12 || ch == 13)
	    continue;
	  if (ch == '<' && firstCh == '<')
	    {
	      skipit = 1;
	      continue;
	    }
	  if (skipit)
	    {
	      if (ch == '>')
		skipit = 0;
	      continue;
	    }
	  if (ch == '-')
	    {
	      nch = fgetc (inputFile);
	      if (nch == 10)
		continue;
	      ungetc (nch, inputFile);
	    }
	  if (!((pch == 10 && ch == 10) || (ppch == 10 && pch == 10)))
	    {
	      if (ch <= 32 && pch <= 32)
		continue;
	      if (!
		  (pch == 10 && ((ppch >= 97 && ppch <= 122) || ppch == ',')))
		{
		  if (pch == 10 && ch < 97)
		    fputc (10, tempFile);
		}
	    }
	  ppch = pch;
	  pch = ch;
	  fputc (ch, tempFile);
	  charsRead++;
	}
    }
  else
    while ((ch = fgetc (inputFile)) != EOF)
      {
	if (charsRead == 0 && ch <= ' ')
	  continue;
	if (ch == 13)
	  continue;
	if (charsRead == 0)
	  {
	    if (ch != '<' && whichProc == 'x')
	      whichProc = 't';
	    nch = fgetc (inputFile);
	    if (!(mode & htmlDoc) && whichProc == 'x' && nch != '?')
	      fprintf (tempFile, "%s\n", ud->xml_header);
	  }
	if (pch == '>' && ch == '<')
	  fputc (10, tempFile);
	fputc (ch, tempFile);
	pch = ch;
	charsRead++;
	if (charsRead == 1)
	  {
	    fputc (nch, tempFile);
	    charsRead++;
	  }
      }
  fclose (tempFile);
  if (inputFile != stdin)
    fclose (inputFile);
  if (charsRead > 2)
    switch (whichProc)
      {
      case 'b':
	lbx_backTranslateFile (configFileName, tempFileName,
			       outputFileName, mode);
	break;
      case 'r':
	{
	  char temp2FileName[MAXNAMELEN];
	  strcpy (temp2FileName, ud->writeable_path);
	  strcat (temp2FileName, "xml2brl2.temp");
	  if ((lbx_backTranslateFile (configFileName, tempFileName,
				      temp2FileName, mode)) != 1)
	    exit (1);
	  if (ud->back_text == html)
	    lbx_translateFile (configFileName, temp2FileName,
			       outputFileName, mode);
	  else
	    lbx_translateTextFile (configFileName, temp2FileName,
				   outputFileName, mode);
	}
	break;
      case 't':
      case 'p':
	lbx_translateTextFile (configFileName, tempFileName,
			       outputFileName, mode);
	break;
      case 'x':
	lbx_translateFile (configFileName, tempFileName, outputFileName,
			   mode);
	break;
      default:
	lou_logPrint ("Program bug %c\n", whichProc);
	break;
      }
  lbx_free ();
  if (configSettings != NULL)
    free (configSettings);
  return 0;
}
