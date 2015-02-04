package com.lam.android.attackhelicopter2;

import java.util.concurrent.ArrayBlockingQueue;

import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class InputObject 
{
	public static final byte EVENT_TYPE_KEY = 1;
	public static final byte EVENT_TYPE_TOUCH = 2;
	public static final int DPAD_KEY_DOWN = 1;
	public static final int DPAD_KEY_UP = 2;
	public static final int DPAD_KEY_LEFT = 3;
	public static final int DPAD_KEY_RIGHT = 4;
	public static final int DPAD_KEY_CENTER = 5;
	
	public static final int ACTION_TOUCH_DOWN = 1;
	public static final int ACTION_TOUCH_MOVE = 2;
	public static final int ACTION_TOUCH_UP = 3; 
	public ArrayBlockingQueue<InputObject> pool;
	public byte eventType;
	public long time;
	public int action;
	public int keyCode;
	public int x;
	public int y;
	
	protected final String TAG = "InputObject ";

	public InputObject(ArrayBlockingQueue<InputObject> pool) {
		this.pool = pool;
	}

	/** convert android event type to InputObject event type*/
	public void useEvent(int keyCode, KeyEvent event) 
	{
		Log.d(TAG + "useEvent for key event", "begin");
		try
		{
		
			eventType = EVENT_TYPE_KEY;
			
			switch (keyCode) 
			{
			case KeyEvent.KEYCODE_DPAD_DOWN:
				action = DPAD_KEY_DOWN;
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				action = DPAD_KEY_UP;
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				action = DPAD_KEY_LEFT;
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				action = DPAD_KEY_RIGHT;
				break;
			case KeyEvent.KEYCODE_DPAD_CENTER:
				action = DPAD_KEY_CENTER;
				break;
			default:
				action = 0;
			}
			time = event.getEventTime();
			keyCode = event.getKeyCode();
		}
		catch(Exception ex)
		{
			Log.e(TAG + "useEvent for key event", ex.getMessage());
		}
		
		Log.d(TAG + "useEvent for key event", "end");
	}

	public void useEvent(MotionEvent event) 
	{
		Log.d(TAG + "useEvent for motion event", "begin");
		
		try
		{
			eventType = EVENT_TYPE_TOUCH;
			int a = event.getAction();
			switch (a) 
			{
			case MotionEvent.ACTION_DOWN:
				action = ACTION_TOUCH_DOWN;
				break;
			case MotionEvent.ACTION_MOVE:
				action = ACTION_TOUCH_MOVE;
				break;
			case MotionEvent.ACTION_UP:
				action = ACTION_TOUCH_UP;
				break;
			default:
				action = 0;
			}
			time = event.getEventTime();
			x = (int) event.getX();
			y = (int) event.getY();
		}
		catch(Exception ex)
		{
			Log.e(TAG + "useEvent for motion event", ex.getMessage());
		}
		
		Log.d(TAG + "useEvent for motion event", "end");
	}

	public void useEventHistory(MotionEvent event, int historyItem) 
	{
		Log.d(TAG + "useEventHistory", "begin");
		
		eventType = EVENT_TYPE_TOUCH;
		action = ACTION_TOUCH_MOVE;
		time = event.getHistoricalEventTime(historyItem);
		x = (int) event.getHistoricalX(historyItem);
		y = (int) event.getHistoricalY(historyItem);
		
		Log.d(TAG + "useEventHistory", "end");
	}

	public void returnToPool() 
	{
		try
		{
			pool.add(this);
		}
		catch(Exception ex)
		{
			Log.e(TAG + "returnToPool", ex.getMessage());
		}
	}
} 

