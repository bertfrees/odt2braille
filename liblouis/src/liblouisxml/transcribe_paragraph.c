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

/**** Added by Bert Frees *****************************************/

static int           saved_text_length;
static int           saved_translated_length;
static int           saved_outlen_so_far;
static int           saved_buffer2_len_so_far;
static int           saved_running_head_length;
static int           saved_footer_length;
static int           saved_braille_page_number;
static int           saved_brl_page_num_format;
static int           saved_cur_brl_page_num_format;
static int           saved_lines_on_page;
static int           saved_line_spacing;
static int           saved_blank_lines;
static int           saved_fill_pages;
static int           saved_fill_page_skipped;
static char          saved_soft_hyphens[2 * BUFSIZE];
static widechar      saved_text_buffer[2 * BUFSIZE];
static widechar      saved_translated_buffer[2 * BUFSIZE];
static widechar      saved_outbuf[2 * BUFSIZE];
static widechar      saved_buffer2[2 * BUFSIZE];
static widechar      saved_page_separator_number_first[MAXNUMLEN];
static widechar      saved_page_separator_number_last[MAXNUMLEN];
static widechar      saved_print_page_number_first[MAXNUMLEN];
static widechar      saved_print_page_number_last[MAXNUMLEN];
static widechar      saved_print_page_number[MAXNUMLEN];
static widechar      saved_braille_page_string[MAXNUMLEN];
static widechar      saved_running_head[MAXNAMELEN / 2];
static widechar      saved_footer[MAXNAMELEN / 2];
static unsigned char saved_typeform[2 * BUFSIZE];

static int saveData(void) {

    saved_text_length             = ud->text_length;
    saved_translated_length       = ud->translated_length;
    saved_outlen_so_far           = ud->outlen_so_far;
    saved_buffer2_len_so_far      = ud->buffer2_len_so_far;
    saved_running_head_length     = ud->running_head_length;
    saved_footer_length           = ud->footer_length;
    saved_braille_page_number     = ud->braille_page_number;
    saved_brl_page_num_format     = ud->brl_page_num_format;
    saved_cur_brl_page_num_format = ud->cur_brl_page_num_format;
    saved_lines_on_page           = ud->lines_on_page;
    saved_line_spacing            = ud->line_spacing;
    saved_blank_lines             = ud->blank_lines;
    saved_fill_pages              = ud->fill_pages;
    saved_fill_page_skipped       = ud->fill_page_skipped;

    /*text_buffer
    translated_buffer
    outbuf
    buffer2
    page_separator_number_first
    page_separator_number_last
    print_page_number_first
    print_page_number_last
    print_page_number
    braille_page_string
    running_head
    footer
    typeform
    soft_hyphens

    savePointers();*/

}

static int restoreData(void) {

    ud->text_length             = saved_text_length;
    ud->translated_length       = saved_translated_length;
    ud->outlen_so_far           = saved_outlen_so_far;
    ud->buffer2_len_so_far      = saved_buffer2_len_so_far;
    ud->running_head_length     = saved_running_head_length;
    ud->footer_length           = saved_footer_length;
    ud->braille_page_number     = saved_braille_page_number;
    ud->brl_page_num_format     = saved_brl_page_num_format;
    ud->cur_brl_page_num_format = saved_cur_brl_page_num_format;
    ud->lines_on_page           = saved_lines_on_page;
    ud->line_spacing            = saved_line_spacing;
    ud->blank_lines             = saved_blank_lines;
    ud->fill_pages              = saved_fill_pages;
    ud->fill_page_skipped       = saved_fill_page_skipped;

    /*text_buffer
    translated_buffer
    outbuf
    buffer2
    page_separator_number_first
    page_separator_number_last
    print_page_number_first
    print_page_number_last
    print_page_number
    braille_page_string
    running_head
    footer
    typeform
    soft_hyphens

    restorePointers();*/

}
/******************************************************************/

int
transcribe_paragraph (xmlNode * node, int action)
{
  StyleType *style;
  xmlNode *child;
  int branchCount = 0;
  if (node == NULL)
    return 0;
  if (ud->top == 0)
    action = 1;
  if (action != 0)
    push_sem_stack (node);
  switch (ud->stack[ud->top])
    {
    case no:
      if (ud->text_length > 0 && ud->text_length < MAX_LENGTH &&
	  ud->text_buffer[ud->text_length - 1] > 32)
	ud->text_buffer[ud->text_length++] = 32;
      break;
    case skip:
      if (action != 0)
	pop_sem_stack ();
      return 0;
    case configtweak:
      do_configstring (node);
      break;
    case htmllink:
      if (ud->format_for != browser)
	break;
      insert_linkOrTarget (node, 0);
      if (action != 0)
	pop_sem_stack ();
      return 1;
    case htmltarget:
      if (ud->format_for != browser)
	break;
      insert_linkOrTarget (node, 1);
      if (action != 0)
	pop_sem_stack ();
      return 1;
    case boxline:
      do_boxline (node);
      if (action != 0)
	pop_sem_stack ();
      return 1;
    case blankline:
      do_blankline ();
      if (action != 0)
	pop_sem_stack ();
      return 1;
    case linespacing:
      do_linespacing;
      if (action != 0)
	pop_sem_stack ();
      return 1;
    case softreturn:
      do_softreturn ();
      if (action != 0)
	pop_sem_stack ();
      return 1;
    case righthandpage:
      do_righthandpage ();
      if (action != 0)
	pop_sem_stack ();
      return 1;
    case code:
      transcribe_computerCode (node, 0);
      if (action != 0)
	pop_sem_stack ();
      return 1;
    case math:
      transcribe_math (node, 0);
      if (action != 0)
	pop_sem_stack ();
      return 1;
    case graphic:
      transcribe_graphic (node, 0);
      if (action != 0)
	pop_sem_stack ();
      return 1;
    case chemistry:
      transcribe_chemistry (node, 0);
      if (action != 0)
	pop_sem_stack ();
      return 1;
    case music:
      transcribe_music (node, 0);
      if (action != 0)
	pop_sem_stack ();
      return 1;
    case changetable:
      change_table (node);
      return 1;

/**** Added by Bert Frees *****************************************/
    case pagenum:
      do_pagenum();
      break;
/******************************************************************/

    default:
      break;
    }

/**** Removed by Bert Frees ***************************************
  if ((style = is_style (node)) != NULL)
    start_style (style);
/**** Added by Bert Frees *****************************************/

  int done = 0;
  style = is_style(node);
  while (!done) {
    if (style) {
      if (ud->braille_pages && ud->lines_on_page > 0 && !ud->buffer3_enabled) {
        if (ud->check_dont_split < 0) {
          if (style->dont_split) {
            saveData();
            ud->buffer3_enabled = 1;
            ud->check_dont_split = 1;
          }
        }
      }
      start_style (style);
    }
/******************************************************************/

  child = node->children;
  while (child)
    {
      insert_code (node, branchCount);
      branchCount++;
      switch (child->type)
	{
	case XML_ELEMENT_NODE:
	  transcribe_paragraph (child, 1);
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
  insert_code (node, branchCount);
  insert_code (node, -1);

/**** Removed by Bert Frees ***************************************
  if (style)
    end_style (style);
/**** Added by Bert Frees *****************************************/

  done = 1;
  if (style) {
    end_style (style);
    if (ud->braille_pages && ud->buffer3_enabled) {

      if (ud->check_dont_split < 0) {
      } else if (ud->check_dont_split == 0) {
        done = 0;
        restoreData();
        ud->buffer3_len_so_far = 0;
        ud->buffer3_enabled = 0;
        do_newpage();
      } else {
        writeBuffer(3, 0);
        ud->buffer3_enabled = 0;
      }

    /*switch(ud->check_keep_with_next) {
      case 0:

        break;
      case 1:

       break;
      case -1:
      default:
    }

    switch(ud->check_widow_control) {
      case 0:

        break;
      case 1:

       break;
      case -1:
      default:
    }

    switch(ud->check_orphan_control) {
      case 0:

        break;
      case 1:

       break;
      case -1:
      default:
    }*/

      ud->check_dont_split = -1;
      ud->check_keep_with_next = -1;
      ud->check_widow_control = -1;
      ud->check_orphan_control = -1;
   }
 }
/******************************************************************/

  else
    switch (ud->stack[ud->top])
      {
      case runninghead:
	insert_translation (ud->mainBrailleTable);
	if (ud->translated_length > (ud->cells_per_line - 9))
	  ud->running_head_length = ud->cells_per_line - 9;
	else
	  ud->running_head_length = ud->translated_length;
	memcpy (&ud->running_head[0], &ud->translated_buffer[0],
		ud->running_head_length * CHARSIZE);
	break;
      case footer:
	insert_translation (ud->mainBrailleTable);
	if (ud->translated_length > (ud->cells_per_line - 9))
	  ud->footer_length = ud->cells_per_line - 9;
	else
	  ud->footer_length = ud->translated_length;
	memcpy (&ud->footer[0], &ud->translated_buffer[0],
		ud->footer_length * CHARSIZE);
	break;
      default:
	break;
      }

/**** Added by Bert Frees *****************************************/
  }
/******************************************************************/

  if (action != 0)
    pop_sem_stack ();
  else
    {
      insert_translation (ud->mainBrailleTable);
      write_paragraph (para);
    }
  return 1;
}
