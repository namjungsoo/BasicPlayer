//#include <stdlib.h>
#include <signal.h>

double atof(const char *nptr)
{
    return (strtod(nptr, 0));
}

// void (*signal(int sig, void (*func)(int)))(int) {
//     return bsd_signal(sig, func);
// }

/* differentiater between sysv and bsd behaviour 8*/
extern __sighandler_t sysv_signal(int, __sighandler_t);
extern __sighandler_t bsd_signal(int, __sighandler_t);

/* the default is bsd */
 __inline__ __sighandler_t signal(int s, __sighandler_t f)
{
    return bsd_signal(s,f);
}