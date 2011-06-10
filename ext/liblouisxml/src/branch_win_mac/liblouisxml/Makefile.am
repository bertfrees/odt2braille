INCLUDES =		\
	-I$(top_srcdir) \
	-DLBX_PATH=\"$(datadir)/liblouisxml/lbx_files/\" \
	-DLIBLOUIS_TABLES_PATH=\"$(datadir)/liblouis/tables/\"
liblouisxmlincludedir = $(includedir)/liblouisxml

liblouisxmlinclude_HEADERS = \
	liblouisxml.h \
	louisxml.h

lib_LTLIBRARIES = liblouisxml.la

liblouisxml_la_CFLAGS =	\
	$(BASE_DEPENDENCIES_CFLAGS)

liblouisxml_la_LDFLAGS =	\
	-version-info \
	$(LIBLOUISXML_CURRENT):$(LIBLOUISXML_REVISION):$(LIBLOUISXML_AGE) \
	-no-undefined \
	$(BASE_DEPENDENCIES_LIBS)

liblouisxml_la_SOURCES =			\
	change_table.c \
	makeContents.c \
	examine_document.c \
	liblouisxml.c \
	liblouisxml.h \
	louisxml.h \
	paths.c \
	readconfig.c \
	semantics.c \
	sem_enum.h \
	sem_names.h \
	transcribe_cdataSection.c \
	transcribe_chemistry.c \
	transcribe_computerCode.c \
	transcribe_document.c \
	transcribe_graphic.c \
	transcribe_math.c \
	transcribe_music.c \
	transcribe_paragraph.c \
	transcriber.c

EXTRA_DIST = \
	README \
	make_sem_names.c \
	Makesemnames

