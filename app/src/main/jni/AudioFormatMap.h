#ifndef __AUDIOFORMATMAP_H__
#define __AUDIOFORMATMAP_H__

#ifdef __cplusplus
extern "C" {
#endif
	void initAudioFormatMap();
	const char* getAudioFormatString(int format);
#ifdef __cplusplus
}
#endif

#endif//__AUDIOFORMATMAP_H__