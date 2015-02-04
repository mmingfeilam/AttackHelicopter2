
package com.lam.android.attackhelicopter2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

/**
 * A Vehicle object has a current location, a trajectory angle, a speed in pixels per
 * second, and a last update time.  It is capable of updating itself based on
 * its trajectory and speed.
 *
 * It also knows its boundaries, and will 'bounce' off them when it reaches them.
 */
public class StingerLauncher extends Vehicle {

    private static final String TAG = "StingerLauncher ";
    
    /** radius or width*/
    private final int mParaRadiusPixels; 
    
    protected final int mParaHeight;
    
    /** bogey is changing direction */
	private long mProgress = 0;
	
	private final Bitmap mLauncherBitmap;
	private final Bitmap mLauncherRBitmap;

	private Bitmap mParaBitmap;

	private Bitmap mParaRBitmap;

	private int mLauncherRadiusPixels;

	private int mLauncherHeight;
	
    private StingerLauncher(Context context, 
    		long now, float pixelsPerSecond, 
    		float x, float y,
            double angle, boolean isFiring, boolean isReversing,
            int[] Rids) 
    {
    	super(context, now, 
    			Rids[0], Rids[1],
    			pixelsPerSecond, x, 
    			y, angle, isFiring, isReversing, TAG);
        
        mLauncherBitmap = BitmapFactory.decodeResource(
        		mContext.getResources(),
        		Rids[0]);

        mLauncherRBitmap = BitmapFactory.decodeResource(
        		mContext.getResources(),
        		Rids[1]);
        
        mLauncherRadiusPixels = mLauncherBitmap.getWidth()/2;
        
        mLauncherHeight = mLauncherBitmap.getHeight();
        
        mParaBitmap = BitmapFactory.decodeResource(
        		mContext.getResources(),
        		Rids[2]);

        mParaRBitmap = BitmapFactory.decodeResource(
        		mContext.getResources(),
        		Rids[3]);

        mParaRadiusPixels = mParaBitmap.getWidth()/2;
        
        mParaHeight = mParaBitmap.getHeight();

    }
    
    /** collision check*/
    public boolean isOverlapping(StingerLauncher other) 
    {
    	try
    	{
	        final float dy = other.mY - mY;
	        final float dx = other.mX - mX;
	
	        final float distance = (float) Math.sqrt(dy * dy + dx * dx);
		        
	        return (distance < (mParaRadiusPixels+other.getRadiusPixels()));
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "isOverlapping", e.getMessage());
    	}
    	
    	return false;
    }
    
    public boolean isOverlappingHeli(Helicopter heli) 
    {
    	try
    	{
    		if(heli != null)
    		{
	    		float heliY = (heli.getBottom() + heli.getTop())/2;
	    		float heliX = (heli.getLeft() + heli.getRight())/2;
	    		
		        final float dy = heliY - mY;
		        final float dx = heliX - mX;
		
		        final float distance = (float) Math.sqrt(dy * dy + dx * dx);
			        
		        return (distance < (mParaRadiusPixels+heli.mRadius-35));
    		}
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "isOverlappingHeli", e.getMessage());
    	}
    	
    	return false;
    }
    
    /** move object by changing mX, mY based on mAngle*/
    public void update(long now) 
    {
        if (now <= mLastUpdate) return;      

        try
        {       	
	        // should it move
	        if(mMoving)
	        {
	        	float bottom = mRegion.getBottom();
		        // when at bottom
		        if (mY >= bottom - mParaHeight*3/4) 
		        {
		            // at bottom wall
		        	/* change mY since we are changing bitmap
		            the parachute launcher bitmap and landed 
		            launcher have diff bitmaps, therefore diff
		            sizes*/
		            mY = bottom - mLauncherHeight*3/4;
		            mMoving = false;
		        }
		        
		        else
		        {
			        float delta = (now - mLastUpdate) * mPixelsPerSecond;
			        delta = delta / 1000f;
			
			        mY += (delta * Math.sin(mAngle));
			
			        mLastUpdate = now;
		        }
	        }        
	    }
        catch (Exception e)
        {
        	Log.e(TAG + "update", e.getMessage());
        }
    }
    
    @Override
    public void draw(Canvas canvas, Paint paint) 
    {
    	if(mMoving)
    	{
    		if(!mIsReversing)
	    	{
	            canvas.drawBitmap(mParaBitmap, mX - mParaRadiusPixels, 
	            		mY - mParaRadiusPixels, paint);
	    	}
	    	else
	    	{
	    		canvas.drawBitmap(mParaRBitmap, mX - mParaRadiusPixels, 
	            		mY - mParaRadiusPixels, paint);
	    	}
    	}
    	else
    	{
	    	if(!mIsReversing)
	    	{
	            canvas.drawBitmap(mLauncherBitmap, mX - mLauncherRadiusPixels, 
	            		mY - mLauncherRadiusPixels, paint);
	    	}
	    	else
	    	{
	    		canvas.drawBitmap(mLauncherRBitmap, mX - mLauncherRadiusPixels, 
	            		mY - mLauncherRadiusPixels, paint);
	    	}
    	}

    }
    
    @Override
    public float getRadiusPixels() 
    {
    	if(mMoving)
    	{
    		return mParaRadiusPixels;
    	}
    	else
    	{
    		return mLauncherRadiusPixels;
    	}
    }
    
    @Override
    public String toString() {
        return String.format(
            "StingerLauncher(x=%f, y=%f, angle=%f)",
                mX, mY, Math.toDegrees(mAngle));
    }
    
    @Override
    public float getLeft() {
        return mX - mParaRadiusPixels;
    }
    
    @Override
    public float getRight() {
        return mX + mParaRadiusPixels;
    }

    @Override
    public float getTop() 
    {
    	if(mMoving)
    	{
    		return mY - mParaRadiusPixels;
    	}
    	else
    	{
    		return mY - mLauncherRadiusPixels;
    	}
    }

    @Override
    public float getBottom() {
        return mY + mParaRadiusPixels;
    }

    /**
     * A more readable way to create balls than using a 5 param
     * constructor of all numbers.
     */
    public static class Builder 
    {
        private long mNow = -1;
        private float mX = -1;
        private float mY = -1;
        private double mAngle = -1;
        private float mRadiusPixels = -1;

        private float mPixelsPerSecond = 45f;
        private boolean mIsStationary = false; 
        
        private final String TAG = "StingerLauncher Builder ";
        
        private Context mContext = null;
        
        private boolean mIsFiring;
        
        private boolean mIsReversing = false;
        
        private int[] mBitmapRids = null;

        public StingerLauncher create() 
        {
        	try
        	{
        		if(mContext == null)
        		{
        			throw new IllegalStateException("must set 'context'");
        		}
	            if (mNow < 0) {
	                throw new IllegalStateException("must set 'now'");
	            }
//	            if (mX < 0) {
//	                throw new IllegalStateException("X must be set");
//	            }
//	            if (mY < 0) {
//	                throw new IllegalStateException("Y must be stet");
//	            }
	            if (mAngle < 0) {
	                throw new IllegalStateException("angle must be set");
	            }
	            if (mAngle > 2 * Math.PI) {
	                throw new IllegalStateException("angle must be less that 2Pi");
	            }
	            if (mBitmapRids == null) {
	                throw new IllegalStateException("mBitmapRids must be set");
	            }
        	}
        	catch(Exception e)
        	{
        		Log.e(TAG + "create", e.getMessage());
        	}
            return new StingerLauncher(mContext, mNow, mPixelsPerSecond, 
            		mX, mY, mAngle, mIsFiring, mIsReversing, mBitmapRids);
        }

        public Builder setNow(long now) {
            mNow = now;
            return this;
        }

        public Builder setPixelsPerSecond(float pixelsPerSecond) {
            mPixelsPerSecond = pixelsPerSecond;
            return this;
        }

        public Builder setX(float x) {
            mX = x;
            return this;
        }

        public Builder setY(float y) {
            mY = y;
            return this;
        }

        public Builder setAngle(double angle) {
            mAngle = angle;
            return this;
        }

        public Builder setRadiusPixels(float pixels) {
            mRadiusPixels = pixels;
            return this;
        }
        
        public Builder setIsStationary(boolean isStationary) {
            mIsStationary = isStationary;
            return this;
        }
        
        public Builder setContext(Context context) {
            mContext = context;
            return this;
        }
        
        public Builder setIsFiring(boolean isFiring) {
            mIsFiring = isFiring;
            return this;
        }
        
        public Builder setBitmapRids(int[] bitMapRids) {
            mBitmapRids = bitMapRids;
            return this;
        }
        
        public Builder setIsReversing(boolean isReversing) {
            mIsReversing = isReversing;
            return this;
        }
    }
}
