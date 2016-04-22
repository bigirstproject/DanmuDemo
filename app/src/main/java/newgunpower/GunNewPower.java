package newgunpower;

import android.content.Context;
import android.graphics.*;
import utils.BasicConfig;
import utils.DimenConverter;
import utils.EmoticonFilter;

/**
 * Created by jay_zs on 16/1/28.
 */
public class GunNewPower {
    public int gunId;

    public String mCount = "";
    public boolean mIsMyself = false;

    public int width;//弹幕的宽度
    public int height;//弹幕的高度

    public String content;

    public String nickName;
    public int sid;
    public int subid;

    public Bitmap bitmap;

    public float mTextSize = 16;

    public float mTextNicknameSize = 13;

    private final Matrix mShaderMatrix = new Matrix();

    public void createPowertoShell(Context context) {

//        RectF powerAvatarRect = new RectF();
//        Rect powerAvatarRect1 = new Rect();
//        Bitmap giftDefaultIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_default_gift);
//        Bitmap avatarDefaultIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.default_portrait);

        Paint myPaint; //画笔
        Paint mPainter;
        Paint mAvatarBorderPaint;
        Paint nickNamePaint;//昵称的画笔
        Paint myNickNamePaint;//自己昵称的画笔


        nickNamePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nickNamePaint.setTextSize(DimenConverter.sp2px(BasicConfig.getInstance().getAppContext(), mTextNicknameSize));
        nickNamePaint.setColor(Color.WHITE);//
        nickNamePaint.setTextAlign(Paint.Align.LEFT);


        mPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPainter.setTextSize(DimenConverter.sp2px(BasicConfig.getInstance().getAppContext(), mTextSize));
        mPainter.setColor(Color.WHITE);//
        mPainter.setTextAlign(Paint.Align.LEFT);


        myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        myPaint.setTextSize(DimenConverter.sp2px(BasicConfig.getInstance().getAppContext(), mTextSize));
        myPaint.setColor(Color.parseColor("#FF8900"));//我发言的样式
        myPaint.setTextAlign(Paint.Align.LEFT);

        myNickNamePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        myNickNamePaint.setTextSize(DimenConverter.sp2px(BasicConfig.getInstance().getAppContext(), mTextNicknameSize));
        myNickNamePaint.setColor(Color.parseColor("#FF8900"));//我发言的样式
        myNickNamePaint.setTextAlign(Paint.Align.LEFT);

        mAvatarBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAvatarBorderPaint.setAntiAlias(true);
        mAvatarBorderPaint.setColor(Color.parseColor("#99000000"));

        myPaint.setShadowLayer(BasicConfig.getInstance().getAppContext().getResources().getDisplayMetrics().density + 1, BasicConfig.getInstance().getAppContext().getResources().getDisplayMetrics().density, 1F, Color.BLACK);//0x33000000
        myNickNamePaint.setShadowLayer(BasicConfig.getInstance().getAppContext().getResources().getDisplayMetrics().density + 1, BasicConfig.getInstance().getAppContext().getResources().getDisplayMetrics().density, 1F, Color.BLACK);//0x33000000
        mPainter.setShadowLayer(BasicConfig.getInstance().getAppContext().getResources().getDisplayMetrics().density + 1, BasicConfig.getInstance().getAppContext().getResources().getDisplayMetrics().density, 1F, Color.BLACK);//0x33000000
        nickNamePaint.setShadowLayer(BasicConfig.getInstance().getAppContext().getResources().getDisplayMetrics().density + 1, BasicConfig.getInstance().getAppContext().getResources().getDisplayMetrics().density, 1F, Color.BLACK);//0x33000000
        float baseline = (int) (-mPainter.ascent() + 7.5f); // ascent() is negative
        int width = 0;
        if (this.mIsMyself) {
            baseline = (int) (-mPainter.ascent() + 0.5f);
            width = (int) (mPainter.measureText(this.nickName + ": " + this.content) + 0.5f); // round
        } else {
            width = (int) (mPainter.measureText(this.nickName + ": " + this.content) + 0.5f); // round
        }
        int textHeight = (int) (baseline + mPainter.descent() + 4.5f);
        int offsetLeft = DimenConverter.dip2px(BasicConfig.getInstance().getAppContext(), 8);
        int offsetRight = DimenConverter.dip2px(BasicConfig.getInstance().getAppContext(), 8);
//        int avatarWidth = DimenConverter.dip2px(BasicConfig.getInstance().getAppContext(), 24);
        int avatarHeight = DimenConverter.dip2px(BasicConfig.getInstance().getAppContext(), 24);
        float len;
        if (this.mIsMyself) {
            len = EmoticonFilter.measureWidth(BasicConfig.getInstance().getAppContext(), this.content, 0, baseline, width, textHeight, mPainter, EmoticonFilter.SMILE_TYPE_NEW);
        } else {
            len = EmoticonFilter.measureWidth(BasicConfig.getInstance().getAppContext(), this.nickName + ": " + this.content, 0, baseline, width, textHeight, mPainter, EmoticonFilter.SMILE_TYPE_NEW);
        }
        int actualWidth = 0;
        if (this.content != null && EmoticonFilter.isContainNewSmile(this.content, BasicConfig.getInstance().getAppContext())) {
            //    MLog.info("GunPower", "isEmtionFilter len " + len);
            actualWidth = (int) (len + offsetLeft + offsetRight + width);
        } else {
            //    MLog.info("GunPower","isEmtionFilter width "+len);
            actualWidth = (int) (offsetRight + width + offsetLeft);
        }

        Bitmap bitmap = Bitmap.createBitmap(actualWidth, avatarHeight, Bitmap.Config.ARGB_8888);
        //利用bitmap生成画布
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
//        float radius =  (float) textHeight / 2;
//        powerAvatarRect.set(0, 0, actualWidth, textHeight);
//        powerAvatarRect1.set(0, 0, actualWidth, textHeight);
//        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//        updateShaderMatrix(shader, bitmap.getWidth(),bitmap.getHeight(), powerAvatarRect1);
//        canvas.drawRoundRect(powerAvatarRect, radius, radius, mAvatarBorderPaint);
//        mAvatarBorderPaint.setShader(shader);

//        canvas.drawCircle(0, 0, textHeight / 2, mAvatarBorderPaint);
        int save = canvas.save();
//        canvas.translate(offsetLeft, 0);
        canvas.translate(offsetLeft, 0);
        // canvas.drawText(ammo.mPowder, 5, baseline, mPainter);
        if (this.mIsMyself) {

            int posX = 0;
            this.nickName = this.nickName + " : ";
//            canvas.drawText(this.nickName, posX,  baseline, myNickNamePaint);
            EmoticonFilter.parseToBitmapStroke(BasicConfig.getInstance().getAppContext(), canvas, this.nickName, posX, baseline, (int) len, textHeight, myNickNamePaint, EmoticonFilter.SMILE_TYPE_GENERAL);

            posX += nickNamePaint.measureText(this.nickName);
            EmoticonFilter.parseToBitmapStroke(BasicConfig.getInstance().getAppContext(), canvas, this.content, posX, baseline, (int) len, textHeight, myPaint, EmoticonFilter.SMILE_TYPE_GENERAL);
        } else {
            int posX = 0;
            this.nickName = this.nickName + " : ";
            //  canvas.drawText(this.nickName, posX,  baseline, nickNamePaint);
            EmoticonFilter.parseToBitmapStroke(BasicConfig.getInstance().getAppContext(), canvas, this.nickName, posX, baseline, (int) len, textHeight, nickNamePaint, EmoticonFilter.SMILE_TYPE_GENERAL);


            posX += nickNamePaint.measureText(this.nickName);
            EmoticonFilter.parseToBitmapStroke(BasicConfig.getInstance().getAppContext(), canvas, this.content, posX, baseline, (int) len, textHeight, mPainter, EmoticonFilter.SMILE_TYPE_GENERAL);
        }


//        int save = canvas.save();
//        canvas.translate(offsetLeft, 0);
//        int posX = 0;
//        canvas.drawText(this.nickName, posX,  baseline, mPainter);
//        posX += mPainter.measureText(this.nickName) +avatarWidth;
//        canvas.drawBitmap(planeTicketIcon, posX, 0, mPainter);
//        posX += giftWidth + 6;
//        canvas.drawText(content, posX, baseline, mPainter);
//        canvas.restoreToCount(save);

        canvas.restoreToCount(save);
        this.bitmap = bitmap;
    }

    public GunNewPower(int gunId, String content, boolean mIsMyself) {
        this.content = content;
        this.mIsMyself = mIsMyself;
        this.gunId = gunId;

    }

    public GunNewPower(int gunId, String content, int sid, int subid, boolean mIsMyself) {
        this.content = content;
        this.gunId = gunId;
        this.sid = sid;
        this.subid = subid;
        this.mIsMyself = mIsMyself;
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

}
