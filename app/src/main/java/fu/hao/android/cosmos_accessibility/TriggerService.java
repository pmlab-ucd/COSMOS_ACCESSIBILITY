package fu.hao.android.cosmos_accessibility;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;

import java.io.OutputStream;

@Deprecated
public class TriggerService extends Service {
    public TriggerService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        startActivity("fu.hao.testcondroid", "fu.hao.testcondroid.Activity2");
        try {
            Thread.sleep(2000);
            screenShot();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startActivity(String packageName, String activityFullName) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, activityFullName));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void screenShot() throws Exception {
        Process sh = Runtime.getRuntime().exec("su", null,null);
        OutputStream os = sh.getOutputStream();
        os.write(("/system/bin/screencap -p " + Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/img.png").getBytes("ASCII"));
        os.flush();
        os.close();
        sh.waitFor();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
