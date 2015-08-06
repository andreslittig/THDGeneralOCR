/*====================================================================*
 -  Copyright (C) 2001 Leptonica.  All rights reserved.
 -  This software is distributed in the hope that it will be
 -  useful, but with NO WARRANTY OF ANY KIND.
 -  No author or distributor accepts responsibility to anyone for the
 -  consequences of using this software, or for whether it serves any
 -  particular purpose or works at all, unless he or she says so in
 -  writing.  Everyone is granted permission to copy, modify and
 -  redistribute this source code, for commercial or non-commercial
 -  purposes, with the following restrictions: (1) the origin of this
 -  source code must not be misrepresented; (2) modified versions must
 -  be plainly marked as such; and (3) this notice may not be removed
 -  or altered from any source or modified source distribution.
 *====================================================================*/

/*
 * fcombautogen.c
 *
 *    This program is used to generate the two files of dwa code for combs,
 *    that are used in linear composite dwa operations for brick Sels.
 *    If filename is not given, the files are:
 *         dwacomb.<n>.c
 *         dwacomblow.<n>.c
 *    Otherwise they are:
 *         <filename>.<n>.c
 *         <filename>low.<n>.c
 *    where <n> is the input index.
 *    These two files, when compiled, are used to implement all
 *    the composite dwa operations for brick Sels, that are
 *    generated by selaAddDwaCombs().
 *
 *    The library files dwacomp.2.c and dwacomblow.2.c were made
 *    using <n> = 2.
 */

#include "allheaders.h"

main(int    argc,
     char **argv)
{
char        *filename;
l_int32      index, ret;
SELA        *sela;
static char  mainName[] = "fcombautogen";

    if (argc != 2 && argc != 3)
	exit(ERROR_INT(" Syntax:  fcombautogen index <filename>",
                       mainName, 1));

    index = atoi(argv[1]);
    sela = selaAddDwaCombs(NULL);

    if (argc == 2)
        filename = stringNew("dwacomb");
    else
        filename = argv[2];
    ret = fmorphautogen(sela, index, filename);

    if (argc == 2)
        lept_free(filename);
    selaDestroy(&sela);
    return ret;
}

