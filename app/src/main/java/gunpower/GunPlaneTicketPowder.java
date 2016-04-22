package gunpower;

import android.content.Context;
import android.graphics.*;
import com.duowan.danmu.R;
import newgunpower.GunNewPower;
import utils.BasicConfig;
import utils.ChannelTicketFilter;
import utils.DimenConverter;

/**
 * Created by jay_zs on 15/12/23.
 */
public class GunPlaneTicketPowder extends GunNewPower {

    private final Matrix mShaderMatrix = new Matrix();

    public GunPlaneTicketPowder(int gunId, String content, String nickName,int sid, int subid, boolean mIsMyself) {
        super(gunId, content, sid, subid, mIsMyself);
        this.nickName = nickName;
    }


    public void createPowertoShell(Context context) {
        Bitmap planeTicketIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg_planeticket);

        int giftHeight = 0;
        int giftWidth = 0;
        int offsetLeft = 0;
        int avatarWidth = 0;
        Paint mPainter;
        Paint mAvatarBorderPaint;
        Paint myPainter;

//        RectF powerAvatarRect = new RectF();
//        Rect powerAvatarRect1 = new Rect();

        mPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPainter.setTextSize(DimenConverter.sp2px(BasicConfig.getInstance().getAppContext(), mTextSize));
        mPainter.setColor(Color.parseColor("#b5d000"));//飞机票弹幕颜色
        mPainter.setTextAlign(Paint.Align.LEFT);

        myPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
        myPainter.setTextSize(DimenConverter.sp2px(BasicConfig.getInstance().getAppContext(), mTextNicknameSize));
        myPainter.setColor(Color.parseColor("#b5d000"));//
        myPainter.setTextAlign(Paint.Align.LEFT);

        mAvatarBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAvatarBorderPaint.setAntiAlias(true);
        mAvatarBorderPaint.setColor(Color.WHITE);

        mPainter.setShadowLayer(5f, 3, 0, Color.BLACK);//0x33000000
        mAvatarBorderPaint.setShadowLayer(5f, 3, 0, Color.BLACK);//0x33000000
        String str1 = ChannelTicketFilter.replaceChannelTicketWithGivenStr(this.content, "");

        String content = str1+"点击进入"+this.sid+"频道";
        if(str1.length() >= 15){
            content = str1.substring(0,15)+"..."+"点击进入"+this.sid+"频道";
        }
//        content = this.nickName + content;

        float baseline = (int) (-mPainter.ascent() + 0.5f); // ascent() is negative

        int width = (int) (mPainter.measureText(this.nickName+content+"  "
            ) + 2.0f); // round


        //int textHeight = (int) (baseline + mPainter.descent() + 0.5f);

        giftHeight = DimenConverter.dip2px(BasicConfig.getInstance().getAppContext(), 20);
        giftWidth = DimenConverter.dip2px(BasicConfig.getInstance().getAppContext(), 20);
        offsetLeft = DimenConverter.dip2px(BasicConfig.getInstance().getAppContext(), 8);
        avatarWidth = DimenConverter.dip2px(BasicConfig.getInstance().getAppContext(), 12);
//        Bitmap planeBmp = zoom(planeTicketIcon,giftWidth,textHeight+giftHeight);
        int offsetRight = DimenConverter.dip2px(BasicConfig.getInstance().getAppContext(), 20);
        int avatarHeight = DimenConverter.dip2px(BasicConfig.getInstance().getAppContext(), 24);

        int actualWidth = (int) (width + offsetLeft + giftWidth+offsetRight);

        Bitmap bitmap = Bitmap.createBitmap(actualWidth, avatarHeight, Bitmap.Config.ARGB_8888);

        //利用bitmap生成画布
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
//        float radius =  (float) textHeight / 2;
//        powerAvatarRect.set(0, 0, actualWidth, textHeight);
//        powerAvatarRect1.set(0, 0, actualWidth, textHeight);
//        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//        updateShaderMatrix(shader, bitmap.getWidth(), bitmap.getHeight(), powerAvatarRect1);
//        canvas.drawRoundRect(powerAvatarRect, radius, radius, mAvatarBorderPaint);
//        mAvatarBorderPaint.setShader(shader);
//        canvas.drawCircle(0, 0, textHeight / 2, mAvatarBorderPaint);


        int save = canvas.save();
        canvas.translate(offsetLeft, 0);
        Paint strokePaint = new Paint(mPainter);
        strokePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        strokePaint.setStrokeWidth(3);
        strokePaint.setColor(Color.BLACK);
        Paint strokeMyPaint = new Paint(myPainter);
        strokeMyPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        strokeMyPaint.setStrokeWidth(3);
        strokeMyPaint.setColor(Color.BLACK);
        int posX = 0;
        canvas.drawText(this.nickName+" : ", posX,  baseline, strokeMyPaint);
        canvas.drawText(this.nickName+" : ", posX,  baseline, myPainter);
        posX += myPainter.measureText(this.nickName) +avatarWidth;
        canvas.drawBitmap(planeTicketIcon, posX,0 , strokePaint);
        canvas.drawBitmap(planeTicketIcon, posX,0 , mPainter);
        posX += giftWidth + 3.0;
        canvas.drawText(content, posX, baseline, strokePaint);
        canvas.drawText(content, posX, baseline, mPainter);
        canvas.restoreToCount(save);
        this.bitmap = bitmap;
    }


    private Bitmap zoom(Bitmap oriBmp,int dstWidth, int dstHeight) {

        int width = oriBmp.getWidth();
        int height = oriBmp.getHeight();

        float scaleWidth = ((float) dstWidth) / width;
        float scaleHeight = ((float) dstHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        return Bitmap.createBitmap(oriBmp, 0, 0,
                width, height, matrix, true);

    }


    protected void updateShaderMatrix(BitmapShader bitmapShader, int bitmapWidth, int bitmapHeight, Rect drawableRect) {
        float scale;
        float dx = 0;
        float dy = 0;

        mShaderMatrix.set(null);

        if (bitmapWidth * drawableRect.height() > drawableRect.width() * bitmapHeight) {
            scale = drawableRect.height() / (float) bitmapHeight;
            dx = (drawableRect.width() - bitmapWidth * scale) * 0.5f;
        } else {
            scale = drawableRect.width() / (float) bitmapWidth;
            dy = (drawableRect.height() - bitmapHeight * scale) * 0.5f;
        }

        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate((int) (dx + 0.5f) + 2, (int) (dy + 0.5f) + 2);

        bitmapShader.setLocalMatrix(mShaderMatrix);
    }


    public void setCount(String count){
        mCount = count;
    }

    @Override
    public String toString() {
        return "GunPowder{" +
                "mPowder'" + this.content+ '\'' +
                ", mIsMyself='" + mIsMyself + '\'' +
                '}';
    }
}
