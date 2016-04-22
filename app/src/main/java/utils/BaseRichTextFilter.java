package utils;

import android.content.Context;
import android.text.Spannable;
import android.text.style.CharacterStyle;
import android.view.View;

import java.util.List;

/**
 * Created by huangjinwen on 2014/6/23.
 */
public abstract class BaseRichTextFilter {
    public interface OnSpanClickListener {
        public void onClick(View view, Object what);
    }
    protected OnSpanClickListener mOnSpanClickListener;
    public void setSpanClickListener(OnSpanClickListener listener) {
        mOnSpanClickListener = listener;
    }

    protected void setSpannable(List<Object> list, Spannable spannable, int start, int end, int flags) {
        if (removeOrIgnoreSpan(spannable, start, end)) {
            return;
        }
        for (Object what : list) {
            spannable.setSpan(what, start, end, flags);
        }
    }

    protected void setSpannable(Object what, Spannable spannable, int start, int end, int flags) {
        if (removeOrIgnoreSpan(spannable, start, end)) {
            return;
        }
        spannable.setSpan(what, start, end, flags);
    }

    /**
     * 判断当前位置是否存在span, 如果后一个span覆盖前一个span，则把前一个span移除。
     * 如果span存在冲突，则忽略设置后一个span
     * @param spannable
     * @param start 需要设置的span的起始位置
     * @param end 需要设置的span的结束位置
     * @return
     */
    protected boolean removeOrIgnoreSpan(Spannable spannable, int start, int end) {
        CharacterStyle[] spans = spannable.getSpans(0, spannable.length(), CharacterStyle.class);
        for (CharacterStyle span : spans) {
            int spanStart = spannable.getSpanStart(span);
            int spanEnd = spannable.getSpanEnd(span);
            if (start <= spanStart && end > spanStart) {
                spannable.removeSpan(span);
                return false;
            } else if ((start > spanStart && start < spanEnd) || (end > spanStart && end < spanEnd)){
                return true;
            }
        }
        return false;
    }

    public abstract void parseSpannable(Context context, Spannable spannable, int maxWidth);

    public void parseSpannable(Context context, Spannable spannable, int maxWidth, int normal){

    }

    public abstract void parseSpannable(Context context, Spannable spannable, int maxWidth, Object tag);
}
