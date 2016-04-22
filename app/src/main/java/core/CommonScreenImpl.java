
package core;

import android.os.Looper;
import android.util.Log;

import java.util.LinkedList;

import utils.asynctask.SafeDispatchHandler;
import utils.asynctask.ScheduledTask;

/**
 * Created by jay_zs on 16/1/13.
 */
public class CommonScreenImpl implements ICommonScreenCore {

    private static final String TAG = CommonScreenImpl.class.getSimpleName();

    // 公屏缓存信息列表
    private LinkedList<ChannelMessage> chatCacheList;
    //自己发的信息
    private LinkedList<ChannelMessage> myChannelMessae;

    //true： 接受公屏的数据； false：不接受公屏的数据 ;默认是不接收公屏数据
    private boolean isReceive;

    //true： 接受公屏的数据； false：不接受公屏的数据 ;默认是不接收公屏数据
    private boolean onPause;

    //安全线程
    private SafeDispatchHandler safeDispatchHandler = new SafeDispatchHandler(Looper.getMainLooper());

    public CommonScreenImpl() {
        chatCacheList = new LinkedList<>();
        myChannelMessae = new LinkedList<>();
    }


    public class safeDispatchHandlerRun implements Runnable {

        public ChannelMessage channelMessage;

        public safeDispatchHandlerRun(ChannelMessage channelMessage) {
            this.channelMessage = channelMessage;
        }

        @Override
        public void run() {
            if (channelMessage != null) {
                //如果公屏的数据超过300 条就直接要显示完在接受数据
                if (chatCacheList.size() >= 300) {
                    return;
                }
                chatCacheList.addLast(channelMessage);
            }
        }
    }

    /**
     * 添加公屏信息
     */
    public void appendChannelMessage(final ChannelMessage message) {
        Log.d(TAG, "[appendChannelMessage] : message.nickname = " + message.nickname + " length = " + message.text.length() + " message.text = " + message.text + ";uid = " + message.uid + "  onPause = " + onPause +"  time = "+System.currentTimeMillis());
        if (onPause) {
            return;
        }
        if (message == null) {
            return;
        }
        if (message.text == null) {
            return;
        }
        ScheduledTask.getInstance().scheduledDelayed(new Runnable() {
            @Override
            public void run() {
                message.text = message.text.trim();
                if (message != null) {
                    safeDispatchHandler.postDelayed(new safeDispatchHandlerRun(message), 100);
                }
            }
        }, 0);
    }

    /**
     * get 公屏聊天信息
     *
     * @return
     */
    public LinkedList<ChannelMessage> getChatCacheList() {
        if (chatCacheList != null && chatCacheList.size() >= 0) {
            return chatCacheList;
        }
        return chatCacheList;
    }


    @Override
    public void onResume() {
        this.onPause = false;
        Log.d(TAG, "onResume onPause = " + onPause);
    }

    @Override
    public void setReceiveStatus(boolean isReceive) {
        this.isReceive = isReceive;
    }

    @Override
    public boolean getReceiveStatus() {
        return isReceive;
    }

    /**
     * set 自己发送消息的缓存
     *
     * @param message
     */
    @Override
    public void setMineMessage(ChannelMessage message) {
        if (!onPause && message != null && myChannelMessae != null) {
            myChannelMessae.offer(message);
        }
    }

    /**
     * get 自己发送消息的缓存
     *
     * @return
     */
    @Override
    public LinkedList<ChannelMessage> getMineMessageCache() {
        if (myChannelMessae != null && myChannelMessae.size() >= 0) {
            Log.d(TAG, myChannelMessae.toString());
            return myChannelMessae;
        }
        return myChannelMessae;
    }


    @Override
    public void onPause() {
        this.onPause = true;
        Log.d(TAG, "onPause onPause = " + onPause);
    }

    @Override
    public void clear() {
        if (chatCacheList != null && chatCacheList.size() >= 0) {
            chatCacheList.clear();
        }
        if (myChannelMessae != null && myChannelMessae.size() >= 0) {
            myChannelMessae.clear();
        }
        isReceive = false;
    }

    @Override
    public void onDestroy() {
        if (chatCacheList != null && chatCacheList.size() >= 0) {
            chatCacheList.clear();
        }
        if (myChannelMessae != null && myChannelMessae.size() >= 0) {
            myChannelMessae.clear();
        }
        isReceive = false;
    }

}
