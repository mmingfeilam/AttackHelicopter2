package com.lam.android.attackhelicopter2;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

public class Helicopter extends Vehicle {

    private static final String TAG = "Helicopter ";
    
    private final Bitmap m1Bitmap;
	private final Bitmap m2Bitmap;
	private final Bitmap m3Bitmap;
	private final Bitmap m1RBitmap;
	private final Bitmap m2RBitmap;
	private final Bitmap m3RBitmap;
	
	/** the last time the heli was hit*/
	protected long mLastTimeHeliWasHit;

	/** the last time level up occurred.*/
	protected long mLastTimeLevelUp;
	
	protected boolean mIsDone;
	
    private Helicopter(Context context, 
    		long now, 
    		ArrayList<Integer> Rids,
    		float pixelsPerSecond, 
    		float x, float y,
            double angle, boolean isFiring,
            boolean isReversing) 
    {
    	super(context, now, 
    			Rids.get(0), Rids.get(1),
    			pixelsPerSecond, x, 
    			y, angle, isFiring, isReversing, TAG);
    	
    	 m1Bitmap = BitmapFactory.decodeResource(
         		context.getResources(),
                 Rids.get(0));
         m2Bitmap = BitmapFactory.decodeResource(
         		context.getResources(),
         		Rids.get(1));
         m3Bitmap = BitmapFactory.decodeResource(
         		context.getResources(),
         		Rids.get(2));
         m1RBitmap = BitmapFactory.decodeResource(
         		context.getResources(),
         		Rids.get(3));
         m2RBitmap = BitmapFactory.decodeResource(
         		context.getResources(),
         		Rids.get(4));
         m3RBitmap = BitmapFactory.decodeResource(
         		context.getResources(),
         		Rids.get(5));
         
         // initial value
         mLastTimeHeliWasHit = -1;
         
         mLastTimeLevelUp = -1;
         
    }
    
    @Override
    public void draw(Canvas canvas, Paint paint) 
    {
    	if (mProgress < 70L) 
    	{
    		if(!mIsReversing)
    		{
    			canvas.drawBitmap(m1Bitmap, mX - mRadius, 
    					mY - mHeight/2, paint);
    		}
    		else
    		{
    			canvas.drawBitmap(m1RBitmap, mX - mRadius, 
    					mY - mHeight/2, paint);
    		}
    	} 
    	else if (mProgress < 140L) 
    	{
    		if(!mIsReversing)
    		{
    			canvas.drawBitmap(m2Bitmap, mX - mRadius, 
    					mY - mHeight/2, paint);
    		}
    		else
    		{
    			canvas.drawBitmap(m2RBitmap, mX - mRadius, 
    					mY - mHeight/2, paint);
    		}
    	} 
    	else if (mProgress < 200L) 
    	{
    		if(!mIsReversing)
    		{
    			canvas.drawBitmap(m3Bitmap, mX - mRadius, 
    					mY - mHeight/2, paint);
    		}
    		else
    		{
    			canvas.drawBitmap(m3RBitmap, mX - mRadius, 
    					mY - mHeight/2, paint);
    		}
    	}
    }
    
    
    /** move object by changing mX, mY based on mAngle,
     * bouncing the object off walls when it hits a wall*/
    public void update(long now) 
    {
    	try
    	{
	        mProgress += (now - mLastUpdate);
	        
	        if(mProgress >= 200L)
	        {
	        	mProgress = 0L;
	//        	mMpHelicopter.start();
	        }
	        
	        mLastUpdate = now;
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "update", e.getMessage());
    	}
    }
    
    @Override
    public String toString() {
        return String.format(
            "Helicopter(x=%f, y=%f, angle=%f)",
                mX, mY, Math.toDegrees(mAngle));
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
        
        private final String TAG = "Tank Builder ";
        
        private Context mContext = null;
        
        private boolean mIsFiring;
        private boolean mIsReversing = false;
        
        private ArrayList<Integer> mBitmapRids = null;

        public Helicopter create() 
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
	            if (mX < 0) {
	                throw new IllegalStateException("X must be set");
	            }
	            if (mY < 0) {
	                throw new IllegalStateException("Y must be stet");
	            }
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
            return new Helicopter(mContext, mNow, 
            		mBitmapRids,
            		mPixelsPerSecond, 
            		mX, mY, mAngle, mIsFiring, mIsReversing);
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
        
        public Builder setBitmapRids(ArrayList<Integer> bitMapRids) {
            mBitmapRids = bitMapRids;
            return this;
        }
        
        public Builder setIsReversing(boolean isReversing) {
            mIsReversing = isReversing;
            return this;
        }
    }
}
