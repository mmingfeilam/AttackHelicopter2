
package com.lam.android.attackhelicopter2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

/**
 * A bomb has a current location, a trajectory angle, a speed in pixels per
 * second, and a last update time.  It is capable of updating itself based on
 * its trajectory and speed.
 *
 * It also knows its boundaries, and will 'bounce' off them when it reaches them.
 */
public class Bomb extends Shape2d {

    private static final String TAG = "Bomb ";
	private long mLastUpdate;
    
	/** center x*/
	private float mX;
	
	/** center y*/
    private float mY;
    
    /** angle of trajectory*/
    private double mAngle;

    private final float mPixelsPerSecond;
    private final float mRadiusPixels;

    protected Shape2d mRegion;
    private boolean mIsFromHelicopter;  
    protected boolean mIsDone;
	private boolean mIsHeliReversing;
	private Bitmap mHeliBombsRBitmap;
	private Bitmap mHeliBombsBitmap;
	private Context mContext;

    private Bomb(long now, Context context, 
    		float pixelsPerSecond, float x, float y,
            double angle, float radiusPixels, boolean isFromHelicopter,
            boolean isHeliReversing) {
        
    	mContext = context;
    	mLastUpdate = now;
        mPixelsPerSecond = pixelsPerSecond;
        mX = x;
        mY = y;
        mAngle = angle;
        mRadiusPixels = radiusPixels;
        mIsFromHelicopter = isFromHelicopter;
        mIsDone = false;
        mIsHeliReversing = isHeliReversing;
        
        mHeliBombsBitmap = BitmapFactory.decodeResource(
        		mContext.getResources(),
                R.drawable.bombs2);
        
        mHeliBombsRBitmap = BitmapFactory.decodeResource(
        		mContext.getResources(),
                R.drawable.bombs2_r);
    }

    public float getLeft() {
        return mX - mRadiusPixels;
    }

    public float getRight() {
        return mX + mRadiusPixels;
    }

    public float getTop() {
        return mY - mRadiusPixels;
    }

    public float getBottom() {
        return mY + mRadiusPixels;
    }

    public float getRadiusPixels() {
        return mRadiusPixels;
    }

    public double getAngle() {
        return mAngle;
    }
    
    public int getNumRedBalls() {
        return -1;
    }

    /**
     * Get the region the ball is contained in.
     */
    public Shape2d getRegion() {
        return mRegion;
    }
    
    public boolean getIsFromHelicopter()
    {
    	return mIsFromHelicopter;
    }
    
    public long getLastUpdate()
    {
    	return this.mLastUpdate;
    }

    public void setNow(long now) {
        mLastUpdate = now;
    }

    /** collision check*/
    public boolean isBombOverlapping(Bomb otherBomb) 
    {
    	try
    	{
	        final float dy = otherBomb.mY - mY;
	        final float dx = otherBomb.mX - mX;
	
	        final float distance = (float) Math.sqrt(dy * dy + dx * dx);
		        
	        return (distance < (mRadiusPixels+otherBomb.getRadiusPixels()));
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "isBombOverlapping", e.getMessage());
    	}
    	
    	return false;
    }

    /** check to see if bomb hits target*/
    public boolean isHittingTank(Tank tank) 
    {
    	try
    	{
    		float bogeyLeft = tank.getLeft();
    		float bogeyRight = tank.getRight();
    		float bogeyTop = tank.getTop();
    		float bogeyBottom = tank.getBottom();
    		
    		float left = getLeft();
    		float right = getRight();
    		float top = getTop();
    		float bottom = getBottom();
    		
    		return ((left <= bogeyRight) &&
    				(top <= bogeyBottom) &&
    				(right >= bogeyLeft) &&
    				(bottom >= bogeyTop)
    				);    	
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "isHittingTank", e.getMessage());
    	}
    	
    	return false;
    }
    
    /** check to see if bomb hits target*/
    public boolean isHittingStingerLauncher(StingerLauncher launcher) 
    {
    	try
    	{
    		float bogeyLeft = launcher.getLeft();
    		float bogeyRight = launcher.getRight();
    		float bogeyTop = launcher.getTop();
    		float bogeyBottom = launcher.getBottom();
    		
    		float left = getLeft();
    		float right = getRight();
    		float top = getTop();
    		float bottom = getBottom();
    		
    		return ((left <= bogeyRight) &&
    				(top <= bogeyBottom) &&
    				(right >= bogeyLeft) &&
    				(bottom >= bogeyTop)
    				);       	
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "isHittingStingerLauncher", e.getMessage());
    	}
    	
    	return false;
    }
    
    /** move bomb by changing mX, mY based on mAngle,
     * when it hits a wall it cease to exist*/
    public void update(long now) 
    {
    	try
    	{
	        if (now <= mLastUpdate) return;
	
	        // should the ball move
	        boolean bMoveBomb = true;
	                
	        if(bMoveBomb)
	        {
		        // bounce when at walls
		        if (mX <= mRegion.getLeft() + mRadiusPixels) {
		            // we're at left wall
		            mX = mRegion.getLeft() + mRadiusPixels;
		            mIsDone = true;
	//	            bounceOffLeft();
		        } else if (mY <= mRegion.getTop() + mRadiusPixels) {
		            // at top wall
		            mY = mRegion.getTop() + mRadiusPixels;
		            mIsDone = true;
	//	            bounceOffTop();
		        } else if (mX >= mRegion.getRight() - mRadiusPixels) {
		            // at right wall
		            mX = mRegion.getRight() - mRadiusPixels;
		            mIsDone = true;
	//	            bounceOffRight();
		        } else if (mY >= mRegion.getBottom() - mRadiusPixels) {
		            // at bottom wall
		            mY = mRegion.getBottom() - mRadiusPixels;
		            mIsDone = true;
	//	            bounceOffBottom();
		        }
		
		        float delta = (now - mLastUpdate) * mPixelsPerSecond;
		        delta = delta / 1000f;
		
		        mX += (delta * Math.cos(mAngle));
		        mY += (delta * Math.sin(mAngle));
		
		        mLastUpdate = now;
	        }
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "update", e.getMessage());
    	}
    }
    
    /** is the same ball*/
    public boolean isSameBomb(Bomb otherBomb)
    {
    	try
    	{
	    	return (this.mAngle == otherBomb.getAngle() &&
	    			this.mRadiusPixels == otherBomb.getRadiusPixels() &&
	    			this.mX == otherBomb.mX &&
	    			this.mY == otherBomb.mY &&
	    			this.mLastUpdate == otherBomb.getLastUpdate());
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "isSameBomb", e.getMessage());
    	}
    	
    	return false;
    }
    
    public void draw(Canvas canvas, Paint paint)
    {
    	if(getIsFromHelicopter())
		{
    		if(mIsHeliReversing)
    		{
	        	canvas.drawBitmap(
	                    mHeliBombsRBitmap,
	                    mX - mRadiusPixels,
	                    mY - mRadiusPixels,
	                    paint);
    		}
    		else
    		{
	        	canvas.drawBitmap(
	        			mHeliBombsBitmap,
	        			mX - mRadiusPixels,
	                    mY - mRadiusPixels,
	                    paint);
    		}

		}
    }


    @Override
    public String toString() {
        return String.format(
            "Ball(x=%f, y=%f, angle=%f)",
                mX, mY, Math.toDegrees(mAngle));
    }
    
    /** reset the last update time for non-running mode*/
    public void setLastUpdate(long update)
    {
    	mLastUpdate = update;
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
        private boolean mIsFromHeli = false; 
        private Context mContext;
        
        private boolean mIsHeliReversing;
        
        private final String TAG = "Bomb Builder ";

        public Bomb create() 
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
	            if (mRadiusPixels <= 0) {
	                throw new IllegalStateException("radius must be set");
	            }
        	}
        	catch(Exception e)
        	{
        		Log.e(TAG + "create", e.getMessage());
        	}
            return new Bomb(mNow, mContext, 
            		mPixelsPerSecond, mX, mY, mAngle, mRadiusPixels, 
            		mIsFromHeli, mIsHeliReversing);
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
        
        public Builder setIsFromHeli(boolean isFromHeli) {
            mIsFromHeli = isFromHeli;
            return this;
        }
        
        public Builder setContext(Context context) {
            mContext = context;
            return this;
        }
        
        public Builder setIsHeliReversing(boolean isReversing)
        {
        	mIsHeliReversing = isReversing;
        	return this;
        }
    }
}
