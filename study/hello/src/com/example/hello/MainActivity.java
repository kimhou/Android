package com.example.hello;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
	
	EditText input;
	Button btn;
	TextView label;
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        input = (EditText)findViewById(R.id.input);
        btn = (Button)findViewById(R.id.btn);
        label = (TextView)findViewById(R.id.text);
        label.setText("hello world");
        
        btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				input.setText("you clicked butotn!!!");
			}
		});
        
        label.setOnClickListener(new View.OnClickListener(){
        	@Override
        	public void onClick(View arg0){
        		label.setText("you clicked me");
        	}
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    
}
