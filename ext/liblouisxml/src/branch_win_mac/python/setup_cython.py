#!/usr/bin/python
from distutils.core import setup
from distutils.extension import Extension

from Cython.Distutils import build_ext

setup(
    name = "louisxml",
    version = "1.1",
    url = "http://pypi.python.org/pypi/louisxml",
    description = """Python interface for liblouisxml""",
    long_description =
        """
        Python interface for liblouisxml
        =======================================
        
        This is a set of python bindings for the liblouisxml Braille translation library.
        """,
    author = "Michael Whapples",
    author_email = "mwhapples@users.berlios.de",
    cmdclass = {"build_ext": build_ext},
    ext_modules = [Extension("louisxml", ["louisxml.pyx"], libraries=["louisxml"])],
    classifiers = [
        "Development Status :: 5 - Production/Stable",
        "License :: OSI Approved :: Artistic License",
        "Intended Audience :: Developers",
        "Topic :: Software Development :: Libraries :: Python Modules",
        "Operating System :: POSIX",
        "Operating System :: Microsoft"
    ]
)
