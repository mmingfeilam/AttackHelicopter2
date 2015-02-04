
package com.lam.android.attackhelicopter2;

import android.content.Context;
import android.util.Log;

/**
 * A Vehicle object has a current location, a trajectory angle, a speed in pixels per
 * second, and a last update time.  It is capable of updating itself based on
 * its trajectory and speed.
 *
 * It also knows its boundaries, and will 'bounce' off them when it reaches them.
 */
public class Bomber extends Vehicle {

    private static final String TAG = "Bomber ";
	
    private Bomber(Context context, 
    		long now, 
    		int bitmapRid, int bitmapRRid,
    		float pixelsPerSecond, 
    		float x, float y,
            double angle, boolean isFiring, 
            boolean isReversing) 
    {
    	super(context, now, 
    			bitmapRid, bitmapRRid,
    			pixelsPerSecond, x, 
    			y, angle, isFiring, isReversing,
    			TAG);

    }

    /** collision check*/
    @Override
    public boolean isHittingHeli(Helicopter heli) 
    {
    	try
    	{
    		if(heli != null)
    		{
    			float minHeight = (mHeight+heli.mHeight)/2;
    			float minWidth = (mRadius+heli.mRadius);
    			
    			return (Math.abs(mX-heli.mX)<minWidth &&
    					Math.abs(mY-heli.mY)<(minHeight-15));
    		}
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "isOverlappingHeli", e.getMessage());
    	}
    	
    	return false;
    }
    
    @Override
    public String toString() {
        return String.format(
            "Bomber(x=%f, y=%f, angle=%f)",
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
        
        private final String TAG = "Bomber Builder ";
        
        private Context mContext = null;
        
        private boolean mIsFiring;
        
        private int mBitmapRid = -1;
		private int mBitmapRRid = -1;
		private boolean mIsReversing = false;

        public Bomber create() 
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
	            if (mBitmapRid < 0) {
	                throw new IllegalStateException("bitMapRid must be set");
	            }
	            if (mBitmapRRid < 0) {
	                throw new IllegalStateException("bitMapRRid must be set");
	            }
        	}
        	catch(Exception e)
        	{
        		Log.e(TAG + "create", e.getMessage());
        	}
            return new Bomber(mContext, mNow, mBitmapRid,
            		mBitmapRRid, mPixelsPerSecond, 
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
        
        public Builder setBitmapRid(int bitMapRid) {
            mBitmapRid = bitMapRid;
            return this;
        }
        
        public Builder setBitmapRRid(int bitMapRRid) {
            mBitmapRRid = bitMapRRid;
            return this;
        }
        
        public Builder setIsReversing(boolean isReversing) {
            mIsReversing = isReversing;
            return this;
        }
    }
}
