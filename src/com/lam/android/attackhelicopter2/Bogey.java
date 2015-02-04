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
public class Bogey extends Vehicle {

    private static final String TAG = "Bogey ";
		
	private final Bitmap mBogeyBitmap;
	private final Bitmap mBogey2Bitmap;
	private final Bitmap mBogey3Bitmap;
	private final Bitmap mBogeyRBitmap;
	private final Bitmap mBogey2RBitmap;
	private final Bitmap mBogey3RBitmap;
	
    private Bogey(Context context, 
    		long now, float pixelsPerSecond, 
    		float x, float y,
            double angle, float radiusPixels, 
            boolean isFiring, boolean isReversing, 
            int[] Rids) 
    {
    	
    	super(context, now, 
    			Rids[0], Rids[1],
    			pixelsPerSecond, x, 
    			y, angle, isFiring, isReversing, TAG);
    	        
        mBogeyBitmap = BitmapFactory.decodeResource(
        		context.getResources(),
                Rids[0]);
        mBogey2Bitmap = BitmapFactory.decodeResource(
        		context.getResources(),
        		Rids[1]);
        mBogey3Bitmap = BitmapFactory.decodeResource(
        		context.getResources(),
        		Rids[2]);
        mBogeyRBitmap = BitmapFactory.decodeResource(
        		context.getResources(),
        		Rids[3]);
        mBogey2RBitmap = BitmapFactory.decodeResource(
        		context.getResources(),
        		Rids[4]);
        mBogey3RBitmap = BitmapFactory.decodeResource(
        		context.getResources(),
        		Rids[5]);
    }

    /** collision check*/
    public boolean isBogeyOverlapping(Bogey otherBogey) 
    {
    	try
    	{
	        final float dy = otherBogey.mY - mY;
	        final float dx = otherBogey.mX - mX;
	
	        final float distance = (float) Math.sqrt(dy * dy + dx * dx);
		        
	        return (distance < (mRadius+otherBogey.getRadiusPixels())
	                // avoid jittery collisions
	                && !movingAwayFromEachother(this, otherBogey));
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "isBogeyOverlapping", e.getMessage());
    	}
    	
    	return false;
    }
    
    public boolean isBogeyHittingHeli(Helicopter heli) 
    {
    	try
    	{
    		if(heli != null)
    		{
	    		float heliY = heli.mY;
	    		float heliX = heli.mX;
	    		
		        final float dy = heliY - mY;
		        final float dx = heliX - mX;
		
		        final float distance = (float) Math.sqrt(dy * dy + dx * dx);
			        
		        return (distance < (mRadius+heli.mRadius-50));
    		}
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "isBogeyOverlappingHeli", e.getMessage());
    	}
    	
    	return false;
    }

    private boolean movingAwayFromEachother(Bogey bogeyA, Bogey bogeyB) {
        if(bogeyA.getAngle() == 0 ||
        		bogeyB.getAngle() ==0)
        {
        	return false;
        }
    	
    	double collA = Math.atan2(bogeyB.mY - bogeyA.mY, bogeyB.mX - bogeyA.mX);
        double collB = Math.atan2(bogeyA.mY - bogeyB.mY, bogeyA.mX - bogeyB.mX);

        double ax = Math.cos(bogeyA.mAngle - collA);
        double bx = Math.cos(bogeyB.mAngle - collB);

        return ax + bx < 0;        
    }

    /** move bogey by changing mX, mY based on mAngle,
     * bouncing the bogey off walls when it hits a wall*/
    public void update(long now) 
    {
        if (now <= mLastUpdate) return;      

        try
        {
            mProgress += (now - mLastUpdate);
            
            // keep blades turning
            if(mProgress >= 200L)
            {
            	mProgress = 0L;
            }
        	
	        // should the bogey move
	        boolean bMoveBogey = true;
	                
	        if(bMoveBogey)
	        {
		        // bounce when at walls
		        if (mX <= mRegion.getLeft() + mRadius) {
		            // we're at left wall
		            mX = mRegion.getLeft() + mRadius;
		            mIsReversing = true;
		            bounceOffLeft();
		        } else if (mY <= mRegion.getTop() + mRadius) {
		            // at top wall
		            mY = mRegion.getTop() + mRadius;
		            bounceOffTop();
		        } else if (mX >= mRegion.getRight() - mRadius) {
		            // at right wall
		            mX = mRegion.getRight() - mRadius;
		            mIsReversing = false;
		            bounceOffRight();
		        } else if (mY >= mRegion.getBottom() - mRadius) {
		            // at bottom wall
		            mY = mRegion.getBottom() - mRadius;
		            bounceOffBottom();
		        }
		
		        float delta = (now - mLastUpdate) * mPixelsPerSecond;
		        delta = delta / 1000f;
		
		        mX += (delta * Math.cos(mAngle));
		        mY += (delta * Math.sin(mAngle));
		
		        mLastUpdate = now;
	        }
        }
        catch (Exception e)
        {
        	Log.e(TAG + "update", e.getMessage());
        }
    }
    
    @Override
    public void draw(Canvas canvas, Paint paint) {
        if (mProgress < 70L) 
        {
        	if(!mIsReversing)
        	{
	            canvas.drawBitmap(mBogeyBitmap, mX - mRadius, 
	            		mY - mRadius, paint);
        	}
        	else
        	{
        		canvas.drawBitmap(mBogeyRBitmap, mX - mRadius, 
	            		mY - mRadius, paint);
        	}
        } 
        else if (mProgress < 140L) 
        {
        	if(!mIsReversing)
        	{
	            canvas.drawBitmap(mBogey2Bitmap, mX - mRadius, 
	            		mY - mRadius, paint);
        	}
        	else
        	{
        		canvas.drawBitmap(mBogey2RBitmap, mX - mRadius, 
	            		mY - mRadius, paint);
        	}
        } 
        else if (mProgress < 200L) 
        {
        	if(!mIsReversing)
        	{
	            canvas.drawBitmap(mBogey3Bitmap, mX - mRadius, 
	            		mY - mRadius, paint);
        	}
        	else
        	{
        		canvas.drawBitmap(mBogey3RBitmap, mX - mRadius, 
	            		mY - mRadius, paint);
        	}
        }
    }
    
    /** is the same ball*/
    public boolean isSameBogey(Bogey otherBogey)
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
            "Ball(x=%f, y=%f, angle=%f)",
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
        
        private final String TAG = "Bogey Builder ";
        
        private Context mContext = null;
        
        private int[] mBitmapRids = null;
        
        private boolean mIsFiring;
        
        private boolean mIsReversing = false;
		
        public Bogey create() 
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
            return new Bogey(mContext, mNow, mPixelsPerSecond, mX, 
            		mY, mAngle, mRadiusPixels, mIsFiring, mIsReversing, 
            		mBitmapRids);
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
        
        public Builder setBitmapRids(int[] bitMapRids) {
            mBitmapRids = bitMapRids;
            return this;
        }
        
        public Builder setIsFiring(boolean isFiring) {
            mIsFiring = isFiring;
            return this;
        }
        
        public Builder setIsReversing(boolean isReversing) {
            mIsReversing = isReversing;
            return this;
        }
    }
	
}
