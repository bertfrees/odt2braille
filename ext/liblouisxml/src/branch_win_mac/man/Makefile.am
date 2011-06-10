# Only build man pages if configure found the HELP2MAN tool
if HAVE_HELP2MAN
man_MANS = xml2brl.1 msword2brl.1 rtf2brl.1 pdf2brl.1
endif

CLEANFILES = $(man_MANS)

# The man pages depend on the --help strings and the version number.
common_mandeps = $(top_srcdir)/configure.ac

# We are not distributing the man pages otherwise we would need rules
# such as below. The user will have to install help2man if they want
# man pages.
# xml2brl.1: $(top_srcdir)/tools/xml2brl.c $(common_mandeps)
# 	 cd ../liblouisxml && $(MAKE) $(AM_MAKEFLAGS) liblouisxml.la
# 	 cd ../gnulib && $(MAKE) $(AM_MAKEFLAGS) libgnu.la
# 	 cd ../tools && $(MAKE) $(AM_MAKEFLAGS) xml2brl$(EXEEXT)
# 	$(HELP2MAN) ../tools/xml2brl$(EXEEXT) --info-page=$(PACKAGE) --output=$@

# Depend on the source, not the binary; we don't need to regenerate the
# binary when any source file changes, only the main one.
xml2brl.1: $(top_srcdir)/tools/xml2brl.c $(common_mandeps)
	$(HELP2MAN) ../tools/xml2brl$(EXEEXT) --info-page=$(PACKAGE) \
	--name="Translate an xml or a text file into an embosser-ready Braille file." \
	--output=$@

msword2brl.1: $(top_srcdir)/tools/msword2brl $(common_mandeps)
	$(HELP2MAN) ../tools/msword2brl --info-page=$(PACKAGE) \
	--name="Translate an MS Word file into an embosser-ready Braille file." \
	--output=$@

rtf2brl.1: $(top_srcdir)/tools/rtf2brl $(common_mandeps)
	$(HELP2MAN) ../tools/rtf2brl --info-page=$(PACKAGE) \
	--name="Translate an RTF file into an embosser-ready Braille file." \
	--output=$@

pdf2brl.1: $(top_srcdir)/tools/pdf2brl $(common_mandeps)
	$(HELP2MAN) ../tools/pdf2brl --info-page=$(PACKAGE) \
	--name="Translate a PDF file into an embosser-ready Braille file." \
	--output=$@

