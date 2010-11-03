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
#include <string.h>
#include "louisxml.h"

static void mathTrans (void);
static void mathText (xmlNode * node, int action);

int
transcribe_math (xmlNode * node, int action)
{
  StyleType *style;
  xmlNode *child;
  int branchCount = 0;
  if (action == 0)
    {
      insert_translation (ud->mainBrailleTable);
    }
  push_sem_stack (node);
  switch (ud->stack[ud->top])
    {
    case skip:
      pop_sem_stack ();
      return 1;
    case reverse:
      do_reverse (node);
      break;
    case math:
      break;
    default:
      break;
    }
  if ((style = is_style (node)) != NULL)
    start_style (style);
  child = node->children;
  while (child)
    {
      insert_code (node, branchCount);
      branchCount++;
      switch (child->type)
	{
	case XML_ELEMENT_NODE:
	  transcribe_math (child, 1);
	  break;
	case XML_TEXT_NODE:
	  mathText (child, 1);
	  break;
	case XML_CDATA_SECTION_NODE:
	  transcribe_cdataSection (child);
	  break;
	default:
	  break;
	}
      child = child->next;
    }
  insert_code (node, branchCount);
  insert_code (node, -1);
  if (style)
    {
      mathTrans ();
      end_style (style);
    }
  pop_sem_stack ();
  if (action == 0)
    mathTrans ();
  return 1;
}

static void
mathTrans (void)
{
  insert_translation (ud->mathexpr_table_name);
}

static void
mathText (xmlNode * node, int action)
{
  if (ud->stack[ud->top] == mtext)
    {
      mathTrans ();
      insert_text (node);
    }
  else
    insert_utf8 (node->content);
}
