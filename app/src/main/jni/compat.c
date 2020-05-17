#include <signal.h>
#include <sys/cdefs.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#include <stdint.h>
#include <linux/termios.h>

int tcgetattr(int fd, struct termios *s)
{
    return ioctl(fd, TCGETS, s);
}

int tcsetattr(int fd, int __opt, const struct termios *s)
{
    return ioctl(fd, __opt, (void *)s);
}

char *stpcpy(char *dst, const char *src)
{
    const size_t len = strlen(src);
    return (char *)memcpy(dst, src, len + 1) + len;
}
