package com.github.tvbox.osc.wxwz.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.github.tvbox.osc.R;


/**
 * 素材来自MediaCenter
 * @author pikawz
 */

public class MediaCenterImageButton extends androidx.appcompat.widget.AppCompatImageButton {
    private Bitmap compositeBitmap = null;
    private ValueAnimator alphaAnimator;
    private ValueAnimator  translationAnimator;
    private LayerDrawable layerDrawable;
    private boolean isPressEnd = false;
    private boolean hideFirstFrame = false;
    private boolean holdPress = false;
    private boolean isKeyUp = false;
    private boolean clickAnimEnd = true;

    public MediaCenterImageButton(Context context) {
        super(context);
    }

    public MediaCenterImageButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        //setPadding(20,5,20,5);
        init();
        initFocusAnim();
    }

    public MediaCenterImageButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        //setFocusableInTouchMode(true);
        setFocusable(true);
    }

    private void initFocusAnim() {
        //compositeBitmap = photoCovert();
        compositeBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.common_button_right_focusoverlay);
        // 创建透明度动画，从 0 到 1 再从 1 到 0
        alphaAnimator = ValueAnimator.ofInt(0, 255, 0);
        alphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
        alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
        alphaAnimator.setDuration(3000); // 设置动画持续时间，单位毫秒
        // 添加监听器，在动画更新时设置透明度
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int alpha = (int) animation.getAnimatedValue();
                if (getForeground()!=null){
                    getForeground().setAlpha(alpha);
                }
            }
        });

        setOnFocusChangeListener(new OnFocusChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onFocusChange(View view, boolean b) {

                if (b) {
                    if (clickAnimEnd){
                        // 获取原始的 Foreground
                        setForeground(new BitmapDrawable(getResources(),compositeBitmap));
                        alphaAnimator.start();
                    }
                } else {
                    // 清除动画以确保下次获得焦点时可以重新播放
                    setForeground(null);
                    if(alphaAnimator!=null){
                        if (alphaAnimator.isRunning()){
                            alphaAnimator.cancel();
                        }
                    }

                    if (translationAnimator!=null){
                        if (translationAnimator.isRunning()){
                            translationAnimator.cancel();
                        }
                    }
                }
            }
        });
        setOnTouchListener(new OnTouchListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    holdPress = false;
                    isKeyUp = false;
                    clickAnimEnd = false;
                    startTranslationAnimator(R.drawable.btn_left_focus_press,false,500,true);
                } else if (motionEvent.getAction()==MotionEvent.ACTION_UP) {
                    isKeyUp = true;
                    if (translationAnimator.isRunning()&&holdPress){
                        translationAnimator.cancel();
                    }
                    if (!alphaAnimator.isRunning()&&clickAnimEnd){
                        // 获取原始的 Foreground
                        setForeground(new BitmapDrawable(getResources(),compositeBitmap));
                        alphaAnimator.start();
                    }
                    Log.d("wxwz","结束");
                }
                return false;
            }
        });
        setOnKeyListener(new OnKeyListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                Log.d("wxwz","event=" + keyEvent.getAction() + ",keyCode=" + keyEvent.getKeyCode());
                int action = keyEvent.getAction();
                int keyCode = keyEvent.getKeyCode();
                if (keyCode==KeyEvent.KEYCODE_NUMPAD_ENTER||keyCode==KeyEvent.KEYCODE_ENTER){
                    if (action==KeyEvent.ACTION_DOWN&&!isPressEnd){
                        isPressEnd = true;
                        isKeyUp = false;
                        holdPress = false;
                        clickAnimEnd = false;
                        startTranslationAnimator(R.drawable.btn_left_focus_press,false,500,true);
                    } else if (action==KeyEvent.ACTION_UP) {
                        isKeyUp = true;
                        isPressEnd = false;
                        if (translationAnimator!=null&&holdPress){
                            if (translationAnimator.isRunning()){
                                translationAnimator.cancel();
                            }
                        }
                        if (alphaAnimator!=null&&clickAnimEnd){
                            if (!alphaAnimator.isRunning()){
                                // 获取原始的 Foreground
                                setForeground(new BitmapDrawable(getResources(),compositeBitmap));
                                alphaAnimator.start();
                            }
                        }
                        Log.d("wxwz","结束");
                    }
                }
                return false;
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startTranslationAnimator(int res,boolean repeat,int duration,boolean endListener){
        hideFirstFrame = false;
        if (alphaAnimator.isRunning()){
            alphaAnimator.cancel();
        }
        if (translationAnimator!=null){
            if (translationAnimator.isRunning()){
                translationAnimator.cancel();
            }
        }

        Bitmap bp = BitmapFactory.decodeResource(getResources(),res);

        // 获取原始的 Foreground
        Drawable originalForeground = new BitmapDrawable(getResources(),bp);

        // 创建一个新的 LayerDrawable，包含原始的 Foreground
        Drawable[] layers = {originalForeground};
        layerDrawable = new LayerDrawable(layers);
        // 更新 Drawable 的位置
        layerDrawable.setAlpha(0);
        setForeground(layerDrawable);
        Log.d("wxwz","al=" + getForeground().getAlpha());
        Log.d("wxwz","al1=" + getForeground().getAlpha());
        int translationX = getWidth();
        Rect originalBounds = originalForeground.getBounds();

        int startX = originalBounds.right - translationX;
        translationAnimator = ValueAnimator.ofFloat(0f, 1f);
        if (repeat){
            translationAnimator.setRepeatMode(ValueAnimator.RESTART);
            translationAnimator.setRepeatCount(ValueAnimator.INFINITE);
        }else {
            // 设置动画结束后不进行重复
            translationAnimator.setRepeatCount(0);
        }
        translationAnimator.setDuration(duration); // 设置动画时长，单位为毫秒
        // 设置动画更新监听器
        translationAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                int currentX = (int) (startX + fraction * translationX);
                int max =startX + 50;
                if (currentX > max){
                    // 更新 Drawable 的位置
                    originalForeground.setBounds(currentX , originalBounds.top, (currentX + originalBounds.width())  , originalBounds.bottom);

                    // 设置透明度，当图片左边到按钮最右边位置时开始逐渐消失
                    int alpha = (int) (255 * (1 - fraction));
                    originalForeground.setAlpha(alpha);
                }else {

                }


                // 刷新视图
                invalidate();
            }

        });
        if (endListener){
            translationAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(@NonNull Animator animator) {

                }

                @Override
                public void onAnimationEnd(@NonNull Animator animator) {
                    Log.d("wxwz","动画结束");
                    clickAnimEnd = true;
                    if (!isKeyUp){
                        startTranslationAnimator(R.drawable.btn_press_hold,true,2000,false);
                        holdPress = true;
                    } else {
                        if (alphaAnimator!=null&&isFocused()){
                            if (!alphaAnimator.isRunning()){
                                // 获取原始的 Foreground
                                setForeground(new BitmapDrawable(getResources(),compositeBitmap));
                                alphaAnimator.start();
                            }
                        }
                    }
                }

                @Override
                public void onAnimationCancel(@NonNull Animator animator) {

                }

                @Override
                public void onAnimationRepeat(@NonNull Animator animator) {

                }
            });
        }

        translationAnimator.start();
    }

    private Bitmap photoCovert() {
        int paddingTop = 1;
        int paddingBottom = 1;
        float contrastFactor = 10;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.btn_left_bottom_focus);
        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.btn_left_top_focus);
        int newHeight = bitmap.getHeight() + paddingTop + paddingBottom;
        if (!bitmap.isMutable()) {
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        }

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        canvas.drawBitmap(bitmap2, 0, 0, paint);//插入图标
        canvas.save();
        canvas.restore();
        // 绘制原始 Bitmap 到新的 Bitmap，并应用 padding
        Bitmap paddedBitmap = Bitmap.createBitmap(bitmap.getWidth(), newHeight, bitmap.getConfig());
        Canvas canvas1 = new Canvas(paddedBitmap);
        Rect originalRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Rect paddedRect = new Rect(0, paddingTop, bitmap.getWidth(), newHeight - paddingBottom);
        canvas1.drawBitmap(bitmap, originalRect, paddedRect, null);
        canvas1.save();
        canvas1.restore();
        //加深图像
        Bitmap enhancedBitmap = Bitmap.createBitmap(paddedBitmap.getWidth(), newHeight, paddedBitmap.getConfig());
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < paddedBitmap.getWidth(); x++) {
                int pixel = paddedBitmap.getPixel(x, y);

                int alpha = Color.alpha(pixel);
                int red = (int) (Color.red(pixel) * contrastFactor);
                int green = (int) (Color.green(pixel) * contrastFactor);
                int blue = (int) (Color.blue(pixel) * contrastFactor);

                // 防止颜色值溢出
                red = Math.min(255, Math.max(0, red));
                green = Math.min(255, Math.max(0, green));
                blue = Math.min(255, Math.max(0, blue));

                enhancedBitmap.setPixel(x, y, Color.argb(alpha, red, green, blue));
            }
        }
        return enhancedBitmap;
    }
    public Bitmap combineBitmaps(Bitmap bitmap1, Bitmap bitmap2) {
        int width = bitmap1.getWidth() + bitmap2.getWidth();
        int height = Math.max(bitmap1.getHeight(), bitmap2.getHeight());

        Bitmap resultBitmap = Bitmap.createBitmap(width, height, bitmap1.getConfig());
        Canvas canvas = new Canvas(resultBitmap);

        // 绘制第一个 Bitmap
        canvas.drawBitmap(bitmap2, 0, 0, null);

        // 绘制第二个 Bitmap，将其放在第一个 Bitmap 的右侧
        canvas.drawBitmap(bitmap1, bitmap2.getWidth(), 0, null);

        return resultBitmap;
    }
}
