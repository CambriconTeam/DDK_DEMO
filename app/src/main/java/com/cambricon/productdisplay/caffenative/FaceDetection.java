package com.cambricon.productdisplay.caffenative;

/**
 * Created by Chengyu Yang on 18-3-9.
 */

public class FaceDetection {

    static {
        System.loadLibrary("face_jni");
    }

    /**
     * 通过JNI方式对传入图像(int数组)进行人脸检测
     *
     * @param modelDir:模型存放的位置
     * @param w:图像的宽度,便于在JNI中设置一个新的容器来存放图像
     * @param h:图像的高度
     * @param resultPixel:其即使传入的图像(int数组),又是传出的图像(int数组)在JNI中
     *                   对同一图像数组转换为Mat进行图像的操作,其最终的操作结果都将完
     *                   整的保存在这个数组中,故不必再特意设置回传的值.
     * @return resultPixel
     * <p>
     * 在工程中如果直接导入OpenCV for Android其实可以直接传递Mat到JNI中,在Java
     * 层中制定Mat容器地址,将地址传入JNI,再将该地址转换为(Mat *)就可以有效的在JNI
     * 中操作Mat,降低整体的难度. 对于该方法的速度还未做完整测试,未来可以尝试.
     */
    public static native int[] doFaceDetector(String modelDir, int w, int h, int[] resultPixel);

}
