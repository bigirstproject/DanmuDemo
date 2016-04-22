package utils;

import android.content.Context;

/**
 * Created by xujiexing on 14-6-12.
 */
public class BasicConfig {

    private Context mContext;

    private static BasicConfig mInstance;

    public synchronized static BasicConfig getInstance() {
        if (mInstance == null)
            mInstance = new BasicConfig();
        return mInstance;
    }

    private BasicConfig() {

    }

    public Context getAppContext() {
        return mContext;
    }

    public void setAppContext(Context context) {
        this.mContext = context;
    }

}
