package com.cambricon.productdisplay.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cambricon.productdisplay.R;
import com.huawei.hiai.asr.AsrConstants;
import com.huawei.hiai.asr.AsrListener;
import com.huawei.hiai.asr.AsrRecognizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SpeechRecognitionActivity extends AppCompatActivity implements View.OnClickListener{
    private AsrRecognizer mAsrRecognizer;
    private MyAsrListener mMyAsrListener = new MyAsrListener();

    private static final String TAG = "api";
    private TextView tv_text;
    private Button btn_stop;
    private Button btn_start;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_recognition);
        initView();
        setActionbar();
        initService();

    }

    private void initService() {
        mAsrRecognizer = AsrRecognizer.createAsrRecognizer(this);
//        调用init和destroy
        initEngine(AsrConstants.ASR_SRC_TYPE_RECORD);
//        mAsrRecognizer.destroy();
    }


    private void setActionbar(){
        toolbar.setTitle(getIntent().getStringExtra("BaseToolBarTitle"));
        setSupportActionBar(toolbar);
        Drawable toolDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.toolbar_bg);
        toolDrawable.setAlpha(50);
        toolbar.setBackground(toolDrawable);
        /*显示Home图标*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void initView(){

        toolbar = findViewById(R.id.speech_recognition_toolbar);
        btn_start = findViewById(R.id.btn_start);
        btn_stop = findViewById(R.id.btn_stop);
        tv_text = findViewById(R.id.tv_text);

        btn_start.setOnClickListener(this);
        btn_stop.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                start();

                break;
            case R.id.btn_stop:
                stop();
                break;
        }
    }
    private void start(){
        Intent intent = new Intent();
        intent.putExtra(AsrConstants.ASR_VAD_FRONT_WAIT_MS, 4000);
        intent.putExtra(AsrConstants.ASR_VAD_END_WAIT_MS, 5000);
        intent.putExtra(AsrConstants.ASR_TIMEOUT_THRESHOLD_MS, 20000);
        if (mAsrRecognizer != null) {
            mAsrRecognizer.startListening(intent);
        }
    }

    private void stop() {
        Log.d(TAG, "stopListening() ");
        if (mAsrRecognizer != null) {
            mAsrRecognizer.stopListening();
        }
    }
    private void initEngine(int srcType) {
        Log.d(TAG, "initEngine() ");
        mAsrRecognizer = AsrRecognizer.createAsrRecognizer(this);
        Intent initIntent = new Intent();
        initIntent.putExtra(AsrConstants.ASR_AUDIO_SRC_TYPE, srcType);
        if (mAsrRecognizer != null) {
            mAsrRecognizer.init(initIntent, mMyAsrListener);
        }
    }


    private class MyAsrListener implements AsrListener {
        @Override
        public void onInit(Bundle params) {
            Log.d(TAG, "onInit() called with: params = [" + params + "]");
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech() called");
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            Log.d(TAG, "onRmsChanged() called with: rmsdB = [" + rmsdB + "]");
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "onBufferReceived() called with: buffer = [" + buffer + "]");
        }

        @Override
        public void onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech: ");


        }

        @Override
        public void onError(int error) {
            Log.d(TAG, "onError() called with: error = [" + error + "]");

        }


        @Override
        public void onResults(Bundle results) {
            Log.d(TAG, "onResults() called with: results = [" + results + "]");
//            endTime = getTimeMillis();
//            waitTime = endTime - startTime;
//            mResult = getOnResult(results, AsrConstants.RESULTS_RECOGNITION);
//
//            stopListening();
//            if (isAutoTest) {
//                resultList.add(pathList.get(count) + "\t" + mResult);
//                Log.d(TAG, "isAutoTest: " + waitTime + "count :" + count);
//                if (count == fileTotalCount - 1) {
//                    resultList.add("\n\nwaittime:\t" + waitTime + "ms");
//                    mHandler.sendEmptyMessage(WRITE_RESULT_SD);
//                    Log.d(TAG, "waitTime: " + waitTime + "count :" + count);
//                    count = 0;
//                } else {
//                    Log.d(TAG, "isAutoTest: else" + waitTime + "count :" + count);
//                    count += 1;
//                    mHandler.sendEmptyMessageDelayed(NEXT_FILE_TEST, 1000);
//                }
//            } else {
//                startRecord.setEnabled(true);
//            }

        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            Log.d(TAG, "onPartialResults() called with: partialResults = [" + partialResults + "]");
            getOnResult(partialResults, AsrConstants.RESULTS_PARTIAL);
        }

        @Override
        public void onEnd() {

        }

        private String getOnResult(Bundle partialResults, String key) {
            Log.d(TAG, "getOnResult() called with: getOnResult = [" + partialResults + "]");
            String json = partialResults.getString(key);
            final StringBuilder sb = new StringBuilder();
            try {
                JSONObject result = new JSONObject(json);
                JSONArray items = result.getJSONArray("result");
                for (int i = 0; i < items.length(); i++) {
                    String word = items.getJSONObject(i).getString("word");
                    double confidences = items.getJSONObject(i).getDouble("confidence");
                    sb.append(word);
                    Log.d(TAG, "asr_engine: result str " + word);
                    Log.d(TAG, "asr_engine: confidence " + String.valueOf(confidences));
                }
                Log.d(TAG, "getOnResult: " + sb.toString());
                tv_text.setText(sb.toString());
            } catch (JSONException exp) {
                Log.w(TAG, "JSONException: " + exp.toString());
            }
            return sb.toString();
        }


        @Override
        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent() called with: eventType = [" + eventType + "], params = [" + params + "]");
//            switch (eventType) {
//                case AsrEvent.EVENT_PERMISSION_RESULT:
//                    int result = params.getInt(AsrEvent.EVENT_KEY_PERMISSION_RESULT, PackageManager.PERMISSION_DENIED);
//                    if (result != PackageManager.PERMISSION_GRANTED) {
//                        reset();
//                    }
//                default:
//                    return;
//            }
        }
    }
}
