package com.lam.android.attackhelicopter2;

import android.content.Context;
import android.util.Log;

/**
 * A Bullet has a current location, a trajectory angle, a speed in pixels per
 * second, and a last update time.  It is capable of updating itself based on
 * its trajectory and speed.
 *
 * It also knows its boundaries, and will 'bounce' off them when it reaches them.
 */
public class Bullet extends Vehicle {

    private static final String TAG = "Bullet ";
	private static final float BULLET_Y_OFFSET = -5;
    protected boolean mIsFromHelicopter;  

    private Bullet(Context context, long now, 
    		int bitmapRid, int bitmapRRid,
    		float pixelsPerSecond, float x, float y,
            double angle, boolean isFiring, 
            boolean isFromHelicopter,
            boolean isReversing) 
    {
    	super(context, now, 
    			bitmapRid, bitmapRRid,
    			pixelsPerSecond, x, 
    			y, angle, isFiring, isReversing, TAG);
    	
        mIsFromHelicopter = isFromHelicopter;
    }


    /**
     * Get the region the ball is contained in.
     */
    public Shape2d getRegion() {
        return mRegion;
    }
    
    public boolean getIsFromHelicopter()
    {
    	try
    	{
    		
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG +"getIsFromHelicopter", e.getMessage());
    	}
    	
    	return mIsFromHelicopter;
    }
    
    /** check to see if bullet hits target*/
    public boolean isHittingBogey(Bogey bogey) 
    {
    	try
    	{
    		final float dy = bogey.mY - mY;
	        final float dx = bogey.mX - mX;
	
	        final float distance = (float) Math.sqrt(dy * dy + dx * dx);
		        
	        return (distance < (mRadius+bogey.mRadius));
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "isHittingBogey", e.getMessage());
    	}
    	
    	return false;
    }
    
    public boolean isHittingVehicle(Vehicle vehicle) 
    {
    	try
    	{
    		float vehicleLeft = vehicle.getLeft();
    		float vehicleRight = vehicle.getRight();
    		float vehicleTop = vehicle.getTop();
    		float vehicleBottom = vehicle.getBottom();
    		
    		float left = getLeft();
    		float right = getRight();
    		float top = getTop();
    		float bottom = getBottom();
    		
    		return ((left <= vehicleRight) &&
    				(top <= vehicleBottom) &&
    				(right >= vehicleLeft) &&
    				(bottom >= vehicleTop)
    				);
    	
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "isHittingVehicle", e.getMessage());
    	}
    	
    	return false;
    }
    
    /** check to see if bullet hits target*/
    public boolean isHittingBomber(Bomber bomber) 
    {
    	try
    	{
//    		float minHeight = (mRadius*2+bomber.mHeight)/2-5;
//			float minWidth = mRadius+bomber.mRadius;
//			
//			return (Math.abs(mX-bomber.mX)<minWidth &&
//					Math.abs(mY-bomber.mY)<minHeight);
    	
    		float bogeyLeft = bomber.getLeft();
    		float bogeyRight = bomber.getRight();
    		float bogeyTop = bomber.getTop();
    		float bogeyBottom = bomber.getBottom();
    		
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
    		Log.e(TAG + "isHittingBomber", e.getMessage());
    	}
    	
    	return false;
    }
    
    /** check to see if bullet hits target*/
    public boolean isHittingFighter(Fighter fighter) 
    {
    	try
    	{
//    		float minHeight = (mRadius*2+fighter.mHeight)/2;
//			float minWidth = mRadius+fighter.mRadius;
//			
//			return (Math.abs(mX-fighter.mX)<minWidth &&
//					Math.abs(mY-fighter.mY)<minHeight);
    		float bogeyLeft = fighter.getLeft();
    		float bogeyRight = fighter.getRight();
    		float bogeyTop = fighter.getTop();
    		float bogeyBottom = fighter.getBottom();
    		
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
    		Log.e(TAG + "isHittingFighter", e.getMessage());
    	}
    	
    	return false;
    }
    
    /** check to see if ball hits target*/
    public boolean isHittingHeli(Helicopter heli) 
    {
    	try
    	{
    		float heliX = (heli.getLeft()+heli.getRight())/2;
    		float heliY = (heli.getTop()+heli.getBottom())/2;
    		
	        final float dy = heliY - mY;
	        final float dx = heliX - mX;
	
	        final float distance = (float) Math.sqrt(dy * dy + dx * dx);
		        
	        return (distance < (mRadius+heli.mRadius)-25);
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
	
	        // should the ball move
	        if(!mIsDone)
	        {
		        // bounce when at walls
		        if (mX <= mRegion.getLeft() + mRadius) {
		            // we're at left wall
		            mX = mRegion.getLeft() + mRadius;
		            mIsDone = true;
		        } else if (mY <= mRegion.getTop() + mRadius) {
		            // at top wall
		            mY = mRegion.getTop() + mRadius;
		            mIsDone = true;
		        } else if (mX >= mRegion.getRight() - mRadius) {
		            // at right wall
		            mX = mRegion.getRight() - mRadius;
		            mIsDone = true;
		        } else if (mY >= mRegion.getBottom() - mRadius) {
		            // at bottom wall
		            mY = mRegion.getBottom() - mRadius;
		            mIsDone = true;
		        }
		
		        if(!mIsDone)
		        {
			        float delta = (now - mLastUpdate) * mPixelsPerSecond;
			        delta = delta / 1000f;
			
			        mX += (delta * Math.cos(mAngle));
			        mY += (delta * Math.sin(mAngle));
			
			        mLastUpdate = now;
		        }
	        }
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "update", e.getMessage());
    	}
    }

    /**
     * Given that ball a and b have collided, adjust their angles to reflect 
     * their state after the collision.
     *
     * This method works based on the conservation of energy and momentum in an elastic
     * collision.  Because the balls have equal mass and speed, it ends up being that they
     * simply swap velocities along the axis of the collision, keeping the velocities tangent
     * to the collision constant.
     *
     * @param ballA The first ball in a collision
     * @param ballB The second ball in a collision
     */
    public static void adjustForCollision(Bullet ballA, Bullet ballB) 
    {
    	try
    	{
    		// arctangent(opp/adj) = q
	        final double collA = Math.atan2(ballB.mY - ballA.mY, ballB.mX - ballA.mX);
	        final double collB = Math.atan2(ballA.mY - ballB.mY, ballA.mX - ballB.mX);
	
	        // cosine(q) = adj/hyp
	        final double ax = Math.cos(ballA.mAngle - collA);
	        
	        // sine(q) = opp/hyp
	        final double ay = Math.sin(ballA.mAngle - collA);
	
	        // cosine(q) = adj/hyp
	        final double bx = Math.cos(ballB.mAngle - collB);
	        
	        // sine(q) = opp/hyp
	        final double by = Math.sin(ballB.mAngle - collB);
	
	        // arctangent(opp/adj) = q
	        final double diffA = Math.atan2(ay, -bx);
	        final double diffB = Math.atan2(by, -ax);
	
	        // now get the right proportion in terms of area of each ball
	        float fFractionA = ballA.getArea()/
	        	(ballA.getArea() + ballB.getArea());
	        
	        // now get the right proportion in terms of area of each ball
	        float fFractionB = ballB.getArea()/
        	(ballA.getArea() + ballB.getArea());
	        
	        ballA.mAngle = (collA + diffA) * fFractionB;
	        ballB.mAngle = (collB + diffB) * fFractionA;
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "adjustForCollision", e.getMessage());
    	}
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
        private Context mContext = null;
        
        private float mPixelsPerSecond = 45f;
        private boolean mIsFromHeli = false; 
        
        private int mBitmapRid = -1;
		private int mBitmapRRid = -1;
        private boolean mIsReversing = false;
		
        private final String TAG = "Bullet Builder ";

        public Bullet create() 
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
        	
        	/* Bullet(Context context, long now, 
    		int bitmapRid, int bitmapRRid,
    		float pixelsPerSecond, float x, float y,
            double angle, float radiusPixels, boolean isFiring, 
            boolean isFromHelicopter)
            */
        	
            return new Bullet(mContext, mNow, 
            		mBitmapRid, mBitmapRRid,
            		mPixelsPerSecond, mX, mY, mAngle, false, 
            		mIsFromHeli, mIsReversing);
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
        
        public Builder setBitmapRid(int bitMapRid) {
            mBitmapRid = bitMapRid;
            return this;
        }
        
        public Builder setBitmapRRid(int bitMapRRid) {
            mBitmapRRid = bitMapRRid;
            return this;
        }
        
        public Builder setContext(Context context) {
            mContext = context;
            return this;
        }
        
        public Builder setIsReversing(boolean isReversing) {
            mIsReversing = isReversing;
            return this;
        }
    }
}
