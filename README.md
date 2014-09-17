AirNavigationDrawer
=========
----


A view that implements behaivior similar to [PHAirViewController](https://github.com/taphuochai/PHAirViewController)

It looks like this:

![Example](https://raw.githubusercontent.com/serzhby/airnavigationdrawer/master/example.gif)

Usage
----


You can add the view to a layout like this:
```

<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <by.serzh.airnavigationdrawer.AirNavigationDrawer
        android:id="@+id/drawer"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
    
</LinearLayout>

```

Or set it directly as a view of your activity:

```

    private AirNavigationDrawer drawer;
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		drawer = new AirNavigationDrawer(this);
		setContentView(drawer);
	}

```

The view consists of 2 FrameLayouts: menu and content. You can get their ids by `AirNavigationDrawer.getMenuContainerId()` and `AirNavigationDrawer.getContentContainerId()`.
Now you can put fragments into the view:

```
    getFragmentManager().beginTransaction().add(drawer.getMenuContainerId(), new MenuFragment()).commit();
	getFragmentManager().beginTransaction().add(drawer.getContentContainerId(), new FirstFragment()).commit();
```

Setting up
----
The view supports 3 touch modes:
* `TouchMode.NONE` - menu won't be opened by a swipe.
* `TouchMode.MARGIN` - can open the menu by a swipe from the edge of the screen.
* `TouchMode.FULLSCREEN` - can open the menu by a swipe anywhere on the screen.

Use `AirNavigationDrawer.setTouchMode(TouchMode)` to set the mode.

Also you can set width of edge margin in `TouchMode.MARGIN` mode (default is 20dp). Use `AirNavigationDrawer.setLeftEdgeSize()` or `AirNavigationDrawer.setLeftEdgeSizeDp()`


License
----

MIT


