package net.kimhou.videodemopro;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import net.kimhou.playersdk.PlayerSDKActivity;

import util.PlayerConst;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String testUrl = "http://vcloud1200.tc.qq.com/1200_99d41be4ad1c11e4b444ed5f927b4c84.f30.mp4";
        final EditText editText = (EditText)findViewById(R.id.editText);
        editText.setText(testUrl);


        ((Button) findViewById(R.id.playButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = editText.getText().toString();

                /**
                 * SDK API
                 * @param String VPConst.VIDEO_URL 视频URL
                 */
                Intent intent = new Intent(MainActivity.this, PlayerSDKActivity.class);
                intent.putExtra(PlayerConst.VIDEO_URL, url);
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
