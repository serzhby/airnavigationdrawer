package by.serzh.airnavigationdrawer;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import by.serzhyairnavigationdrawer.R;

public class AirNavigationDrawer extends ViewGroup {
	@SuppressWarnings("unused")
	private static final String TAG = AirNavigationDrawer.class.getSimpleName();
	
	private static final int ANIMATION_DURATION = 250;
	private static final int LEFT_TOUCH_ZONE_WIDTH_DP = 20;
	private static int LEFT_TOUCH_ZONE_WIDTH;
	
	private static final int MENU_RIGHT_MARGIN_DP = 50;
	private static int MENU_RIGHT_MARGIN;
	
	private static final int INTERCEPT_DELTA_DP = 2;
	private static int INTERCEPT_DELTA;
	
	//private static final float CONTENT_ROTATION_ANGLE = 15f;
	private int topPaddingHeight = 0;
	
	private int visibleHeight;
	private int visibleWidth;
	private int viewWidth;
	
	private int menuWidth;
	private int menuHeight;
	private int contentWidth;
	private int contentHeight;
	
	private FrameLayout menuContainer;
	private FrameLayout contentContainer;
	
	private int previousX;
	private int previousY;
	private boolean isTouchMove = false;
	private VelocityTracker velocityTracker;
	
	@SuppressWarnings("unused")
	private int minimumVelocity;
	private int maximumVelocity;
	private int maximumFlingVelocity;
	private int minimumFlingVelocity;
	
	private boolean isAnimationWorking = false;
	private boolean intercept = false;
	
	private TouchMode touchMode = TouchMode.NONE;
	
	private OnMenuShownListener onMenuShownListener;
	private OnMenuClosedListener onMenuClosedListener;
	
	private List<OpeningPercentListener> openingPercentListeners = new ArrayList<OpeningPercentListener>();

	private FrameLayout contentRootContainer;
	private ContentSubstitutionView contentSubstitutionView;
	
	public AirNavigationDrawer(Context context) {
		this(context, null);
	}

	public AirNavigationDrawer(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AirNavigationDrawer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		LEFT_TOUCH_ZONE_WIDTH = (int) Utils.dpToPx(LEFT_TOUCH_ZONE_WIDTH_DP, context);
		MENU_RIGHT_MARGIN = (int) Utils.dpToPx(MENU_RIGHT_MARGIN_DP, context);
		INTERCEPT_DELTA = (int) Utils.dpToPx(INTERCEPT_DELTA_DP, context);
		
		createMenuContainer(context);
		createContentContainer(context);

		addView(menuContainer);
		
		contentRootContainer = new FrameLayout(getContext());
		contentRootContainer.addView(contentContainer);
		
		contentSubstitutionView = new ContentSubstitutionView(context, this);
		contentSubstitutionView.setVisibility(View.GONE);
		
		contentRootContainer.addView(contentSubstitutionView);
		addView(contentRootContainer);
		
		//addView(contentContainer);
		initializeVelocityConstants(context);
	}

	private void createMenuContainer(Context context) {
		menuContainer = new FrameLayout(context);
		menuContainer.setId(R.id.menu_container);
		menuContainer.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
	}
	
	private void createContentContainer(Context context) {
		contentContainer = new FrameLayout(context);
		contentContainer.setId(R.id.content_container);
		contentContainer.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
	}
	
	private void initializeVelocityConstants(Context context) {
		final ViewConfiguration configuration = ViewConfiguration.get(context);
		minimumVelocity = configuration.getScaledMinimumFlingVelocity();
		maximumVelocity = configuration.getScaledMaximumFlingVelocity();
		maximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
		minimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		visibleHeight = heightSize;
		visibleWidth = widthSize;
		
		menuWidth = visibleWidth - MENU_RIGHT_MARGIN;
		menuHeight = visibleHeight;
		int menuWidthMeasureSpec = MeasureSpec.makeMeasureSpec(menuWidth, MeasureSpec.EXACTLY);
		int menuHeightMeasureSpec = MeasureSpec.makeMeasureSpec(menuHeight, MeasureSpec.EXACTLY);
		getChildAt(0).measure(menuWidthMeasureSpec, menuHeightMeasureSpec);
		
		contentWidth = visibleWidth;
		contentHeight = visibleHeight;
		int contentWidthMeasureSpec = MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY);
		int contentHeightMeasureSpec = MeasureSpec.makeMeasureSpec(contentHeight, MeasureSpec.EXACTLY);
		getChildAt(1).measure(contentWidthMeasureSpec, contentHeightMeasureSpec);
		
		viewWidth = menuWidth + contentWidth;
		setMeasuredDimension(resolveSize(viewWidth, widthMeasureSpec), resolveSize(visibleHeight, heightMeasureSpec));
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		getChildAt(0).layout(0, 0, menuWidth, menuHeight);
		getChildAt(1).layout(menuWidth, 0, contentWidth + menuWidth, contentHeight);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean result = false;
		if(!isAnimationWorking) {
			final int action = ev.getAction();
			int y = (int) ev.getY();
			int x = (int) ev.getX();

			switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN: {
					initOrResetVelocityTracker();
					previousX = x;
					previousY = y;
					if((isMenuShown() && x > menuWidth)
							|| (touchMode == TouchMode.MARGIN && x > 0 && x < LEFT_TOUCH_ZONE_WIDTH && y > topPaddingHeight)) {
						result = true;
					}
					break;
				}
				case MotionEvent.ACTION_MOVE: {
					int deltaY = (int) previousY - y;
					int deltaX = (int) previousX - x;
					int delta = INTERCEPT_DELTA; 
					if(Math.abs(deltaX) > delta || Math.abs(deltaY) > delta) {
						if(Math.abs(deltaY) < Math.abs(deltaX) &&
								( (touchMode == TouchMode.MARGIN && previousX > 0 && previousX < LEFT_TOUCH_ZONE_WIDTH)
									|| (touchMode == TouchMode.FULLSCREEN)
									|| isMenuShown()) ) {
							result = true;
						}
					}
					previousX = x;
					previousY = y;
					break;
				}
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP: {
					break;
				}
			}
		}
		intercept = result;
		return result;
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if(!intercept) {
			return false;
		}
		final int action = ev.getAction();
		int y = (int) ev.getY();
		int x = (int) ev.getX();
		
		if(velocityTracker != null) {
			velocityTracker.addMovement(ev);
		}

		switch (action & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				initOrResetVelocityTracker();
				break;
			case MotionEvent.ACTION_MOVE: {
				isTouchMove = true;
				int deltaX = (int) previousX - x;
				
				if(getScrollX() + deltaX < 0) {
					scrollTo(0, 0);
				} else if(getScrollX() + deltaX + visibleWidth > viewWidth) {
					scrollTo(menuWidth, 0);
				} else {
					scrollBy(deltaX, 0);
				}
				updateContentSubstitutionViewAngle();
				updateMenuSubstitutionViewAngle();
				notifyListeners();
				previousX = x;
				previousY = y;
				break;
			}
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP: {
				if(velocityTracker != null) {
					if(isTouchMove) {
						velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
						int initialVelocity = (int) velocityTracker.getXVelocity(ev.getPointerId(ev.getActionIndex()));

						if (Math.abs(initialVelocity) > minimumFlingVelocity 
								&& Math.abs(initialVelocity) < maximumFlingVelocity) {
							if(initialVelocity < 0) {
								closeMenu();
							} else {
								showMenu();
							}
						} else {
							onTouchFinished();
						}
					} else if(isMenuShown()) {
						animateTo(menuWidth);
					}
				} else {
					animateTo(menuWidth);
				}
				isTouchMove = false;
				recycleVelocityTracker();
				break;
			}
		}
		return true;
	}

	private void initOrResetVelocityTracker() {
		if (velocityTracker == null) {
			velocityTracker = VelocityTracker.obtain();
		} else {
			velocityTracker.clear();
		}
	}
	
	private void recycleVelocityTracker() {
		if(velocityTracker != null) {
			velocityTracker.recycle();
			velocityTracker = null;
		}
	}
	
	private void onTouchFinished() {
		int scrollX = getScrollX();
		if(scrollX <= menuWidth / 2) {
			showMenu();
		} else if(scrollX > menuWidth / 2) {
			closeMenu();			
		}
	}
	
	private void animateToImmediate(int targetX) {
		animateTo(targetX, 0);
	}
	
	private void animateTo(int targetX) {
		animateTo(targetX, ANIMATION_DURATION);
	}
	
	private void animateTo(int targetX, int duration) {
		if(!isAnimationWorking) {
			ObjectAnimator animator = ObjectAnimator.ofInt(this, "scrollX", getScrollX(), targetX);
			animator.setDuration(duration);
			animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					updateContentSubstitutionViewAngle();
					updateMenuSubstitutionViewAngle();
					notifyListeners();
				}
			});
			animator.addListener(new Animator.AnimatorListener() {
				
				@Override
				public void onAnimationStart(Animator animation) {				
					isAnimationWorking = true;
				}
				
				@Override
				public void onAnimationRepeat(Animator animation) { }
				
				@Override
				public void onAnimationEnd(Animator animation) {
					updateContentSubstitutionViewAngle();
					updateMenuSubstitutionViewAngle();
					notifyListeners();
					onTransitionFinished();
					isAnimationWorking = false;
				}
				
				@Override
				public void onAnimationCancel(Animator animation) {
					isAnimationWorking = false;
				}
			});
			animator.start();
		}
	}
	
	private void updateContentSubstitutionViewAngle() {
		float percent = getOpeningPercent();
		contentSubstitutionView.setOpeningPercent(percent);
		if(percent == 0) {
			contentSubstitutionView.setVisibility(View.INVISIBLE);
			contentContainer.setVisibility(View.VISIBLE);
			contentSubstitutionView.recycle();
		} else {
			contentSubstitutionView.setVisibility(View.VISIBLE);
			contentContainer.setVisibility(View.INVISIBLE);
			contentSubstitutionView.setView(contentContainer);
		}
//		contentContainer.setPivotX(0);
//		contentContainer.setPivotY(contentContainer.getHeight() / 2);
//		contentContainer.setRotationY(-CONTENT_ROTATION_ANGLE * percent);
//		contentContainer.setScaleX(1 - 0.4f * percent);
//		contentContainer.setScaleY(1 - 0.4f * percent);
	}
	
	private void updateMenuSubstitutionViewAngle() {
	}
	
	private float getOpeningPercent() {
		return ((float) (menuWidth - getScrollX())) / (float) menuWidth;
	}
	
	private void onTransitionFinished() {
		if(isMenuShown() && onMenuShownListener != null) {
			onMenuShownListener.onMenuShown();
		} else if(onMenuClosedListener != null) {
			onMenuClosedListener.onMenuClosed();
		}
	}
	
	public boolean isMenuShown() {
		return getScrollX() == 0;
	}
	
	public void showMenu() {
		animateTo(0);
	}
	
	public void closeMenu() {
		animateTo(menuWidth);
	}
	
	public void showMenuImmediate() {
		animateToImmediate(0);
	}
	
	public void closeMenuImmediate() {
		animateToImmediate(menuWidth);
	}
	
	public int getMenuContainerId() {
		return R.id.menu_container;
	}
	
	public int getContentContainerId() {
		return R.id.content_container;
	}
	
	public void setTouchMode(TouchMode touchMode) {
		this.touchMode = touchMode;
	}
	
	public void setOnMenuShownListener(OnMenuShownListener onMenuShownListener) {
		this.onMenuShownListener = onMenuShownListener;
	}
	
	public void setOnMenuClosedListener(OnMenuClosedListener onMenuClosedListener) {
		this.onMenuClosedListener = onMenuClosedListener;
	}
	
	public interface OnMenuShownListener {
		void onMenuShown();
	}
	
	public interface OnMenuClosedListener {
		void onMenuClosed();
	}
	
	public void setLeftEdgeSizeDp(float dps) {
		LEFT_TOUCH_ZONE_WIDTH = (int) Utils.dpToPx(dps, getContext());
	}
	
	public void setLeftEdgeSizePx(int pxs) {
		LEFT_TOUCH_ZONE_WIDTH = pxs;
	}
	
	public void addOpeningPercentListener(OpeningPercentListener listener) {
		this.openingPercentListeners.add(listener);
	}
	
	public void removeOpeningPercentListener(OpeningPercentListener listener) {
		this.openingPercentListeners.remove(listener);
	}
	
	private void notifyListeners() {
		float percent = getOpeningPercent();
		for(OpeningPercentListener listener : openingPercentListeners) {
			listener.onOpeningPercentUpdate(1 - percent);
		}
	}
	
	public void setTopPaddingHeight(int topPaddingHeight) {
		this.topPaddingHeight = topPaddingHeight;
	}

	void measureContentView(View view) {
		view.measure(MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY), 
				MeasureSpec.makeMeasureSpec(contentHeight, MeasureSpec.EXACTLY));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
	}
	
	public void updateView(View view) {
		contentSubstitutionView.updateView(view == null ? contentContainer : view);
	}
}
