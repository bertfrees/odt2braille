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

int
transcribe_document (xmlNode * node)
{
  StyleType *style;
  xmlNode *child;
  ud->top = -1;
  ud->style_top = -1;
  start_document ();
  push_sem_stack (node);
  switch (ud->stack[ud->top])
    {
    case no:
      if (ud->text_length > 0 && ud->text_length < MAX_LENGTH &&
	  ud->text_buffer[ud->text_length - 1] > 32)
	ud->text_buffer[ud->text_length++] = 32;
      break;
    case skip:
      pop_sem_stack ();
      return 0;
    case htmllink:
      if (ud->format_for != browser)
	break;
      insert_linkOrTarget (node, 0);
      pop_sem_stack ();
      return 1;
    case htmltarget:
      if (ud->format_for != browser)
	break;
      insert_linkOrTarget (node, 1);
      pop_sem_stack ();
      return 1;
    case code:
      transcribe_computerCode (node, 0);
      write_paragraph (para);
      pop_sem_stack ();
      return 1;
    case changetable:
      change_table (node);
      write_paragraph (para);
      return 1;
    case math:
      transcribe_math (node, 0);
      write_paragraph (para);
      pop_sem_stack ();
      return 1;
    case graphic:
      transcribe_graphic (node, 0);
      write_paragraph (para);
      pop_sem_stack ();
      return 1;
    case chemistry:
      transcribe_chemistry (node, 0);
      write_paragraph (para);
      pop_sem_stack ();
      return 1;
    case music:
      transcribe_music (node, 0);
      write_paragraph (para);
      pop_sem_stack ();
      return 1;
    case para:
      transcribe_paragraph (node, 0);
      pop_sem_stack ();
      return 1;
    default:
      break;
    }
  if ((style = is_style (node)) != NULL)
    start_style (style);
  child = node->children;
  while (child)
    {
      switch (child->type)
	{
	case XML_ELEMENT_NODE:
	  transcribe_paragraph (child, 0);
	  break;
	case XML_TEXT_NODE:
	  insert_text (child);
	  break;
	case XML_CDATA_SECTION_NODE:
	  transcribe_cdataSection (child);
	  break;
	default:
	  break;
	}
      child = child->next;
    }
  if (style)
    end_style (style);
  end_document ();
  pop_sem_stack ();
  return 1;
}
