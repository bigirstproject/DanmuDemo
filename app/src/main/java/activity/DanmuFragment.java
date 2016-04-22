package activity;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.duowan.danmu.R;

import java.util.HashMap;

import core.ChannelMessage;
import core.CommonScreenImpl;
import newgunpower.GunNewPower;
import newgunpower.IDanmuOpenStatus;
import opengl.DanmukuTextureView;
import utils.BasicConfig;
import utils.DimenConverter;
import utils.asynctask.AsyncTask;
import utils.asynctask.SafeDispatchHandler;
import utils.opglutil.TexturePool;

/**
 * Created by dexian on 2016/3/22.
 */
public class DanmuFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = DanmuFragment.class.getSimpleName();
    private DanmukuTextureView danmukuTextureView;
    private EditText editText;
    private Button button;

    private CommonScreenImpl screenCore;

    private float totalWidth;

    private AsyncTask task;
    private SafeDispatchHandler handlerTask;
    private int count;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        screenCore = new CommonScreenImpl();
        TexturePool.uninit();
        task = new AsyncTask();
        handlerTask = task.executeTask(runnable);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            ChannelMessage message = new ChannelMessage();
            message.uid = 123456;
            message.nickname = "test12006";
            message.text = "大家好  count = " + count++;
            screenCore.appendChannelMessage(message);
            handlerTask.postDelayed(this, 1000);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_basic_danmu, container, false);
        danmukuTextureView = (DanmukuTextureView) view.findViewById(R.id.fragment_danmu);
        editText = (EditText) view.findViewById(R.id.edit_text);
        button = (Button) view.findViewById(R.id.button);
        button.setOnClickListener(this);
        totalWidth = getResources().getDisplayMetrics().widthPixels;
        danmukuTextureView.setOpenView();
        danmukuTextureView.setScreenWidth(totalWidth);
        danmukuTextureView.setLines(4);
        danmukuTextureView.setLineSpace(5);
        danmukuTextureView.setDanmuSpeed(80);

        danmukuTextureView.queryDanmuOpenStatus(new IDanmuOpenStatus() {
            @Override
            public void getGunPower(HashMap<Integer, Boolean> hashMap) {
                //弹幕如果是关闭状态 直接不发射弹幕
                if (!danmukuTextureView.onDanmuSwitchState()) {
                    return;
                }
                try {
                    //反向循环从底部开始塞弹幕
                    for (int i = hashMap.size() - 1; i >= 0; i--) {
                        if (hashMap.get(i)) {

                            if (i == hashMap.size() - 1) {

                                if (screenCore.getMineMessageCache() != null && screenCore.getMineMessageCache().size() > 0) {

                                    ChannelMessage message = screenCore.getMineMessageCache().poll();
                                    if (message == null || message.text == null || message.nickname == null) {
                                        continue;
                                    }
                                    if (message.nickname.length() >= 7) {
                                        message.nickname = message.nickname.substring(0, 7);
                                    }
                                    if (message.text.length() > 30) {
                                        message.text = message.text.substring(0, 30) + "...";
                                    }
                                    GunNewPower gun = new GunNewPower(message.uid, message.text, true);
                                    gun.nickName = message.nickname;
                                    gun.createPowertoShell(getActivity());
//                                        danmuViewBasicCore.sendNewGunPower(gun, i);
                                    danmukuTextureView.sendGunPower(gun, i);

                                } else if (screenCore.getChatCacheList() != null && screenCore.getChatCacheList().size() > 0) {
                                    ChannelMessage message = screenCore.getChatCacheList().poll();
                                    if (message == null || message.text == null || message.nickname == null) {
                                        continue;
                                    }
                                    if (message.nickname.length() >= 7) {
                                        message.nickname = message.nickname.substring(0, 7);
                                    }
                                    if (message.text.length() > 31) {
                                        message.text = message.text.substring(0, 30) + "...";
                                    }
                                    GunNewPower gun = new GunNewPower(message.uid, message.text, false);
                                    gun.nickName = message.nickname;
                                    gun.createPowertoShell(getActivity());
                                    // danmuViewBasicCore.sendNewGunPower(gun, i);
                                    danmukuTextureView.sendGunPower(gun, i);
                                }
                            } else {
                                if (screenCore.getChatCacheList() == null || screenCore.getChatCacheList().size() < 0) {
                                    continue;
                                }

                                ChannelMessage message = screenCore.getChatCacheList().poll();

                                if (message == null || message.text == null || message.nickname == null) {
                                    continue;
                                }
                                if (message.nickname.length() > 7) {
                                    message.nickname = message.nickname.substring(0, 7);
                                }

                                if (message.text.length() > 31) {
                                    message.text = message.text.substring(0, 30) + "...";
                                }
                                GunNewPower gun = new GunNewPower(message.uid, message.text, false);
                                gun.nickName = message.nickname;
                                gun.createPowertoShell(getActivity());
//                                    danmuViewBasicCore.sendNewGunPower(gun, i);
                                danmukuTextureView.sendGunPower(gun, i);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RelativeLayout.LayoutParams mPortraitLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DimenConverter.dip2px(BasicConfig.getInstance().getAppContext(), 120));
        danmukuTextureView.setLayoutParams(mPortraitLayoutParams);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button) {
            if (editText != null) {
                danmukuTextureView.setDanmuSpeed(Integer.valueOf(editText.getText().toString().trim()));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (screenCore != null) {
            screenCore.onResume();
        }
        if (danmukuTextureView != null) {
            danmukuTextureView.setOpenView();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (screenCore != null) {
            screenCore.onPause();
        }
        if (danmukuTextureView != null) {
            danmukuTextureView.setCloseView();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        handlerTask.removeCallbacks(runnable);
    }

}
