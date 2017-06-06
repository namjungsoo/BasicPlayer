#ifndef __PLAYERMAP_H__
#define __PLAYERMAP_H__

#ifdef __cplusplus
extern "C" {
#endif

#include "Movie.h"

int MovieMap_insert(Movie *movie);
Movie *MovieMap_get(int id);
void MovieMap_remove(int id);
void MovieMap_clear();

#ifdef __cplusplus
}
#endif

#endif//__PLAYERMAP_H__