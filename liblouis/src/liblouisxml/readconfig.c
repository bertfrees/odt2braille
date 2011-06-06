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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <sys/types.h>
#include <sys/stat.h>
#include "louisxml.h"
#include "sem_names.h"

typedef struct
{
  const char *fileName;
#define configString fileName
  FILE *in;
  int stringPos;
  int lineNumber;
  char line[1024];
  char *action;
  int actionLength;
  char *value;
  int valueLength;
  char *value2;
  int value2Length;
}
FileInfo;
static int errorCount = 0;

static void
configureError (FileInfo * nested, char *format, ...)
{
  char buffer[1024];
  va_list arguments;
  va_start (arguments, format);
#ifdef _WIN32
  _vsnprintf (buffer, sizeof (buffer), format, arguments);
#else
  vsnprintf (buffer, sizeof (buffer), format, arguments);
#endif
  va_end (arguments);
  if (nested)
    lou_logPrint ("%s:%d: %s", nested->fileName, nested->lineNumber, buffer);
  else
    lou_logPrint ("%s", buffer);
  errorCount++;
}

int
file_exists (const char *completePath)
{
  struct stat statInfo;
  if (stat (completePath, &statInfo) != -1)
    return 1;
  return 0;
}

int
find_file (const char *fileList, char *filePath)
{
  struct stat statInfo;
  char trialPath[MAXNAMELEN];
  int commaPos;			/*for file lists */
  int listLength;
  int k;
  int currentListPos = 0;
  int pathLength;
  char pathEnd[2];
  pathEnd[0] = ud->file_separator;
  pathEnd[1] = 0;
  filePath[0] = 0;
  for (commaPos = 0; fileList[commaPos] && fileList[commaPos] != ',';
       commaPos++);
/*Process path list*/
  listLength = strlen (ud->path_list);
  for (k = 0; k < listLength; k++)
    if (ud->path_list[k] == ',')
      break;
  if (k == listLength)
    {				/* Only one path */
      strcpy (trialPath, ud->path_list);
      if (trialPath[strlen (trialPath) - 1] != ud->file_separator)
	strcat (trialPath, pathEnd);
      pathLength = strlen (trialPath);
      strncat (trialPath, (char *) fileList, commaPos);
      if (stat (trialPath, &statInfo) != -1)
	{
	  strcpy (&trialPath[pathLength], fileList);
	  strcpy (filePath, trialPath);
	  return 1;
	}
    }
  else
    {				/* Search a list of paths */
      strncpy (trialPath, ud->path_list, k);
      trialPath[k] = 0;
      if (trialPath[strlen (trialPath) - 1] != ud->file_separator)
	strcat (trialPath, pathEnd);
      pathLength = strlen (trialPath);
      strncat (trialPath, (char *) fileList, commaPos);
      if (stat (trialPath, &statInfo) != -1)
	{
	  strcpy (&trialPath[pathLength], fileList);
	  strcpy (filePath, trialPath);
	  return 1;
	}
      currentListPos = k + 1;
      while (currentListPos < listLength)
	{
	  for (k = currentListPos; k < listLength; k++)
	    if (ud->path_list[k] == ',')
	      break;
	  strncpy (trialPath, &ud->path_list[currentListPos],
		   k - currentListPos);
	  trialPath[k - currentListPos] = 0;
	  if (trialPath[strlen (trialPath) - 1] != ud->file_separator)
	    strcat (trialPath, pathEnd);
	  pathLength = strlen (trialPath);
	  strncat (trialPath, (char *) fileList, commaPos);
	  if (stat (trialPath, &statInfo) != -1)
	    {
	      strcpy (&trialPath[pathLength], fileList);
	      strcpy (filePath, trialPath);
	      return 1;
	    }
	  currentListPos = k + 1;
	}
    }
  return 0;
}

static int
findTable (FileInfo * nested, const char *tableName, char *tablePath)
{
  if (!find_file (tableName, tablePath))
    {
      configureError (nested, "Table %s cannot be found.", tableName);
      return 0;
    }
  return 1;
}

static int
controlCharValue (FileInfo * nested)
{
/*Decode centrol characters*/
  int k = 0;
  char decoded[100];
  int decodedLength = 0;
  while (k < nested->valueLength)
    {
      if (nested->value[k] == '~' || nested->value[k] == '^')
	{
	  decoded[decodedLength++] = (nested->value[k + 1] | 32) - 96;
	  k += 2;
	}
      else if (nested->value[k] == '\\')
	{
	  k++;
	  switch (nested->value[k] | 32)
	    {
	    case 'f':
	      decoded[decodedLength++] = '\f';
	      break;
	    case 'n':
	      decoded[decodedLength++] = '\n';
	      break;
	    case 'r':
	      decoded[decodedLength++] = '\r';
	      break;
	    default:
	      configureError (nested, "invalid value %s", nested->value);
	      return 0;
	    }
	  k++;
	}
      else
	decoded[decodedLength++] = nested->value[k++];
    }
  decoded[decodedLength] = 0;
  strcpy (nested->value, decoded);
  nested->valueLength = decodedLength;
  return 1;
}

static int compileConfig (FileInfo * nested);

int
config_compileSettings (const char *fileName)
{
/*Compile an input file or string */
  FileInfo nested;
  char completePath[MAXNAMELEN];
  if (!*fileName)
    return 1;			/*Probably run with defaults */
  nested.fileName = fileName;
  nested.lineNumber = 0;
  nested.stringPos = 1;
  if (nested.fileName[0] == ud->string_escape)
    return compileConfig (&nested);
  if (!find_file (fileName, completePath))
    {
      configureError (NULL, "Can't find configuration file %s", fileName);
      return 0;
    }
  if ((nested.in = fopen ((char *) completePath, "r")))
    {
      compileConfig (&nested);
      fclose (nested.in);
    }
  else
    {
      configureError (NULL, "Can't open configuration file %s", fileName);
      return 0;
    }
  return 1;
}

static int
getLine (FileInfo * nested)
{
  int lineLen = 0;
  int ch;
  if (nested->fileName[0] != ud->string_escape)
    {
      int pch = 0;
      while ((ch = fgetc (nested->in)) != EOF)
	{
	  if (ch == 13)
	    continue;
	  if (pch == '\\' && ch == 10)
	    {
	      lineLen--;
	      continue;
	    }
	  if (ch == 10 || lineLen > (sizeof (nested->line) - 2))
	    break;
	  nested->line[lineLen++] = ch;
	  pch = ch;
	}
      nested->line[lineLen] = 0;
      if (ch == EOF)
	return 0;
      return 1;
    }
  if (nested->configString[nested->stringPos] == 0)
    return 0;
  while ((ch = nested->configString[nested->stringPos]))
    {
      nested->line[lineLen++] = ch;
      nested->stringPos++;
      if (ch == 10 || ch == 13)
	break;
    }
  nested->line[lineLen] = 0;
  return 1;
}

static int
parseLine (FileInfo * nested)
{
  char *curchar = NULL;
  int ch = 0;
  while (getLine (nested))
    {
      nested->lineNumber++;
      curchar = nested->line;
      while ((ch = *curchar++) <= 32 && ch != 0);
      if (ch == 0 || ch == '#' || ch == '<')
	continue;
      nested->action = curchar - 1;
      while ((ch = *curchar++) > 32);
      nested->actionLength = curchar - nested->action - 1;
      nested->action[nested->actionLength] = 0;
      while ((ch = *curchar++) <= 32 && ch != 0);
      if (ch == 0)
	{
	  nested->value = NULL;
	  return 1;
	}
      else
	{
	  nested->value = curchar - 1;
	  if (*nested->value == 34)	/*quote */
	    {
	      nested->value++;
	      while (*curchar && *curchar != 34)
		curchar++;
	      nested->valueLength = curchar - nested->value;
	    }
	  else
	    {
	      while (*curchar++ > 32);
	      nested->valueLength = curchar - nested->value - 1;
	    }
	  nested->value[nested->valueLength] = 0;
	}
      while ((ch = *curchar++) <= 32 && ch != 0);
      if (ch != 0)
	{
	  nested->value2 = curchar - 1;
	  if (*nested->value2 == 34)	/*quote */
	    {
	      nested->value2++;
	      while (*curchar && *curchar != 34)
		curchar++;
	      nested->value2Length = curchar - nested->value2;
	    }
	  else
	    {
	      while (*curchar++ > 32);
	      nested->value2Length = curchar - nested->value2 - 1;
	    }
	  nested->value2[nested->value2Length] = 0;
	}
      else
	nested->value2 = NULL;
      return 1;
    }
  return 0;
}

#define NOTFOUND 1000
static int mainActionNumber = NOTFOUND;
static int subActionNumber;
static int entities = 0;

static int
ignoreCaseComp (const char *str1, const char *str2, int length)
{
/* Replaces strncasecmp, which some compilers don't support */
  int k;
  for (k = 0; k < length; k++)
    if ((str1[k] | 32) != (str2[k] | 32))
      break;
  if (k != length)
    return 1;
  return 0;
}

int
find_action (const char **actions, const char *action)
{
  int actionLength = strlen (action);
  int k;
  for (k = 0; actions[k]; k += 2)
    if (actionLength == strlen (actions[k])
	&& ignoreCaseComp (actions[k], action, actionLength) == 0)
      break;
  if (actions[k] == NULL)
    return -1;
  return atoi (actions[k + 1]);
}

static int
checkActions (FileInfo * nested, const char **actions)
{
  int actionNum = find_action (actions, nested->action);
  if (actionNum == -1)
    return NOTFOUND;
  return actionNum;
}

static int
checkValues (FileInfo * nested, const char **values)
{
  int k;
  for (k = 0; values[k]; k += 2)
    if (nested->valueLength == strlen (values[k]) &&
	ignoreCaseComp (values[k], nested->value, nested->valueLength) == 0)
      break;
  if (values[k] == NULL)
    {
      configureError (nested, "word %s in column 2 not recognized",
		      nested->value);
      return NOTFOUND;
    }
  return atoi (values[k + 1]);
}

static int
checkSubActions (FileInfo * nested, const char **mainActions, const char
		 **subActions)
{
  int subAction;
  mainActionNumber = NOTFOUND;
  subAction = checkActions (nested, subActions);
  if (subAction != NOTFOUND && nested->value == NULL)
    {
      configureError (nested, "column 2 is required");
      return NOTFOUND;
    }
  if (subAction == NOTFOUND)
    {
      mainActionNumber = checkActions (nested, mainActions);
      if (mainActionNumber == NOTFOUND)
	configureError (nested, "word %s in first column not recognized",
			nested->action);
      return NOTFOUND;
    }
  return (subActionNumber = subAction);
}

static int
compileConfig (FileInfo * nested)
{
  static const char *mainActions[] = {
    "outputFormat",
    "0",
    "translation",
    "0",
    "xml",
    "0",
    "cellsPerLine",
    "1",
    "linesPerPage",
    "2",
    "interpoint",
    "3",
    "lineEnd",
    "4",
    "pageEnd",
    "5",
    "beginningPageNumber",
    "6",
    "braillePages",
    "7",
    "paragraphs",
    "8",
    "fileEnd",
    "9",
    "printPages",
    "10",
    "printPageNumberAt",
    "11",
    "braillePageNumberAt",
    "12",
    "hyphenate",
    "13",
    "outputEncoding",
    "14",
    "encoding",
    "14",
    "backFormat",
    "15",
    "backLineLength",
    "16",
    "interline",
    "17",
    "contractedTable",
    "18",
    "literarytextTable",
    "18",
    "editTable",
    "19",
    "uncontractedTable",
    "20",
    "compbrlTable",
    "21",
    "mathtextTable",
    "22",
    "mathexprTable",
    "23",
    "interlineBackTable",
    "24",
    "xmlHeader",
    "25",
    "entity",
    "26",
    "internetAccess",
    "27",
    "semanticFiles",
    "28",
    "newEntries",
    "29",
    "include",
    "30",
    "formatFor",
    "31",
    "inputTextEncoding",
    "32",
    "contents",
    "33",
    "linefill",
    "34",
    "debug",
    "35",
    "pageSeparator",
    "36",
    "pageSeparatorNumber",
    "37",
    "ignoreEmptyPages",
    "38",
    "continuePages",
    "39",
    "mergeUnnumberedPages",
    "40",
    "pageNumberTopSeparateLine",
    "41",
    "pageNumberBottomSeparateLine",
    "42",
    "printPageNumberRange",
    "43",
    "printPageNumbersInContents",
    "44",
    "braillePageNumbersInContents",
    "45",
    "minSyllableLength",
    "46",
    "style",
    "90",
    NULL
  };
  static const char *yesNo[] = {
    "no", "0", "yes", "1", NULL
  };
  static const char *topBottom[] = {
    "bottom", "0", "top", "1", NULL
  };
  static const char *encodings[] = {
    "utf8", "0", "utf16", "1", "utf32", "2", "ascii8", "3", NULL
  };
  static const char *backFormats[] = {
    "plain", "0", "html", "1", NULL
  };

  static const char *formatFor[] = {
    "textDevice", "0", "browser", "1", NULL
  };

  int k;
  while (parseLine (nested))
    {
      mainActionNumber = checkActions (nested, mainActions);
      if (mainActionNumber == NOTFOUND)
	{
	  configureError (nested,
			  "word %s in first column not recognized",
			  nested->action);
	  return 0;
	}
    choseMainAction:
      switch (mainActionNumber)
	{
	case NOTFOUND:
	case 0:
	  break;
	case 1:
	  ud->cells_per_line = atoi (nested->value);
	  break;
	case 2:
	  ud->lines_per_page = atoi (nested->value);
	  break;
	case 3:
	  if ((k = checkValues (nested, yesNo)) != NOTFOUND)
	    ud->interpoint = k;
	  break;
	case 4:
	  if (controlCharValue (nested))
	    memcpy (ud->lineEnd, nested->value, nested->valueLength + 1);
	  break;
	case 5:
	  if (controlCharValue (nested))

	    memcpy (ud->pageEnd, nested->value, nested->valueLength + 1);
	  break;
	case 6:
	  ud->beginning_braille_page_number = atoi (nested->value);
	  break;
	case 7:
	  if ((k = checkValues (nested, yesNo)) != NOTFOUND)
	    ud->braille_pages = k;
	  break;
	case 8:
	  if ((k = checkValues (nested, yesNo)) != NOTFOUND)
	    ud->paragraphs = k;
	  break;
	case 9:
	  if (controlCharValue (nested))
	    memcpy (ud->fileEnd, nested->value, nested->valueLength + 1);
	  break;
	case 10:
	  if ((k = checkValues (nested, yesNo)) != NOTFOUND)
	    ud->print_pages = k;
	  break;
	case 11:
	  if ((k = checkValues (nested, topBottom)) != NOTFOUND)
	    ud->print_page_number_at = k;
	  break;
	case 12:
	  if ((k = checkValues (nested, topBottom)) != NOTFOUND)
	    {
	      if (k)
		k = 0;
	      else
		k = 1;

	    ud->braille_page_number_at = k;
	    }
	  break;
	case 13:
	  if ((k = checkValues (nested, yesNo)) != NOTFOUND)
	    ud->hyphenate = k;
	  break;
	case 14:
	  if ((k = checkValues (nested, encodings)) != NOTFOUND)
	    ud->output_encoding = k;
	  break;
	case 15:
	  if ((k = checkValues (nested, backFormats)) != NOTFOUND)
	    ud->back_text = k;
	  break;
	case 16:
	  ud->back_line_length = atoi (nested->value);
	  break;
	case 17:
	  if ((k = checkValues (nested, yesNo)) != NOTFOUND)
	    ud->interline = k;
	  break;
	case 18:
	  findTable (nested, nested->value, ud->contracted_table_name);
	  break;
	case 19:
	  findTable (nested, nested->value, ud->edit_table_name);
	  break;
	case 20:
	  findTable (nested, nested->value, ud->uncontracted_table_name);
	  break;
	case 21:
	  findTable (nested, nested->value, ud->compbrl_table_name);
	  break;
	case 22:
	  findTable (nested, nested->value, ud->mathtext_table_name);
	  break;
	case 23:
	  findTable (nested, nested->value, ud->mathexpr_table_name);
	  break;
	case 24:
	  findTable (nested, nested->value, ud->interline_back_table_name);
	  break;
	case 25:
	  if (entities)
	    {
	      configureError
		(nested,
		 "The header definition must precede all entity definitions.");
	      break;
	    }
	  strcpy (ud->xml_header, nested->value);
	  break;
	case 26:
	  if (!entities)
	    strcat (ud->xml_header, "<!DOCTYPE entities [\n");
	  entities = 1;
	  strcat (ud->xml_header, "<!ENTITY ");
	  strcat (ud->xml_header, nested->value);
	  strcat (ud->xml_header, " \"");
	  strcat (ud->xml_header, nested->value2);
	  strcat (ud->xml_header, "\">\n");
	  break;
	case 27:
	  if ((k = checkValues (nested, yesNo)) != NOTFOUND)
	    ud->internet_access = k;
	  break;
	case 28:
	  strcpy (ud->semantic_files, nested->value);
	  break;
	case 29:
	  if ((k = checkValues (nested, yesNo)) != NOTFOUND)
	    ud->new_entries = k;
	  break;
	case 30:
	  {
	    static const char *actions[] = {
	      NULL
	    };
	    if (nested->value == NULL)
	      configureError (nested, "a file name in column 2 is required");
	    else
	      config_compileSettings (nested->value);
	    parseLine (nested);
	    checkSubActions (nested, mainActions, actions);
	    if (mainActionNumber != NOTFOUND)
	      goto choseMainAction;
	  }
	  break;
	case 31:
	  if ((k = checkValues (nested, formatFor)) != NOTFOUND)
	    ud->format_for = k;
	  break;
	case 32:
	  if ((k = checkValues (nested, encodings)) != NOTFOUND)
	    ud->input_text_encoding = k;
	  break;
	case 33:
	  if ((k = checkValues (nested, yesNo)) != NOTFOUND)
	    ud->contents = k;
	  break;
	case 34:
	  if (nested->value == NULL)
	    ud->line_fill = ' ';
	  else
	    ud->line_fill = nested->value[0];
	  break;
	case 35:
	  if ((k = checkValues (nested, yesNo)) != NOTFOUND)
	    ud->debug = k;
	  break;
	case 36:
	  if ((k = checkValues (nested, yesNo)) != NOTFOUND)
	    {
	      ud->page_separator = k;
	    }
	  break;
	case 37:
	  if ((k = checkValues (nested, yesNo)) != NOTFOUND)
	    {
	      ud->page_separator_number = k;
	    }
	  break;
	case 38:
	  if ((k = checkValues (nested, yesNo)) != NOTFOUND)
	    {
	      ud->ignore_empty_pages = k;
	    }
	  break;
	case 39:
	  if ((k = checkValues (nested, yesNo)) != NOTFOUND)
	    {
	      ud->continue_pages = k;
	    }
	  break;
	case 40:
	  if ((k = checkValues (nested, yesNo)) != NOTFOUND)
	    {
	      ud->merge_unnumbered_pages = k;
	    }
	  break;
	case 41:
	  if ((k = checkValues (nested, yesNo)) != NOTFOUND)
	    {
	      ud->page_number_top_separate_line = k;
	    }
	  break;
	case 42:
	  if ((k = checkValues (nested, yesNo)) != NOTFOUND)
	    {
	      ud->page_number_bottom_separate_line = k;
	    }
	  break;
	case 43:
	  if ((k = checkValues (nested, yesNo)) != NOTFOUND)
	    {
	      ud->print_page_number_range = k;
	    }
	  break;
    case 44:
	  if ((k = checkValues (nested, yesNo)) != NOTFOUND)
		ud->print_page_numbers_in_contents = k;
	  break;
    case 45:
	  if ((k = checkValues (nested, yesNo)) != NOTFOUND)
		ud->braille_page_numbers_in_contents = k;
	  break;
    case 46:
	  ud->min_syllable_length = atoi (nested->value);
	  break;
	case 90:
	  {
	    static const char *actions[] = {
	      "linesBefore",
	      "0",
	      "linesAfter",
	      "1",
	      "leftMargin",
	      "2",
	      "firstLineIndent",
	      "3",
	      "translate",
	      "6",
	      "skipNumberLines",
	      "7",
	      "format",
	      "8",
	      "newPageBefore",
	      "9",
	      "newPageAfter",
	      "10",
	      "rightHandPage",
	      "11",
		  "braillePageNumberFormat",
		  "12",
          "centeredMargin",
          "13",
          "keepWithNext",
          "14",
          "dontSplit",
          "15",
          "orphanControl",
          "16",
          "widowControl",
          "17",
          "linesFromTop",
          "18",
	      NULL
	    };
	    static const char *formats[] = {
	      "leftJustified",
	      "0",
	      "rightJustified",
	      "1",
	      "centered",
	      "2",
	      "alignColumnsLeft",
	      "3",
	      "alignColumnsRight",
	      "4",
	      "listColumns",
	      "5",
	      "listLines",
	      "6", "computerCoded", "7", "contents", "8", NULL
	    };
	    static const char *pageNumFormats[] = {
	      "normal", "0", "blank", "1", "p", "2", "roman", "3", "romancaps", "4", NULL
	    };
	    StyleType *style;
	    sem_act styleAction;
	    if (nested->value == NULL)
	      {
		configureError (nested,
				"no style name given in second column");
		break;
	      }
	    styleAction = find_semantic_number (nested->value);
	    style = new_style (nested->value);
	    style->action = styleAction;
	    while (parseLine (nested))
	      {
		checkSubActions (nested, mainActions, actions);
		if (mainActionNumber != NOTFOUND)
		  goto choseMainAction;
		switch (subActionNumber)
		  {
		  case NOTFOUND:
		    break;
		  case 0:
		    style->lines_before = atoi (nested->value);
		    break;
		  case 1:
		    style->lines_after = atoi (nested->value);
		    break;
		  case 2:
		    style->left_margin = atoi (nested->value);
		    break;
		  case 3:
		    style->first_line_indent = atoi (nested->value);
		    break;
		  case 6:
		    switch ((k = find_semantic_number (nested->value)))
		      {
		      case contracted:
		      case uncontracted:
		      case compbrl:
			style->translate = k;
			break;
		      default:
			configureError (nested, "no such translation");
			break;
		      }
		    break;
		  case 7:
		    if ((k = checkValues (nested, yesNo)) != NOTFOUND)
		      style->skip_number_lines = k;
		    break;
		  case 8:
		    if ((k = checkValues (nested, formats)) != NOTFOUND)
		      style->format = k;
		    break;
		  case 9:
		    if ((k = checkValues (nested, yesNo)) != NOTFOUND)
		      style->newpage_before = k;
		    break;
		  case 10:
		    if ((k = checkValues (nested, yesNo)) != NOTFOUND)
		      style->newpage_after = k;
		    break;
		  case 11:
		    if ((k = checkValues (nested, yesNo)) != NOTFOUND)
		      style->righthand_page = k;
		    break;
		  case 12:
		    if ((k = checkValues (nested, pageNumFormats)) !=
			NOTFOUND)
		      style->brlNumFormat = k;
		    break;
          case 13:
            style->centered_margin = atoi (nested->value);
            break;
          case 14:
            if ((k = checkValues (nested, yesNo)) != NOTFOUND)
		      style->keep_with_next = k;
		    break;
          case 15:
            if ((k = checkValues (nested, yesNo)) != NOTFOUND)
		      style->dont_split = k;
		    break;
          case 16:
            style->orphan_control = atoi (nested->value);
            break;
          case 17:
            style->widow_control = atoi (nested->value);
            break;
          case 18:
            style->lines_from_top = atoi (nested->value);
            break;
		  default:
		    configureError (nested, "Program error in readconfig.c");
		    continue;
		  }
	      }
	    break;

	  }
	default:
	  configureError (nested, "Program error in readconfig.c");
	  continue;
	}
    }
  return 1;
}

static const char const *logFileNamex = NULL;

static int
initConfigFiles (const char *firstConfigFile, char *fileName)
{
  char configPath[MAXNAMELEN];
  int k;
  strcpy (configPath, firstConfigFile);
  for (k = strlen (configPath); k >= 0; k--)
    if (configPath[k] == ud->file_separator)
      break;
  strcpy (fileName, &configPath[k + 1]);
  if (k < 0)
    k++;
  configPath[k] = 0;
  set_paths (configPath);
  if (logFileNamex)
    {
      strcpy (ud->contracted_table_name, ud->writeable_path);
      strcat (ud->contracted_table_name, logFileNamex);
      lou_logFile (ud->contracted_table_name);
    }
  if (!config_compileSettings ("canonical.cfg"))
    return 0;
  return 1;
}

int
read_configuration_file (const char *configFileList, const char
			 *logFileName,
			 const char *configString, unsigned int mode)
{
/* read the configuration file and perform other initialization*/
  int k;
  char mainFile[MAXNAMELEN];
  char subFile[MAXNAMELEN];
  int listLength;
  int currentListPos = 0;
  errorCount = 0;
  logFileNamex = logFileName;
  if (mode & dontInit)
    {
      ud->has_comp_code = 0;
      ud->has_math = 0;
      ud->has_chem = 0;
      ud->has_graphics = 0;
      ud->has_music = 0;
      ud->has_cdata = 0;
      ud->has_contentsheader = 0;
      ud->prelim_pages = 0;
      ud->braille_page_string[0] = 0;
      ud->print_page_number[0] = '_';
      ud->inFile = NULL;
      ud->outFile = NULL;
      ud->mainBrailleTable = ud->contracted_table_name;
      ud->outbuf1_len_so_far = 0;
	  ud->outbuf2_len_so_far = 0;
      ud->outbuf3_len_so_far = 0;
	  ud->outbuf2_enabled = ud->braille_pages &&
		ud->print_pages &&
		ud->print_page_number_range &&
		ud->print_page_number_at;
      ud->outbuf3_enabled = 0;
	  ud->fill_pages = 0;
      ud->fill_page_skipped = 0;
      ud->blank_lines = 0;
      ud->lines_length = 0;
      ud->new_print_page = 0;
      ud->lines_on_page = 0;
      ud->braille_page_number = ud->beginning_braille_page_number;
      ud->print_page_number_first[0] = '_';
      ud->print_page_number[1] = 0;
      ud->print_page_number_first[1] = 0;
      ud->print_page_number_last[0] = 0;
      ud->page_separator_number_first[0] = 0;
      ud->page_separator_number_first[0] = 0;
      return 1;
    }
  lbx_free ();
  if (!(ud = malloc (sizeof (UserData))))
    return 0;
  memset (ud, 0, sizeof (UserData));
  entities = 0;
  ud->mode = mode;
  ud->top = -1;
  ud->style_top = -1;
  for (k = document; k < notranslate; k++)
    {
      StyleType *style = new_style ((xmlChar *) semNames[k]);
      style->action = k;
    }
  ud->input_encoding = utf8;
  ud->output_encoding = ascii8;
  *ud->print_page_number = '_';
  ud->string_escape = ',';
#ifdef _WIN32
  ud->file_separator = '\\';
#else
  ud->file_separator = '/';
#endif
/*Process file list*/
  listLength = strlen (configFileList);
  for (k = 0; k < listLength; k++)
    if (configFileList[k] == ',')
      break;
  if (k == listLength || k == 0)
    {				/* Only one file */
      initConfigFiles (configFileList, mainFile);
      config_compileSettings (mainFile);
    }
  else
    {				/* Compile a list of files */
      strncpy (subFile, configFileList, k);
      subFile[k] = 0;
      initConfigFiles (subFile, mainFile);
      currentListPos = k + 1;
      config_compileSettings (mainFile);
      while (currentListPos < listLength)
	{
	  for (k = currentListPos; k < listLength; k++)
	    if (configFileList[k] == ',')
	      break;
	  strncpy (subFile,
		   &configFileList[currentListPos], k - currentListPos);
	  subFile[k - currentListPos] = 0;
	  config_compileSettings (subFile);
	  currentListPos = k + 1;
	}
    }

/* Process configString */
  if (configString != NULL)
    {
      if (configString[0] == ud->string_escape)
	config_compileSettings (configString);
      else
	{
	  k = 0;
	  ud->typeform[k++] = ud->string_escape;
	  ud->typeform[k] = 0;
	  strcat ((char *) ud->typeform, configString);
	  config_compileSettings ((char *) ud->typeform);
	  memset (ud->typeform, 0, sizeof (ud->typeform));
	}
    }
  ud->braille_page_number = ud->beginning_braille_page_number;
  if (entities)
    strcat (ud->xml_header, "]>\n");
  return 1;
}
