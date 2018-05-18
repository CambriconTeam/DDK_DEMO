package com.cambricon.productdisplay.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cambricon.productdisplay.R;


/**
 * Created by dell on 18-4-13.
 */

public class DialogUtil {
    public static void showDialog(Context context,String title,String content,String button){

        final Dialog builder = new Dialog(context, R.style.update_dialog);
        View view = View.inflate(context,R.layout.custom_dialog,null);//加载自己的布局
        TextView contentText = view.findViewById(R.id.content);
        contentText.setText(content);
        Button noUpdateBtn = view.findViewById(R.id.alert_no_update_btn);
        noUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.dismiss();
            }
        });
        builder.setContentView(view);//这里还可以指定布局参数
        builder.setCancelable(false);// 不可以用“返回键”取消
        builder.show();
    }
}
