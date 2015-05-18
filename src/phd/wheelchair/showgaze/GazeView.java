package phd.wheelchair.showgaze;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


/**
 * View used to draw the level line.
 */
public class GazeView extends View {

	
	private Paint mPaint = new Paint();
	private float mx = 0.f;
	private float my = 0.f;
    public GazeView(Context context) {
        this(context, null, 0);
    }

    public GazeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GazeView(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);

        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(5);
    }
    
    /**
     * Set the angle of the level line.
     *
     * @param angle Angle of the level line.
     */
    public void setPoint(float x, float y) {
        mx = 100*x;
        my = 100*y;
        // Redraw the point.
       invalidate();
    }

    public float getPointx() {
        return mx;
    }
    
    public float getPointy(){
    	return my;
    }

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float width = canvas.getWidth()/2;
		float height = canvas.getHeight()/2;
		
		canvas.drawPoint(mx+width, my+height, mPaint);
	}
	
	

}
