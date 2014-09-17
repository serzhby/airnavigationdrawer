AirNavigationDrawer
=========


A view that implements behaivior similar to [PHAirViewController](https://github.com/taphuochai/PHAirViewController)

It looks like this:
![Example](https://github.com/serzhby/airnavigationdrawer/example.gif)

Usage
=========

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

The view consists of 2 FrameLayouts: menu and content. You can get their ids by AirNavigationDrawer.getMenuContainerId() and AirNavigationDrawer.getContentContainerId().
Now you can put fragments into the view:

```
    getFragmentManager().beginTransaction().add(drawer.getMenuContainerId(), new MenuFragment()).commit();
	getFragmentManager().beginTransaction().add(drawer.getContentContainerId(), new FirstFragment()).commit();
```



License
----

MIT


