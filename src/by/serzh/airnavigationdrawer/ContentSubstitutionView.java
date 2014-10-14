package by.serzh.airnavigationdrawer;

import android.content.Context;
import android.graphics.Canvas;

class ContentSubstitutionView extends SubstitutionView {
	
	private static final float MAX_ANGLE = -15;
	
	public ContentSubstitutionView(Context context, AirNavigationDrawer drawer) {
		super(context, drawer);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(bitmap != null) {
			drawBitmap(canvas);
		}
	}

	@Override
	protected float getMaxAngle() {
		return MAX_ANGLE;
	}
}
