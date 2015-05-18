package phd.wheelchair.showgaze;

import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.glass.timeline.DirectRenderingCallback;

public class GazeRenderer implements DirectRenderingCallback{

	private static final String TAG = GazeRenderer.class.getSimpleName();

	/** The refresh rate, in frames per second, of the Live Card. */
	private static final int REFRESH_RATE_FPS = 33;

	/** The duration, in milliseconds, of one frame. */
	private static final long FRAME_TIME_MILLIS = TimeUnit.SECONDS.toMillis(1) / REFRESH_RATE_FPS;

	private SurfaceHolder mHolder;
	private RenderThread mRenderThread;
	private SensorManager mSensorManager;
	private int mSurfaceWidth;
	private int mSurfaceHeight;
	private boolean mRenderingPaused;

	private final FrameLayout mLayout;
	private final GazeView mGazeView;

	private final SensorEventListener mSensorEventListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION)
				computeCoordinates(event);			
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// nothing to do here

		}

		/**
		 * compute the coordinate of the point to display
		 */
		private void computeCoordinates(SensorEvent event){
			float x = (float) event.values[0];
			float y = (float) event.values[1];
			if ( x>1 || x<-1 || y>1 || y<-1)
				mGazeView.setPoint(x,y);
		}
	};

	/**
	 * Creates a new instance of the {@code LevelRenderer} .
	 */
	public GazeRenderer(SensorManager sensorManager, Context context){
		LayoutInflater inflater = LayoutInflater.from(context);

		mLayout = (FrameLayout) inflater.inflate(R.layout.activity_main, null);
		mGazeView = (GazeView) mLayout.findViewById(R.id.gaze);
		mSensorManager = sensorManager;
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		mSurfaceHeight = height;
		mSurfaceWidth = width;
		doLayout();

	}	
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mHolder = holder;
		updateRenderingState();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mHolder = null;
		updateRenderingState();		
	}

	@Override
	public void renderingPaused(SurfaceHolder arg0, boolean arg1) {
		mRenderingPaused = arg1;
		updateRenderingState();
	}
	
	/**
	 * Starts or stops rendering according to the {@link LiveCard}'s state.
	 */
	private void updateRenderingState() {
		boolean shouldRender = (mHolder != null) && !mRenderingPaused;
		boolean isRendering = (mRenderThread != null);

		if (shouldRender != isRendering){
			if (shouldRender){
				mSensorManager.registerListener(mSensorEventListener, 
						mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
						SensorManager.SENSOR_DELAY_UI);

				mRenderThread = new RenderThread();
				mRenderThread.start();
			} else {
				mRenderThread.quit();
				mRenderThread = null;

				mSensorManager.unregisterListener(mSensorEventListener);
			}
		}
	}

	/**
	 * Requests that the views redo their layout. This must be called manually every time the
	 * tips view's text is updated because this layout doesn't exist in a GUI thread where those
	 * requests will be enqueued automatically.
	 */
	private void doLayout() {
		// Measure and update the layout so that it will take up the entire surface space
		// when it is drawn.
		int measuredWidth = View.MeasureSpec.makeMeasureSpec(mSurfaceWidth,
				View.MeasureSpec.EXACTLY);
		int measuredHeight = View.MeasureSpec.makeMeasureSpec(mSurfaceHeight,
				View.MeasureSpec.EXACTLY);

		mLayout.measure(measuredWidth, measuredHeight);
		mLayout.layout(0, 0, mLayout.getMeasuredWidth(), mLayout.getMeasuredHeight());
	}



	private class RenderThread extends Thread {
		private boolean mShouldRun;

		/**
		 * Initializes the background rendering thread.
		 */
		public RenderThread(){
			mShouldRun = true;
		}

		/**
		 * Returns true if the rendering thread should continue to run.
		 *
		 * @return true if the rendering thread should continue to run
		 */
		private synchronized boolean shouldRun(){
			return mShouldRun;
		}

		/**
		 * Requests that the rendering thread exit at the next opportunity.
		 */
		public synchronized void quit() {
			mShouldRun = false;
		}

		@Override
		public void run (){
			while(shouldRun()){
				long frameStart = SystemClock.elapsedRealtime();
				repaint();
				long frameLength = SystemClock.elapsedRealtime() - frameStart;

				long sleepTime = FRAME_TIME_MILLIS - frameLength;
				if (sleepTime > 0) {
					SystemClock.sleep(sleepTime);
				}
			}

		}

		private synchronized void repaint() {
			Canvas canvas = null;

			try {
				canvas = mHolder.lockCanvas();
			} catch (RuntimeException e) {
				Log.d(TAG, "lockCanvas failed", e);
			}

			if (canvas != null) {

				doLayout();
				mLayout.draw(canvas);

				try {
					mHolder.unlockCanvasAndPost(canvas);
				} catch (RuntimeException e) {
					Log.d(TAG, "unlockCanvasAndPost failed", e);
				}
			}
		}
	}
}