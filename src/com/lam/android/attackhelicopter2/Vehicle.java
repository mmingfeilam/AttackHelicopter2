package com.lam.android.attackhelicopter2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

public class Vehicle extends Shape2d
{
	protected long mLastUpdate;
    
	/** center x*/
	protected float mX;
	/** center y*/
	protected float mY;
	
	/** old center x*/
	protected float mOldX;
	/** old center y*/
	protected float mOldY;
    
    /** angle of trajectory*/
	protected double mAngle;

    /** speed*/
	protected float mPixelsPerSecond;

	protected Shape2d mRegion;  
    protected boolean mIsDone;
    
    /** bogey is changing direction */
    protected boolean mIsReversing;
	
    protected Context mContext;
    
    /** radius or width/2*/
    protected final float mRadius; 
    protected final float mWidth;
    protected final float mHeight;
    protected final float mHalfHeight;
    
    /** bogey is changing direction */
	protected long mProgress = 0;
	
	protected final Bitmap mBitmap;
	protected final Bitmap mRBitmap;

	protected boolean mIsFiring;
//	protected boolean mIsAtFiringLevel;

	protected boolean mMoving;
	
    protected final String TAG;

	protected Vehicle(Context context, 
    		long now, 
    		int bitmapRid, int bitmapRRid,
    		float pixelsPerSecond, 
    		float x, float y,
            double angle, boolean isFiring,
            boolean isReversing,
            String tag) 
    {
    	mContext = context;
    	
        mLastUpdate = now;
        mPixelsPerSecond = pixelsPerSecond;
        mX = x;
        mY = y;
        
        mOldX = x;
        mOldY = y;
        
        mAngle = angle;
        mIsDone = false;
        mIsReversing = isReversing;
        
        mBitmap = BitmapFactory.decodeResource(
        		mContext.getResources(),
        		bitmapRid);

        mRBitmap = BitmapFactory.decodeResource(
        		mContext.getResources(),
        		bitmapRRid);

        mWidth = mBitmap.getWidth();
        mRadius = mWidth/2;
        
        mHeight = mBitmap.getHeight();
        mHalfHeight = mHeight/2;
        
        mIsFiring = isFiring;
        
//        mIsAtFiringLevel = false;
        
        mMoving = true;
        
        TAG = tag;
    }

    protected float getLeft(float radiusPixels) {
        return mX - radiusPixels;
    }

    protected float getRight(float radiusPixels) {
        return mX + radiusPixels;
    }

    protected float getTop(float radiusPixels) {
        return mY - radiusPixels;
    }

    protected float getBottom(float radiusPixels) {
        return mY + radiusPixels;
    }
    
    protected double getAngle() {
        return mAngle;
    }
    
    /**
     * Get the region the ball is contained in.
     */
    protected Shape2d getRegion() {
        return mRegion;
    }
    
    protected long getLastUpdate()
    {
    	return this.mLastUpdate;
    }
    
    protected boolean getIsDone()
    {
    	return mIsDone;
    }
    
    protected boolean getIsReversing()
    {
    	return mIsReversing;
    }
    
    /** reset the last update time for non-running mode*/
    protected void setLastUpdate(long update)
    {
    	mLastUpdate = update;
    }

    protected void setNow(long now) {
        mLastUpdate = now;
    }
    
    public boolean isHittingHeli(Helicopter heli) 
    {
    	try
    	{
    		if(heli != null)
    		{
    			float minHeight = (mHeight+heli.mHeight)/2;
    			float minWidth = mRadius+heli.mRadius;
    			
    			return (Math.abs(mX-heli.mX)<minWidth &&
    					Math.abs(mY-heli.mY)<(minHeight-35));
    		}
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "isOverlappingHeli", e.getMessage());
    	}
    	
    	return false;
    }
    
    public void draw(Canvas canvas, Paint paint) 
    {
    	if(!mIsReversing)
    	{
            canvas.drawBitmap(mBitmap, mX - mRadius, 
            		mY - mHalfHeight, paint);
    	}
    	else
    	{
    		canvas.drawBitmap(mRBitmap, mX - mRadius, 
            		mY - mHalfHeight, paint);
    	}

    }
    
    @Override
    public float getLeft() {
        return mX - mRadius;
    }
    
    
    public float getRight() {
        return mX + mRadius;
    }

    
    public float getTop() {
        return mY - mHalfHeight;
    }

    
    public float getBottom() {
        return mY + mHalfHeight;
    }
    
    public boolean getIsFiring()
    {
    	return mIsFiring;
    }
    
    public void setIsFiring(boolean isFiring)
    {
    	mIsFiring = isFiring;
    }
    
//    public boolean getIsAtFiringLevel()
//    {
//    	return mIsAtFiringLevel;
//    }
//    
//    public void setIsAtFiringLevel(boolean isAtFiringLevel)
//    {
//    	mIsAtFiringLevel = isAtFiringLevel;
//    }
    
    // if it's reversing, going right
    public boolean IsReversing()
    {
    	return mIsReversing;
    }
    
    protected void bounceOffBottom() 
    {
        if (mAngle < 0.5*Math.PI) {
            // going right
            mAngle = -mAngle;
        } else {
            // going left
            mAngle += (Math.PI - mAngle) * 2;
        }
    }

    protected void bounceOffRight() {
        if (mAngle > 1.5*Math.PI) {
            // going up
            mAngle -= (mAngle - 1.5*Math.PI) * 2;
        } else {
            // going down
            mAngle += (.5*Math.PI - mAngle) * 2;
        }
    }

    protected void bounceOffTop() {
        if (mAngle < 1.5 * Math.PI) {
            // going left
            mAngle -= (mAngle - Math.PI) * 2;
        } else {
            // going right
            mAngle += (2*Math.PI - mAngle) * 2;
            mAngle -= 2*Math.PI;
        }
    }

    protected void bounceOffLeft() {
        if (mAngle < Math.PI) {
            // going down
            mAngle -= ((mAngle - (Math.PI / 2)) * 2);
        } else {
            // going up
            mAngle += (((1.5 * Math.PI) - mAngle) * 2);
        }
    }
    
    /** collision check*/
    public boolean isOverlapping(Vehicle other) 
    {
    	try
    	{
//	        final float dy = other.mY - mY;
//	        final float dx = other.mX - mX;
//	
//	        final float distance = (float) Math.sqrt(dy * dy + dx * dx);
//		        
//	        return (distance < (mRadiusPixels+other.mRadiusPixels));
    		float minHeight = (mHeight+other.mHeight)/2;
			float minWidth = (mRadius+other.mRadius)/2;
			
			float xDiff = Math.abs(mX-other.mX);
			float yDiff = Math.abs(mY-other.mY);
			
			return (xDiff<minWidth-20 &&
					yDiff<minHeight);
    	
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "isOverlapping", e.getMessage());
    	}
    	
    	return false;
    }
    
    /** collision check with just coordinates*/
    public boolean isOverlapping(float otherRadius, float otherHeight, 
    		float otherX, float otherY) 
    {
    	try
    	{
    		float minHeight = (mHeight+otherHeight)/2;
			float minWidth = (mRadius+otherRadius)/2;
			
			float xDiff = Math.abs(mX-otherX);
			float yDiff = Math.abs(mY-otherY);
			
			return (xDiff<minWidth-20 &&
					yDiff<minHeight);
    	
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "isOverlapping", e.getMessage());
    	}
    	
    	return false;
    }
    
    public float getRadiusPixels() 
    {
        return mRadius;
    }
    
    /** is the same*/
    public boolean isSame(Vehicle other)
    {
    	try
    	{
	    	return (this.mAngle == other.getAngle() &&
	    			this.mRadius == other.getRadiusPixels() &&
	    			this.mX == other.mX &&
	    			this.mY == other.mY &&
	    			this.mLastUpdate == other.getLastUpdate());
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "isSame", e.getMessage());
    	}
    	
    	return false;
    }
    
    /** move Vehicle by changing mX, mY based on mAngle,
     * bouncing the Vehicle off wall when it hits a wall*/
    public void update(long now) 
    {
        if (now <= mLastUpdate) return;      

        try
        {       	
	        // should the object move
	        if(!mIsDone)
	        {
		        // check when at end of screen
		        if (!mIsReversing &&
		        		mX <= (mRegion.getLeft()+mRadius)) 
		        {
		            // we're at left wall
		            mX = mRegion.getLeft() + mRadius;
		            mIsDone = true;
		        } 
		        if (mIsReversing &&
		        		mX >= (mRegion.getRight()-mRadius)) 
		        {
		            // we're at right wall
		            mX = mRegion.getRight() - mRadius;
		            mIsDone = true;
		        } 
		
		        if(!mIsDone)
		        {
			        float delta = (now - mLastUpdate) * mPixelsPerSecond;
			        delta = delta / 1000f;
			
			        mX += (delta * Math.cos(mAngle));
			        mLastUpdate = now;
		        }
	        }
        }
        catch (Exception e)
        {
        	Log.e(TAG + "update", e.getMessage());
        }
    }

}
