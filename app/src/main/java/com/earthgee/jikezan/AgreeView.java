package com.earthgee.jikezan;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by earthgee on 17/10/16.
 *
 * 左边bitmap使用一个动画控制缩放，右边文本通过一个动画控制偏移距离
 *
 */

public class AgreeView extends View{
    //点赞数
    private int zanNum = 0;

    private Paint paintTop=new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint paintBottom=new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint stablePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint circlePaint=new Paint(Paint.ANTI_ALIAS_FLAG);

    private String lastStr=zanNum+"";
    private String nextStr=zanNum+"";
    //动作的方向，0为add，1为sub
    private int direction;
    private int moveDistance;
    private int stable=0;

    private static final int TEXT_SIZE=50;
    private static final int TEXT_SPACING=80;

    //绘制内容的基本坐标位置，大概在屏幕中央
    private int middleX;
    private int topY=0;
    private int middleY=0;
    private int bottomY=0;

    private Bitmap unSelectBitmap;
    private Bitmap selectBitmap;
    private Bitmap selectShinBitmap;
    private float scaleProgress=1.0f;

    private ObjectAnimator animator=ObjectAnimator.ofInt(this,"move",0,TEXT_SPACING);
    private ObjectAnimator bitmapAnimator=ObjectAnimator.ofFloat(this,"scale",1,0.8f,1.2f,1);

    public AgreeView(Context context) {
        super(context);
        init();
    }

    public AgreeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AgreeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化画笔，动画，bitmap
     */
    private void init(){
        setPaintStyle(paintTop);
        setPaintStyle(paintBottom);
        setPaintStyle(stablePaint);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(10);
        circlePaint.setColor(Color.rgb(237,109,0));
        circlePaint.setAlpha(20);
        animator.setDuration(300);
        animator.setInterpolator(new LinearInterpolator());
        bitmapAnimator.setDuration(300);
        bitmapAnimator.setInterpolator(new LinearInterpolator());
        unSelectBitmap=BitmapFactory.decodeResource(getResources(),R.drawable.ic_messages_like_unselected);
        selectBitmap=BitmapFactory.decodeResource(getResources(),R.drawable.ic_messages_like_selected);
        selectShinBitmap=BitmapFactory.decodeResource(getResources(),R.drawable.ic_messages_like_selected_shining);
    }

    private void setPaintStyle(Paint paint){
        paint.setColor(Color.GRAY);
        paint.setTextSize(TEXT_SIZE);
    }

    /**
     * +1s
     */
    public void add(){
        direction=0;
        measureTextChange();
        animator.start();
        bitmapAnimator.start();
    }

    /**
     * -1s
     */
    public void sub(){
        direction=1;
        measureTextChange();
        animator.start();
        bitmapAnimator.start();
    }

    /**
     * 获得开始的文本和结束的文本，并计算出不需要变动的文本
     * 如11-->12,lastStr=11,nextStr=12,stable=1
     */
    private void measureTextChange(){
        if(direction==0){
            //add
            lastStr=zanNum+"";
            zanNum++;
            nextStr=zanNum+"";
        }else{
            nextStr=zanNum+"";
            zanNum--;
            lastStr=zanNum+"";
        }

        if(lastStr.length()!=nextStr.length()){
            //补齐长度
            if(lastStr.length()<nextStr.length()){
                for(int i=0;i<(nextStr.length()-lastStr.length());i++){
                    lastStr=lastStr+" ";
                }
            }else{
                for(int i=0;i<(lastStr.length()-nextStr.length());i++){
                    nextStr=nextStr+" ";
                }
            }
        }

        assert lastStr.length()==nextStr.length();

        stable=0;
        for(int i=0;i<lastStr.length();i++){
            if(lastStr.charAt(i)==nextStr.charAt(i)){
                stable++;
            }
        }
    }

    /**
     * 动画触发文本偏移距离
     *
     * @param distance
     */
    public void setMove(int distance){
        this.moveDistance=distance;
        invalidate();
    }

    /**
     * 动画触发bitmap缩放
     * @param progress
     */
    public void setScale(float progress){
        this.scaleProgress=progress;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //第一次绘制时确定坐标
        if(middleY==0){
            middleX=getWidth()/2;
            middleY=getHeight()/2;
            topY=middleY-TEXT_SPACING;
            bottomY=middleY+TEXT_SPACING;
        }

        int paintTopY=0;
        int paintBottomY=0;

        //通过两个paint绘制内容的偏移制造出移动的动效,例如add时，两个文本都向上移动，上面的文本透明度减小，下面的文本透明度增加
        if(direction==0){
            paintTopY=middleY-moveDistance;
            int paintTopAlpha=(int)(((float)(TEXT_SPACING-moveDistance)/TEXT_SPACING)*255);
            paintTop.setAlpha(paintTopAlpha);

            paintBottomY=bottomY-moveDistance;
            int paintBottomAlpha=(int)(((float)moveDistance/TEXT_SPACING)*255);
            paintBottom.setAlpha(paintBottomAlpha);
        }else{
            paintTopY=topY+moveDistance;
            int paintTopAlpha=(int)(((float)moveDistance/TEXT_SPACING)*255);
            paintTop.setAlpha(paintTopAlpha);

            paintBottomY=middleY+moveDistance;
            int paintBottomAlpha=(int)(((float)(TEXT_SPACING-moveDistance)/TEXT_SPACING)*255);
            paintBottom.setAlpha(paintBottomAlpha);
        }

        //计算出不需要偏移的文本和偏移的文本
        float stableWidth=paintTop.measureText(lastStr,0,stable);
        String clipLastStr=lastStr.substring(stable);
        String clipNextStr=nextStr.substring(stable);
        String stableStr=lastStr.substring(0,stable);

        //绘制文本
        if(stableStr.length()>0){
            canvas.drawText(stableStr,middleX,middleY,stablePaint);
        }
        canvas.drawText(clipLastStr,middleX+stableWidth,paintTopY,paintTop);
        canvas.drawText(clipNextStr,middleX+stableWidth,paintBottomY,paintBottom);

        //计算出绘制图片的坐标
        int bitmapWidth=unSelectBitmap.getWidth();
        int bitmapHeight=unSelectBitmap.getHeight();
        int drawBitmapWidth=middleX-100;
        int drawBitmapHeight=middleY-70;
        boolean isSelect=false;

        //对图片进行缩放
        canvas.save();

        canvas.scale(scaleProgress,scaleProgress,drawBitmapWidth+bitmapWidth/2,drawBitmapHeight+bitmapHeight/2);
        if(direction==0){
            if(moveDistance<=TEXT_SPACING/2){
                canvas.drawBitmap(unSelectBitmap,drawBitmapWidth,drawBitmapHeight,new Paint());
            }else{
                canvas.drawBitmap(selectBitmap,drawBitmapWidth,drawBitmapHeight,new Paint());
                isSelect=true;
            }
        }else{
            if(moveDistance<=TEXT_SPACING/2){
                canvas.drawBitmap(selectBitmap,drawBitmapWidth,drawBitmapHeight,new Paint());
                isSelect=true;
            }else{
                canvas.drawBitmap(unSelectBitmap,drawBitmapWidth,drawBitmapHeight,new Paint());
            }
        }

        canvas.restore();

        //绘制选中时的...
        if(isSelect){
            canvas.drawBitmap(selectShinBitmap,drawBitmapWidth+10,drawBitmapHeight-40,new Paint());
        }

        //波纹扩散效果
        if(direction==0&&moveDistance!=TEXT_SPACING){
            canvas.drawCircle(drawBitmapWidth+bitmapWidth/2,drawBitmapHeight+bitmapHeight/2,20+50*(((float)moveDistance/TEXT_SPACING)),circlePaint);
        }
    }

}
