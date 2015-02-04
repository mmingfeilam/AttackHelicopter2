
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
public class Tank extends Vehicle {

    private static final String TAG = "Tank ";
	
    private Tank(Context context, 
    		long now, 
    		int bitmapRid, int bitmapRRid,
    		float pixelsPerSecond, 
    		float x, float y,
            double angle, boolean isFiring, boolean isReversing) 
    {
    	super(context, now, 
    			bitmapRid, bitmapRRid,
    			pixelsPerSecond, x, 
    			y, angle, isFiring, isReversing, TAG);

    }
    
    @Override
    public boolean isHittingHeli(Helicopter heli) 
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
			        
		        return (distance < (mRadius+heli.mRadius)-25);
    		}
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "isTankOverlappingHeli", e.getMessage());
    	}
    	
    	return false;
    }
    /** move bogey by changing mX, mY based on mAngle,
     * bouncing the bogey off walls when it hits a wall*/
    public void update(long now) 
    {
        if (now <= mLastUpdate) return;      

        try
        {       	
	        if(!mIsDone)
	        {
		        // bounce when at walls
		        if (mX <= mRegion.getLeft() + mRadius) {
		            // we're at left wall
		            mX = mRegion.getLeft() + mRadius;
		            mIsReversing = true;
		            
		            bounceOffLeft();
		            
		        } 
		        else if (mX >= mRegion.getRight() - mRadius) {
		            // at right wall
		            mX = mRegion.getRight() - mRadius;
		            mIsReversing = false;
		            
		            bounceOffRight();
		            
		        } 
		
		        float delta = (now - mLastUpdate) * mPixelsPerSecond;
		        delta = delta / 1000f;
		
		        mX += (delta * Math.cos(mAngle));
		
		        mLastUpdate = now;
	        }
        }
        catch (Exception e)
        {
        	Log.e(TAG + "update", e.getMessage());
        }
    }
    
    /** is the same ball*/
    public boolean isSameBogey(Tank otherBogey)
    {
    	try
    	{
	    	return (this.mAngle == otherBogey.getAngle() &&
	    			this.mRadius == otherBogey.getRadiusPixels() &&
	    			this.mX == otherBogey.mX &&
	    			this.mY == otherBogey.mY &&
	    			this.mLastUpdate == otherBogey.getLastUpdate());
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "isSameBogey", e.getMessage());
    	}
    	
    	return false;
    }
    
    @Override
    public String toString() {
        return String.format(
            "Tank(x=%f, y=%f, angle=%f)",
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
        
        private int mBitmapRid = -1;
		private int mBitmapRRid = -1;

        public Tank create() 
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
            return new Tank(mContext, mNow, 
            		mBitmapRid,
            		mBitmapRRid, 
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
