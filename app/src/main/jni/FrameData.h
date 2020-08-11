class FrameData {
    uint8_t *gData[3];// YUV 데이터

public: 
    FrameData(int width, int height);
    virtual ~FrameData();
    uint8_t *getData(int idx);
};