#include <queue>
//#include <unistd.h> //pthread_t
#include <pthread.h>

template<typename T>
class SharedQueue {
public:
    //static int (*clearCallback)(T item);

    SharedQueue() {
        pthread_mutex_init(&mutex, NULL);
    }
    void clear(void (*callback)(T item)) {
        lock();
        while(_queue.size() > 0) {
            T item = _queue.front();
            _queue.pop();
            if(callback) {
                callback(item);
            }
        }
        unlock();
    }
    size_t size() {
        lock();
        size_t s = _queue.size();
        unlock();
        return s;
    }
    T pop() {
        lock();
        T item = _queue.front();
        _queue.pop();
        unlock();
        return item;
    }
    void push(T item) {
        lock();
        _queue.push(item);
        unlock();
    }

private:
    void lock() {
        pthread_mutex_lock(&mutex); // 잠금
    }
    void unlock() {
        pthread_mutex_unlock(&mutex); // 잠금해제
    }
    pthread_mutex_t mutex;
    std::queue<T> _queue;
};