package com.lam.android.attackhelicopter2;

import java.util.List;
import java.util.ArrayList;
import java.lang.ref.WeakReference;

import android.os.SystemClock;
import android.util.Log;

/**
 * A ball region is a rectangular region that contains bouncing
 * balls, and possibly one animating line.  In its {@link #update(long)} method,
 * it will update all of its balls, the moving line.  It detects collisions
 * between the balls and the moving line, and when the line is complete, handles
 * splitting off a new region.
 */
public class GameRegion extends Shape2d {

    protected float mLeft;
    protected float mRight;
    protected float mTop;
    protected float mBottom;

    private ArrayList<Bullet> mBullets;
    protected ArrayList<Bogey> mBogeys;

    private long mLastUpdate = 0;

	private static final String TAG = "GameRegion ";
	private static final long MIN_RESPITE_TIME = 3000;

    private WeakReference<GameEngine.BulletEventCallBack> mBulletEventCallBack;
    private WeakReference<GameEngine.BogeyEventCallBack> mBogeyEventCallBack;
    private WeakReference<GameEngine.BombEventCallBack> mBombEventCallBack;
    private WeakReference<GameEngine.MissileEventCallBack> mMissileEventCallBack;
    private WeakReference<GameEngine.TankEventCallBack> mTankEventCallBack;
    private WeakReference<GameEngine.BomberEventCallBack> mBomberEventCallBack;
    private WeakReference<GameEngine.FighterEventCallBack> mFighterEventCallBack;
    
    protected Helicopter mHeli;
	protected ArrayList<Tank> mTanks;
	protected ArrayList<Bomb> mBombs;
	protected ArrayList<Missile> mMissiles;
	protected ArrayList<Missile> mStingerMissiles;
	
	protected ArrayList<Bomber> mBombers;
	protected ArrayList<StingerLauncher> mLaunchers;
	protected ArrayList<Fighter> mFighters;
	
    /*
     * @param left The minimum x component
     * @param right The maximum x component
     * @param top The minimum y component
     * @param bottom The maximum y component
     * @param balls the balls of the region
     */
    public GameRegion(long now, float left, float right, 
    		float top, float bottom, ArrayList<Bogey> bogeys, 
    		ArrayList<Tank> tanks, ArrayList<Bomber> bombers,
    		ArrayList<StingerLauncher> launchers, 
    		ArrayList<Fighter> fighters,
    		Helicopter heli) 
    {
        mLastUpdate = now;
        mLeft = left;
        mRight = right;
        mTop = top;
        mBottom = bottom;
        mBullets = new ArrayList<Bullet>(8);
        mBogeys = bogeys;
        mHeli = heli;
        mTanks = tanks;
        mBombers = bombers;
        mLaunchers = launchers;
        mFighters = fighters;
        
        mBombs = new ArrayList<Bomb>(8);
        mMissiles = new ArrayList<Missile>(8);
        mStingerMissiles = new ArrayList<Missile>(8);
        
        final int numBogeys = mBogeys.size();
        for (int i = 0; i < numBogeys; i++) 
        {
            final Bogey bogey = mBogeys.get(i);
            bogey.mRegion = this;
        }
        
        final int numTanks = mTanks.size();
        for (int i = 0; i < numTanks; i++) 
        {
            final Tank tank = mTanks.get(i);
            tank.mRegion = this;
        }
        
        final int numBombers = mBombers.size();
        for (int i = 0; i < numBombers; i++) 
        {
            final Bomber bomber = mBombers.get(i);
            bomber.mRegion = this;
        }
        
        final int numLaunchers = mLaunchers.size();
        for (int i = 0; i < numLaunchers; i++) 
        {
            final StingerLauncher launcher = mLaunchers.get(i);
            launcher.mRegion = this;
        }
        
        final int numFighters = mFighters.size();
        for (int i = 0; i < numFighters; i++) 
        {
            final Fighter fighter = mFighters.get(i);
            fighter.mRegion = this;
        }
    }

    public void setCallBack(GameEngine.BulletEventCallBack callBack) {
        mBulletEventCallBack = new WeakReference<GameEngine.BulletEventCallBack>(callBack);
    }
    
    public void setCallBack(GameEngine.BogeyEventCallBack callBack) {
        mBogeyEventCallBack = new WeakReference<GameEngine.BogeyEventCallBack>(callBack);
    }
    
    public void setBombEventCallBack(GameEngine.BombEventCallBack callBack) {
        mBombEventCallBack = new WeakReference<GameEngine.BombEventCallBack>(callBack);
    }
    
    public void setMissileEventCallBack(GameEngine.MissileEventCallBack callBack) {
        mMissileEventCallBack = new WeakReference<GameEngine.MissileEventCallBack>(callBack);
    }
    
    public void setTankEventCallBack(GameEngine.TankEventCallBack callBack) {
        mTankEventCallBack = new WeakReference<GameEngine.TankEventCallBack>(callBack);
    }
    
    public void setBomberEventCallBack(GameEngine.BomberEventCallBack callBack) {
        mBomberEventCallBack = new WeakReference<GameEngine.BomberEventCallBack>(callBack);
    }
    
    public void setFighterEventCallBack(GameEngine.FighterEventCallBack callBack) {
        mFighterEventCallBack = new WeakReference<GameEngine.FighterEventCallBack>(callBack);
    }

    public float getLeft() {
        return mLeft;
    }

    public float getRight() {
        return mRight;
    }

    public float getTop() {
        return mTop;
    }

    public float getBottom() {
        return mBottom;
    }

    public ArrayList<Bullet> getBullets() {
        return mBullets;
    }
    
    public List<Bomb> getBombs() {
        return mBombs;
    }
    
    public List<Missile> getMissiles() {
        return mMissiles;
    }
    
    public List<Bogey> getBogeys() {
        return mBogeys;
    }
    
    public List<Tank> getTanks() {
        return mTanks;
    }

    public void setNow(long now) {
        mLastUpdate = now;

        // update the bullets
        final int numBalls = mBullets.size();
        for (int i = 0; i < numBalls; i++) {
            final Bullet ball = mBullets.get(i);
            ball.setNow(now);
        }
        
        // update the bombs
        final int numBombs = mBombs.size();
        for (int i = 0; i < numBombs; i++) {
            final Bomb bomb = mBombs.get(i);
            bomb.setNow(now);
        }
    }

    /**
     * Update the balls and checks for level up.
     * @param now in millis
     * @return level up is to occur.
     */
    public boolean update(long now) {

    	boolean isLevelUp = false; 

    	try
    	{
    		int numBullets = mBullets.size();
    		int numBogeys = mBogeys.size();
    		int numBombers = mBombers.size();
    		int numFighters = mFighters.size();

    		// move bullets
    		for (int i = 0; i < numBullets; i++) 
    		{
    			final Bullet bullet = mBullets.get(i);

    			if(bullet != null && !bullet.mIsDone)
    			{
    				// move bullet
    				bullet.update(now);

    				for(int j=0; j<numBogeys; j++)
    				{
    					final Bogey bogey = mBogeys.get(j);

    					if(bogey != null)
    					{
    						if (bullet.isHittingBogey(bogey) &&
    								bullet.getIsFromHelicopter()) 
    						{

    							if (mBulletEventCallBack != null)
    								mBulletEventCallBack.get().onBulletHitsBogey(now, bullet, bogey);
    						}
    					}
    					numBogeys = mBogeys.size();
    				}
    				
    				for(int j=0; j<numBombers; j++)
    				{
    					final Bomber bomber = mBombers.get(j);

    					if(bomber != null)
    					{
    						if (bullet.isHittingBomber(bomber) &&
    								bullet.mIsFromHelicopter) 
    						{

    							if (mBulletEventCallBack != null)
    								mBulletEventCallBack.get().onBulletHitsBomber(now, bullet, bomber);
    						}
    					}
    					numBombers = mBombers.size();
    				}
    				
    				for(int j=0; j<numFighters; j++)
    				{
    					final Fighter fighter = mFighters.get(j);
    					
    					if(fighter != null)
    					{
    						if (bullet.isHittingFighter(fighter) &&
    								bullet.mIsFromHelicopter) 
    						{

    							if (mBulletEventCallBack != null)
    								mBulletEventCallBack.get().onBulletHitsVehicle(now, 
    										bullet, fighter, false);
    						}
    					}
    				}
    			}
    			numBullets = mBullets.size();
    		}


    		int numBombs = mBombs.size();
    		int numTanks = mTanks.size();
    		int numLaunchers = mLaunchers.size();

    		// move bombs
    		for (int i = 0; i < numBombs; i++) 
    		{
    			final Bomb bomb = mBombs.get(i);

    			if(bomb != null &&
    					!bomb.mIsDone)
    			{
	    			// move bomb
	    			bomb.update(now);
	
	    			for(int j=0; j<numTanks; j++)
	    			{
	    				final Tank tank = mTanks.get(j);
	
	    				if (bomb.isHittingTank(tank) &&
	    						bomb.getIsFromHelicopter()) 
	    				{
	    					if (mBombEventCallBack != null && mBombEventCallBack.get() != null)
	    						mBombEventCallBack.get().onBombHitsTank(now, bomb, tank);
	    				}
	
	    				numTanks = mTanks.size();
	    			}
	    			
	    			for(int j=0; j<numLaunchers; j++)
	    			{
	    				final StingerLauncher launcher = mLaunchers.get(j);
	
	    				if (bomb.isHittingStingerLauncher(launcher) &&
	    						bomb.getIsFromHelicopter()) 
	    				{
	    					if (mBombEventCallBack != null && mBombEventCallBack.get() != null)
	    						mBombEventCallBack.get().onBombHitsLauncher(now, bomb, launcher);
	    				}
	
	    				numLaunchers = mLaunchers.size();
	    			}
    			}
    			numBombs = mBombs.size();

    		}
    		
    		// move missiles
    		int numMissiles = mMissiles.size();
    		for (int i = 0; i < numMissiles; i++) 
    		{
    			final Missile missile = mMissiles.get(i);

    			// move missile
    			if(missile != null && 
    					!missile.getIsDone())
    			{
	    			missile.update(now);
	
	    			if(mHeli != null && !mHeli.mIsDone &&
	    					missile.isHittingHeli(mHeli) &&
	    					!isWithinRespitePeriod(mHeli))
	    			{
//	    				mHeli.mIsDone = true;
	    				// update the last time heli was hit
	    				mHeli.mLastTimeHeliWasHit = SystemClock.elapsedRealtime();
						if (mMissileEventCallBack != null && mMissileEventCallBack.get() != null)
							mMissileEventCallBack.get().onMissileHitsHeli(now, missile, mHeli);
					}
    			}
    			numMissiles = mMissiles.size();

    		}

    		numBogeys = mBogeys.size();
    		// move bogeys
    		for (int i = 0; i < numBogeys; i++) 
    		{
    			final Bogey bogey = mBogeys.get(i);

    			if(bogey != null &&
    				!bogey.mIsDone)
    			{
	    			// move ball
	    			bogey.update(now);
	
	    			if(mHeli != null && !mHeli.mIsDone &&
	    					bogey.isBogeyHittingHeli(mHeli) &&
	    					!isWithinRespitePeriod(mHeli))
	    			{
//	    				mHeli.mIsDone = true;
	    				// update the last time heli was hit
	    				mHeli.mLastTimeHeliWasHit = SystemClock.elapsedRealtime();
	    				
	    				if (mBogeyEventCallBack != null && mBogeyEventCallBack.get() != null)
	    				{
	    					mBogeyEventCallBack.get().onBogeyHitsHeli(now, bogey);
	    				}
	    			}
    			}
    			
    			numBogeys = mBogeys.size();
    		}


    		numTanks = mTanks.size();

    		// move tanks
    		for (int i = 0; i < numTanks; i++) 
    		{
    			final Tank tank = mTanks.get(i);

    			if(tank != null &&
    				!tank.mIsDone)
    			{
	    			// move tank
	    			tank.update(now);
	
//	    			if(mHeli != null && !mHeli.mIsDone &&
//	    					!isWithinRespitePeriod(mHeli))
//	    			{
////	    				mHeli.mIsDone = true;
//	    				// update the last time heli was hit
//	    				mHeli.mLastTimeHeliWasHit = SystemClock.elapsedRealtime();
//		        		if (mTankEventCallBack != null && mTankEventCallBack.get() != null)
//		        		{
//		        			mTankEventCallBack.get().onTankHitsHeli(now, tank);
//		        		}
//	    			}
    			}

    			numTanks = mTanks.size();
    		}

    		// move bombers
    		numBombers = mBombers.size();
    		for (int i = 0; i < numBombers; i++) 
    		{
    			final Bomber bomber = mBombers.get(i);

    			if(bomber != null &&
    				!bomber.mIsDone)
    			{
	    			// move bomber
	    			bomber.update(now);
	
	    			if(mHeli != null && !mHeli.mIsDone &&
	    					bomber.isHittingHeli(mHeli)&&
	    					!isWithinRespitePeriod(mHeli))
	    			{
//	    				mHeli.mIsDone = true;
	    				// update the last time heli was hit
	    				mHeli.mLastTimeHeliWasHit = SystemClock.elapsedRealtime();
		        		if (mBomberEventCallBack != null && mBomberEventCallBack.get() != null)
		        		{
		        			mBomberEventCallBack.get().onBomberHitsHeli(now, bomber);
		        		}
	    			}
    			}

    			numBombers = mBombers.size();
    		}

    		// move launcherss
    		numLaunchers = mLaunchers.size();
    		for (int i = 0; i < numLaunchers; i++) 
    		{
    			final StingerLauncher launcher = 
    				mLaunchers.get(i);

    			if(launcher != null &&
    				!launcher.mIsDone &&
    				launcher.mX > -1 &&
    				launcher.mY > -1)
    			{
	    			// move launcher
	    			launcher.update(now);
    			}

    			numLaunchers = mLaunchers.size();
    		}
    		
    		// move stinger missiles
    		int numStingerMissiles = mStingerMissiles.size();
    		for (int i = 0; i < numStingerMissiles; i++) 
    		{
    			final Missile missile = mStingerMissiles.get(i);

    			// move stinger missile
    			if(missile != null && 
    					!missile.mIsDone)
    			{
	    			missile.update(now);
	
	    			if(mHeli != null && !mHeli.mIsDone &&
	    					missile.isHittingHeli(mHeli) &&
	    					!isWithinRespitePeriod(mHeli))
	    			{
//	    				mHeli.mIsDone = true;
	    				// update the last time heli was hit
	    				mHeli.mLastTimeHeliWasHit = SystemClock.elapsedRealtime();
						if (mMissileEventCallBack != null && mMissileEventCallBack.get() != null)
							mMissileEventCallBack.get().onMissileHitsHeli(now, missile, mHeli);
					}
    			}
    			numStingerMissiles = mStingerMissiles.size();

    		}
    		
    		// move fighters
    		numFighters = mFighters.size();
    		for (int i = 0; i < numFighters; i++) 
    		{
    			final Fighter fighter = mFighters.get(i);

    			if(fighter != null &&
    				!fighter.mIsDone)
    			{
	    			// move fighter
	    			fighter.update(now);
	
	    			if(mHeli != null && !mHeli.mIsDone &&
	    					fighter.isHittingHeli(mHeli)&&
	    					!isWithinRespitePeriod(mHeli))
	    			{
//	    				mHeli.mIsDone = true;
	    				// update the last time heli was hit
	    				mHeli.mLastTimeHeliWasHit = SystemClock.elapsedRealtime();
		        		if (mFighterEventCallBack != null)
		        		{
		        			mFighterEventCallBack.get().onFighterHitsHeli(now, fighter);
		        		}
	    			}
    			}

    			numFighters = mFighters.size();
    		}


    		// check for leveling up
    		isLevelUp = isTimeToLevelUp();
    		
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG + "update", e.getMessage());
    	}

    	return isLevelUp;
    }
    
    /** check for whether it's time to level up, 
     * ie all important enemies are killed*/
    private boolean isTimeToLevelUp()
    {
    	boolean isLevelUp = false;
    	
    	int numBogeys = mBogeys.size();

		int numTanks = mTanks.size();
		if(numBogeys <= 0 && 
				numTanks <= 0 )
		{
			isLevelUp = true;
			mHeli.mLastTimeLevelUp = SystemClock.elapsedRealtime();
		}
		else
		{
			isLevelUp = false;
		}
		
		return isLevelUp;
    }
    
    /** when the heli gets hit or during level up, 
     * there is a period of respite so
     * there is no double hits or bogeys spawning right next to
     * heli and thereby killing it*/
    private boolean isWithinRespitePeriod(Helicopter heli) 
    {
    	long now = SystemClock.elapsedRealtime();
    	boolean bFlag1 = false;
    	boolean bFlag2 = false;
    	
    	// for heli hit repite time check
    	// make sure it's not when game first starts
    	if(heli.mLastTimeHeliWasHit > -1)
    	{
    		bFlag1 = ((now - heli.mLastTimeHeliWasHit) 
    				<= MIN_RESPITE_TIME);
    	}
    	
    	// now for leveling up respite time check
    	// make sure it's not when game first starts
    	if(heli.mLastTimeLevelUp > -1)
    	{
    		bFlag2 = ((now - heli.mLastTimeLevelUp) 
    				<= MIN_RESPITE_TIME);
    	}
    	
    	// if one condition is T, then return T
    	return (bFlag1 || bFlag2);
		
	}

	/** Clear region of all heli, bogeys, and bullets*/
    public void clearRegion()
    {
    	mBullets.clear();
    	mBombs.clear();
        mBogeys.clear();
        mTanks.clear();
        mMissiles.clear();
        mBombers.clear();
        mLaunchers.clear();
        mStingerMissiles.clear();
    }
    
    public void clearBullets()
    {
    	mBullets.clear();
    }
    
    public void clearBombs()
    {
    	mBombs.clear();
    }
    
    public void clearMissiles()
    {
    	mMissiles.clear();
    }
    
    public void clearBogeys()
    {
    	mBogeys.clear();
    }
    
    public void clearTanks()
    {
    	mTanks.clear();
    }
    
    public void setBogeys(ArrayList<Bogey> bogeys)
    {
    	mBogeys = bogeys;
    	
    	final int numBogeys = mBogeys.size();
        for (int i = 0; i < numBogeys; i++) {
            final Bogey bogey = mBogeys.get(i);
            bogey.mRegion = this;
        }
    }

	public void reset(ArrayList<Bogey> bogeys, ArrayList<Tank> tanks,
			ArrayList<Bomber> bombers, ArrayList<StingerLauncher> launchers,
			ArrayList<Fighter> fighters) 
	{
        mBogeys = bogeys;
        mTanks = tanks;
        mBombers = bombers;
        mLaunchers = launchers;
        mFighters = fighters;
        
        final int numBogeys = mBogeys.size();
        for (int i = 0; i < numBogeys; i++) 
        {
            final Bogey bogey = mBogeys.get(i);
            bogey.mRegion = this;
        }
        
        final int numTanks = mTanks.size();
        for (int i = 0; i < numTanks; i++) 
        {
            final Tank tank = mTanks.get(i);
            tank.mRegion = this;
        }
        
        final int numBombers = mBombers.size();
        for (int i = 0; i < numBombers; i++) 
        {
            final Bomber bomber = mBombers.get(i);
            bomber.mRegion = this;
        }
        
        final int numLaunchers = mLaunchers.size();
        for (int i = 0; i < numLaunchers; i++) 
        {
            final StingerLauncher launcher = mLaunchers.get(i);
            launcher.mRegion = this;
        }
        
        final int numFighters = mFighters.size();
        for (int i = 0; i < numFighters; i++) 
        {
            final Fighter fighter = mFighters.get(i);
            fighter.mRegion = this;
        }
		
	}
}
