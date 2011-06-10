#!/usr/bin/python
from distutils.core import setup
from distutils.extension import Extension

setup(
    name = "louisxml",
    version = "1.1",
    url = "http://pypi.python.org/pypi/louisxml",
    description = """Python interface for liblouisxml""",
    long_description =
        """
        Python interface for liblouisxml
        =======================================
        
        This is a set of python bindings for the liblouisxml Braille translation 
        library. It aims to try and keep to the liblouisxml API, only
        changing things where it makes more sense and is simpler for use in
        python.
        """,
    author = "Michael Whapples",
    author_email = "mwhapples@users.berlios.de",
    ext_modules = [Extension("louisxml", ["louisxml.c"], libraries=["louisxml"])],
    include_dirs = ['../liblouisxml/'],
    classifiers = [
        "Development Status :: 5 - Production/Stable",
        "License :: OSI Approved :: Artistic License",
        "Intended Audience :: Developers",
        "Topic :: Software Development :: Libraries :: Python Modules",
        "Operating System :: POSIX",
        "Operating System :: Microsoft"
    ]
)
