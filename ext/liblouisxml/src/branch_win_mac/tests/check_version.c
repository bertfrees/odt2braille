#include "config.h"
#include <stdio.h>
#include <string.h>
#include "liblouisxml.h"

/* Make sure lbx_version does report the actual version */
int
main (int argc, char **argv)
{
  int test_passed = 0;
  const char *version = lbx_version();

  if (strcmp(version, PACKAGE_VERSION) != 0) {
    printf("lbx_version() doesn't match PACKAGE_VERSION\n");
    printf("expected: %s; actual: %s\n", PACKAGE_VERSION, version);
    test_passed = 1;
  }
  return test_passed;
}
