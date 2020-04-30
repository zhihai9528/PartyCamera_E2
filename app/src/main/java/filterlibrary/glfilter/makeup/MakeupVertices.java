package filterlibrary.glfilter.makeup;

/**
 * 彩妆顶点坐标
 */
public final class MakeupVertices {

    /**
     * 唇彩索引, 0 ~ 19 表示关键点 84 ~ 103
     */
    public static short[] lipsIndices = new short[] {
            // 上嘴唇部分
            0, 1, 12,
            12, 1, 13,
            13, 1, 2,
            2, 13, 14,
            2, 14, 3,
            3, 14, 4,
            4, 14, 15,
            4, 15, 5,
            5, 15, 16,
            5, 16, 6,
            // 下嘴唇部分
            6, 16, 7,
            16, 7, 17,
            17, 7, 8,
            17, 8, 18,
            18, 8, 9,
            18, 9, 10,
            18, 10, 19,
            19, 10, 11,
            19, 11, 12,
            12, 11, 0,
    };

    /**
     * 唇彩遮罩纹理坐标，跟 assets/makeup/lips_mask.png的坐标对应
     */
    public static float[] lipsMaskTextureVertices = new float[] {
            0.171821f, 0.409089f,
            0.281787f, 0.318381f,
            0.398625f, 0.272727f,
            0.515464f, 0.300303f,
            0.618557f, 0.287279f,
            0.728522f, 0.333333f,
            0.845361f, 0.424242f,
            0.776632f, 0.575758f,
            0.687285f, 0.742424f,
            0.515464f, 0.833333f,
            0.357388f, 0.757576f,
            0.274914f, 0.651515f,
            0.226804f, 0.424242f,
            0.378007f, 0.439393f,
            0.508592f, 0.469696f,
            0.680413f, 0.454545f,
            0.776632f, 0.439393f,
            0.646048f, 0.575757f,
            0.501718f, 0.590909f,
            0.336769f, 0.515152f,
    };


    /**
     * 腮红顶点索引，腮红共有44个顶点
     */
    public static short[] blushIndices = new short[] {

    };

}
