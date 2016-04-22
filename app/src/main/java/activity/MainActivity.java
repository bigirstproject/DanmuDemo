package activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.WindowManager;

import com.duowan.danmu.R;

/**
 * Created by Administrator on 2016/3/21.
 */
public class MainActivity extends FragmentActivity {


    public static final String TAG_NAME_FRAGMENT = "fragment";
    private DanmuFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mFragment = (DanmuFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_NAME_FRAGMENT);
    }


    @Override
    public void onResume() {
        super.onResume();
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        if (mFragment == null) {
            mFragment = new DanmuFragment();
            Intent intent = getIntent();
            if (intent != null) {
                Bundle params = intent.getExtras();
                if (mFragment != null) {
                    mFragment.setArguments(params);
                }
            }
            transaction.add(R.id.container, mFragment, TAG_NAME_FRAGMENT);
        }
        transaction.show(mFragment);
        transaction.commitAllowingStateLoss();
    }


}
