package phd.wheelchair.showgaze;

import com.google.android.glass.timeline.LiveCard;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.IBinder;

/**
 * The main application service that manages the lifetime of the live card.
 */
public class GazeService extends Service {

	private static final String LIVE_CARD_TAG = "gaze";

	private LiveCard mLiveCard;
	private GazeRenderer mRenderer;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mLiveCard == null) {
			SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

			mLiveCard = new LiveCard(getApplicationContext(), LIVE_CARD_TAG);
			mRenderer = new GazeRenderer(sensorManager, this);

			mLiveCard.setDirectRenderingEnabled(true);
			mLiveCard.getSurfaceHolder().addCallback(mRenderer);

			// Display the options menu when the live card is tapped.
			Intent menuIntent = new Intent(this, MainActivity.class);
			menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

			mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));

			mLiveCard.publish(LiveCard.PublishMode.REVEAL);
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		if (mLiveCard != null && mLiveCard.isPublished()) {
			mLiveCard.unpublish();
			mLiveCard.getSurfaceHolder().removeCallback(mRenderer);
			mLiveCard = null;
		}
		super.onDestroy();
	}

}
