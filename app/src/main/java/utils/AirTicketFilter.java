package utils;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.View;

/**
 * Created by huangjinwen on 2014/8/27. by huangjinwen
 */
public abstract class AirTicketFilter extends BaseRichTextFilter {
    protected Drawable ticketDrawable;
    private final int resId;
    public AirTicketFilter(int id) {
        resId = id;
    }

    public class AirTicketSpan extends ImageSpan {
        String content;
        private int maxWidth = Integer.MAX_VALUE;

        public AirTicketSpan(Drawable drawable, String text) {
            super(drawable);
            this.content = text;
        }

        public AirTicketSpan(Drawable drawable, String text, int maxWidth) {
            super(drawable);
            this.content = text;
            this.maxWidth = maxWidth;
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            Drawable d = getDrawable();
            Rect rect = d.getBounds();
            if (fm != null) {
                fm.ascent = -rect.bottom;
                fm.descent = 0;

                fm.top = fm.ascent;
                fm.bottom = 0;
            }
            int width = rect.width();
            int height = rect.height();
            float textWidth = paint.measureText(content);
            width = Math.max(width, (int) (textWidth + width / 3 + 30));
            width = Math.min(width, maxWidth);
            d.setBounds(0, 0, width, height);
            return width;
        }

        @Override
        public void updateMeasureState(TextPaint p) {
            super.updateMeasureState(p);
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            Paint.FontMetricsInt fm = paint.getFontMetricsInt();
            float textHeight = (float) Math.ceil(fm.descent - fm.ascent);
            float textWidth = paint.measureText(content);
            Drawable drawable = getDrawable();
            Rect rect = drawable.getBounds();
            int width = rect.width();
            int height = rect.height();
            float textX = x + width / 3;
            float textY = top + textHeight + fm.descent;

            super.draw(canvas, text, start, end, x, top, y, bottom, paint);
            canvas.save();

            canvas.translate(textX, textY);
            paint.setColor(0xff5194d6);
            //paint.setTextSize(22);
            paint.setUnderlineText(true);
            canvas.drawText(content, 0, 0, paint);
            canvas.restore();
        }
    }

    public class TicketClickSpan extends ClickableSpan {

        @Override
        public void onClick(View view) {
            if (mOnSpanClickListener != null) {
                mOnSpanClickListener.onClick(view, this);
            }

        }
    }

    protected Drawable getTicketDrawable(Context context) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        byte[] chunk = bitmap.getNinePatchChunk();
        ticketDrawable = new NinePatchDrawable(context.getResources(), bitmap, chunk, null, null);
        int width = ticketDrawable.getIntrinsicWidth();
        int height = ticketDrawable.getIntrinsicHeight();
        ticketDrawable.setBounds(0, 0, width > 0 ? width : 0, height > 0 ? height : 0);
        return ticketDrawable;
    }

}
