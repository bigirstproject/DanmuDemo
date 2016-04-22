package gunpower;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import utils.*;
import utils.opglutil.MatrixUtils;
import utils.opglutil.ShaderUtils;
import utils.opglutil.TexturePool;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * 弹幕
 */
public class DanmuItem {
    //这里默认设置纹理id为-1值，在OpenGLES底下，负值是无效的，如果不设置负值，那么int默认是0，是有效的纹理id
    //当一个弹幕还没有绑定到纹理的时候，业务端触发所有弹幕回收，就会触发回收纹理0
    private int mTextureId = -1;//绑定的纹理id
    private int mProgram;//自定义渲染管线程序id
    private int muMVPMatrixHandle;//总变换矩阵引用id
    private int maPositionHandle; //顶点位置属性引用id
    private int maTexCoorHandle; //顶点纹理坐标属性引用id

    private String mVertexShader;//顶点着色器
    private String mFragmentShader;//片元着色器

    private FloatBuffer mVertexBuffer;//顶点坐标数据缓冲
    private ShortBuffer mTexCoorBuffer;//顶点纹理坐标数据缓冲

    private Bitmap mBitmap;//弹幕纹理
    public float offsetX;//偏移的x坐标，范围是0-mViewWidth
    public float offsetY;//偏移的y坐标，范围是0-mViewHeight
    private int mViewWidth;//窗口宽度
    private int mViewHeight;//窗口高度
    private int mVertexCount = 4;//纹理顶点个数，这个是矩形，四个顶点
    private boolean isInited = false;

    public  int bitmapWidth = 0;
    public  int bitmapHeight = 0;
    public String content = "";

    public int gunId;

    public DanmuItem(int gunId,Bitmap bitmap,String content) {
        this.gunId = gunId;
        this.mBitmap = bitmap;
        if(mBitmap !=null){
            bitmapWidth = bitmap.getWidth();
            bitmapHeight = bitmap.getHeight();
        }

        if(content != null){
            this.content = content;
        }
    }

    public boolean init() {
        if (mBitmap == null) {
            return false;
        }

        //初始化顶点坐标与纹理着色数据
        initVertexData();

        //初始化着色器
        initShader();

        //生成纹理，必须在opengl的现场绑定纹理才有用
        if(!initTexture()) {
            return false;
        }

        isInited = true;
        return isInited;
    }


    public int getBitmapWidth(){
        return bitmapWidth;
    }

    /**
     * 设置着色器
     *
     * @param vertexShader   顶点着色器
     * @param fragmentShader 片元着色器
     */
    public void setShader(String vertexShader, String fragmentShader) {
        this.mVertexShader = vertexShader;
        this.mFragmentShader = fragmentShader;
    }

    /**
     * 设置view的宽高
     * @param width
     * @param height
     */
    public void setViewSize(int width, int height) {
        this.mViewWidth = width;
        this.mViewHeight = height;
    }

    /**
     * 设置行偏移量
     * @param offsety
     */
    public void setOffsetY(float offsety) {
        this.offsetY = offsety;
    }

    /**
     * 设置列偏移量
     * @param offsetx
     */
    public void setOffsetX(float offsetx) {
        this.offsetX = offsetx;
    }

    /**
     * 获取当前的行偏移量
     * @return
     */
    public float getCurrentOffsetX() {
        return this.offsetX;
    }

    /**
     * 获取弹幕宽度
     * @return
     */
//    public int getDanmakuWidth() {
//        if(mBitmap == null) {
//            return 0;
//        }
//        return mBitmap.getWidth();
//    }

    //回收bitmap
    public synchronized void recyle(){
        if(mBitmap != null){
            mBitmap.recycle();
            mBitmap = null;
        }
        gunId = 0;
        //isInited = false;

//        moveX = 0;
//        content = null;
        uninit();

        mVertexShader = null;
        mFragmentShader = null;

        if(mVertexBuffer!=null)mVertexBuffer.clear();
        if(mTexCoorBuffer!=null)mTexCoorBuffer.clear();

    }

    public int getmViewHeight() {
        if(mBitmap == null) {
            return 0;
        }
        return mBitmap.getHeight();
    }

//    public void setmViewHeight(int mViewHeight) {
//
//        this.mViewHeight = mViewHeight;
//    }

    public int getmViewWidth() {
        return mViewWidth;
    }

    public void setmViewWidth(int mViewWidth) {
        this.mViewWidth = mViewWidth;
    }

    /**
     * 初始化顶点坐标与着色数据
     */
    public void initVertexData() {
        //顶点坐标数据
        //顶点坐标系：窗口取值范围是-1至1，所以，左上角坐标是(-1,1),终点坐标是(0,0)，右下角坐标是(1, -1)
        //其实就是把坐标给归一化了，下面是计算弹幕的归一化宽和高
        //实际上顶点坐标是正负1的，所以弹幕在坐标系中要乘以2，放大两倍

        float danmakuHeight = (float) bitmapHeight / mViewHeight * 2.0f;
        float danmakuWidth = (float) bitmapWidth / mViewWidth * 2.0f;

        //弹幕四个角的顶点坐标，我默认把它绘制在屏幕的左上角了，这样方便处理
        //为什么不是顺时针或者逆时针，实际上opengl只能绘制三角形，所以，
        //这里其实是绘制了两个三角形的，前三个点和后三个点分别是三角形
        float vertices[] = new float[]
                {
                        -1, 1, 0,                                   //左上角
                        -(1 - danmakuWidth), 1, 0,                  //右上角
                        -1, 1 - danmakuHeight, 0,                   //左下角
                        -(1 - danmakuWidth), 1 - danmakuHeight, 0   //右下角
                };

        //一个float是4个字节，所以*4
        //关于ByteBuffer，可以看nio相关知识：http://zhgeaits.me/java/2013/06/17/Java-nio.html
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);

        //把纹理绘制到矩形中去，就需要制定读取纹理的坐标
        //纹理坐标系：范围是0-1，左上角坐标是(0,0),右下角坐标是(1,1)
        short texCoor[] = new short[]//顶点颜色值数组，每个顶点4个色彩值RGBA
                {
                        0, 0,   //左上角
                        1, 0,   //右上角
                        0, 1,   //左下角
                        1, 1    //右下角
                };

        ByteBuffer cbb = ByteBuffer.allocateDirect(texCoor.length * 2);
        cbb.order(ByteOrder.nativeOrder());
        mTexCoorBuffer = cbb.asShortBuffer();
        mTexCoorBuffer.put(texCoor);
        mTexCoorBuffer.position(0);
    }

    /**
     * 初始化着色器
     */
    public void initShader() {
        if(FP.empty(mVertexShader)){
            //加载顶点着色器的脚本内容
            mVertexShader = ShaderUtils.loadFromAssetsFile("vertex.sh", BasicConfig.getInstance().getAppContext().getResources());
        }

        if(FP.empty(mFragmentShader)){
            //加载片元着色器的脚本内容
            mFragmentShader = ShaderUtils.loadFromAssetsFile("frag.sh", BasicConfig.getInstance().getAppContext().getResources());
        }

        mProgram = TexturePool.getProgram(mVertexShader, mFragmentShader);
        maPositionHandle = TexturePool.getMaPositionHandle();
        maTexCoorHandle = TexturePool.getMaTexCoorHandle();
        muMVPMatrixHandle = TexturePool.getMuMVPMatrixHandle();
    }

    /**
     * 初始化纹理
     */
    private boolean initTexture() {
        mTextureId = TexturePool.pollTextureId();

        if(mTextureId < 0) {
            return false;
        }

        //绑定纹理，并制定纹理的采样方式
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        //设置MIN时为最近采样点
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        //设置MAG时为线性采样
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        //设置S轴的拉伸方式为截取
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
        //设置T轴的拉伸方式为截取
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);

        //把bitmap加载到纹理
        //纹理类型在OpenGL ES中必须为GL10.GL_TEXTURE_2D
        //第二个参数是纹理的层次，0表示基本图像层，可以理解为直接贴图
        //最后一个参数是纹理边框尺寸
        if(mBitmap != null&&!mBitmap.isRecycled()) {

            try {
                ByteBuffer pixels = ByteBuffer.allocateDirect(mBitmap.getByteCount());
                pixels.order(ByteOrder.nativeOrder());
                mBitmap.copyPixelsToBuffer(pixels);
                pixels.position(0);

                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,
                        0,
                        GLES20.GL_RGBA,
                        mBitmap.getWidth(),
                        mBitmap.getHeight(),
                        0,
                        GLES20.GL_RGBA,
                        GLES20.GL_UNSIGNED_BYTE,
                        pixels);

                //上面bind了mTextureId，这里不要再bind 0了，导致重复
//                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

                if (mBitmap != null && !mBitmap.isRecycled()) {
                    mBitmap.recycle();
                    mBitmap = null;
                }
            } catch (OutOfMemoryError e) {
                System.gc();
            }

        }
//        if(mBitmap != null&&!mBitmap.isRecycled()){
//            mBitmap.recycle();
//        }

        return true;
    }

    private void uninit() {
        TexturePool.offerTextureId(mTextureId);
        mTextureId = -1;
    }

    /**
     * 绘制弹幕
     */
    public void drawDanmaku() {

//        if(!isInited && !init()) {
//            return;
//        }

        if((!isInited && !init()) || mTextureId < 0) {
            return;
        }

        //使用shader程序
        GLES20.glUseProgram(mProgram);

        //初始化矩阵
        MatrixUtils.setInitStack();

        //首先把弹幕移动到右上角
        MatrixUtils.transtate(2.0f, 0, 0);

        //弹幕平移，同理坐标放大两倍
        float unitY = -offsetY / mViewHeight * 2.0f;
        float unitX = -offsetX / mViewWidth * 2.0f;
        MatrixUtils.transtate(unitX, unitY, 0);

        //将最终变换矩阵传入shader程序
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MatrixUtils.getFinalMatrix(), 0);

        //传递顶点位置数据
        //坐标是xyz三维，所以size是3，每个float是4个字节，所以stride是3 * 4
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);

        //传递纹理坐标数据
        //坐标是xy二维的，所以size是2
//        mPositionHandle: The OpenGL index of the position attribute of our shader program.
//        POSITION_DATA_SIZE: How many elements (floats) define this attribute.
//        GL_FLOAT: The type of each element.
//        false: Should fixed-point data be normalized? Not applicable since we are using floating-point data.
//        0: The stride. Set to 0 to mean that the positions should be read sequentially.
//         mCubePositions: The pointer to our buffer, containing all of the positional data.
        GLES20.glVertexAttribPointer(maTexCoorHandle, 2, GLES20.GL_SHORT, false, 0, mTexCoorBuffer);

        //允许顶点位置数据数组
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        GLES20.glEnableVertexAttribArray(maTexCoorHandle);
//
        //绑定纹理
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

        //绘制纹理矩形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount);

        //GLES20.glFlush();
//        GLES20.glFinish();
    }
}
