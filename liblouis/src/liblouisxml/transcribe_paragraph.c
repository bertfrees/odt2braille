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

static int           dont_split = 0;
static int           dont_split_status = 0;
static int           keep_with_next = 0;
static int           keep_with_previous = 0;
static int           keep_with_previous_pos = 0;
static int           keep_with_previous_status = 0;
static int           widow_control = 0;
static int           widow_control_pos = 0;
static int           widow_control_status = 0;
static int           orphan_control = 0;

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

static int saveState(void) {

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

    widecharcpy(saved_text_buffer,                 ud->text_buffer,         saved_text_length);
    widecharcpy(saved_translated_buffer,           ud->translated_buffer,   saved_translated_length);
    widecharcpy(saved_outbuf,                      ud->outbuf,              saved_outlen_so_far);
    widecharcpy(saved_buffer2,                     ud->buffer2,             saved_buffer2_len_so_far);
    widecharcpy(saved_running_head,                ud->running_head,        saved_running_head_length);
    widecharcpy(saved_footer,                      ud->footer,              saved_footer_length);
    unsignedcharcpy(saved_typeform,                ud->typeform,            saved_text_length);
    charcpy(saved_soft_hyphens,                    ud->soft_hyphens,        saved_translated_length);

    widestrcpy(saved_page_separator_number_first,  ud->page_separator_number_first);
    widestrcpy(saved_page_separator_number_last,   ud->page_separator_number_last);
    widestrcpy(saved_print_page_number_first,      ud->print_page_number_first);
    widestrcpy(saved_print_page_number_last,       ud->print_page_number_last);
    widestrcpy(saved_print_page_number,            ud->print_page_number);
    widestrcpy(saved_braille_page_string,          ud->braille_page_string);

    savePointers();

}

static int restoreState(void) {

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

    widecharcpy(ud->text_buffer,                saved_text_buffer,         saved_text_length);
    widecharcpy(ud->translated_buffer,          saved_translated_buffer,   saved_translated_length);
    widecharcpy(ud->outbuf,                     saved_outbuf,              saved_outlen_so_far);
    widecharcpy(ud->buffer2,                    saved_buffer2,             saved_buffer2_len_so_far);
    widecharcpy(ud->running_head,               saved_running_head,        saved_running_head_length);
    widecharcpy(ud->footer,                     saved_footer,              saved_footer_length);
    unsignedcharcpy(ud->typeform,               saved_typeform,            saved_text_length);
    charcpy(ud->soft_hyphens,                   saved_soft_hyphens,        saved_translated_length);

    widestrcpy(ud->page_separator_number_first, saved_page_separator_number_first);
    widestrcpy(ud->page_separator_number_last,  saved_page_separator_number_last);
    widestrcpy(ud->print_page_number_first,     saved_print_page_number_first);
    widestrcpy(ud->print_page_number_last,      saved_print_page_number_last);
    widestrcpy(ud->print_page_number,           saved_print_page_number);
    widestrcpy(ud->braille_page_string,         saved_braille_page_string);

    restorePointers();

}
/******************************************************************/

int
transcribe_paragraph (xmlNode * node, int action)
{
/**** Added by Bert Frees *****************************************/

  xmlNode* saved_child;
  int saved_branchCount = 0;
  int state_saved = 0;
  StyleType* style_this;
  int dont_split_this = 0;
  int keep_with_next_this = 0;
  int keep_with_previous_this = 0;
  int widow_control_this = 0;
  int orphan_control_this = 0;

/******************************************************************/

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
  if ((style = is_style (node)) != NULL)
    start_style (style);
  child = node->children;

/**** Removed by Bert Frees ***************************************
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
/**** Added by Bert Frees *****************************************/

  while (child) {
    insert_code (node, branchCount);
    branchCount++;

    dont_split_this = 0;
    widow_control_this = 0;
    orphan_control_this = 0;
    keep_with_previous_this = 0;

    switch (child->type) {
	case XML_ELEMENT_NODE:

	  style_this = is_style(child);
	  if (style_this && ud->braille_pages) {

        if (keep_with_next_this && ud->lines_length > 0) {
          keep_with_previous_this = 1;
        }
        keep_with_next_this = 0;
        if (!dont_split) {
          if (ud->lines_on_page > 0 && !((keep_with_previous || keep_with_previous_this) && !ud->buffer3_enabled)) {
            if (style_this->dont_split || style_this->keep_with_next) {
              dont_split_this = 1;
            } else if (style_this->widow_control > 1 ) {
              widow_control_this = style_this->widow_control;
            }
          }
          keep_with_next_this = style_this->keep_with_next;
        }
        if (dont_split_this || widow_control_this) {
          if (!ud->buffer3_enabled) {
            saved_child = child;
            saved_branchCount = branchCount-1;
            saveState();
            state_saved = 1;
            ud->buffer3_enabled = 1;
            ud->lines_length = 0;
          }
        }
        if (dont_split_this)         { dont_split             = dont_split_this; }
        if (keep_with_next_this)     { keep_with_next         = keep_with_next_this; }
        if (keep_with_previous_this) { keep_with_previous     = keep_with_previous_this;
                                       keep_with_previous_pos = ud->lines_length; }
        if (widow_control_this)      { widow_control          = widow_control_this;
                                       widow_control_pos      = ud->lines_length; }
      }

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

    if (ud->buffer3_enabled) {

      if (dont_split_status >= 0 &&
          keep_with_previous_status >= 0 &&
          widow_control_status >=0) {

        if (keep_with_previous) {
          if (keep_with_previous_this) {
            keep_with_previous_status = 1;
          } else if (keep_with_previous_pos < ud->lines_length) {
            keep_with_previous_status = 1;
          } else {
            keep_with_previous_status = 0;
          }
          if (keep_with_previous_pos < ud->lines_length &&
              ud->lines_pagenum[keep_with_previous_pos] > ud->lines_pagenum[keep_with_previous_pos-1] &&
              !ud->lines_newpage[keep_with_previous_pos]) {
            keep_with_previous_status = -1;
          }
        }
        if (dont_split_this) {
          dont_split_status = 1;
          int i;
          for (i=1; i<ud->lines_length; i++) {
            if (ud->lines_pagenum[i] > ud->lines_pagenum[i-1]) {
              if (!ud->lines_newpage[i]) {
                dont_split_status = -1;
              }
              break;
            }
          }
        }
        if (widow_control) {
          if (widow_control_this) {
            widow_control_status = 1;
          } else {
            widow_control_status = 0;
          }
          int i=1;
          while (i < widow_control && widow_control_pos + i < ud->lines_length) {
            if (ud->lines_pagenum[widow_control_pos + i] > ud->lines_pagenum[widow_control_pos + i - 1]) {
              if (!ud->lines_newpage[i]) {
                widow_control_status = -1;
              }
              break;
            }
            i++;
          }
          if (i == widow_control) {
            widow_control_status = 1;
          }
        }
      }
      if (dont_split_status < 0 ||
          keep_with_previous_status < 0 ||
          widow_control_status < 0) {
        if (state_saved) {
          dont_split = 0;
          dont_split_status = 0;
          keep_with_next = 0;
          keep_with_previous = 0;
          keep_with_previous_status = 0;
          widow_control = 0;
          widow_control_status = 0;
          restoreState();
          child = saved_child;
          branchCount = saved_branchCount;
          state_saved = 0;
          ud->buffer3_len_so_far = 0;
          ud->buffer3_enabled = 0;
          do_newpage();
        } else {
          break;
        }
      }
      if (dont_split_status > 0) {
        dont_split = 0;
        dont_split_status = 0;
      }
      if (keep_with_previous_status > 0) {
        keep_with_previous = 0;
        keep_with_previous_status = 0;
      }
      if (widow_control_status > 0) {
        widow_control = 0;
        widow_control_status = 0;
      }
      if ((!dont_split && !keep_with_previous && !keep_with_next && !widow_control) ||
          (!child && state_saved)) {
        writeBuffer(3, 0);
        ud->buffer3_enabled = 0;
        state_saved = 0;
      }
    }
    if (dont_split_this)         { dont_split = 0; }
    if (keep_with_next_this)     { keep_with_next = 0; }
    if (keep_with_previous_this) { keep_with_previous = 0; }
    if (widow_control_this)      { widow_control = 0; }
  }
/******************************************************************/

  insert_code (node, branchCount);
  insert_code (node, -1);
  if (style)
    end_style (style);
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
  if (action != 0)
    pop_sem_stack ();
  else
    {
      insert_translation (ud->mainBrailleTable);
      write_paragraph (para);
    }
  return 1;
}
