package by.serzh.airnavigationdrawer;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

class Utils {

	public static float dpToPx(float dp, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * (metrics.densityDpi / 160f);
		return px;
	}
}
