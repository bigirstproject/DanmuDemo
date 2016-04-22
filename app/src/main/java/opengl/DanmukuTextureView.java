package opengl;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import gunpower.DanmuItem;
import newgunpower.GunNewPower;
import newgunpower.IDanmakuCore;
import newgunpower.IDanmuClickListener;
import newgunpower.IDanmuOpenStatus;
import newgunpower.IDanmuSwitchListener;
import utils.BasicConfig;
import utils.DimenConverter;
import utils.NetworkUtils;
import utils.asynctask.ScheduledTask;
import utils.opglutil.DimensUtils;

/**
 * Created by dexian  on 2016/3/14.
 */
public class DanmukuTextureView extends GLTextureView implements IDanmakuCore {
    private final String TAG = DanmukuTextureView.class.getSimpleName();

    private final int DEFAULT_TOP_MARGIN = DimenConverter.dip2px(BasicConfig.getInstance().getAppContext(), 5);  //12

    private Context mContext;
    private DanmakuRenderer mRenderer;//渲染器
    private int gapLine = 4;//默认4行
    private float mLineSpace;//行距

    //默认两个弹幕之间的距离
    private int KTopMargin = DEFAULT_TOP_MARGIN;
    private boolean isInited = false;

    private Map<Integer, DanmuItem> mLinesAvaliable;

    //判断第几行弹道是否可以插入弹道 true 可以插入  false不可以插入（弹幕的开关）
    private HashMap<Integer, Boolean> hashMap = new HashMap<Integer, Boolean>();

    private ScheduledTask scheduledTask;
    private ScheduledTask touchTask;

    //弹幕启动的标记
    private AtomicBoolean mOnOff = new AtomicBoolean(false);

    private IDanmuOpenStatus danmuOpenStatus;
    private IDanmuSwitchListener switchListener;
    private IDanmuClickListener listener;

    //弹幕宽度
    private float width = 0;
    private int tx = 0;
    private int ty = 0;

    private CopyOnWriteArrayList<DanmuItem> danmuItemList = new CopyOnWriteArrayList<>();

    private RelativeLayout.LayoutParams layoutParams;

    public DanmukuTextureView(Context context) {
        super(context);
    }

    public DanmukuTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        mLinesAvaliable = new HashMap<Integer, DanmuItem>();
        setEGLContextClientVersion(2); //设置使用OPENGL ES2.0
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mRenderer = new DanmakuRenderer(context, this);
        setRenderer(mRenderer);
        setRenderMode(GLTextureView.RENDERMODE_CONTINUOUSLY);//设置渲染模式为主动渲染
        setOpaque(false);
        setDanmuSpeed(120);//默认50dp/s速度
        setLineSpace(8);//默认8dp行距
        mRenderer.setListener(new DanmakuRenderer.RenderListener() {
            @Override
            public void onInited() {
                isInited = true;
            }

            @Override
            public void onOpenDanmuSwitch() {
                getAvaliableLine();
            }

            @Override
            public void onListInit(CopyOnWriteArrayList<DanmuItem> list) {
                if (list != null && danmuItemList != null) {
                    if (danmuItemList.size() > 0) {
                        danmuItemList.clear();
                    }
                    danmuItemList.addAll(list);
                }
            }
        });
    }

    /**
     * 所有的弹道都是可以发射的
     */
    public void initFlag() {
        if (hashMap.size() > 0) {
            hashMap.clear();
        }
        for (int i = 0; i < gapLine; i++) {
            hashMap.put(i, true);
        }
    }

    /**
     * 获取有效的弹道
     *
     * @return
     */
    private synchronized boolean getAvaliableLine(int position) {
        if (position > gapLine) {
            return false;
        }
        if (mLinesAvaliable.get(position) == null) {
            setDanmuRoadSwitchStatus(position, true);
            return true;
        }
        DanmuItem danmaku = mLinesAvaliable.get(position);
        if (danmaku.getCurrentOffsetX() > danmaku.getBitmapWidth()) {
            setDanmuRoadSwitchStatus(position, true);
            return true;
        }
        setDanmuRoadSwitchStatus(position, false);
        return false;
    }

    /**
     *
     */
    private synchronized void getAvaliableLine() {
        for (int i = 0; i < gapLine; i++) {
            if (mLinesAvaliable.get(i) == null) {
                setDanmuRoadSwitchStatus(i, true);
                continue;
            }
            DanmuItem danmaku = mLinesAvaliable.get(i);

            if (danmaku == null) {
                setDanmuRoadSwitchStatus(i, true);
                continue;
            }
            if (danmaku.getCurrentOffsetX() > danmaku.getBitmapWidth()) {
                setDanmuRoadSwitchStatus(i, true);
                continue;
            }
            setDanmuRoadSwitchStatus(i, false);
        }

    }

    /**
     * 启动弹幕的开关
     *
     * @param on
     */
    private void onBarrageSwitch(boolean on) {
        if (on) {
            mOnOff.set(true);
            if (switchListener != null) {
                switchListener.openSwitch();
            }
            initFlag();
            if (scheduledTask == null) {
                scheduledTask = ScheduledTask.getInstance();
            }
            scheduledTask.scheduledDelayed(scheduRunnable, 0);

            if (touchTask == null) {
                touchTask = ScheduledTask.getInstance();
            }
        } else {
            mOnOff.set(false);
            if (switchListener != null) {
                switchListener.closeSwitch();
            }
            initFlag();
            if (mLinesAvaliable != null) {
                mLinesAvaliable.clear();
            }
            if (scheduledTask != null) {
                scheduledTask.removeCallbacks(scheduRunnable);
            }
            if (touchTask != null) {
                touchTask.removeCallbacks(touchRunnable);
            }
            if (danmuItemList != null && danmuItemList.size() > 0) {
                danmuItemList.clear();
            }

        }
    }

    @Override
    public void queryDanmuOpenStatus(IDanmuOpenStatus status) {
        this.danmuOpenStatus = status;
    }

    @Override
    public void setScreenWidth(float width) {
        this.width = width;
    }

    @Override
    public void moveScreenHeight(boolean isLandscape, int screenHeight) {
        if (isLandscape) {
            int left = DimenConverter.dip2px(BasicConfig.getInstance().getAppContext(), 48);
            int right = DimenConverter.dip2px(BasicConfig.getInstance().getAppContext(), 48);
            layoutParams.setMargins(left, 0, right, screenHeight);
        } else {
            layoutParams.setMargins(0, 0, 0, screenHeight);
        }
    }

    @Override
    public void setLayoutParams(RelativeLayout.LayoutParams layoutParams) {
        this.layoutParams = layoutParams;
    }

    @Override
    public void setDanmakuMargin(int kTopMargin) {
        this.KTopMargin = kTopMargin;
    }

    @Override
    public int getDanmakuMargin() {
        return KTopMargin;
    }

    @Override
    public boolean onDanmuSwitchState() {
        return mOnOff.get();
    }

    @Override
    public void setOpenView() {
        onBarrageSwitch(true);
    }

    @Override
    public void setCloseView() {
        onBarrageSwitch(false);
    }

    @Override
    public void setDanmuSpeed(float speed) {
        float pxSpeed = DimensUtils.dip2pixel(mContext, speed);
        this.mRenderer.setSpeed((int) pxSpeed);
    }

    @Override
    public void setLines(int lines) {
        this.gapLine = lines;
    }

    @Override
    public void setLineSpace(int lineSpace) {
        float pxLineSpace = DimensUtils.dip2pixel(mContext, lineSpace);
        this.mLineSpace = pxLineSpace;
    }

    @Override
    public void setDanmuRoadSwitchStatus(int position, boolean isOpen) {
        if (position < gapLine) {
            hashMap.put(position, isOpen);
        } else {
            Log.e(TAG, "getLineStatus is line > gapLine , gapLine " + gapLine);
        }
    }

    @Override
    public void sendGunPower(GunNewPower gun, int position) {
        if (mOnOff.get()) {
            if (getAvaliableLine(position)) {
                if (gun != null && gun.bitmap != null) {
                    setDanmuRoadSwitchStatus(position, false);
                    DanmuItem danmakuItem = new DanmuItem(gun.gunId, gun.bitmap, gun.content);
                    //关键代码  设置当前弹幕
                    mLinesAvaliable.put(position, danmakuItem);
                    //得到弹幕的Y坐标
                    float offsetY = danmakuItem.getmViewHeight() * position + mLineSpace;
                    danmakuItem.setOffsetY(offsetY);
                    mRenderer.addDanmaku(danmakuItem);
                }
            }
        }
    }

    @Override
    public void setOnClickListener(IDanmuClickListener clickListener) {
        this.listener = clickListener;
    }

    @Override
    public void setOnSwitchListener(IDanmuSwitchListener listener) {
        this.switchListener = listener;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * 弹幕点击
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            tx = (int) event.getX();
            ty = (int) event.getY();

            touchTask.scheduledDelayed(touchRunnable, 0);
        }
        return false;
    }


    private Runnable scheduRunnable = new Runnable() {
        @Override
        public void run() {
            //关键代码
            //System.gc();
            if (danmuOpenStatus != null) {
                danmuOpenStatus.getGunPower(hashMap);
            }
            if (scheduledTask != null) {
                scheduledTask.scheduledDelayed(scheduRunnable, 500);
            }
        }
    };

    private Runnable touchRunnable = new Runnable() {
        @Override
        public void run() {

            if (danmuItemList != null && danmuItemList.size() > 0) {

                touchTask.scheduledDelayed(new Runnable() {
                    @Override
                    public void run() {
                        for (DanmuItem point : danmuItemList) {
                            //float x = width - (point.offsetX/2);
                            float x = width - point.offsetX;
                            //MLog.debug(TAG,"danmuClick x="+tx+" danmuClick y="+ty+" offsetX="+x+" bitmapWidth"+point.bitmapWidth+" screenwi");
                            if (tx > x && tx < x + point.bitmapWidth && ty > point.offsetY && ty < point.offsetY + point.bitmapHeight) {

                                if (NetworkUtils.isNetworkStrictlyAvailable(BasicConfig.getInstance().getAppContext())) {

                                    if (listener != null) {
                                        listener.setOnClickListener(point.gunId, point.content);
                                    }
                                    //     Toast.makeText(BasicConfig.getInstance().getAppContext(),"你点到我啦 "+point.content,Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }, 0);
            }

        }
    };


}
