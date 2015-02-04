package com.lam.android.attackhelicopter2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.XmlResourceParser;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.lam.android.attackhelicopter2.AttackHelicopterView.AttackHelicopterThread;

/**
 * <ul>
 * <li>animating by calling invalidate() from draw()
 * <li>loading and drawing resources
 * <li>handling onPause() in an animation
 * </ul>
 */
public class AttackHelicopter extends Activity 
	implements AttackHelicopterView.BallEngineCallBack,
	NewGameCallback,
	DialogInterface.OnCancelListener,
	AccelerometerListener,
	OrientationListener
{
    private static final int MENU_EASY = 1;
    private static final int MENU_HARD = 2;
    private static final int MENU_MEDIUM = 3;
    private static final int MENU_PAUSE = 4;
    private static final int MENU_RESUME = 5;
    private static final int MENU_START = 6;
    private static final int MENU_STOP = 7;
    
    private static final int MENU_LEAST_SENSITIVE = 8;
    private static final int MENU_LESS_SENSITIVE = 9;
    private static final int MENU_NORMAL_SENSITIVE = 10;
    private static final int MENU_MORE_SENSITIVE = 11;
    private static final int MENU_MOST_SENSITIVE = 12;
    
    protected static final String HI_SCORE = "High_Score";

    /** A handle to the thread that's actually running the animation. */
    private AttackHelicopterThread mAttackHelicopterThread;

    /** A handle to the View in which the game is running. */
    private AttackHelicopterView mAttackHelocopterView;

	private boolean mVibrateOn;

	private Vibrator mVibrator;
	
    public static final int COLLISION_VIBRATE_MILLIS = 50;

	private static final int WELCOME_DIALOG = 0;
    
    private MediaPlayer mMpExplosion;
	private MediaPlayer mMpMachineGun;
	private MediaPlayer mMpHelicopter;

	private SoundManager mSoundManager;

	private String TAG = "AttackHelicopter ";

	private TextView mLevelInfo;

	private TextView mLivesLeftInfo;

	private TextView mScoreInfo;

	private int mNumLives;
	private int mScore;
	private int mLevelNum;
	
	private WelcomeDialog mWelcomeDialog;

	protected static Context CONTEXT;
    
	private Handler mHandler = new Handler();
	
	private Runnable mOnBogeyHitsHelicopterRunnable = 
		new Runnable() {
		public void run() 
		{
			updateLivesDisplay(mNumLives);
		}
	};
	
	private Runnable mOnBulletHitsBogeyRunnable = 
		new Runnable() {
		public void run() 
		{
			updateScoreDisplay(mScore);
		}
	};
	
	private Runnable mOnBulletHitsHelicopterRunnable = 
		new Runnable() {
		public void run() 
		{
			updateLivesDisplay(mNumLives);
		}
	};
	
	private Runnable mCreateNewHelicopter = 
		new Runnable() {
		public void run()
		{
			mAttackHelicopterThread.CreateNewHelicopter();
		}
		
	};
	
	private Runnable mOnActivityLevelUpRunnable = 
		new Runnable() {

		public void run() 
		{
			updateLevelDisplay(mLevelNum);
		}
	};
	
	private Runnable mHelicopterSoundRunnable = 
		new Runnable() {
		public void run() 
		{
			if(!mMpHelicopter.isPlaying())
			{
				mMpHelicopter.start();
			}
		}
	};

	private boolean mIsFirstTime;
	
	protected ArrayList<LevelInfo> mLevelInfos = 
		new ArrayList<LevelInfo>();

	protected class LevelInfo
	{
		protected int mNumBombers;
		protected boolean mIsBomberFiring;
		protected int mBomberBitmap;
		
		protected int mNumTanks;
		protected boolean mIsTankFiring;
		protected int mTankBitmap;
		
		protected int mNumLaunchers;
		protected boolean mIsLauncherFiring;
		protected int mLauncherBitmap;
		
		protected int mNumBogeys;
		protected boolean mIsBogeyFiring;
		protected int mBogeyBitmap;
		
		protected int mNumFighters;
		protected boolean mIsFighterFiring;
		protected int mFighterBitmap;
		
		public LevelInfo(int numBogeys, boolean isBogeyFiring, int bogeyBitmap,
				int numTanks, boolean isTankFiring, int tankBitmap,
				int numLaunchers, boolean isLauncherFiring, int launcherBitmap,
				int numBombers, boolean isBomberFiring, int bomberBitmap,
				int numFighters, boolean isFighterFiring, int fighterBitmap)
		{
			mNumBombers = numBombers;
			mIsBomberFiring = isBomberFiring;
			mBomberBitmap = bomberBitmap;
			
			mNumTanks = numTanks;
			mIsTankFiring = isTankFiring;
			mTankBitmap = tankBitmap;
			
			mNumLaunchers = numLaunchers;
			mIsLauncherFiring = isLauncherFiring;
			mLauncherBitmap = launcherBitmap;
			
			mNumBogeys = numBogeys;
			mIsBogeyFiring = isBogeyFiring;
			mBogeyBitmap = bogeyBitmap;
			
			mNumFighters = numFighters;
			mIsFighterFiring = isFighterFiring;
			mFighterBitmap = fighterBitmap;
		}
		
	}
	
	protected void getLevelInfo()
	{
		try
		{
			int numBombers;
			boolean isBomberFiring;
			int bomberBitmap;
			
			int numTanks;
			boolean isTankFiring;
			int tankBitmap;
			
			int numLaunchers;
			boolean isLauncherFiring;
			int launcherBitmap;
			
			int numBogeys;
			boolean isBogeyFiring;
			int bogeyBitmap;
			
			int numFighters;
			boolean isFighterFiring;
			int fighterBitmap; 
			
			LevelInfo levelInfo = null;
			
			// Get the Android-specific compiled XML parser.
			XmlResourceParser xrp = this.getResources().getXml(R.xml.level_info);
			while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
				if (xrp.getEventType() == XmlResourceParser.START_TAG) {
					String s = xrp.getName();
					if (s.equals("level")) 
					{
						isBomberFiring = 
							xrp.getAttributeBooleanValue(null, "isBomberFiring", false);
						numBombers = 
							xrp.getAttributeIntValue(null, "numBombers", 0);
						bomberBitmap = xrp.getAttributeIntValue(null, "bomberBitmap", 0);;
						
						numTanks = 
							xrp.getAttributeIntValue(null, "numTanks", 0);
						isTankFiring = 
							xrp.getAttributeBooleanValue(null, "isTankFiring", false);
						tankBitmap = xrp.getAttributeIntValue(null, "tankBitmap", 0);
						
						numLaunchers = xrp.getAttributeIntValue(null, "numLaunchers", 0);
						isLauncherFiring = 
							xrp.getAttributeBooleanValue(null, "isLauncherFiring", false);
						launcherBitmap = 
							xrp.getAttributeIntValue(null, "launcherBitmap", 0);
						
						numBogeys = xrp.getAttributeIntValue(null, "numBogeys", 0);
						isBogeyFiring = 
							xrp.getAttributeBooleanValue(null, "isBogeyFiring", false);
						bogeyBitmap = xrp.getAttributeIntValue(null, "bogeyBitmap", 0);
						
						numFighters = xrp.getAttributeIntValue(null, "numFighters", 0);
						isFighterFiring = 
							xrp.getAttributeBooleanValue(null, "isFighterFiring", false);
						fighterBitmap = xrp.getAttributeIntValue(null, "fighterBitmap", 0);
						
						
						levelInfo = new LevelInfo(numBogeys, isBogeyFiring, bogeyBitmap,
								numTanks, isTankFiring, tankBitmap,
								numLaunchers, isLauncherFiring, launcherBitmap,
								numBombers, isBomberFiring, bomberBitmap,
								numFighters, isFighterFiring, fighterBitmap);
					
						if(levelInfo != null)
						{
							mLevelInfos.add(levelInfo);
						}
					}
				} 
				xrp.next();
			}
			xrp.close();

		} 
		catch (XmlPullParserException xppe) 
		{
			Log.e(TAG + "getLevelInfo", "Failure of .getEventType or .next, probably bad file format");
		} 
		catch (IOException ioe) 
		{
			Log.e(TAG + "getLevelInfo", "Unable to read resource file");
		}
		catch (Exception ioe) 
		{
			Log.e(TAG + "getLevelInfo", ioe.getMessage());
		}
	}

    /**
     * Invoked during init to give the Activity a chance to set up its Menu.
     * 
     * @param menu the Menu to which entries may be added
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_START, 0, R.string.menu_start);
        menu.add(0, MENU_STOP, 0, R.string.menu_stop);
        menu.add(0, MENU_PAUSE, 0, R.string.menu_pause);
        menu.add(0, MENU_RESUME, 0, R.string.menu_resume);
        menu.add(0, MENU_EASY, 0, R.string.menu_easy);
        menu.add(0, MENU_MEDIUM, 0, R.string.menu_medium);
        menu.add(0, MENU_HARD, 0, R.string.menu_hard);
        menu.add(0, MENU_LEAST_SENSITIVE, 0, R.string.menu_least_sensitive);
        menu.add(0, MENU_LESS_SENSITIVE, 0, R.string.menu_less_sensitive);
        menu.add(0, MENU_NORMAL_SENSITIVE, 0, R.string.menu_normal_sensitive);
        menu.add(0, MENU_MORE_SENSITIVE, 0, R.string.menu_more_sensitive);
        menu.add(0, MENU_MOST_SENSITIVE, 0, R.string.menu_most_sensitive);

        return true;
    }

    /**
     * Invoked when the user selects an item from the Menu.
     * 
     * @param item the Menu entry which was selected
     * @return true if the Menu item was legit (and we consumed it), false
     *         otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        switch (item.getItemId()) 
        {
        case MENU_START:
        	
	    	mAttackHelocopterView.resetEngine();
    		mAttackHelicopterThread.doStart();
    		
    		mNumLives = mAttackHelocopterView.mNumLives;
	    	mScore = 0;
	    	mLevelNum = 1;
	    	
	        updateScoreDisplay(mScore);
	        updateLivesDisplay(mNumLives);
	        updateLevelDisplay(mLevelNum);
    		
    		mAttackHelicopterThread.setState(
    				mAttackHelicopterThread.STATE_RUNNING);
            return true;
        case MENU_STOP:
        	finish();
            return true;
        case MENU_PAUSE:
        	mAttackHelicopterThread.pause();
        	return true;
        case MENU_RESUME:
        	mAttackHelicopterThread.unpause();
        	return true;
        case MENU_EASY:
            mAttackHelicopterThread.setDifficulty(AttackHelicopterView.DIFFICULTY_EASY);
            return true;
        case MENU_MEDIUM:
            mAttackHelicopterThread.setDifficulty(AttackHelicopterView.DIFFICULTY_MEDIUM);
            return true;
        case MENU_HARD:
            mAttackHelicopterThread.setDifficulty(AttackHelicopterView.DIFFICULTY_HARD);
            return true;
        case MENU_LEAST_SENSITIVE:
            mAttackHelicopterThread.
            setTiltSensitivity(AttackHelicopterView.LEAST_TILT_SENSITIVITY);
            return true;
        case MENU_LESS_SENSITIVE:
            mAttackHelicopterThread.
            setTiltSensitivity(AttackHelicopterView.LESS_TILT_SENSITIVITY);
            return true;
        case MENU_NORMAL_SENSITIVE:
            mAttackHelicopterThread.
            setTiltSensitivity(AttackHelicopterView.NORMAL_TILT_SENSITIVITY);
            return true;
        case MENU_MORE_SENSITIVE:
            mAttackHelicopterThread.
            setTiltSensitivity(AttackHelicopterView.MORE_TILT_SENSITIVITY);
            return true;
        case MENU_MOST_SENSITIVE:
            mAttackHelicopterThread.
            setTiltSensitivity(AttackHelicopterView.MOST_TILT_SENSITIVITY);
            return true;
        default:
            return true;
        }

    }

    /**
     * Invoked when the Activity is created.
     * 
     * @param savedInstanceState a Bundle containing state saved from a previous
     *        execution, or null if this is a new execution
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try
        {
	        // turn off the window's title bar
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	
	        // tell system to use the layout defined in our XML file
	        setContentView(R.layout.attack_helicopter_layout);
	
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	        
	        mLevelInfo = (TextView) findViewById(R.id.levelInfo);
	        mLivesLeftInfo = (TextView) findViewById(R.id.lives_left);
	        mScoreInfo = (TextView) findViewById(R.id.score);
	        
	        // get handles to the View from XML, and its Thread
	        mAttackHelocopterView = (AttackHelicopterView) findViewById(R.id.helicopter_view);
	        mAttackHelocopterView.mCallback = this;
	        
	        mAttackHelicopterThread = mAttackHelocopterView.thread;
	
	        // give the View a handle to the TextView used for messages
	        mAttackHelocopterView.mStatusText = 
	        	(TextView) findViewById(R.id.text);
	        
	        // we'll vibrate when the ball hits the moving line
	        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	        
	        mAttackHelicopterThread.setState(AttackHelicopterThread.STATE_READY);
	        
	        mMpExplosion = MediaPlayer.create(getBaseContext(), R.raw.explosions_3);
	        mMpMachineGun = MediaPlayer.create(getBaseContext(), R.raw.machinegun);
//	        mMpHelicopter = MediaPlayer.create(getBaseContext(), R.raw.helicopter_4);
//	        mSoundManager = new SoundManager();
//	        mSoundManager.initSounds(getBaseContext());
////	        mSoundManager.addSound(1, R.raw.explosions_3);
//	        mSoundManager.addSound(1, R.raw.machinegun);
////	        mSoundManager.addSound(3, R.raw.machinegun);
//	
//	        mSoundManager.playLoopedSound(1);
	        
	        setListeners();
	        
	        mNumLives = mAttackHelocopterView.mNumLives;
	    	mScore = 0;
	    	mLevelNum = 1;
	    	
	        updateScoreDisplay(mScore);
	        updateLivesDisplay(mNumLives);
	        updateLevelDisplay(mLevelNum);
	        
	        mIsFirstTime = true;
	        
	        getLevelInfo();
	        	        
	        mAttackHelocopterView.requestFocusFromTouch();
	        
	        CONTEXT = this;
//	        this.getWindow().setSoftInputMode(
//	        		WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	        
        }
        catch(Exception e)
        {
        	Log.e(TAG+ "onCreate", e.getMessage());
        }
    }
    
    private void setListeners()
    {
        Button fireButton = (Button) findViewById(R.id.fire_button);
	       
        fireButton.setOnClickListener(new View.OnClickListener() {
        	        	
        	public void onClick(View view) 
        	{
        		if(mAttackHelicopterThread.mMode == 
        			mAttackHelicopterThread.STATE_RUNNING)
        		{
	        		mAttackHelocopterView.mPrimaryHelicopter.
	        			mIsFiring = true;
        			
//        			mAttackHelicopterThread.feedInput(true);
	        		
	        		mAttackHelocopterView.requestFocusFromTouch();
	        		            	
//	        		if(!mMpMachineGun.isPlaying() &&
//	        				mAttackHelicopterThread.mMode == 
//	        					mAttackHelicopterThread.STATE_RUNNING)
//	        			mMpMachineGun.start();
        		}
     
        	    setResult(RESULT_OK);
        	}
          
        });
    	
        Button bombButton = (Button) findViewById(R.id.bomb_button);
	       
        bombButton.setOnClickListener(new View.OnClickListener() {
        	        	
        	public void onClick(View view) 
        	{
        		if(mAttackHelicopterThread.mMode == 
        			mAttackHelicopterThread.STATE_RUNNING)
        		{
	        		mAttackHelocopterView.mIsHeliBombing = true;
	        		mAttackHelocopterView.requestFocusFromTouch();
        		}
     
        	    setResult(RESULT_OK);
        	}
        });
    }

    /**
     * Invoked when the Activity loses user focus.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mAttackHelocopterView.thread.pause(); // pause game when Activity pauses
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mVibrateOn = true;
        
        if (AccelerometerManager.isSupported()) {
    		AccelerometerManager.startListening(this);
    	}
        
        if (OrientationManager.isSupported()) {
    		OrientationManager.startListening(this);
    	}
    }
    
    @Override
    protected void onDestroy()
    {
    	super.onDestroy();
    	if (AccelerometerManager.isListening()) {
    		AccelerometerManager.stopListening();
    	}
    	
    	if (OrientationManager.isListening()) {
    		OrientationManager.stopListening();
    	}
    }

    /**
     * Notification that something is about to happen, to give the Activity a
     * chance to save state.
     * 
     * @param outState a Bundle into which this Activity should save its state
     */
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        // just have the View's thread save its state into our Bundle
//        super.onSaveInstanceState(outState);
//        mAttackHelicopterThread.saveState(outState);
//        Log.w(this.getClass().getName(), "SIS called");
//    }
    
    /** {@inheritDoc} */
    public void onEngineReady(GameEngine gameEngine) 
    {
    	mNumLives = mAttackHelocopterView.mNumLives;
    	mScore = 0;
    	
        updateScoreDisplay(mScore);
        updateLivesDisplay(mNumLives);
        updateLevelDisplay(mLevelNum);
        
        LevelInfo info = mLevelInfos.get(0);
        
        if(info != null)
        {
	        
	        gameEngine.reset(SystemClock.elapsedRealtime(), 
	    			info.mNumBogeys, 
	    			info.mIsBogeyFiring,
	    			info.mBogeyBitmap,
	    			info.mNumTanks,
	    			info.mIsTankFiring,
	    			info.mTankBitmap,
	    			info.mNumBombers,
	    			info.mIsBomberFiring,
	    			info.mBomberBitmap,
	    			info.mNumLaunchers, 
	    			info.mIsLauncherFiring,
	    			info.mLauncherBitmap,
	    			info.mNumFighters,
	    			info.mIsFighterFiring,
	    			info.mFighterBitmap);
        }
    
        if(mIsFirstTime)
        {
	        // show the welcome dialog
	        showDialog(WELCOME_DIALOG);
	        mIsFirstTime = false;
        }
        
        mAttackHelocopterView.requestFocusFromTouch();
    }
    
    /** {@inheritDoc} */
    @SuppressWarnings("static-access")
	public void onNewGame() 
    {
		mLevelNum = 1;
		mScore = 0;
		mNumLives = mAttackHelocopterView.mNumLives;
		
		mAttackHelocopterView.resetEngine();
		mAttackHelicopterThread.doStart();
	
		mAttackHelicopterThread.setState(
				mAttackHelicopterThread.STATE_RUNNING);
	
//		mAttackHelocopterView.requestFocusFromTouch();
		
	    setResult(RESULT_OK);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == WELCOME_DIALOG) {
            mWelcomeDialog = new WelcomeDialog(this, this);
            mWelcomeDialog.setOnCancelListener(this);
            return mWelcomeDialog;
        } 
        return null;
    }
    
    /** {@inheritDoc} */
    public void onCancel(DialogInterface dialog) {
        if (dialog == mWelcomeDialog ) 
        {
            // user hit back button, they're done
            finish();
        }
    }
    
    /** {@inheritDoc} */
    public void onBulletHitsBogey(final GameEngine engine, 
    		float x, float y, float bogeyX, float bogeyY) 
    {
    	if(!mMpExplosion.isPlaying() &&
    			mAttackHelicopterThread.mMode == mAttackHelicopterThread.STATE_RUNNING)
    		mMpExplosion.start();
    	    	
    	if(engine != null)
    	{
    		GameRegion region = engine.getRegions().get(0);
    		
    		List<Bogey> bogeys = region.getBogeys();
    		
    		for(int j=0; j<bogeys.size(); j++)
    		{
    			if(bogeys.get(j).mX == bogeyX &&
    					bogeys.get(j).mY == bogeyY	)
    			{
    				bogeys.remove(j);
    			}
    		}	
    	}
    	
    	++mScore;
    	
    	mHandler.post(mOnBulletHitsBogeyRunnable);
    }
    
    /** {@inheritDoc} */
    public void onBulletHitsBomber(final GameEngine engine, 
    		float x, float y, float bomberX, float bomberY) 
    {
    	if(!mMpExplosion.isPlaying() &&
    			mAttackHelicopterThread.mMode == mAttackHelicopterThread.STATE_RUNNING)
    		mMpExplosion.start();   
    	
    	if(engine != null)
    	{
    		GameRegion region = engine.getRegions().get(0);
    		
    		ArrayList<Bomber> bombers = region.mBombers;
    		
    		for(int j=0; j<bombers.size(); j++)
    		{
    			final Bomber bomber = bombers.get(j);
    			if(bomber.mIsDone)
    			{
    				bombers.remove(j);
    			}
    		}	
    	}
    	
    	++mScore;
    	
    	mHandler.post(mOnBulletHitsBogeyRunnable);
    }
    
    /** {@inheritDoc} */
    public void onBulletHitsVehicle(final GameEngine engine, 
    		float x, float y, float vehicleX, float vehicleY) 
    {
    	if(!mMpExplosion.isPlaying() &&
    			mAttackHelicopterThread.mMode == mAttackHelicopterThread.STATE_RUNNING)
    		mMpExplosion.start();
    	    	
    	if(engine != null)
    	{
    		GameRegion region = engine.getRegions().get(0);
    		
    		ArrayList<Fighter> fighters = region.mFighters;
    		
    		for(int j=0; j<fighters.size(); j++)
    		{
    			final Fighter fighter = fighters.get(j);
    			if(fighter.mIsDone)
    			{
    				fighters.remove(j);
    			}
    		}	
    	}
    	
    	++mScore;
    	
    	mHandler.post(mOnBulletHitsBogeyRunnable);
    }
    
    /** {@inheritDoc} */
    public void onBombHitsTank(final GameEngine engine) 
    {
    	if(!mMpExplosion.isPlaying() &&
    			mAttackHelicopterThread.mMode == mAttackHelicopterThread.STATE_RUNNING)
    		mMpExplosion.start();
    	    	
    	if(engine != null)
    	{
    		GameRegion region = engine.getRegions().get(0);
    		
    		List<Tank> tanks = region.getTanks();
    		
    		for(int j=0; j<tanks.size(); j++)
    		{
    			if(tanks.get(j).getIsDone())
    			{
    				tanks.remove(j);
    			}
    		}	
    	}
    	
    	++mScore;
    	
    	mHandler.post(mOnBulletHitsBogeyRunnable);
    }
    
    /** {@inheritDoc} */
    public void onBombHitsStingerLauncher(final GameEngine engine) 
    {
    	if(!mMpExplosion.isPlaying() &&
    			mAttackHelicopterThread.mMode == mAttackHelicopterThread.STATE_RUNNING)
    		mMpExplosion.start();
    	    	
    	if(engine != null)
    	{
    		GameRegion region = engine.getRegions().get(0);
    		
    		ArrayList<StingerLauncher> launchers = region.mLaunchers;
    		
    		int size = launchers.size();
    		for(int j=0; j<size; j++)
    		{
    			if(launchers.get(j).mIsDone)
    			{
    				launchers.remove(j);
    			}
    		}	
    	}
    	
    	++mScore;
    	
    	mHandler.post(mOnBulletHitsBogeyRunnable);
    }
    
    /** {@inheritDoc} */
    public void onBulletHitsHelicopter(GameEngine engine)
    {
    	if(!mMpExplosion.isPlaying() &&
    			mAttackHelicopterThread.mMode == mAttackHelicopterThread.STATE_RUNNING)
    		mMpExplosion.start();
    	    	
        if (--mNumLives == 0) 
        {
            // vibrate three times
            if (mVibrateOn) {
                mVibrator.vibrate(
                    new long[]{0l, COLLISION_VIBRATE_MILLIS,
                                   50l, COLLISION_VIBRATE_MILLIS,
                                   50l, COLLISION_VIBRATE_MILLIS},
                        -1);
            }
            mAttackHelicopterThread.setState(mAttackHelicopterThread.STATE_LOSE);
        } 
        else 
        {
            if (mVibrateOn) {
                mVibrator.vibrate(COLLISION_VIBRATE_MILLIS);
            }
 
            mHandler.postDelayed(mCreateNewHelicopter, 1000);
        }
    	
    	mHandler.post(mOnBulletHitsHelicopterRunnable);
    }
    
    /** {@inheritDoc} */
    public void onMissileHitsHelicopter(GameEngine engine)
    {
    	if(!mMpExplosion.isPlaying() &&
    			mAttackHelicopterThread.mMode == mAttackHelicopterThread.STATE_RUNNING)
    		mMpExplosion.start();
    	
        if (--mNumLives == 0) 
        {
            // vibrate three times
            if (mVibrateOn) {
                mVibrator.vibrate(
                    new long[]{0l, COLLISION_VIBRATE_MILLIS,
                                   50l, COLLISION_VIBRATE_MILLIS,
                                   50l, COLLISION_VIBRATE_MILLIS},
                        -1);
            }
            mAttackHelicopterThread.setState(mAttackHelicopterThread.STATE_LOSE);
        } 
        else 
        {
            if (mVibrateOn) {
                mVibrator.vibrate(COLLISION_VIBRATE_MILLIS);
            }
 
            mHandler.postDelayed(mCreateNewHelicopter, 1000);
        }
    	
    	mHandler.post(mOnBulletHitsHelicopterRunnable);
    }
    
    /** {@inheritDoc} */
    public void onBogeyHitsHelicopter(GameEngine engine, 
    		float bogeyX, float bogeyY)
    {
    	if(!mMpExplosion.isPlaying())
    		mMpExplosion.start();
     	
    	if(engine != null)
    	{    		
    		GameRegion region = engine.getRegions().get(0);
    		    		
    		List<Bogey> bogeys = region.getBogeys();
    		
    		for(int j=0; j<bogeys.size(); j++)
    		{
    			if(bogeys.get(j).mX == bogeyX &&
    					bogeys.get(j).mY == bogeyY	)
    			{
    				bogeys.remove(j);
    			}
    		}	
    	}
    	
        if (--mNumLives == 0) 
        {
            // vibrate three times
            if (mVibrateOn) {
                mVibrator.vibrate(
                    new long[]{0l, COLLISION_VIBRATE_MILLIS,
                                   50l, COLLISION_VIBRATE_MILLIS,
                                   50l, COLLISION_VIBRATE_MILLIS},
                        -1);
            }
            
            mHandler.post(mOnBogeyHitsHelicopterRunnable);
            mAttackHelicopterThread.setState(mAttackHelicopterThread.STATE_LOSE);
        } 
        else 
        {
            if (mVibrateOn) {
                mVibrator.vibrate(COLLISION_VIBRATE_MILLIS);
            }
            
            mHandler.post(mOnBogeyHitsHelicopterRunnable);
            mHandler.postDelayed(mCreateNewHelicopter, 1000);
        }
    }
    
    /** {@inheritDoc} */
    public void onTankHitsHelicopter(GameEngine engine, 
    		float tankX, float tankY)
    {
    	if(!mMpExplosion.isPlaying() &&
    			mAttackHelicopterThread.mMode == mAttackHelicopterThread.STATE_RUNNING)
    		mMpExplosion.start();
     	
    	if(engine != null)
    	{    		
    		GameRegion region = engine.getRegions().get(0);
    		    		
    		ArrayList<Tank> tanks = region.mTanks;
    		
    		for(int j=0; j<tanks.size(); j++)
    		{
    			final Tank tank = tanks.get(j);
    			if(tank.mIsDone)
    			{
    				tanks.remove(j);
    			}
    		}	
    	}
    	
        if (--mNumLives == 0) 
        {
            // vibrate three times
            if (mVibrateOn) {
                mVibrator.vibrate(
                    new long[]{0l, COLLISION_VIBRATE_MILLIS,
                                   50l, COLLISION_VIBRATE_MILLIS,
                                   50l, COLLISION_VIBRATE_MILLIS},
                        -1);
            }
            
            mHandler.post(mOnBogeyHitsHelicopterRunnable);
            mAttackHelicopterThread.setState(mAttackHelicopterThread.STATE_LOSE);
        } 
        else 
        {
            if (mVibrateOn) {
                mVibrator.vibrate(COLLISION_VIBRATE_MILLIS);
            }
            
            mHandler.post(mOnBogeyHitsHelicopterRunnable);
            mHandler.postDelayed(mCreateNewHelicopter, 1000);
        }
    }
    
    /** {@inheritDoc} */
    public void onBomberHitsHelicopter(GameEngine engine, 
    		float bomberX, float bomberY)
    {
    	if(!mMpExplosion.isPlaying() &&
    			mAttackHelicopterThread.mMode == mAttackHelicopterThread.STATE_RUNNING)
    		mMpExplosion.start();
     	
    	if(engine != null)
    	{    		
    		GameRegion region = engine.getRegions().get(0);
    		    		
    		ArrayList<Bomber> bombers = region.mBombers;
    		
    		for(int j=0; j<bombers.size(); j++)
    		{
    			final Bomber bomber = bombers.get(j);
    			if(bomber.mIsDone)
    			{
    				bombers.remove(j);
    			}
    		}	
    	}
    	
        if (--mNumLives == 0) 
        {
            // vibrate three times
            if (mVibrateOn) {
                mVibrator.vibrate(
                    new long[]{0l, COLLISION_VIBRATE_MILLIS,
                                   50l, COLLISION_VIBRATE_MILLIS,
                                   50l, COLLISION_VIBRATE_MILLIS},
                        -1);
            }
            
            mHandler.post(mOnBogeyHitsHelicopterRunnable);
            mAttackHelicopterThread.setState(mAttackHelicopterThread.STATE_LOSE);
        } 
        else 
        {
            if (mVibrateOn) {
                mVibrator.vibrate(COLLISION_VIBRATE_MILLIS);
            }
            
            mHandler.post(mOnBogeyHitsHelicopterRunnable);
            mHandler.postDelayed(mCreateNewHelicopter, 1000);
        }
    }
    
    /** {@inheritDoc} */
    public void onFighterHitsHelicopter(GameEngine engine, 
    		float fighterX, float fighterY)
    {
    	if(!mMpExplosion.isPlaying() &&
    			mAttackHelicopterThread.mMode == mAttackHelicopterThread.STATE_RUNNING)
    		mMpExplosion.start();
     	
    	if(engine != null)
    	{    		
    		GameRegion region = engine.getRegions().get(0);
    		    		
    		ArrayList<Fighter> fighters = region.mFighters;
    		
    		for(int j=0; j<fighters.size(); j++)
    		{
    			final Fighter fighter = fighters.get(j);
    			if(fighter.mIsDone)
    			{
    				fighters.remove(j);
    			}
    		}	
    	}
    	
        if (--mNumLives == 0) 
        {
            // vibrate three times
            if (mVibrateOn) {
                mVibrator.vibrate(
                    new long[]{0l, COLLISION_VIBRATE_MILLIS,
                                   50l, COLLISION_VIBRATE_MILLIS,
                                   50l, COLLISION_VIBRATE_MILLIS},
                        -1);
            }
            
            mHandler.post(mOnBogeyHitsHelicopterRunnable);
            mAttackHelicopterThread.setState(mAttackHelicopterThread.STATE_LOSE);
        } 
        else 
        {
            if (mVibrateOn) {
                mVibrator.vibrate(COLLISION_VIBRATE_MILLIS);
            }
            
            mHandler.post(mOnBogeyHitsHelicopterRunnable);
            mHandler.postDelayed(mCreateNewHelicopter, 1000);
        }
    }
    
    /** {@inheritDoc} */
    public void onHelicopterFiring()
    {
    	mAttackHelocopterView.requestFocusFromTouch();
//    	if(!mMpMachineGun.isPlaying() &&
//				mAttackHelicopterThread.mMode == 
//					mAttackHelicopterThread.STATE_RUNNING)
//			mMpMachineGun.start();
    }
    
    /** {@inheritDoc} */
    public void onActivityLevelUp(GameEngine gameEngine)
    {
    	if(mAttackHelicopterThread.mMode == 
    		mAttackHelicopterThread.STATE_RUNNING)
    	{
//    		mAttackHelocopterView.mCallback = this;
    		
	    	mHandler.post(mOnActivityLevelUpRunnable);
	    	++mLevelNum;
	    	
	    	int index = mLevelNum-1;
	    	int size = mLevelInfos.size();
	    	
	    	// if we hit the last level, just keep playing that level
	    	if(mLevelNum >= size)
	    	{
	    		index = size-1;
	    	}
	    	
	    	// levels starts at 1
	    	// on even levels the heli is on the upper right side
	    	// on odd levels the heli is on the uppder left side
	    	Helicopter heli = mAttackHelocopterView.mPrimaryHelicopter;
	    	if(mLevelNum%2==0)
	    	{
	    		if(heli != null)
	    		{
	    			heli.mX = mAttackHelocopterView.mViewWidth - 
	    				mAttackHelocopterView.mHelicopterWidth/2;
	    			heli.mY = mAttackHelocopterView.mHelicopterHeight/2;
	    			heli.mIsReversing = true;
	    		}
	    	}
	    	else
	    	{
	    		if(heli != null)
	    		{
	    			heli.mX = mAttackHelocopterView.mHelicopterWidth/2;
	    			heli.mY = mAttackHelocopterView.mHelicopterHeight/2;
	    			heli.mIsReversing = false;
	    		}
	    	}
	    	
	    	heli.mIsDone = false;
	    	heli.mLastUpdate = SystemClock.elapsedRealtime();
	    	
	    	if(gameEngine != null)
	    	{
	    		gameEngine.mHeli = heli;
	    	
		    	if(mLevelNum <= size) 
		    	{
			    	LevelInfo info = mLevelInfos.get(index);
			    	
			    	gameEngine.reset(SystemClock.elapsedRealtime(), 
			    			info.mNumBogeys, 
			    			info.mIsBogeyFiring,
			    			info.mBogeyBitmap,
			    			info.mNumTanks,
			    			info.mIsTankFiring,
			    			info.mTankBitmap,
			    			info.mNumBombers,
			    			info.mIsBomberFiring,
			    			info.mBomberBitmap,
			    			info.mNumLaunchers, 
			    			info.mIsLauncherFiring,
			    			info.mLauncherBitmap,
			    			info.mNumFighters,
			    			info.mIsFighterFiring,
			    			info.mFighterBitmap);
		
			    	mAttackHelicopterThread.setState(mAttackHelicopterThread.STATE_RUNNING);
		    	}
		    	else
		    	{
		    		mAttackHelicopterThread.setState(mAttackHelicopterThread.STATE_WIN);
		    	}
	    	}
    	}
    	
    	mAttackHelocopterView.requestFocusFromTouch();
    }
    
    /** {@inheritDoc} */
    public void onMenuButtonClicked()
    {
//    	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
//	    imm.hideSoftInputFromWindow(mAttackHelocopterView.getWindowToken(), 
//	    		0); 
	        
    	this.getWindow().openPanel(Window.FEATURE_OPTIONS_PANEL, new KeyEvent 
    			(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU));
    	
    	mAttackHelocopterView.requestFocusFromTouch();
    }
    
    /**
     * Update the header displaying the current level
     */
    private void updateLevelDisplay(int numBalls) 
    {
        mLevelInfo.setText(getString(R.string.level, 
        		numBalls));
    }

    /**
     * Update the display showing the number of lives left.
     * @param numLives The number of lives left.
     */
    void updateLivesDisplay(int numLives) {
        String text = (numLives == 1) ?
                getString(R.string.one_life_left) : 
                	getString(R.string.lives_left, numLives);
        mLivesLeftInfo.setText(text);
    }
    
    /**
     * Update the header that displays how much of the space has been contained.
     * @param amountFilled The fraction, between 0 and 1, that is filled.
     */
    private void updateScoreDisplay(int score) 
    {
        mScoreInfo.setText(
                getString(R.string.score, score));
    }
    
    /**
     * onShake callback
     */
	public void onShake(float force) {
//		Toast.makeText(this, "Phone shaked : " + force, 1000).show();
	}

	/**
	 * onAccelerationChanged callback
	 */
	public void onAccelerationChanged(float x, float y, float z) {
		mAttackHelocopterView.onAccelerationChanged(x, y, z);
	}
	
	@Override
	public void onOrientationChangedTooMuch(float pitch, float roll, 
			float azimuth) 
	{
		mAttackHelocopterView.onOrientationChangedTooMuch(pitch, roll, azimuth);
	}
	
	@Override
	public void onOrientationChanged(float pitch, float roll, 
			float azimuth) 
	{
		mAttackHelocopterView.onOrientationChanged(pitch, roll, azimuth);
	}

	@Override
	public void onLoseGame() 
	{
//		Intent i = new Intent(this, HiScore.class);
//		i.putExtra(AttackHelicopter.HI_SCORE, mScore);
//        startActivity(i);
	}

}
