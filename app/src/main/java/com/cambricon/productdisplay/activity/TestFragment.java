package com.cambricon.productdisplay.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.adapter.GridViewAdapter;

public class TestFragment extends Fragment {
    private final int CLASSIFICATION = 0;
    private final int DETECTION = 1;
    private final int VOICE = 2;
    private final int MOREFUNCTIONS = 3;

    private View view;
    private GridView mGridView;
    private GridViewAdapter mGridViewAdapter;
    //private ImageButton testingBtn;
    private TextView hardware;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.test_fragment, null);
        initView();
        setListener();
        return view;
    }

    private void initView() {
        mGridView = view.findViewById(R.id.functions_gv);
        mGridViewAdapter = new GridViewAdapter(getContext());
        mGridView.setAdapter(mGridViewAdapter);
        //testingBtn = view.findViewById(R.id.testing);
        hardware=view.findViewById(R.id.hardware);
        hardware.setText(Build.HARDWARE);

        /*testingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ClassificationActivity.class));
            }
        });*/
    }

    private void setListener() {
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case CLASSIFICATION:
                        Intent intent_classify = new Intent(getActivity(), ClassificationActivity.class);
                        intent_classify.putExtra("deploy_prototxt",true);
                        startActivity(intent_classify);
                        break;
                    case DETECTION:
                        Intent intent = new Intent(getActivity(), DetectionActivity.class);
                        startActivity(intent);
                        break;
                    case VOICE:
                        intent = new Intent(getActivity(), FaceDetectorActivity.class);
                        startActivity(intent);
                        break;
                    case MOREFUNCTIONS:
                        startActivity(new Intent(getActivity(), MoreFunctionsAct.class));
                        break;
                }
            }
        });
    }


}
