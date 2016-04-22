package opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import gunpower.DanmuItem;
import newgunpower.IDanmuSwitchListener;
import utils.opglutil.MatrixUtils;
import utils.opglutil.ShaderUtils;

/**
 * 弹幕渲染
 */
public class DanmakuRenderer implements GLTextureView.Renderer {

    private static final String TAG = "DanmakuRenderer";

    private CopyOnWriteArrayList<DanmuItem> mDanmakus;
    private Context mContext;
    private RenderListener mListener;
    private String mVertexShader;//顶点着色器
    private String mFragmentShader;//片元着色器
    private int mViewWidth;//窗口宽度
    private int mViewHeight;//窗口高度
    private long mLastTime;
    private float mSpeed;//速度，单位px/s
    private List<DanmuItem> mShouldRemove;

    private boolean isOpenCloseSwitch = false;

    /**
     * 新增加的临时用的方法
     *
     * @param context
     * @param danmakuView
     */
    public DanmakuRenderer(Context context, DanmukuTextureView danmakuView) {
        mDanmakus = new CopyOnWriteArrayList<>();
        mShouldRemove = new ArrayList<>();
        this.mContext = context;
        if (danmakuView != null) {
            danmakuView.setOnSwitchListener(new IDanmuSwitchListener() {
                @Override
                public void openSwitch() {
                    isOpenCloseSwitch = true;
                }

                @Override
                public void closeSwitch() {
                    isOpenCloseSwitch = false;
                    clearDanmuKu();
                }
            });
        }
    }

    public DanmakuRenderer(Context context, DanmakuView danmakuView) {
        mDanmakus = new CopyOnWriteArrayList<>();
        mShouldRemove = new ArrayList<>();
        this.mContext = context;
        if (danmakuView != null) {
            danmakuView.setOnSwitchListener(new IDanmuSwitchListener() {
                @Override
                public void openSwitch() {
                    isOpenCloseSwitch = true;
                }

                @Override
                public void closeSwitch() {
                    isOpenCloseSwitch = false;
                    clearDanmuKu();
                }
            });
        }

    }

    public void setListener(RenderListener listener) {
        this.mListener = listener;
    }

    /**
     * 设置速度
     *
     * @param speed
     */
    public void setSpeed(float speed) {
        this.mSpeed = speed;
    }

    /**
     * 添加一个弹幕
     *
     * @param danmaku
     */
    public void addDanmaku(DanmuItem danmaku) {
        danmaku.setShader(mVertexShader, mFragmentShader);
        danmaku.setViewSize(mViewWidth, mViewHeight);

        mDanmakus.add(danmaku);
    }

    public CopyOnWriteArrayList<DanmuItem> getmDanmakus() {
        return mDanmakus;
    }

    /**
     * 清除缓冲区的位图
     */
    public void clearDanmuKu() {
        if (mDanmakus != null) {
            for (int i = 0; i < mDanmakus.size(); i++) {
                mDanmakus.get(i).recyle();
            }
            mDanmakus.clear();
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        //关闭背面剪裁
        //GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_FRONT);

        //开启混色功能，这样是为了让png图片的透明能显示
        GLES20.glEnable(GLES20.GL_BLEND);

        //指定混色方案
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        //加载顶点着色器的脚本内容
        mVertexShader = ShaderUtils.loadFromAssetsFile("vertex.sh", mContext.getResources());

        //加载片元着色器的脚本内容
        mFragmentShader = ShaderUtils.loadFromAssetsFile("frag.sh", mContext.getResources());


    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {

        this.mViewWidth = width;
        this.mViewHeight = height;

        //设置视窗大小及位置为整个view范围
        GLES20.glViewport(0, 0, width, height);

        //计算产生正交投影矩阵
        //一般会设置前两个参数为-width / height，width / height，使得纹理不会变形，
        //但是我这里不这样设置，为了控制位置，变形这个问题在顶点坐标那里处理即可
        MatrixUtils.setProjectOrtho(-1, 1, -1, 1, 0, 1);

        //产生摄像机9参数位置矩阵
        MatrixUtils.setCamera(0, 0, 1, 0f, 0f, 0f, 0f, 1, 0);

        if (mListener != null) {
            mListener.onInited();
        }

        mLastTime = SystemClock.elapsedRealtime();
    }

    private static int FRAME_RATE = 25;

    @Override
    public void onDrawFrame(GL10 gl10) {

        long currentTime = SystemClock.elapsedRealtime();
        float intervalTime = FRAME_RATE / 1000.0f;
        float detalOffset = mSpeed * intervalTime;

        clearCanvas();
        //绘制弹幕纹理
        if (mDanmakus != null && mDanmakus.size() > 0) {

            int size = mDanmakus.size();
            try {
                for (int i = 0; i < size; i++) {

                    if (!isOpenCloseSwitch) {
                        break;
                    }
                    DanmuItem danmaku = mDanmakus.get(i);
                    if (danmaku != null) {
                        float newOffset = detalOffset + danmaku.getCurrentOffsetX();
                        danmaku.setOffsetX(newOffset);

                        if (newOffset <= mViewWidth + danmaku.getBitmapWidth()) {
                            //mDanmakus.offer(danmaku);
                            if (isOpenCloseSwitch) {
                                danmaku.drawDanmaku();
                                //打开弹幕
//                            mListener.onOpenDanmuSwitch();
//                            mListener.onListInit(mDanmakus);
                            }
                        } else {
                            mShouldRemove.add(danmaku);
                            danmaku.recyle();
                        }
                    }

                    //从循环内移到循环外
                    mListener.onOpenDanmuSwitch();
                    mListener.onListInit(mDanmakus);
                }
            } catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "");
            }


            mDanmakus.removeAll(mShouldRemove);
            mShouldRemove.clear();
        } else {
            waitForSync(250);
        }

        mLastTime = currentTime;
        long dt = SystemClock.elapsedRealtime() - currentTime;
        if (dt < FRAME_RATE) {
            waitForSync(FRAME_RATE - dt);
        }
    }

    public void waitForSync(long t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onSurfaceDestroyed(GL10 gl) {
        Log.d("sur", "onSurfaceDestroyed");
    }

    public void clearCanvas() {
        //设置屏幕背景色RGBA
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        //清除深度缓冲与颜色缓冲
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

    }

    public interface RenderListener {
        void onInited();

        void onOpenDanmuSwitch();

        void onListInit(CopyOnWriteArrayList<DanmuItem> list);
    }

}
