#ifndef __UTIL_H__
#define __UTIL_H__

#ifdef __cplusplus
extern "C" {
#endif

#include <sys/types.h>

int64_t getTimeNsec();
long getMicrotime();

#ifdef __cplusplus
}
#endif

#endif//__UTIL_H__