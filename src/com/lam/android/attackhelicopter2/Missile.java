package com.lam.android.attackhelicopter2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

public class Missile extends Vehicle {

    private static final String TAG = "Missile ";
	public static final int FROM_HELICOPTER = 0;
	public static final int FROM_TANK = 1;
	public static final int FROM_STINGER = 2;
	public static final int FROM_FIGHTER = 3;
	
    private int mIsFrom;  

    private Missile(long now, float pixelsPerSecond, float x, float y,
    		Context context, 
    		int bitmapRid, int bitmapRRid,
            double angle, boolean isFiring,
            int from, boolean isReversing) 
    {
    	
    	super(context, now, 
    			bitmapRid, bitmapRRid,
    			pixelsPerSecond, x, 
    			y, angle, isFiring, isReversing, TAG);
        
        mIsFrom = from;
    }

    /**
     * Get the region the ball is contained in.
     */
    public Shape2d getRegion() {
        return mRegion;
    }
    
    public int getIsFrom()
    {
    	try
    	{
    		
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG +"getIsFromHelicopter", e.getMessage());
    	}
    	
    	return mIsFrom;
    }
    
    /** check to see if ball hits target*/
    public boolean isHittingBogey(Bogey bogey) 
    {
    	try
    	{
	        final float dy = bogey.mY - mY;
	        final float dx = bogey.mX - mX;
	
	        final float distance = (float) Math.sqrt(dy * dy + dx * dx);
		        
	        return (distance < (mRadius+bogey.getRadiusPixels())-25);
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "isHittingBogey", e.getMessage());
    	}
    	
    	return false;
    }
    
    /** check to see if ball hits target*/
    public boolean isHittingHeli(Helicopter heli) 
    {
    	try
    	{
    		float heliLeft = heli.getLeft();
    		float heliRight = heli.getRight();
    		float heliTop = heli.getTop();
    		float heliBottom = heli.getBottom();
    		
    		return mX>heliLeft && mX<heliRight &&
    			mY>heliTop && mY<(heliBottom-heli.mHalfHeight);

    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "isHittingHeli", e.getMessage());
    	}
    	
    	return false;
    }
    
    /** move ball by changing mX, mY based on mAngle,
     * bouncing the ball off walls when it hits a wall*/
    public void update(long now) 
    {
    	try
    	{
	        if (now <= mLastUpdate) return;
	
	        if(!mIsDone)
	        {
		        // bounce when at walls
		        if (mX <= mRegion.getLeft() + mRadius) {
		            // we're at left wall
		            mX = mRegion.getLeft() + mRadius;
		            mIsDone = true;
	//	            bounceOffLeft();
		        } else if (mY <= mRegion.getTop() + mRadius) {
		            // at top wall
		            mY = mRegion.getTop() + mRadius;
		            mIsDone = true;
//		            bounceOffTop();
		        } else if (mX >= mRegion.getRight() - mRadius) {
		            // at right wall
		            mX = mRegion.getRight() - mRadius;
		            mIsDone = true;
	//	            bounceOffRight();
		        } else if (mY >= mRegion.getBottom() - mRadius) {
		            // at bottom wall
		            mY = mRegion.getBottom() - mRadius;
		            mIsDone = true;
//		            bounceOffBottom();
		        }
		
		        if(!mIsDone)
		        {
			        float delta = (now - mLastUpdate) * mPixelsPerSecond;
			        delta = delta / 1000f;
			
			        mX += (delta * Math.cos(mAngle));
			        mY += (delta * Math.sin(mAngle));
		        }
		
		        mLastUpdate = now;
	        }
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "update", e.getMessage());
    	}
    }

    @Override
    public String toString() {
        return String.format(
            "Missile(x=%f, y=%f, angle=%f)",
                mX, mY, Math.toDegrees(mAngle));
    }
    
//    @Override
//    public void draw(Canvas canvas, Paint paint)
//    {
//    	if(mDirection == Missile.DIRECTION_RIGHT)
//		{
//			canvas.drawBitmap(
//                mRBitmap,
//                mX - mRadius,
//                mY - mHeight,
//                paint);
//		}
//		else
//		{
//			canvas.drawBitmap(
//					mBitmap,
//                    mX - mRadius,
//                    mY - mHeight,
//                    paint);
//		}
//    }

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

        private float mPixelsPerSecond = 45f;
        private int mIsFrom = Missile.FROM_HELICOPTER; 
        
        private final String TAG = "Missile Builder ";
		
		private Paint mPaint = null;
		private Context mContext = null;
		
		private int mBitmapRid = -1;
		private int mBitmapRRid = -1;
		
		private boolean mIsReversing = false;

        public Missile create() 
        {
        	try
        	{
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
	            if (mPaint == null) {
	                throw new IllegalStateException("paint must be set");
	            }
	            if (mContext == null) {
	                throw new IllegalStateException("context must be set");
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
        	
        	/*Missile(long now, float pixelsPerSecond, 
        	 * float x, float y,
    		Context context, 
    		int bitmapRid, int bitmapRRid,
            double angle, boolean isFiring,
            int from, int direction) */
            return new Missile(mNow, mPixelsPerSecond, mX, mY,
            		mContext, mBitmapRid, mBitmapRRid,
            		mAngle, false, mIsFrom, mIsReversing);
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

        public Builder setIsFrom(int isFrom) {
            mIsFrom = isFrom;
            return this;
        }
        
        public Builder setIsReversing(boolean isReversing) {
            mIsReversing = isReversing;
            return this;
        }
        
        public Builder setPaint(Paint paint) {
            mPaint = paint;
            return this;
        }
        
        public Builder setContext(Context context) {
            mContext = context;
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
    }
}

