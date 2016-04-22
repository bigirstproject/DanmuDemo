package core;

public class ChannelMessage {
    public String nickname;//昵称
    public String text;//文字
    public int uid;//uid
    public long sid;//当前频道id
    /**爵位类别*/
    public int nobleLevel;
    public String avatarUrl;//手机开播的url头像
    public String gifUri;//扩展的gifuir 属性
    public  int channel_message_type=0;//消息类型
    public static final int NOBLEEMOTION_MESSAGE_TYPE =1000;//贵族消息类型
//    public static final int

    @Override
    public String toString() {
        return "ChannelMessage{" +
                "nickname='" + nickname + '\'' +
                ", text='" + text + '\'' +
                ", uid=" + uid +
                ", sid=" + sid +
                ", nobleLevel=" + nobleLevel +
                ", channel_message_type=" + channel_message_type +
                ", gifUri=" + gifUri +
                ", avatarUrl = "+avatarUrl+
                '}';
    }
}
