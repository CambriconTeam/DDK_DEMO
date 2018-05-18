package com.cambricon.productdisplay.utils;

import android.util.Log;

import com.cambricon.productdisplay.bean.ClassificationImage;
import com.cambricon.productdisplay.bean.DetectionImage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dell on 18-3-19.
 */

public class FileUtils {
    /**
     * read file test
     *
     * @param path
     * @return
     */
    public static String readFormSDcard(String path) {

        if (!new File(path).exists()) {
            return "\"没有文件\"";
        } else {
            try {
                FileInputStream fis = new FileInputStream(path);
                @SuppressWarnings("resource")
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        fis));
                StringBuilder sb = new StringBuilder("");
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                String[] all = sb.toString().split(":");
                return all[1];
            } catch (Exception e) {
                Log.i("tag", "读取失败！");
            }
        }
        return null;
    }

    private static String sPicName;

    /**
     * parse detection ipu mode txt
     *
     * @param path
     * @return
     * @throws IOException
     */

    public static Map<String, Integer> readFromIPUTxt(String path) throws IOException {
        Map<String, Integer> map = new HashMap<>();
        int count = 1;//初始化 key值
        File file = new File(path);
        if (file.isFile() && file.exists()) {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(file));
            BufferedReader br = new BufferedReader(isr);
            String lineTxt = null;
            while ((lineTxt = br.readLine()) != null) {
                if (!"".equals(lineTxt)) {
                    if (count % 2 != 0) {
                        sPicName = lineTxt;
                        count++;
                    } else {
                        int time = Integer.parseInt(lineTxt);
                        map.put(sPicName, time);
                        count++;
                    }
                }
            }
            isr.close();
            br.close();
        } else {
            Log.e("TAG", "Error in FileUtils:readFromIPUTxt");
        }
        return map;
    }

    /**
     * parse classification ipu mode txt
     *
     * @param path
     * @return
     * @throws Exception
     */
    public static ArrayList<ClassificationImage> readClassificationIPUTxt(String path) throws Exception {
        ArrayList<ClassificationImage> classificationIPUImage = new ArrayList<>();
        String lineTxt = "";
        File file = new File(path);

        InputStreamReader isr = new InputStreamReader(new FileInputStream(file));
        BufferedReader br = new BufferedReader(isr);
        int count = 1;
        ClassificationImage image = null;
        while ((lineTxt = br.readLine()) != null) {
            switch (count % 3) {
                case 0:
                    image.setTime(lineTxt);
                    image.setFps(getFps(Double.valueOf(lineTxt)));
                    classificationIPUImage.add(image);
                    break;
                case 1:
                    image = new ClassificationImage();
                    String[] split = lineTxt.split("/");
                    //image.setName(split[split.length - 1]);
                    image.setName(lineTxt);
                    break;
                case 2:
                    String temp = lineTxt.substring(10);
                    image.setResult(temp);
                    break;
                default:
                    break;
            }
            count++;
        }
        isr.close();
        br.close();
        return classificationIPUImage;
    }


    public static ArrayList<DetectionImage> readDetectionIPUTxt(String path) throws Exception {
        ArrayList<DetectionImage> detectionImages = new ArrayList<>();
        File file = new File(path);
        int count = 1;
        String[] arr = null;
        if (file.exists() && file.isFile()) {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(file));
            BufferedReader br = new BufferedReader(isr);
            String lineTxt = null;
            while ((lineTxt = br.readLine()) != null) {
                DetectionImage detectionImage = new DetectionImage();
                arr = lineTxt.split(" ");
                detectionImage.setName(arr[0]);
                detectionImage.setTime(arr[2]);
                detectionImage.setNetType(arr[1]);
                detectionImage.setFps(getFps(Double.valueOf(arr[2])));
                detectionImages.add(detectionImage);
            }

        } else {
            Log.e("TAG", "Error in FileUtils:readDetectionIPUTxt");
        }
        return detectionImages;
    }

    //single test parse txt
    public static ArrayList<Integer> readSingleTxt(String path) throws Exception{
        ArrayList<Integer> singleFps=new ArrayList<>();
        File file=new File(path);
        if(file.exists()&&file.isFile()){
            InputStreamReader isr=new InputStreamReader(new FileInputStream(file));
            BufferedReader br = new BufferedReader(isr);
            String lineTxt;
            while((lineTxt=br.readLine())!=null){
                int time=Integer.parseInt(lineTxt);
                int fps=time/1000;
                singleFps.add(fps);
            }
        }
        return singleFps;
    }

    //single Layer parse txt
    public static ArrayList<Double> readSingleLayer(String path)throws Exception{
        ArrayList<Double> singleTime=new ArrayList<>();
        File file=new File(path);
        if(file.exists()&&file.isFile()){
            InputStreamReader isr=new InputStreamReader(new FileInputStream(file));
            BufferedReader br=new BufferedReader(isr);
            String linetxt;
            while((linetxt=br.readLine())!=null){
                double time=Double.parseDouble(linetxt);
                singleTime.add(time);
            }
        }
        return singleTime;
    }


    /**
     * FPS格式转换
     *
     * @param classificationTime
     * @return
     */
    private static String getFps(double classificationTime) {
        double fps = 60 * 1000 / classificationTime;
        return String.valueOf(fps);
    }
}
