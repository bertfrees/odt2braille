INCLUDES =		\
	-I$(top_srcdir)/liblouisxml \
	-I$(top_srcdir)/gnulib

LDADD = \
	$(top_builddir)/liblouisxml/liblouisxml.la \
	$(top_builddir)/gnulib/libgnu.la \
	$(BASE_DEPENDENCIES_LIBS) \
	$(LTLIBINTL)

AM_CFLAGS =	\
	$(BASE_DEPENDENCIES_CFLAGS)

AM_LDFLAGS =	\
	$(BASE_DEPENDENCIES_LDFLAGS)

bin_PROGRAMS = \
	xml2brl \
	lbx_devonly

bin_SCRIPTS = \
	msword2brl \
	rtf2brl \
	pdf2brl

CLEANFILES = $(bin_SCRIPTS)

xml2brl_SOURCES = xml2brl.c

lbx_devonly_SOURCES = lbx_devonly.c
