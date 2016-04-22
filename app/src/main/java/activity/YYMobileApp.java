package activity;

import android.app.Application;
import utils.BasicConfig;

/**
 * Created by dexian on 2016/3/21.
 */
public class YYMobileApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BasicConfig.getInstance().setAppContext(getApplicationContext());
    }

}
