#include "AudioFormatMap.h"
#include "Log.h"

#include <map>
#include <string>

#include <libavformat/avformat.h>

std::map<int, std::string> gAudioFormatMap;

void initAudioFormatMap() 
{
	// if (sfmt == AV_SAMPLE_FMT_U8 || sfmt == AV_SAMPLE_FMT_U8P) {
	// 	LOGD("AV_SAMPLE_FMT_U8");
	// }
	// else if (sfmt == AV_SAMPLE_FMT_S16 || sfmt == AV_SAMPLE_FMT_S16P) {
	// 	LOGD("AV_SAMPLE_FMT_S16");
	// }
	// else if (sfmt == AV_SAMPLE_FMT_S32 || sfmt == AV_SAMPLE_FMT_S32P) {
	// 	LOGD("AV_SAMPLE_FMT_S32");
	// }
	// else if (sfmt == AV_SAMPLE_FMT_FLT || sfmt == AV_SAMPLE_FMT_FLTP) {
	// 	LOGD("AV_SAMPLE_FMT_FLT");
	// }
	// else if (sfmt == AV_SAMPLE_FMT_DBL || sfmt == AV_SAMPLE_FMT_DBLP) {
	// 	LOGD("AV_SAMPLE_FMT_DBL");
	// }
	// else {
	// 	LOGD("Unsupported format");
	// }
	gAudioFormatMap.clear();

	//normal
	gAudioFormatMap.insert(std::pair<int, std::string>(AV_SAMPLE_FMT_U8, "AV_SAMPLE_FMT_U8"));// 0
	gAudioFormatMap.insert(std::pair<int, std::string>(AV_SAMPLE_FMT_S16, "AV_SAMPLE_FMT_S16"));// 1
	gAudioFormatMap.insert(std::pair<int, std::string>(AV_SAMPLE_FMT_S32, "AV_SAMPLE_FMT_S32"));// 2
	gAudioFormatMap.insert(std::pair<int, std::string>(AV_SAMPLE_FMT_FLT, "AV_SAMPLE_FMT_FLT"));// 3
	gAudioFormatMap.insert(std::pair<int, std::string>(AV_SAMPLE_FMT_DBL, "AV_SAMPLE_FMT_DBL"));// 4

	//planar
	gAudioFormatMap.insert(std::pair<int, std::string>(AV_SAMPLE_FMT_U8P, "AV_SAMPLE_FMT_U8P"));// 5
	gAudioFormatMap.insert(std::pair<int, std::string>(AV_SAMPLE_FMT_S16P, "AV_SAMPLE_FMT_S16P"));// 6
	gAudioFormatMap.insert(std::pair<int, std::string>(AV_SAMPLE_FMT_S32P, "AV_SAMPLE_FMT_S32P"));// 7
	gAudioFormatMap.insert(std::pair<int, std::string>(AV_SAMPLE_FMT_FLTP, "AV_SAMPLE_FMT_FLTP"));// 8
	gAudioFormatMap.insert(std::pair<int, std::string>(AV_SAMPLE_FMT_DBLP, "AV_SAMPLE_FMT_DBLP"));// 9
}

const char* getAudioFormatString(int format) 
{
	LOGD("format=%d", format);

	std::map<int, std::string>::iterator iter;
	iter = gAudioFormatMap.find(format);
	if(iter != gAudioFormatMap.end()) {
		return iter->second.c_str();
	}
	return NULL;
}
