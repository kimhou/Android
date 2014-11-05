package com.example.caculate;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tencent on 14/11/4.
 */
public class HomePage extends Activity {
    private ArrayList<Button> numBtnList = new ArrayList<Button>();
    private ArrayList<Button> opBtnList = new ArrayList<Button>();

    private TextView outPutText;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        LinearLayout root = (LinearLayout)findViewById(R.id.root);
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.icon);
        root.addView(imageView);

        Resources res = getResources();
        outPutText = (TextView)findViewById(res.getIdentifier("outPutText", "id", getPackageName()));

        for(int i = 0; i < 16; i++){
            int id;
            if(i < 10) {
                id = res.getIdentifier("btn_num_" + i, "id", getPackageName());
                Button btn = (Button)findViewById(id);
                numBtnList.add(btn);
            }else{
                id = res.getIdentifier("btn_op_" + (i - 10), "id", getPackageName());
                opBtnList.add((Button)findViewById(id));
            }

        }
        for(Button btn:numBtnList){
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Button b = (Button)view;
                    String text = b.getText().toString(),
                        currentText = outPutText.getText().toString();

                    text = currentText.equals("0") ? text : currentText + text;
                    outPutText.setText(text);
                }
            });
        }
    }
}