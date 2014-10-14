package by.serzh.airnavigationdrawer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.View;

abstract class SubstitutionView extends View {
	@SuppressWarnings("unused")
	private static final String TAG = SubstitutionView.class.getSimpleName();
	
	protected Bitmap bitmap;
	protected Paint paint;
	private Matrix rotMatrix;
	private Camera camera;
	
	protected float openingPercent = 0;
	
	private boolean isBitmapOld = true;
	
	private AirNavigationDrawer drawer;
	
	private Canvas canvas;
	
	public SubstitutionView(Context context, AirNavigationDrawer drawer) {
		super(context);
		this.drawer = drawer;
		paint = new Paint();
		paint.setAntiAlias(true);
		rotMatrix = new Matrix();
		camera = new Camera();
	}
	
	protected abstract float getMaxAngle();
	
	public void setView(View view) {
		if(isBitmapOld && view.getWidth() > 0 && view.getHeight() > 0) {
			viewToBitmap(view);
			isBitmapOld = false;
			invalidate();
		}
	}
	
	void reset() {
		isBitmapOld = true;
	}
	
	public void updateView(final View view) {
		view.post(new Runnable() {

			@Override
			public void run() {
				if(view.getWidth() <= 0 && view.getHeight() <= 0) {
					drawer.measureContentView(view);
				}
				if(view.getWidth() > 0 && view.getHeight() > 0) {
					viewToBitmap(view);			
				}
				postInvalidate();
			}
			
		});
		
	}
	
	private void viewToBitmap(View view) {
		if(bitmap == null) {
			bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Config.ARGB_8888);
			canvas = new Canvas(bitmap);
		}
		view.draw(canvas);
	}
	
	public void setOpeningPercent(float percent) {
		this.openingPercent = percent;
		invalidate();
	}
	
	protected void drawBitmap(Canvas canvas) {
		recalculateRotationMatrix();

		float scale = 1 - 0.4f * openingPercent;
		int centerY = bitmap.getHeight() / 2;
		rotMatrix.preTranslate(0, -centerY);
		rotMatrix.postScale(scale, scale);
		rotMatrix.postTranslate(0, centerY);
		canvas.drawBitmap(bitmap, rotMatrix, paint);
	}
	
	private void recalculateRotationMatrix() {
		rotMatrix.reset();
		float angle = getMaxAngle() * openingPercent;
		camera.rotateY(angle);
		camera.getMatrix(rotMatrix);
		camera.rotateY(-angle);
	}
	
	public void recycle() {
		if(bitmap != null) {
			bitmap.recycle();
			bitmap = null;
		}
		canvas = null;
		isBitmapOld = true;
		openingPercent = 0;
	}
}
