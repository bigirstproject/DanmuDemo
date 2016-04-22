package core;


import java.util.LinkedList;

/**
 * Created by jay_zs on 16/1/13.
 */
public interface ICommonScreenCore{

    /**
     *  公屏缓存的数据
     * @return
     */
    public LinkedList<ChannelMessage> getChatCacheList();

    /**
     * onPause
     */
    public void onPause();

    /**
     * onResume
     */
    public void onResume();

    /**
     * 设置弹幕转态：ture：打开，fasle：关闭
     *
     * @param isReceive
     */
    public void setReceiveStatus(boolean isReceive);

    /**
     * 获取弹幕转态：ture：打开，fasle：关闭
     *
     * @return
     */
    public boolean getReceiveStatus();

    /**
     * 设置发言缓存的数据
     *
     * @param message
     */
    public void setMineMessage(ChannelMessage message);

    /**
     * 得到我发言的缓存
     *
     * @return
     */
    public LinkedList<ChannelMessage> getMineMessageCache();

    /**
     * 清除数据
     */
    public void clear();

    /**
     * onDestroy
     */
    public void onDestroy();

}
