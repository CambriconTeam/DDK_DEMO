package com.cambricon.productdisplay.utils;

/**
 * Created by dell on 18-2-28.
 */

public class ConvertUtil {
    /**
     * string convert to double
     * @param number
     * @return
     */
    public static double convert2Double(String number){
        try {
            return Double.parseDouble(number);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * double convert to int
     * @param number
     * @return
     */
    public static int convert2Int(double number){
        return Integer.parseInt(new java.text.DecimalFormat("0").format(number));
    }

    public static int getFps(String number){
        return convert2Int(convert2Double(number));
    }
    public static double log(Double value,Double base){
        return Math.log(value)/Math.log(base);
    }
}
