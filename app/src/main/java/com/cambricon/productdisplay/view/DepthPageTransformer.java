package com.cambricon.productdisplay.view;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by huangyaling on 18-1-29.
 * viewpager渲染器
 */

public class DepthPageTransformer implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.75f;
    @Override
    public void transformPage(View page, float position) {
        int pageWidth = page.getWidth();
        if(position<-1){//[-Infinity,-1)
            page.setAlpha(0);
        }else if(position<=0){//[-1,0]
            page.setAlpha(1);
            page.setTranslationX(0);
            page.setScaleX(1);
            page.setScaleY(1);
        }else if(position<=1){//(0,1]
            page.setAlpha(1-position);
            page.setTranslationX(pageWidth*-position);
            float scaleFactor = MIN_SCALE+(1-MIN_SCALE)*(1-Math.abs(position));
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);
        }else{
            page.setAlpha(0);
        }

    }
}
