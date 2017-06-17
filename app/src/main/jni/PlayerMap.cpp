#include "PlayerMap.h"
#include <map>

std::map<int, Movie*> MovieMap;
int LastId = 0;

extern "C" {

    int MovieMap_insert(Movie *movie)
    {
        int ret = LastId;
        MovieMap.insert(std::pair<int, Movie*>(LastId, movie));
        LastId++;
        return ret;
    }

    Movie *MovieMap_get(int id)
    {
        auto iter = MovieMap.find(id);
        if(iter == MovieMap.end())
            return NULL;
        return iter->second;
    }

    void MovieMap_remove(int id)
    {
        MovieMap.erase(id);
    }

    void MovieMap_clear()
    {
        MovieMap.clear();
        LastId = 0;
    }

}