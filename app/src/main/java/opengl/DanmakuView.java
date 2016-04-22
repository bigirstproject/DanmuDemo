package opengl;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import gunpower.DanmuItem;
import newgunpower.GunNewPower;
import newgunpower.IDanmuClickListener;
import newgunpower.IDanmuOpenStatus;
import newgunpower.IDanmuSwitchListener;
import utils.*;
import utils.asynctask.ScheduledTask;
import utils.opglutil.DimensUtils;
import utils.opglutil.TexturePool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 */
public class DanmakuView extends GLTextureView {

    public static final int LEVEL_1 = 1;  //优先级最低 公屏的基础弹幕
    public static final int LEVEL_2 = 2;  //优先级第二高  贵族或者礼物
    public static final int LEVEL_3 = 3;  //优先级最高  暂时没有使用这个（方便以后拓展）


    private final String TAG = "ZGDanmakuView";
    private Context mContext;
    private DanmakuRenderer mRenderer;//渲染器
    private int gapLine = 4;//默认4行
    private float mLineSpace;//行距

    private static final int DEFAULT_TOP_MARGIN = DimenConverter.dip2px(BasicConfig.getInstance().getAppContext(), 5);  //12
    //默认两个弹幕之间的距离
    private int KTopMargin = DEFAULT_TOP_MARGIN;
    private boolean isInited = false;
    private boolean isPaused = false;
    //    private Queue<ZGDanmakuItem> mCachedDanmaku;
    private Map<Integer, DanmuItem> mLinesAvaliable;

    //判断第几行弹道是否可以插入弹道 true 可以插入  false不可以插入（弹幕的开关）
    private HashMap<Integer, Boolean> hashMap = new HashMap<>();

    private ScheduledTask scheduledTask;
    private ScheduledTask touchTask;

    //弹幕启动的标记
    private AtomicBoolean mOnOff = new AtomicBoolean(false);

    private IDanmuOpenStatus danmuOpenStatus;
    private IDanmuSwitchListener switchListener;
    private IDanmuClickListener listener;

    public float width = 0;

    private CopyOnWriteArrayList<DanmuItem> danmuItemList = new CopyOnWriteArrayList<>();

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

    public DanmakuView(Context context) {
        super(context);
        init(context);
    }

    public DanmakuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
//        mCachedDanmaku = new LinkedList<>();
        mLinesAvaliable = new HashMap<>();

        setEGLContextClientVersion(2); //设置使用OPENGL ES2.0
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mRenderer = new DanmakuRenderer(context, this);
        setRenderer(mRenderer);
//        getHolder().setFormat(PixelFormat.TRANSLUCENT);
//        setZOrderOnTop(false);
//        setZOrderMediaOverlay(true);
//        setVersion(GLESVersion.OpenGLES20);
        setRenderMode(GLTextureView.RENDERMODE_CONTINUOUSLY);//设置渲染模式为主动渲染
        setOpaque(false);

        setSpeed(120);//默认50dp/s速度
        setLineSpace(8);//默认8dp行距
//        mCanvas = new Canvas();
//        mPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
//        mPainter.setTextSize(DimensUtils.dip2pixel(mContext, 18));
//        mPainter.setColor(0xffffffff);
//        mPainter.setTextAlign(Paint.Align.LEFT);
//        mPainter.setShadowLayer(2, 3, 3, 0x5a000000);

        mRenderer.setListener(new DanmakuRenderer.RenderListener() {
            @Override
            public void onInited() {
                isInited = true;
//                showCachedDanmaku();
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

    public void setScreenWidth(float width) {
        this.width = width;
    }

    //所有的弹道都是可以发射的
    public void initFlag() {
        if (hashMap.size() > 0) {
            hashMap.clear();
        }
        for (int i = 0; i < gapLine; i++) {
            hashMap.put(i, true);
        }
    }

    //启动弹幕的开关
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

    public void initTexturePool() {
        TexturePool.uninit();
    }

    public void setOnSwitchListener(IDanmuSwitchListener listener) {
        this.switchListener = listener;
    }

    //弹幕是不是已经打开
    public boolean onDanmuSwitch() {
        return mOnOff.get();
    }

    /**
     * 打开弹幕
     */
    public void setOpenView() {
        onBarrageSwitch(true);
    }

    /**
     * 关闭弹幕
     */
    public void setCloseView() {
        onBarrageSwitch(false);
    }

    public int getTopMargin() {
        return KTopMargin;
    }

    @Override
    public void onResume() {
        super.onResume();
        isPaused = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;
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

//        for (int i = 0; i < gapLine; i ++) {
        if (mLinesAvaliable.get(position) == null) {
            setCheckStatus(position, true);
            return true;
        }
        DanmuItem danmaku = mLinesAvaliable.get(position);
        if (danmaku.getCurrentOffsetX() > danmaku.getBitmapWidth()) {
            setCheckStatus(position, true);
            return true;
        }

        setCheckStatus(position, false);
//        }
        return false;
    }

    private synchronized void getAvaliableLine() {
        for (int i = 0; i < gapLine; i++) {
            if (mLinesAvaliable.get(i) == null) {
                setCheckStatus(i, true);
                continue;
            }
            DanmuItem danmaku = mLinesAvaliable.get(i);

            if (danmaku == null) {
                setCheckStatus(i, true);
                continue;
            }
            if (danmaku.getCurrentOffsetX() > danmaku.getBitmapWidth()) {
                setCheckStatus(i, true);
                continue;
            }
            setCheckStatus(i, false);
        }

    }

    /**
     * 设置速度
     *
     * @param speed dp/s
     */
    public void setSpeed(float speed) {
        float pxSpeed = DimensUtils.dip2pixel(mContext, speed);
        this.mRenderer.setSpeed(pxSpeed);
    }

    /**
     * 设置行数
     *
     * @param lines
     */
    public void setLines(int lines) {
        this.gapLine = lines;
    }

    /**
     * 设置行距
     *
     * @param lineSpace
     */
    public void setLineSpace(int lineSpace) {
        float pxLineSpace = DimensUtils.dip2pixel(mContext, lineSpace);
        this.mLineSpace = pxLineSpace;
    }

    //设置每个弹道的开关
    public void setCheckStatus(int position, boolean isOpen) {
        if (position < gapLine) {
            hashMap.put(position, isOpen);
        } else {
            Log.e(TAG, "getLineStatus is line > gapLine , gapLine " + gapLine);
        }
    }


    /**
     * 发一条弹幕
     *
     * @param
     */
    //发射弹道
    public void sendGunPower(GunNewPower gun, int position) {
        if (mOnOff.get()) {
            if (getAvaliableLine(position)) {
                if (gun != null && gun.bitmap != null) {
                    setCheckStatus(position, false);

                    DanmuItem danmakuItem = new DanmuItem(gun.gunId, gun.bitmap, gun.content);
//                    if(danmakuItem == null){
//                        MLog.debug(TAG, "sendGunPower DanmuItem IS NULL");
//                        setCheckStatus(position, true);
//                        return;
//                    }
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

    public void queryDanmuOpenStatus(IDanmuOpenStatus status) {
        this.danmuOpenStatus = status;
    }


    public void setOnClickListener(IDanmuClickListener clickListener) {
        this.listener = clickListener;
    }


    int tx = 0;
    int ty = 0;

    //弹幕点击
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            tx = (int) event.getX();
            ty = (int) event.getY();

            touchTask.scheduledDelayed(touchRunnable, 0);
        }

        return false;

    }

}
