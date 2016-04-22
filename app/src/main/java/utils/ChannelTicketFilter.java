package utils;

import android.content.Context;
import android.text.Spannable;
import android.text.Spanned;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by huangjinwen on 2014/6/19.
 */
public class ChannelTicketFilter extends AirTicketFilter {

    public ChannelTicketFilter(int id) {
        super(id);
    }

    public class ChannelTicketClickSpan extends TicketClickSpan {
        final long channelSid;
        final long channelSubSid;

        public ChannelTicketClickSpan(long sid, long subSid) {
            channelSid = sid;
            channelSubSid = subSid;
        }

        public long getChannelSid() {
            return channelSid;
        }

        public long getChannelSubSid() {
            return channelSubSid;
        }

    }

    public static class ChannelTicketInfo {
        public int start;
        public int end;
        public long sid;
        public long subSid;

        public ChannelTicketInfo(int start, int end, long sid, long subSid) {
            this.start = start;
            this.end = end;
            this.sid = sid;
            this.subSid = subSid;
        }

        @Override
        public String toString() {
            return "[start = " + start + "; end = " + end + "; sid = " + sid + "; subSid = " + subSid + "]";
        }
    }

    /**
     * 飞机票的格式：
     * 1.yy://123456
     * 2.yy://pd-[123456]/[123456]
     * 3.yy://pd-[sid=123456&subid=123456]
     * 4.yy://pd-[sid=123456&subid=123456]/[123456]
     * 5.yy://pd-[sid=2368&subid=68112887&type=wonder_channel]
     * 6.yy://pd-[sid=2368&subid=68112887&type=wonder_channel]/[2368]
     * 7.yy://pd-123456
     * 8.yy://pd-123456/[123456]
     * 9.可能存在各种奇形怪状的飞机票格式？
     */
    private static final String AIR_TICKET_REG = "(yy://(pd-)?(\\[?(sid=)?([0-9]+)(&subid=[0-9]+)?(&type=[^\\[\\]]+)?\\]?)(/\\[[0-9]+\\]*)?)";//\\(?(yy://)(pd-\\[)*[0-9]+(]/\\[)*[0-9]+(])*";
    //private static final String AIR_TICKET_WITH_SUB_CHANNEL_REG = "(yy://pd-((\\[])|([^].]+))(/\\[[0-9]]+])?)";//"\\(?(yy://)(pd-\\[sid=)([0-9]+)(&subid=)([0-9]+)(]/\\[)[0-9]+(])";
    private static final Pattern AIR_TICKET_PATTERN = Pattern.compile(AIR_TICKET_REG);
    //private static final Pattern AIR_TICKET_WITH_SUB_CHANNEL_PATTERN = Pattern.compile(AIR_TICKET_WITH_SUB_CHANNEL_REG);
    private static final String NUMBER_REG = "[0-9]+";
    private static final Pattern NUMBER_PATTERN = Pattern.compile(NUMBER_REG);

    public static List<ChannelTicketInfo> parseChannelTicket(String msg) {
        List<ChannelTicketInfo> infoList = new ArrayList<ChannelTicketInfo>();

        Matcher matcher = AIR_TICKET_PATTERN.matcher(msg);
//        while (matcher.find()) {
//            MLog.debug("hjinw", "matcher0 = " + matcher + "; start = " + matcher.start() + "; end = " + matcher.end());
//            String channelMsg = msg.substring(matcher.start(), matcher.end());
//            Matcher m = NUMBER_PATTERN.matcher(channelMsg);
//            try {
//                if (m.find()) {
//                    infoList.add(new ChannelTicketInfo(matcher.start(), matcher.end(), Integer.parseInt(channelMsg.substring(m.start(), m.end())), 0));
//                    msg = msg.substring(matcher.end(), msg.length());
//                    MLog.debug("hjinw", "msg = " + msg);
//                }
//                //MLog.debug("hjinw", "info = " + infoList.get(0));
//            } catch (NumberFormatException e) {
//                MLog.error("hjinw", "parse Channel sid or subSid error :%s", e);
//                //Dialogs.showTip("该频道不存在");
//            }
//        }
//
//        matcher = AIR_TICKET_WITH_SUB_CHANNEL_PATTERN.matcher(msg);
        while (matcher.find()) {
            //MLog.debug("hjinw", "matcher = " + matcher + "; start = " + matcher.start() + "; end = " + matcher.end());
            String channelMsg = msg.substring(matcher.start(), matcher.end());
            //MLog.debug("hjinw", "channelMsg = " + channelMsg);
            Matcher m = NUMBER_PATTERN.matcher(channelMsg);
            long sid = 0;
            long subSid = 0;
            try {
                if (m.find()) {
                    sid = Long.parseLong(channelMsg.substring(m.start(), m.end()));
                }
                if (m.find()) {
                    //有些子频道的id的长度已经超过31位，但还在32位之内，所以用long来保存
                    subSid = Long.parseLong(channelMsg.substring(m.start(), m.end()));
                }
            } catch (NumberFormatException e) {

            }
            infoList.add(new ChannelTicketInfo(matcher.start(), matcher.end(), sid, subSid));
        }
        return infoList;
    }

    @Override
    public void parseSpannable(Context context, Spannable spannable, int maxWidth) {
        parseSpannable(context,spannable,maxWidth,null);
    }

    @Override
    public void parseSpannable(Context context, Spannable spannable, int maxWidth, Object tag) {
        if (!isChannelTicketMessage(spannable)) {
            return;
        }
        if (ticketDrawable == null) {
            ticketDrawable = getTicketDrawable(context);
        }
        setSpannable(spannable);
    }

    private void setSpannable(Spannable spannable) {
        List<ChannelTicketInfo> infos = parseChannelTicket(spannable.toString());
        for (ChannelTicketInfo info : infos) {
            AirTicketSpan airTicketSpan = new AirTicketSpan(ticketDrawable, String.valueOf(info.sid));
            ChannelTicketClickSpan clickSpan = new ChannelTicketClickSpan(info.sid, info.subSid);
            setSpannable(FP.toList(new Object[]{airTicketSpan, clickSpan}), spannable, info.start, info.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public static boolean isChannelTicketMessage(CharSequence msg) {
        return AIR_TICKET_PATTERN.matcher(msg).find() /*|| AIR_TICKET_WITH_SUB_CHANNEL_PATTERN.matcher(msg).find()*/;
    }

    public static String replaceChannelTicketWithGivenStr(String message, String givenStr) {
        // 清除掉所有特殊字符
        Matcher m = AIR_TICKET_PATTERN.matcher(message);
        return m.replaceAll(givenStr).trim();
    }
}
