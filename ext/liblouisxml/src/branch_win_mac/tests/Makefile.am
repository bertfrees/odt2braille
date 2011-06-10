AM_CPPFLAGS = \
	-I$(top_srcdir)/liblouisxml \
	$(BASE_DEPENDENCIES_CFLAGS)

LDADD = $(top_builddir)/liblouisxml/liblouisxml.la

check_version_SOURCES =	check_version.c

check_PROGRAMS = \
	check_version

TESTS =	\
	$(check_PROGRAMS)

EXTRA_DIST = \
	changetable.html \
	default.cfg \
	emphasis.xml \
	fracbinom.html \
	mathtest.sem \
	matrix1.html \
	matrix2.xml \
	pagebreak.html \
	quadratic.html \
	testcode.xml

