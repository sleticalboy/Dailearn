#include <stdio.h>

#include "hello-clib.h"

void hey() {
    printf("%s() called\n", __func__);
}

void hello(char *s) {
    printf("%s() called with '%s'\n", __func__, s);
}
