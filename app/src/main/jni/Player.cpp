#include "Player.h"

Player::Player() : formatCtx(NULL), currentTimeUs(0L)
{
    video = new Video;
    audio = new Audio;
}