package util;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by tencent on 15/2/5.
 */
public class VPUtil {
    public static void showMessage(Context context, String msg){
        Toast toast = Toast.makeText(context.getApplicationContext(),
                msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
