package katey2658.com.my.surfaceviewdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

public abstract class RenderView extends SurfaceView implements SurfaceHolder.Callback {

    public RenderView(Context context) {
        this(context, null);
    }

    public RenderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
    }

    /**
     * 回调的线程，用于绘图
     */
    private class RenderThread extends Thread {

        //设置一帧的时间长短
        private static final long SLEEP_TIME = 25;

        private SurfaceHolder surfaceHolder;
        private boolean mIsRun = true;

        public RenderThread(SurfaceHolder holder) {
            super("RenderThread");
            surfaceHolder = holder;
        }

        @Override
        public void run() {

            long startAt = System.currentTimeMillis();
            while (true) {
                synchronized (surfaceLock) {
                    if (!mIsRun) {
                        return;
                    }
                    Canvas canvas = surfaceHolder.lockCanvas();
                    if (canvas != null) {
                        //这里做真正绘制的事情
                        render(canvas, System.currentTimeMillis() - startAt);
                        surfaceHolder.unlockCanvasAndPost(canvas);

                    }
                }
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


        /**
         * 设置绘画线程的运行状态
         * @param isRun true开启 false关闭
         */
        public void setRun(boolean isRun) {
            this.mIsRun = isRun;
            if (isRun==false){
                this.interrupt();//中断线程
            }else{
                this.start();//开启线程
            }
        }
    }

    private final Object surfaceLock = new Object();
    private RenderThread renderThread;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        renderer = onCreateRenderer();
        if (renderer != null && renderer.isEmpty()) {
            throw new IllegalStateException();
        }

        renderThread = new RenderThread(holder);
        renderThread.setRun(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //这里可以获取SurfaceView的宽高等信息
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        synchronized (surfaceLock) {  //这里需要加锁，否则doDraw中有可能会crash
            renderThread.setRun(false);
        }
    }

    /*绘图*/

    public interface IRenderer {
        void onRender(Canvas canvas, long millisPassed);
    }

    private List<IRenderer> renderer;

    protected List<IRenderer> onCreateRenderer() {
        return null;
    }

    /**
     * 对
     * @param canvas 画布
     * @param millisPassed  时间偏移量
     */
    private void render(Canvas canvas, long millisPassed) {
        if (renderer != null) {
            for (int i = 0, size = renderer.size(); i < size; i++) {
                renderer.get(i).onRender(canvas, millisPassed);
            }
        } else {
            onRender(canvas, millisPassed);
        }
    }

     /**
     * 渲染surfaceView的回调方法。用于执行绘画
     * @param canvas  画布
     * @param millisPassed 时间偏移量
     */
    protected void onRender(Canvas canvas, long millisPassed) {
    }

}
