package com.lam.android.attackhelicopter2;

import android.os.SystemClock;
import android.util.Log;
import android.content.Context;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Keeps track of the current state of balls bouncing around within a a set of
 * regions.
 *
 * Note: 'now' is the elapsed time in milliseconds since some consistent point in time.
 * As long as the reference point stays consistent, the engine will be happy, though
 * typically this is {@link android.os.SystemClock#elapsedRealtime()} 
 */
public class GameEngine {

    static public interface BulletEventCallBack {

        void onBulletHitsBogey(long when, Bullet bullet, Bogey bogey);
        void onBulletHitsBomber(long when, Bullet bullet, Bomber bomber);
        void onBulletHitsVehicle(long when, Bullet bullet, Vehicle vehicle,
        		boolean isBigExplosion);
    }
    
    static public interface BogeyEventCallBack
    {
    	void onBogeyHitsHeli(long when, Bogey bogey);
    }
    
    static public interface BombEventCallBack
    {
    	void onBombHitsTank(long when, Bomb bomb, Tank tank);
    	void onBombHitsLauncher(long when, Bomb bomb, StingerLauncher launcher);
    }
    
    static public interface MissileEventCallBack
    {
    	void onMissileHitsHeli(long when, Missile missile, Helicopter heli);
    }
    
    static public interface TankEventCallBack
    {
    	void onTankHitsHeli(long when, Tank tank);
    }
    
    static public interface BomberEventCallBack
    {
    	void onBomberHitsHeli(long when, Bomber bomber);
    }
    
    static public interface FighterEventCallBack
    {
    	void onFighterHitsHeli(long when, Fighter fighter);
    }

	private static final int MAX_NUM_BOGEYS = 5;
	
	private static final int MAX_NUM_TANKS = 2;
	
	private static final int MAX_NUM_BOMBERS = 3;
	
	private static final int MAX_NUM_LAUNCHERS = 3;

	private static final String TAG = "GameEngine ";

    private final int mMinX;
    private final int mMaxX;
    private final int mMinY;
    private final int mMaxY;

    private int mBulletSpeed;
    private int mBulletRadius;
    
    private int mBogeySpeed;
    private int mBogeyRadius;
    
    private int mTankSpeed;
    private int mTankRadius;

    private Context mContext;
    
    private BulletEventCallBack mBulletEventCallBack;
    private BogeyEventCallBack mBogeyEventCallBack;
    private BombEventCallBack mBombEventCallBack;
    private MissileEventCallBack mMissileEventCallBack;
    private TankEventCallBack mTankEventCallBack;
    private BomberEventCallBack mBomberEventCallBack;
    private FighterEventCallBack mFighterEventCallBack;

    protected List<GameRegion> mRegions = new ArrayList<GameRegion>(8);
	protected Helicopter mHeli;

	private int mTankHeight;

	private int mBomberRadius;

	private int mBomberHeight;

	private int mLauncherRadius;

	private int mLauncherHeight;
	
	private int mFighterWidth;
	private int mFighterHeight;

    public GameEngine(Context context,
    		Helicopter heli,
    		int minX, 
    		int maxX,
            int minY,
            int maxY,
            int bulletSpeed,
            int bulletRadius,
            int bogeySpeed,
            int bogeyRadius,
            int tankSpeed,
            int tankRadius,
            int tankHeight,
            int bomberRadius,
            int bomberHeight,
            int launcherRadius,
            int launcherHeight,
            int fighterRadius,
            int fighterHeight) 
    {
    	mContext = context;
        mMinX = minX;
        mMaxX = maxX;
        mMinY = minY;
        mMaxY = maxY;
        mBulletSpeed = bulletSpeed;
        mBulletRadius = bulletRadius;
        
        mBogeySpeed = bogeySpeed;
        mBogeyRadius = bogeyRadius;
        
        mTankSpeed = tankSpeed;
        mTankRadius = tankRadius;
        mTankHeight = tankHeight;
        
        mBomberRadius = bomberRadius;
        mBomberHeight = bomberHeight;
        
        mLauncherRadius = launcherRadius;
        mLauncherHeight = launcherHeight;
        
        mFighterWidth = fighterRadius;
        mFighterHeight = fighterHeight;
        
        mHeli = heli;
    }

    public void setCallBack(BulletEventCallBack mCallBack) {
        this.mBulletEventCallBack = mCallBack;
    }
    
    public void setBogeyCallBack(BogeyEventCallBack callBack) {
        this.mBogeyEventCallBack = callBack;
    }
    
    public void setBombCallBack(BombEventCallBack callBack) {
        mBombEventCallBack = callBack;
    }
    
    public void setMissileCallBack(MissileEventCallBack callBack) {
        mMissileEventCallBack = callBack;
    }
    
    public void setTankCallBack(TankEventCallBack callBack) {
        mTankEventCallBack = callBack;
    }
    
    public void setBomberCallBack(BomberEventCallBack callBack) {
        mBomberEventCallBack = callBack;
    }
    
    public void setFighterCallBack(FighterEventCallBack callBack) {
        mFighterEventCallBack = callBack;
    }

    /**
     * Update the notion of 'now' in milliseconds.  This can be usefull
     * when unpausing for instance.
     * @param now Milliseconds since some consistent point in time.
     */
    public void setNow(long now) {
        for (int i = 0; i < mRegions.size(); i++) {
            final GameRegion region = mRegions.get(i);
            region.setNow(now);
        }
    }

    /**
     * Rest the engine with a certain number of bogeys
     * that will be placed randomly and sent in random directions.
     * This is called when the game begins/resets or when it's 
     * leveling up
     */
    public void reset(long now, 
    		int numBogeys, boolean isBogeyFiring, int bogeyBitmap,
    		int numTanks, boolean isTankFiring, int tankBitmap,
    		int numBombers, boolean isBomberFiring, int bomberBitmap,
    		int numLaunchers, boolean isLauncherFiring, int launcherBitmap,
    		int numFighters, boolean isFighterFiring, int fighterBitmap) 
    {
    	GameRegion region1 = null;
    	/** width of frame*/
        int fWidth = mMaxX - mMinX;
        /** height of frame*/
        int fHeight = mMaxY - mMinY;
    	
        // is heli on the left side of the screen
    	boolean isHeliOnLeftSide = true;
    	
    	Helicopter heli = null;
    	
    	if(!mRegions.isEmpty())
    	{
    		region1 = mRegions.get(0);
    		region1.clearRegion(); 
    		
    		heli = region1.mHeli;
    		if(heli != null)
    		{
    			isHeliOnLeftSide = heli.mX <= (fWidth/2);
    		}
    	}
        
        ArrayList<Bogey> bogeys = new ArrayList<Bogey>(numBogeys);
        
        int[] bogeyRids = new int[6];
        bogeyRids[0] = R.drawable.bogey;
        bogeyRids[1] = R.drawable.bogey2;
        bogeyRids[2] = R.drawable.bogey3;
        bogeyRids[3] = R.drawable.bogey_r;
        bogeyRids[4] = R.drawable.bogey2_r;
        bogeyRids[5] = R.drawable.bogey3_r;
        
        float x = 0;
    
        for (int i = 0; i < numBogeys; i++) 
        {
        	Bogey bogey = null;
        	
        	if(isHeliOnLeftSide)
        	{
	        	bogey = new Bogey.Builder()
	                    .setNow(now)
	                    .setPixelsPerSecond(mBogeySpeed)
	                    .setAngle(Math.random() * 2 * Math.PI)
	                    .setX((float) Math.random() * (fWidth/2) + mMinX + fWidth/2)
	                    .setY((float) Math.random() * (fHeight/2) + mMinY + fHeight/2)
	                    .setRadiusPixels(mBogeyRadius)
	                    .setContext(mContext)
	                    .setBitmapRids(bogeyRids)
	                    .setIsFiring(isBogeyFiring)
	                    .setIsReversing(false)
	                    .create();
	        	
	        	// keep creating new bogey until it's not overlapping another bogey
	        	while(isOverlappingOtherBogeys(bogeys, bogey))
	        	{
	        		bogey = new Bogey.Builder()
	                .setNow(now)
	                .setPixelsPerSecond(mBogeySpeed)
	                .setAngle(Math.random() * 2 * Math.PI)
	                .setX((float) Math.random() * (fWidth/2) + mMinX + fWidth/2)
	                .setY((float) Math.random() * (fHeight/2) + mMinY + fHeight/2)
	                .setRadiusPixels(mBogeyRadius)
	                .setContext(mContext)
	                .setBitmapRids(bogeyRids)
	                .setIsFiring(isBogeyFiring)
	                .setIsReversing(false)
	                .create();
	        	}
        	}
        	else
        	{
        		bogey = new Bogey.Builder()
                .setNow(now)
                .setPixelsPerSecond(mBogeySpeed)
                .setAngle(Math.random() * Math.PI)
                .setX((float) Math.random() * (fWidth/2))
                .setY((float) Math.random() * (fHeight/2))
                .setRadiusPixels(mBogeyRadius)
                .setContext(mContext)
                .setBitmapRids(bogeyRids)
                .setIsFiring(isBogeyFiring)
                .setIsReversing(true)
                .create();
    	
		    	// keep creating new bogey until it's not overlapping another bogey
		    	while(isOverlappingOtherBogeys(bogeys, bogey))
		    	{
		    		bogey = new Bogey.Builder()
		            .setNow(now)
		            .setPixelsPerSecond(mBogeySpeed)
		            .setAngle(Math.random() * Math.PI)
		            .setX((float) Math.random() * (fWidth/2))
		            .setY((float) Math.random() * (fHeight/2))
		            .setRadiusPixels(mBogeyRadius)
		            .setContext(mContext)
		            .setBitmapRids(bogeyRids)
		            .setIsFiring(isBogeyFiring)
		            .setIsReversing(true)
		            .create();
		    	}
        	}
         	
        	if(bogey != null)
        	{
        		bogeys.add(bogey);
        	}
        }

        // tanks
        ArrayList<Tank> tanks = new ArrayList<Tank>(numTanks);
        
        int tankRid, tankRRid;
    	
    	if(tankBitmap == 0)
    	{
    		tankRid = R.drawable.tank;
    		tankRRid = R.drawable.tank_r;
    	}
    	else if(tankBitmap == 1)
    	{
    		tankRid = R.drawable.tank2;
    		tankRRid = R.drawable.tank2_r;
    	}
    	else
    	{
    		tankRid = R.drawable.tank;
    		tankRRid = R.drawable.tank_r;
    	}
        
    	x = fWidth/2;
    	//(float) Math.random() * (fWidth/2);

        for (int i = 0; i < numTanks; i++) 
        {
        	Tank tank = null;
        	
        	if(isHeliOnLeftSide)
        	{
	        	x = x+i*100;
	        	
	        	tank = new Tank.Builder()
	                    .setNow(now)
	                    .setPixelsPerSecond(mTankSpeed)
	                    .setAngle(Math.PI)
	                    .setIsReversing(false)
	                    .setX(x)
	                    .setY(mMaxY - mTankHeight/2)
	                    .setContext(mContext)
	                    .setIsFiring(isTankFiring)
	                    .setBitmapRid(tankRid)
	                    .setBitmapRRid(tankRRid)
	                    .create();
	        	
	        	// keep creating new tank coordinates 
	        	// until it's not overlapping another tank
	        	
	        	while(isOverlappingOtherSimilarVehicles((ArrayList)tanks, 
	        			(Vehicle) tank))
	        	{
	        		x += 100;
	        		if(x >= mMaxX-tank.mRadius)
	        		{
	        			x = mMaxX-tank.mRadius;
	        		}
	        	}
	        	
	    		tank = new Tank.Builder()
	            .setNow(now)
	            .setPixelsPerSecond(mTankSpeed)
	            .setAngle(Math.PI)
	            .setIsReversing(false)
	            .setX(x)
	            .setY(mMaxY - mTankHeight/2)
	            .setContext(mContext)
	            .setIsFiring(isTankFiring)
	            .setBitmapRid(tankRid)
	            .setBitmapRRid(tankRRid)
	            .create();
        	}
        	else
        	{
        		x = x-i*100;
	        	
	        	tank = new Tank.Builder()
	                    .setNow(now)
	                    .setPixelsPerSecond(mTankSpeed)
	                    .setAngle(0)
	                    .setIsReversing(true)
	                    .setX(x)
	                    .setY(mMaxY - mTankHeight/2)
	                    .setContext(mContext)
	                    .setIsFiring(isTankFiring)
	                    .setBitmapRid(tankRid)
	                    .setBitmapRRid(tankRRid)
	                    .create();
	        	
	        	// keep creating new tank coordinates 
	        	// until it's not overlapping another tank
	        	
	        	while(isOverlappingOtherSimilarVehicles((ArrayList)tanks, 
	        			(Vehicle) tank))
	        	{
	        		x -= 100;
	        		if(x <= mMinX+tank.mRadius)
	        		{
	        			x = mMinX+tank.mRadius;
	        		}
	        	}
	        	
	    		tank = new Tank.Builder()
	            .setNow(now)
	            .setPixelsPerSecond(mTankSpeed)
	            .setAngle(0)
	            .setIsReversing(true)
	            .setX(x)
	            .setY(mMaxY - mTankHeight/2)
	            .setContext(mContext)
	            .setIsFiring(isTankFiring)
	            .setBitmapRid(tankRid)
	            .setBitmapRRid(tankRRid)
	            .create();
        	}
         	
        	if(tank != null)
        	{
        		tanks.add(tank);
        	}
        }
        
        // bombers
        ArrayList<Bomber> bombers = new ArrayList<Bomber>(numBombers);
        
        int bomberRid, bomberRRid;
    	
    	if(bomberBitmap == 0)
    	{
    		bomberRid = R.drawable.bomber;
    		bomberRRid = R.drawable.bomber_r;
    	}
    	else if(bomberBitmap == 1)
    	{
    		bomberRid = R.drawable.bomber2;
    		bomberRRid = R.drawable.bomber2_r;
    	}
    	else
    	{
    		bomberRid = R.drawable.bomber;
    		bomberRRid = R.drawable.bomber_r;
    	}
        
        for (int i = 0; i < numBombers; i++) 
        {
        	Bomber bomber = null;
        	
        	if(isHeliOnLeftSide)
        	{
        		bomber = new Bomber.Builder()
                .setNow(now)
                .setPixelsPerSecond(mBogeySpeed)
                .setAngle(Math.PI)
                .setIsReversing(false)
                .setX((float) Math.random() * (fWidth/2) + mMinX + fWidth/2)
                .setY(mMinY+mBomberHeight)
                .setContext(mContext)
                .setIsFiring(isBomberFiring)
                .setBitmapRid(bomberRid)
                .setBitmapRRid(bomberRRid)
                .create();
    	
	        	// keep creating new bomber until it's not overlapping another bomber
	        	while(isOverlappingOtherBombers(bombers, bomber))
	        	{
	        		bomber = new Bomber.Builder()
	                .setNow(now)
	                .setPixelsPerSecond(mBogeySpeed)
                	.setAngle(Math.PI)
                	.setIsReversing(false)
                	.setX((float) Math.random() * (fWidth/2) + mMinX + fWidth/2)
	                .setX((float) Math.random() * (fWidth/2))
	                .setY(mMinY+mBomberHeight)
	                .setContext(mContext)
	                .setIsFiring(isBomberFiring)
	                .setBitmapRid(R.drawable.bomber)
	                .setBitmapRRid(R.drawable.bomber_r)
	                .create();
	        	}
        	}
        	else
        	{
	        	bomber = new Bomber.Builder()
	                    .setNow(now)
	                    .setPixelsPerSecond(mBogeySpeed)
	                    .setAngle(0)
	                    .setIsReversing(true)
	                    .setX((float) Math.random() * (fWidth/2))
	                    .setY(mMinY+mBomberHeight)
	                    .setContext(mContext)
	                    .setIsFiring(isBomberFiring)
	                    .setBitmapRid(bomberRid)
	                    .setBitmapRRid(bomberRRid)
	                    .create();
	        	
	        	// keep creating new bomber until it's not overlapping another bomber
	        	while(isOverlappingOtherBombers(bombers, bomber))
	        	{
	        		bomber = new Bomber.Builder()
	                .setNow(now)
	                .setPixelsPerSecond(mBogeySpeed)
	                .setAngle(0)
	                .setIsReversing(true)
	                .setX((float) Math.random() * (fWidth/2))
	                .setY(mMinY+mBomberHeight)
	                .setContext(mContext)
	                .setIsFiring(isBomberFiring)
	                .setBitmapRid(R.drawable.bomber)
	                .setBitmapRRid(R.drawable.bomber_r)
	                .create();
	        	}
        	}
         	
        	if(bomber != null)
        	{
        		bombers.add(bomber);
        	}
        }
        
        // stinger launchers
        ArrayList<StingerLauncher> launchers = 
        	new ArrayList<StingerLauncher>(numLaunchers);
        
        // image resource ids 
        int[] launcherRids = new int[4];
        launcherRids[0] = R.drawable.stinger_launcher;
        launcherRids[1] = R.drawable.stinger_launcher_r;
        launcherRids[2] = R.drawable.paratrooper;
        launcherRids[3] = R.drawable.paratrooper_r;
        
        for (int i = 0; i < numLaunchers; i++) 
        {
	        /* notice the coordinates are not valid, this means 
	         * they won't show until the bombers drops them,
	         * when they are given proper coordinates*/
	        StingerLauncher launcher = null;
	        
	        if(isHeliOnLeftSide)
	        {
				launcher = new StingerLauncher.Builder()
	            .setNow(SystemClock.elapsedRealtime())
	            .setPixelsPerSecond(AttackHelicopterView.BOGEY_SPEED)
	            .setAngle(Math.PI/2)
	            .setIsReversing(false)
	            .setX(-1)
	            .setY(-1)
	            .setContext(mContext)
	            .setIsFiring(isLauncherFiring)
	            .setBitmapRids(launcherRids)
	            .create();
	        }
	        else
	        {
	        	launcher = new StingerLauncher.Builder()
	            .setNow(SystemClock.elapsedRealtime())
	            .setPixelsPerSecond(AttackHelicopterView.BOGEY_SPEED)
	            .setAngle(Math.PI/2)
	            .setIsReversing(true)
	            .setX(-1)
	            .setY(-1)
	            .setContext(mContext)
	            .setIsFiring(isLauncherFiring)
	            .setBitmapRids(launcherRids)
	            .create();
	        }
	        
	        if(launcher != null)
	        {
	        	launchers.add(launcher);
	        }
        }
        
        // fighter
        ArrayList<Fighter> fighters = 
        	new ArrayList<Fighter>(numFighters);
        
        // image Resource ids
        int fighterRid, fighterRRid;
    	
    	if(fighterBitmap == 0)
    	{
    		fighterRid = R.drawable.talon;
    		fighterRRid = R.drawable.talon_r;
    	}
    	else if(fighterBitmap == 1)
    	{
    		fighterRid = R.drawable.fighter;
    		fighterRRid = R.drawable.fighter_r;
    	}
    	else
    	{
    		fighterRid = R.drawable.fighter;
    		fighterRRid = R.drawable.fighter_r;
    	}
        
        for (int i = 0; i < numFighters; i++) 
        {
        	Fighter fighter = null;
        	
        	if(isHeliOnLeftSide)
        	{
	        	fighter = new Fighter.Builder()
                .setNow(now)
                .setPixelsPerSecond((float)(mBogeySpeed*2))
                .setAngle(Math.PI)
                .setX((float) Math.random() * (fWidth/2) + fWidth/2)
                .setY((float) Math.random()*80 + mFighterHeight*2)
                .setContext(mContext)
                .setIsFiring(isFighterFiring)
                .setBitmapRid(fighterRid)
                .setBitmapRRid(fighterRRid)
                .setIsReversing(false)
                .create();
    	
		    	// keep creating new fighter until it's not overlapping another fighter
		    	while(isOverlappingOtherFighters(fighters, fighter))
		    	{
		    		fighter = new Fighter.Builder()
		            .setNow(now)
		            .setPixelsPerSecond((float)(mBogeySpeed*2))
		            .setAngle(Math.PI)
		            .setIsReversing(false)
		            .setX((float) Math.random() * (fWidth/2) + fWidth/2)
		            .setY((float) Math.random()*80 + mFighterHeight*2)
		            .setContext(mContext)
		            .setIsFiring(isFighterFiring)
		            .setBitmapRid(fighterRid)
		            .setBitmapRRid(fighterRRid)
		            .create();
		    	}
        	}
        	else
        	{
	        	fighter = new Fighter.Builder()
	                    .setNow(now)
	                    .setPixelsPerSecond((float)(mBogeySpeed*2))
	                    .setAngle(0)
	                    .setX((float) Math.random() * fWidth/2)
	                    .setY((float) Math.random()*80 + mMinY+mFighterHeight*2)
	                    .setContext(mContext)
	                    .setIsFiring(isFighterFiring)
	                    .setBitmapRid(fighterRid)
	                    .setBitmapRRid(fighterRRid)
	                    .setIsReversing(true)
	                    .create();
	        	
	        	// keep creating new fighter until it's not overlapping another fighter
	        	while(isOverlappingOtherFighters(fighters, fighter))
	        	{
	        		fighter = new Fighter.Builder()
	                .setNow(now)
	                .setPixelsPerSecond((float)(mBogeySpeed*2))
	                .setAngle(0)
	                .setIsReversing(true)
	                .setX((float) Math.random() * fWidth/2)
	                .setY((float) Math.random()*80 + mMinY+mFighterHeight*2)
	                .setContext(mContext)
	                .setIsFiring(isFighterFiring)
	                .setBitmapRid(fighterRid)
	                .setBitmapRRid(fighterRRid)
	                .create();
	        	}
        	}
        	
        	if(fighter != null)
        	{
        		fighters.add(fighter);
        	}
        }
        
        // initial set up of region
        if(mRegions.isEmpty())
        {
	        GameRegion region = new GameRegion(now, mMinX, mMaxX, mMinY, mMaxY, bogeys,
	        		tanks, bombers, launchers, fighters, 
	        		mHeli);
	        region.setCallBack(mBulletEventCallBack);
	        region.setCallBack(mBogeyEventCallBack);
	        region.setBombEventCallBack(mBombEventCallBack);
	        region.setMissileEventCallBack(mMissileEventCallBack);
	        region.setTankEventCallBack(mTankEventCallBack);
	        region.setBomberEventCallBack(mBomberEventCallBack);
	        region.setFighterEventCallBack(mFighterEventCallBack);
	
	        mRegions.add(region);
        }
        else
        {
        	region1.reset(bogeys, tanks, bombers, launchers, fighters);
        }
    }
    
    private boolean isOverlappingOtherBogeys(List<Bogey> bogeys, 
    		Bogey bogey)
    {    	
    	for(int i=0; i<bogeys.size(); i++)
    	{
    		if(bogey.isBogeyOverlapping(bogeys.get(i)))
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    private boolean isOverlappingOtherTanks(List<Tank> tanks, 
    		Tank tank)
    {    	
    	for(int i=0; i<tanks.size(); i++)
    	{
    		final Tank tank1 = tanks.get(i);
    		
    		if(tank.isOverlapping(tank1.mRadius, tank1.mHeight,
    				tank1.mX, tank1.mY))
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    private boolean isOverlappingOtherSimilarVehicles(ArrayList<Vehicle> vehicles, 
    		Vehicle vehicle)
    {    	
    	for(int i=0; i<vehicles.size(); i++)
    	{
    		final Vehicle vehicle1 = vehicles.get(i);
    		
    		if(vehicle.isOverlapping(vehicle1.mRadius, vehicle1.mHeight,
    				vehicle1.mX, vehicle1.mY))
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    private boolean isOverlappingOtherBombers(ArrayList<Bomber> bombers, 
    		Bomber bomber)
    {    	
    	for(int i=0; i<bombers.size(); i++)
    	{
    		if(bomber.isOverlapping(bombers.get(i)))
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    private boolean isOverlappingOtherFighters(ArrayList<Fighter> fighters, 
    		Fighter fighter)
    {    	
    	for(int i=0; i<fighters.size(); i++)
    	{
    		if(fighter.isOverlapping(fighters.get(i)))
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    public List<GameRegion> getRegions() {
        return mRegions;
    }

    /**
     * @return the area in the region in pixel*pixel
     */
    public float getArea() {
        return (mMaxX - mMinX) * (mMaxY - mMinY);
    }

    /**
     * @param now The latest notion of 'now'
     * @return whether any new regions were added by the update.
     */
    public boolean update(long now) {
        //boolean regionChange = false;
        boolean isLevelUp = false;
        
        try
        {
        
	        if(!mRegions.isEmpty())
	        {
		        Iterator<GameRegion> it = mRegions.iterator();
		        while (it.hasNext()) 
		        {
		            final GameRegion region = it.next();
		            isLevelUp = region.update(now);
		        }
	        }
        }
        catch(Exception e)
        {
        	Log.e(TAG + "update", e.getMessage());
        }

        return isLevelUp;
    }
}
