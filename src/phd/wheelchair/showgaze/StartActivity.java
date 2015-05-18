package phd.wheelchair.showgaze;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Having an activity that starts the service allows one to attach with the
 * interactive debugger more predictably
 */
public class StartActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, GazeService.class));
        finish();
    }
}
