package newgunpower;

import android.widget.RelativeLayout;

/**
 * Created by dexian on 2016/3/14.
 */
public interface IDanmakuCore {

    /**
     * set弹幕开关
     * @param status
     */
    public void queryDanmuOpenStatus(IDanmuOpenStatus status);

    /**
     * 设置高度
     *
     * @param width
     */
    public void setScreenWidth(float width);

    /**
     * 设置屏幕高度
     * @param isLandscape
     * @param screenHeight
     */
    public void moveScreenHeight(boolean isLandscape, int screenHeight);

    /**
     * set LayoutParams
     * @param layoutParams
     */
    public void setLayoutParams(RelativeLayout.LayoutParams layoutParams);

    /**
     * set 两个弹幕之间的距离
     * @param KTopMargin
     */
    public void setDanmakuMargin(int KTopMargin);

    /**
     * get 两个弹幕之间的距离
     *
     * @return
     */
    public int getDanmakuMargin();

    /**
     * 弹幕状态：true：打开，false：关闭
     *
     * @return
     */
    public boolean onDanmuSwitchState();

    /**
     * 打开弹幕
     */
    public void setOpenView();

    /**
     * 关闭弹幕
     */
    public void setCloseView();

    /**
     * 设置弹幕速度
     * @param speed
     */
    public void setDanmuSpeed(float speed);

    /**
     * 设置弹幕行数
     *
     * @param lines
     */
    public void setLines(int lines);

    /**
     * 设置弹幕行距
     *
     * @param lineSpace
     */
    public void setLineSpace(int lineSpace);

    /**
     * 设置每个弹道的开关
     * @param position
     * @param isOpen
     */
    public void setDanmuRoadSwitchStatus(int position, boolean isOpen);

    /**
     * 发送一条弹幕
     * @param gun
     * @param position
     */
    public void sendGunPower(GunNewPower gun, int position);

    /**
     * 设置弹幕onItemClick
     * @param clickListener
     */
    public void setOnClickListener(IDanmuClickListener clickListener);


    /**
     * 弹幕开关switchListener
     * @param listener
     */
    public void setOnSwitchListener(IDanmuSwitchListener listener);

}
