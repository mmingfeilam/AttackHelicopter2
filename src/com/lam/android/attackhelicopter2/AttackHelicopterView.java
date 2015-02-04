package com.lam.android.attackhelicopter2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

class AttackHelicopterView extends SurfaceView implements
		SurfaceHolder.Callback, GameEngine.BulletEventCallBack,
		GameEngine.BogeyEventCallBack, GameEngine.BombEventCallBack,
		GameEngine.MissileEventCallBack, GameEngine.TankEventCallBack,
		GameEngine.BomberEventCallBack, GameEngine.FighterEventCallBack {

	public Paint mPaint;

	public static final int BULLET_SPEED = 120;

	protected static final int BOGEY_SPEED = 50;

	private static final int TANK_SPEED = 30;

	/** regular distance increment */
	public static final int DISTANCE_INCREMENT = 40;
	/** distance increment for tilting vertically */
	public static final int DISTANCE_INCREMENT_ACCEL_Y = 20;
	/** distance increment for tilting vertically */
	public static final int DISTANCE_INCREMENT_ACCEL_X = 15;
	/** distance increment for touch screen */
	public static final int DISTANCE_INCREMENT_ON_TOUCH = 5;
	/** distance when movement should be slowed down */
	public static final int COLLISION_ALERT_DISTANTCE_X = 40;
	public static final int COLLISION_ALERT_DISTANTCE_Y = 40;

	private static final int NUM_LIVES_TO_START_EASY = 5;
	private static final int NUM_LIVES_TO_START_MEDIUM = 4;
	private static final int NUM_LIVES_TO_START_HARD = 3;

	public Vibrator mVibrator;

	protected Helicopter mPrimaryHelicopter = null;

	/** Pixel height of heli image. */
	protected int mHelicopterHeight;

	/** What to draw for the Helicopter in its normal state */
	private Drawable mHelicopterImage;

	/** Pixel width of heli image. */
	protected int mHelicopterWidth;

	/**
	 * Current difficulty -- Default is MEDIUM.
	 */
	private int mDifficulty;

	/**
	 * Current tilt sensitivity -- Default is Normal.
	 */
	private int mTiltSensitivity;

	public boolean mIsBogeyFiring;

	/*
	 * Difficulty setting constants
	 */
	public static final int DIFFICULTY_EASY = 0;
	public static final int DIFFICULTY_HARD = 1;
	public static final int DIFFICULTY_MEDIUM = 2;

	/*
	 * Tilt sensitivity setting constants
	 */
	public static final int LEAST_TILT_SENSITIVITY = 0;
	public static final int LESS_TILT_SENSITIVITY = 1;
	public static final int NORMAL_TILT_SENSITIVITY = 2;
	public static final int MORE_TILT_SENSITIVITY = 3;
	public static final int MOST_TILT_SENSITIVITY = 4;

	private static final int INPUT_QUEUE_SIZE = 60;

	/** min value for y tilt in order to move the heli */
	public static final float PRESET_THRESHOLDY_MIN = 0.10F;
	/** max value for y tilt in order to move the heli */
	public static final float PRESET_THRESHOLDY_MAX = 0.25F;

	/** min value for x tilt in order to move the heli */
	public static final float PRESET_THRESHOLDX_MIN = 0.11F;
	/** max value for x tilt in order to move the heli */
	public static final float PRESET_THRESHOLDX_MAX = 0.25F;

	public static final float PRESET_THRESHOLDZ = .25F;

	private ArrayList<Explosion> mExplosions = new ArrayList<Explosion>();

	private Bitmap mExplosion1;
	private Bitmap mExplosion4;
	private Bitmap mExplosion8;

	/** Handle to the application context, used to e.g. fetch Drawables. */
	protected Context mContext;

	/** Pointer to the text view to display "Paused.." etc. */
	protected TextView mStatusText;

	private TextView mLivesLeftInfo;

	protected TextView mScoreInfo;

	protected int mNumLives;

	protected float mTiltSensitivityMultiplier;

	/** The thread that actually draws the animation */
	protected AttackHelicopterThread thread;

	protected GameEngine mEngine;

	protected BallEngineCallBack mCallback;

	private Bitmap mBulletBitmap;

	private Bitmap mBogeyBulletBitmap;

	private Bitmap mBogeyBitmap;

	private int mBulletBitmapRadius;

	private int mBogeyBitmapRadius;

	private String TAG = "AttackHelicopterView ";

	private int mBogeyBulletBitmapRadius;

	private int mTankBitmapRadius;

	private Bitmap mTankBitmap;

	private int mTankBitmapHeight;

	private Bitmap mHeliBombsBitmap;

	protected boolean mIsHeliBombing;

	protected boolean mIsBomberDropping;

	private int mBombBitmapRadius;

	private Bitmap mSAMRightBitmap;

	private Bitmap mBigExplosion1;

	private Bitmap mBigExplosion4;

	private Bitmap mBigExplosion8;

	public boolean mIsTankFiring;

	private int mBomberBitmapRadius;

	private int mBomberBitmapHeight;

	private Bitmap mBomberBitmap;

	protected int mLauncherBitmapRadius;

	protected Bitmap mLauncherBitmap;

	protected int mLauncherBitmapHeight;

	private int mFighterBitmapRadius;

	private int mFighterBitmapHeight;

	private Bitmap mFighterBitmap;

	private ArrayBlockingQueue<InputObject> inputObjectPool;

	ArrayList<Integer> mHeliRids;

	protected int mViewWidth;
	protected int mViewHeight;

	protected float mPrevTiltX;
	protected float mPrevTiltY;
	protected float mPrevTiltZ;

	protected float mPitch;
	protected float mRoll;
	protected float mAzimuth;

	public boolean mIsTooTilted;
	public boolean mIsTooTiltedToLeft;
	public boolean mIsTooTiltedToRight;
	public boolean mIsTooTiltedToTop;
	public boolean mIsTooTiltedToBottom;

	class SoundThread extends Thread {
		private boolean mRun;
		private MediaPlayer mMpHelicopter;

		SoundThread() {
			mMpHelicopter = MediaPlayer.create(mContext, R.raw.helicopter_4);
		}

		public void run() {
			while (mRun) {
				mMpHelicopter.start();
			}
		}

		public void setRunning(boolean b) {
			mRun = b;
		}
	}

	static class Explosion {
		private long mLastUpdate;
		private long mProgress = 0;
		private final float mX;
		private final float mY;

		private final Bitmap mExplosion1;
		private final Bitmap mExplosion4;
		private final Bitmap mExplosion8;

		private final float mRadius;

		Explosion(long mLastUpdate, float mX, float mY, Bitmap explosion1,
				Bitmap explosion4, Bitmap explosion8) {
			this.mLastUpdate = mLastUpdate;
			this.mX = mX;
			this.mY = mY;
			this.mExplosion1 = explosion1;
			mExplosion4 = explosion4;
			mExplosion8 = explosion8;

			mRadius = ((float) mExplosion1.getWidth()) / 2f;

		}

		public void update(long now) {
			mProgress += (now - mLastUpdate);
			mLastUpdate = now;
		}

		public void setNow(long now) {
			mLastUpdate = now;
		}

		public void draw(Canvas canvas, Paint paint) {
			if (mProgress < 150L) {
				canvas.drawBitmap(mExplosion1, mX - mRadius, mY - mRadius,
						paint);
			} else if (mProgress < 300L) {
				canvas.drawBitmap(mExplosion4, mX - mRadius, mY - mRadius,
						paint);
			} else if (mProgress < 400L) {
				canvas.drawBitmap(mExplosion8, mX - mRadius, mY - mRadius,
						paint);
			}

		}

		public boolean done() {
			return mProgress > 700L;
		}
	}

	class AttackHelicopterThread extends Thread {

		/*
		 * State-tracking constants
		 */
		public static final int STATE_LOSE = 1;
		public static final int STATE_PAUSE = 2;
		public static final int STATE_READY = 3;
		public static final int STATE_RUNNING = 4;
		public static final int STATE_WIN = 5;

		/**
		 * arbitrary offest of the Y for the bullet from the center of the
		 * helicopter
		 */
		private static final float HELI_BULLET_OFFSET = 10;

		/**
		 * where the helicopter will be when it gets hit, the number is negative
		 * so it won't get a double hit.
		 */
		private static final float HELI_OFFSCREEN_COORD = -50;

		/*
		 * Member (state) fields
		 */
		/** The drawable to use as the background of the animation canvas */
		private Bitmap mBackgroundImage;

		/** Message handler used by thread to interact with TextView */
		private Handler mHandler;

		/**
		 * Heli heading in degrees, with 0 up, 90 right. Kept in the range
		 * 0..360.
		 */
		private double mHeading;

		/** The state of the game. One of READY, RUNNING, PAUSE, LOSE, or WIN */
		protected int mMode;

		/** Indicate whether the surface has been created & is ready to draw */
		protected boolean mRun = false;

		/** Handle to the surface manager object we interact with */
		private SurfaceHolder mSurfaceHolder;

		/** top of heli */
		private int yTop;
		/** left of heli */
		private int xLeft;
		/** right of heli */
		private int xRight;
		/** bottom of heli */
		private int yBottom;

		private static final int INPUT_QUEUE_SIZE = 10;
		private ArrayBlockingQueue<Boolean> inputQueue = new ArrayBlockingQueue<Boolean>(
				INPUT_QUEUE_SIZE);
		private Object inputQueueMutex = new Object();

		private String TAG = "AttackHelicopterThread ";

		private Runnable mSetStingerFiringRunnable = new Runnable() {

			public void run() {
				mLauncher.mIsFiring = true;
			}
		};

		private Runnable mSetStingerFiringRunnable2 = new Runnable() {

			public void run() {
				mLauncher2.mIsFiring = true;
			}
		};

		private StingerLauncher mLauncher;
		private StingerLauncher mLauncher2;

		private Bomber mBomber;
		private GameRegion mRegion;
		private int mLauncherIndex;
		private int mLauncherIndex2;

		private Runnable mDropStingerLauncherRunnable = new Runnable() {

			public void run() {
				final StingerLauncher launcher = mRegion.mLaunchers
						.get(mLauncherIndex);
				launcher.mX = mBomber.getLeft();
				launcher.mY = mBomber.mY;

			}
		};

		private Runnable mDropStingerLauncherRunnable2 = new Runnable() {

			public void run() {
				final StingerLauncher launcher = mRegion.mLaunchers
						.get(mLauncherIndex2);
				launcher.mX = mBomber.getLeft();
				launcher.mY = mBomber.mY;

			}
		};

		public AttackHelicopterThread(SurfaceHolder surfaceHolder,
				Context context, Handler handler) {
			// get handles to some important objects
			mSurfaceHolder = surfaceHolder;
			mHandler = handler;
			mContext = context;

			Resources res = context.getResources();
			// cache handles to our key sprites & other drawables
			mHelicopterImage = context.getResources().getDrawable(
					R.drawable.heli);

			mHelicopterHeight = mHelicopterImage.getIntrinsicHeight();
			mHelicopterWidth = mHelicopterImage.getIntrinsicWidth();

			// load background image as a Bitmap instead of a Drawable b/c
			// we don't need to transform it and it's faster to draw this way
			mBackgroundImage = BitmapFactory.decodeResource(res,
					R.drawable.desertdunes);

			// Use the regular lander image as the model size for all sprites
			mHelicopterWidth = mHelicopterImage.getIntrinsicWidth();
			mHelicopterHeight = mHelicopterImage.getIntrinsicHeight();

			mHeading = 0;
			mIsBogeyFiring = true;
			mIsTankFiring = true;

			// mMpMachineGun1 = MediaPlayer.create(context, R.raw.machinegun);

		}

		public void feedInput(Boolean input) {
			Log.d(TAG + "feedInput", "begin");
			synchronized (inputQueueMutex) {
				try {
					inputQueue.put(input);
				} catch (InterruptedException e) {
					Log.e(TAG, e.getMessage(), e);
				} catch (Exception e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
			Log.d(TAG + "feedInput", "end");
		}

		private void processInput() {
			Log.d(TAG + "processInput", "before sync");

			synchronized (inputQueueMutex) {
				Log.d(TAG + "processInput", "in sync");

				ArrayBlockingQueue<Boolean> inputQueue = this.inputQueue;

				while (!inputQueue.isEmpty()) {
					try {
						Boolean input = inputQueue.take();
						mPrimaryHelicopter.mIsFiring = true;

					} catch (InterruptedException e) {
						Log.e(TAG, e.getMessage(), e);
					} catch (Exception e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}
			}
			Log.d(TAG + "processInput", "after sync");
		}

		/**
		 * Starts the game, setting parameters for the current difficulty.
		 */
		public void doStart() {
			Log.d(TAG + "doStart", "before sync");
			synchronized (mSurfaceHolder) {
				Log.d(TAG + "doStart", "in sync");

				if (mPrimaryHelicopter != null && !mPrimaryHelicopter.mIsDone) {
					setState(STATE_RUNNING);
				}
			}

			Log.d(TAG + "doStart", "after sync");
		}

		/**
		 * Pauses the physics update & animation.
		 */
		public void pause() {
			synchronized (mSurfaceHolder) {
				if (mMode == STATE_RUNNING)
					setState(STATE_PAUSE);
			}
		}

		/**
		 * Restores game state from the indicated Bundle. Typically called when
		 * the Activity is being restored after having been previously
		 * destroyed.
		 * 
		 * @param savedState
		 *            Bundle containing the game state
		 */
		public synchronized void restoreState(Bundle savedState) {
			synchronized (mSurfaceHolder) {
				// setState(STATE_PAUSE);
				// mDifficulty = savedState.getInt(KEY_DIFFICULTY);
				// mX = savedState.getDouble(KEY_X);
				// mY = savedState.getDouble(KEY_Y);
				// mHeading = savedState.getDouble(KEY_HEADING);
				//
				// mWinsInARow = savedState.getInt(KEY_WINS);
			}
		}

		@Override
		public void run() {
			while (mRun) {
				Canvas c = null;
				try {
					c = mSurfaceHolder.lockCanvas(null);
					synchronized (mSurfaceHolder) {
						// processInput();
						doDraw(c);
					}
				} catch (Exception e) {
					Log.e(TAG + "run", e.getMessage());
				} finally {
					// do this in a finally so that if an exception is thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					if (c != null) {
						mSurfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}
		}

		/**
		 * Dump game state to the provided Bundle. Typically called when the
		 * Activity is being suspended.
		 * 
		 * @return Bundle with this view's state
		 */
		public Bundle saveState(Bundle map) {
			synchronized (mSurfaceHolder) {
				// if (map != null) {
				// map.putInt(KEY_DIFFICULTY, Integer.valueOf(mDifficulty));
				// map.putDouble(KEY_X, Double.valueOf(mX));
				// map.putDouble(KEY_Y, Double.valueOf(mY));
				// map.putDouble(KEY_HEADING, Double.valueOf(mHeading));
				// map.putInt(KEY_WINS, Integer.valueOf(mWinsInARow));
				// }
			}
			return map;
		}

		/**
		 * Sets the current difficulty.
		 * 
		 * @param difficulty
		 */
		public void setDifficulty(int difficulty) {
			synchronized (mSurfaceHolder) {
				mDifficulty = difficulty;

				switch (mDifficulty) {
				case DIFFICULTY_EASY:
					mNumLives = NUM_LIVES_TO_START_EASY;
					break;
				case DIFFICULTY_MEDIUM:
					mNumLives = NUM_LIVES_TO_START_MEDIUM;
					break;
				case DIFFICULTY_HARD:
					mNumLives = NUM_LIVES_TO_START_HARD;
					break;
				default:
					mNumLives = NUM_LIVES_TO_START_MEDIUM;
					break;
				}
			}
		}

		/**
		 * Sets the current tilt sensitivity.
		 * 
		 * @param sensitivity
		 */
		public void setTiltSensitivity(int sensitivity) {
			synchronized (mSurfaceHolder) {
				mTiltSensitivity = sensitivity;

				switch (mTiltSensitivity) {
				case LEAST_TILT_SENSITIVITY:
					mTiltSensitivityMultiplier = 0.25F;
					break;
				case LESS_TILT_SENSITIVITY:
					mTiltSensitivityMultiplier = 0.5F;
					break;
				case NORMAL_TILT_SENSITIVITY:
					mTiltSensitivityMultiplier = 1;
					break;
				case MORE_TILT_SENSITIVITY:
					mTiltSensitivityMultiplier = 1.25F;
					break;
				case MOST_TILT_SENSITIVITY:
					mTiltSensitivityMultiplier = 1.5F;
					break;
				default:
					mTiltSensitivityMultiplier = 1;
					break;
				}
			}
		}

		/**
		 * Sets the game mode. That is, whether we are running, paused, in the
		 * failure state, in the victory state, etc.
		 * 
		 * @see #setState(int, CharSequence)
		 * @param mode
		 *            one of the STATE_* constants
		 */
		public void setState(int mode) {
			synchronized (mSurfaceHolder) {
				setState(mode, null);
			}
		}

		/**
		 * Sets the game mode. That is, whether we are running, paused, in the
		 * failure state, in the victory state, etc.
		 * 
		 * @param mode
		 *            one of the STATE_* constants
		 * @param message
		 *            string to add to screen or null
		 */
		public void setState(int mode, CharSequence message) {
			/*
			 * This method optionally can cause a text message to be displayed
			 * to the user when the mode changes. Since the View that actually
			 * renders that text is part of the main View hierarchy and not
			 * owned by this thread, we can't touch the state of that View.
			 * Instead we use a Message + Handler to relay commands to the main
			 * thread, which updates the user-text View.
			 */
			synchronized (mSurfaceHolder) {
				mMode = mode;

				if (mMode == STATE_RUNNING) {
					Message msg = mHandler.obtainMessage();
					Bundle b = new Bundle();
					b.putString("text", "");
					b.putInt("viz", View.INVISIBLE);
					msg.setData(b);
					mHandler.sendMessage(msg);
				} else {
					Resources res = mContext.getResources();
					CharSequence str = "";
					if (mMode == STATE_READY)
						str = res.getText(R.string.mode_ready);
					else if (mMode == STATE_PAUSE)
						str = res.getText(R.string.mode_pause);
					else if (mMode == STATE_LOSE) {
						str = res.getText(R.string.mode_lose);
						// mCallback.onLoseGame();
					} else if (mMode == STATE_WIN)
						str = res.getString(R.string.mode_win_prefix) + " "
								+ res.getString(R.string.mode_win_suffix);

					if (message != null) {
						str = message + "\n" + str;
					}

					Message msg = mHandler.obtainMessage();
					Bundle b = new Bundle();
					b.putString("text", str.toString());
					b.putInt("viz", View.VISIBLE);
					msg.setData(b);
					mHandler.sendMessage(msg);
				}
			}
		}

		/* Callback invoked when the surface dimensions change. */
		public void setSurfaceSize(int width, int height) {
			// synchronized to make sure these all change atomically
			synchronized (mSurfaceHolder) {
				// don't forget to resize the background image
				mBackgroundImage = mBackgroundImage.createScaledBitmap(
						mBackgroundImage, width, height, true);

			}
		}

		/**
		 * Resumes from a pause.
		 */
		public void unpause() {
			setState(STATE_RUNNING);
		}

		/**
		 * Handles a key-down event.
		 * 
		 * @param keyCode
		 *            the key that was pressed
		 * @param msg
		 *            the original event object
		 * @return true
		 */
		boolean doKeyDown(int keyCode, KeyEvent msg) {
			synchronized (mSurfaceHolder) {
				Log.d(TAG + "doKeyDown", "in sync");

				if (mMode == STATE_RUNNING) {

					if (mPrimaryHelicopter != null
							&& !mPrimaryHelicopter.mIsDone) {
						if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
							mPrimaryHelicopter.mY -= DISTANCE_INCREMENT;

							int height1 = mHelicopterHeight / 2;
							if (mPrimaryHelicopter.mY <= height1) {
								mPrimaryHelicopter.mY = height1;
							}
						} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
							mPrimaryHelicopter.mY += DISTANCE_INCREMENT;

							int height1 = getHeight() - mHelicopterHeight / 2;

							if (mPrimaryHelicopter.mY >= height1)
								mPrimaryHelicopter.mY = height1;
						} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
							mPrimaryHelicopter.mX += DISTANCE_INCREMENT;

							int width1 = getWidth() - mHelicopterWidth / 2;
							if (mPrimaryHelicopter.mX > width1) {
								mPrimaryHelicopter.mX = width1;
							}

						} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
							mPrimaryHelicopter.mX -= DISTANCE_INCREMENT;

							int width1 = mHelicopterWidth / 2;
							if (mPrimaryHelicopter.mX < width1) {
								mPrimaryHelicopter.mX = width1;
							}

						} else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
							mPrimaryHelicopter.mIsFiring = true;
							Log.d(TAG + "processKeyEvent",
									"InputObject.DPAD_KEY_CENTER");
						}
					}
				}

				if (keyCode == KeyEvent.KEYCODE_MENU) {
					mCallback.onMenuButtonClicked();
				}

				Log.d(TAG + "doKeyDown", "after sync");
				return true;
			}
		}

		/**
		 * Handles a key-up event.
		 * 
		 * @param keyCode
		 *            the key that was pressed
		 * @param msg
		 *            the original event object
		 * @return true if the key was handled and consumed, or else false
		 */
		boolean doKeyUp(int keyCode, KeyEvent msg) {
			Log.d(TAG + "doKeyUp", "begin");
			synchronized (mSurfaceHolder) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
					if (mPrimaryHelicopter != null
							&& !mPrimaryHelicopter.mIsDone) {
						mPrimaryHelicopter.mIsFiring = false;
					}
				}
			}
			Log.d(TAG + "doKeyUp", "end");

			return true;
		}

		public boolean doTouchEvent(MotionEvent motionEvent) {
			synchronized (mSurfaceHolder) {
				Log.d(TAG + "processMotionEvent", "in sync");
				if (mMode == STATE_RUNNING) {
					if (mPrimaryHelicopter != null
							&& !mPrimaryHelicopter.mIsDone) {
						int X = (int) motionEvent.getX();
						int Y = (int) motionEvent.getY();

						switch (motionEvent.getAction()) {
						case InputObject.ACTION_TOUCH_DOWN:

							if (xRight < X) {
								mPrimaryHelicopter.mX += DISTANCE_INCREMENT_ON_TOUCH;

								int width1 = getWidth() - mHelicopterWidth / 2;
								if (mPrimaryHelicopter.mX > width1) {
									mPrimaryHelicopter.mX = width1;
								}

								mPrimaryHelicopter.mIsReversing = false;
							} else if (xLeft > X) {
								mPrimaryHelicopter.mX -= DISTANCE_INCREMENT_ON_TOUCH;

								int width1 = mHelicopterWidth / 2;
								if (mPrimaryHelicopter.mX < width1) {
									mPrimaryHelicopter.mX = width1;
								}

								mPrimaryHelicopter.mIsReversing = true;
							}
							if (yTop < Y) {
								mPrimaryHelicopter.mY += DISTANCE_INCREMENT_ON_TOUCH;

								int height1 = getHeight() - mHelicopterHeight
										/ 2;
								if (mPrimaryHelicopter.mY > height1) {
									mPrimaryHelicopter.mY = height1;
								}
							} else if (yBottom > Y) {
								mPrimaryHelicopter.mY -= DISTANCE_INCREMENT_ON_TOUCH;

								int height1 = mHelicopterHeight / 2;
								if (mPrimaryHelicopter.mY < height1) {
									mPrimaryHelicopter.mY = height1;
								}
							}

							break;
						case InputObject.ACTION_TOUCH_MOVE:

							if (xRight < X) {
								mPrimaryHelicopter.mX += DISTANCE_INCREMENT_ON_TOUCH;

								int width1 = getWidth() - mHelicopterWidth / 2;
								if (mPrimaryHelicopter.mX > width1) {
									mPrimaryHelicopter.mX = width1;
								}

								mPrimaryHelicopter.mIsReversing = false;
							} else if (xLeft > X) {
								mPrimaryHelicopter.mX -= DISTANCE_INCREMENT_ON_TOUCH;

								int width1 = mHelicopterWidth / 2;
								if (mPrimaryHelicopter.mX < width1) {
									mPrimaryHelicopter.mX = width1;
								}

								mPrimaryHelicopter.mIsReversing = true;
							}
							if (yTop < Y) {
								mPrimaryHelicopter.mY += DISTANCE_INCREMENT_ON_TOUCH;

								int height1 = getHeight() - mHelicopterHeight
										/ 2;
								if (mPrimaryHelicopter.mY > height1) {
									mPrimaryHelicopter.mY = height1;
								}
							} else if (yBottom > Y) {
								mPrimaryHelicopter.mY -= DISTANCE_INCREMENT_ON_TOUCH;

								int height1 = mHelicopterHeight / 2;
								if (mPrimaryHelicopter.mY < height1) {
									mPrimaryHelicopter.mY = height1;
								}
							}
							break;
						case InputObject.ACTION_TOUCH_UP:

							break;
						default:
							break;
						}
					}
				}
				return true;
			}
		}

		/**
		 * Draws the ship, fuel/speed bars, and background to the provided
		 * Canvas.
		 */
		private void doDraw(Canvas canvas) {

			try {
				// Draw the background image. Operations on the Canvas
				// accumulate
				// so this is like clearing the screen.
				canvas.drawBitmap(mBackgroundImage, 0, 0, null);

				final long now = SystemClock.elapsedRealtime();

				if (!mEngine.getRegions().isEmpty()) {
					mRegion = mEngine.getRegions().get(0);

					if (mMode == STATE_RUNNING) {
						boolean isLevelUp = mEngine.update(now);
						int size = 0;

						if (isLevelUp) {
							// mIsBogeyFiring = true;
							// mIsTankFiring = true;
							// mHandler.removeCallbacks(mSetStingerFiringRunnable);
							// mHandler.removeCallbacks(mSetStingerFiringRunnable2);
							// mHandler.removeCallbacks(mDropStingerLauncherRunnable);
							// mHandler.removeCallbacks(mDropStingerLauncherRunnable2);

							mCallback.onActivityLevelUp(mEngine);
							// mCallback = null;
							return;
						}

						if (mPrimaryHelicopter != null
								&& !mPrimaryHelicopter.mIsDone) {
							yTop = (int) (mPrimaryHelicopter.mY - mHelicopterHeight / 2);
							yBottom = (int) (yTop + mHelicopterHeight);
							xLeft = (int) (mPrimaryHelicopter.mX - mHelicopterWidth / 2);
							xRight = (int) (xLeft + mHelicopterWidth);

							// helicopter firing bullets
							if (mPrimaryHelicopter.mIsFiring) {

								// mCallback.onHelicopterFiring();
								// if(!mMpMachineGun1.isPlaying() &&
								// mMode == STATE_RUNNING)
								// mMpMachineGun1.start();

								Bullet bullet1 = null;

								if (!mPrimaryHelicopter.mIsReversing) {

									bullet1 = new Bullet.Builder()
											.setNow(
													SystemClock
															.elapsedRealtime())
											.setPixelsPerSecond(BULLET_SPEED)
											.setAngle(mHeading)
											.setX(xRight - 5)
											.setY(
													mPrimaryHelicopter.mY
															+ HELI_BULLET_OFFSET)
											.setIsFromHeli(true).setContext(
													mContext).setBitmapRid(
													R.drawable.ball)
											.setBitmapRRid(R.drawable.ball)
											.create();
								} else {
									bullet1 = new Bullet.Builder()
											.setNow(
													SystemClock
															.elapsedRealtime())
											.setPixelsPerSecond(BULLET_SPEED)
											.setAngle(mHeading + Math.PI)
											.setX(xLeft + 5)
											.setY(
													mPrimaryHelicopter.mY
															+ HELI_BULLET_OFFSET)
											.setIsFromHeli(true).setContext(
													mContext).setBitmapRid(
													R.drawable.ball)
											.setBitmapRRid(R.drawable.ball)
											.create();
								}

								if (bullet1 != null) {
									ArrayList<Bullet> bullets = mRegion
											.getBullets();

									bullet1.mRegion = mRegion;
									bullets.add(bullet1);

									bullet1 = null;
								}

								mPrimaryHelicopter.mIsFiring = false;
							}

							// helicopter bombing targets
							if (mIsHeliBombing) {
								Bomb bomb1 = null;

								if (!mPrimaryHelicopter.mIsReversing) {

									bomb1 = new Bomb.Builder().setNow(
											SystemClock.elapsedRealtime())
											.setPixelsPerSecond(BULLET_SPEED)
											.setAngle(mHeading + Math.PI / 2)
											.setX(mPrimaryHelicopter.mX).setY(
													(yBottom + yTop) / 2 + 15)
											.setRadiusPixels(mBombBitmapRadius)
											.setIsFromHeli(true)
											.setIsHeliReversing(false)
											.setContext(mContext).create();
								} else {
									bomb1 = new Bomb.Builder().setNow(
											SystemClock.elapsedRealtime())
											.setPixelsPerSecond(BULLET_SPEED)
											.setAngle(mHeading + Math.PI / 2)
											.setX(mPrimaryHelicopter.mX).setY(
													(yBottom + yTop) / 2 + 15)
											.setRadiusPixels(mBombBitmapRadius)
											.setIsFromHeli(true)
											.setIsHeliReversing(true)
											.setContext(mContext).create();
								}

								if (bomb1 != null) {
									List<Bomb> bombs = mRegion.getBombs();

									bomb1.mRegion = mRegion;
									bombs.add(bomb1);

									bomb1 = null;
								}
								mIsHeliBombing = false;
							}
						}

						List<Missile> missiles = mRegion.mMissiles;

						// bogeys firing missiles
						if (!mRegion.getBogeys().isEmpty()) {
							for (Bogey bogey : mRegion.getBogeys()) {
								if (bogey != null && bogey.mIsFiring) {
									Missile missile1 = null;

									if (bogey.mIsReversing) {
										missile1 = new Missile.Builder()
												.setNow(
														SystemClock
																.elapsedRealtime())
												.setPixelsPerSecond(
														BULLET_SPEED)
												.setAngle(mHeading)
												.setX(bogey.getRight())
												.setY(
														(bogey.getTop() + bogey
																.getBottom()) / 2 + 15)
												.setContext(mContext)
												.setBitmapRid(
														R.drawable.missile)
												.setBitmapRRid(
														R.drawable.missile_r)
												.setIsReversing(true).create();
									} else {
										missile1 = new Missile.Builder()
												.setNow(
														SystemClock
																.elapsedRealtime())
												.setPixelsPerSecond(
														BULLET_SPEED)
												.setAngle(mHeading + Math.PI)
												.setX(bogey.getLeft())
												.setY(
														(bogey.getTop() + bogey
																.getBottom()) / 2 + 15)
												.setContext(mContext)
												.setBitmapRid(
														R.drawable.missile)
												.setBitmapRRid(
														R.drawable.missile_r)
												.setIsReversing(false).create();
									}

									if (missile1 != null) {
										missile1.mRegion = mRegion;
										missiles.add(missile1);

										missile1 = null;
									}
									bogey.mIsFiring = false;
								}
							}
						}

						// tanks firing missiles
						List<Tank> tanks = mRegion.getTanks();

						if (!tanks.isEmpty()) {
							for (int i = 0; i < tanks.size(); i++) {
								final Tank tank = tanks.get(i);

								Missile missile2 = null;

								if (tank != null && tank.mIsFiring) {
									// tank is going right
									if (tank.IsReversing()) {
										missile2 = new Missile.Builder()
												.setNow(
														SystemClock
																.elapsedRealtime())
												.setPixelsPerSecond(
														BULLET_SPEED)
												.setAngle(Math.PI * 7 / 4)
												.setX(tank.mX)
												.setY(tank.getTop())
												.setIsFrom(Missile.FROM_TANK)
												.setIsReversing(true)
												.setContext(mContext)
												.setBitmapRid(R.drawable.sam_l)
												.setBitmapRRid(R.drawable.sam_r)
												.create();

										tank.setIsFiring(false);

									} else {
										missile2 = new Missile.Builder()
												.setNow(
														SystemClock
																.elapsedRealtime())
												.setPixelsPerSecond(
														BULLET_SPEED)
												.setAngle(Math.PI * 5 / 4)
												.setX(tank.mX)
												.setY(tank.getTop())
												.setIsFrom(Missile.FROM_TANK)
												.setIsReversing(false)
												.setPaint(mPaint)
												.setContext(mContext)
												.setBitmapRid(R.drawable.sam_l)
												.setBitmapRRid(R.drawable.sam_r)
												.create();

										tank.setIsFiring(false);
									}

									if (missile2 != null) {

										missile2.mRegion = mRegion;
										missiles.add(missile2);

										missile2 = null;
									}
								}
							}

						}

						// bombers
						if (!mRegion.mBombers.isEmpty()) {
							size = mRegion.mBombers.size();
							ArrayList<Bomber> bombers = mRegion.mBombers;

							for (int i = 0; i < size; i++) {
								final Bomber bomber = bombers.get(i);

								if (bomber != null) {
									if (bomber.mIsFiring) {
										mBomber = bomber;

										int launcherSize = mRegion.mLaunchers
												.size();

										for (int j = 0; j < launcherSize; j++) {
											if (j % 2 == 0) {
												mLauncherIndex2 = j;
												mHandler
														.postDelayed(
																mDropStingerLauncherRunnable2,
																1000);
											} else {
												mLauncherIndex = j;
												mHandler
														.postDelayed(
																mDropStingerLauncherRunnable,
																500);
											}
										}

										bomber.setIsFiring(false);
									}
								}
							}
						}

						// stinger launcher firing missiles
						ArrayList<StingerLauncher> launchers = mRegion.mLaunchers;

						if (!launchers.isEmpty()) {
							size = launchers.size();

							for (int i = 0; i < size; i++) {
								final StingerLauncher launcher = launchers
										.get(i);

								if (launcher != null && launcher.mIsFiring
										&& !launcher.mMoving) {
									Missile missile2 = null;
									// launcher is going right
									if (launcher.IsReversing()) {
										missile2 = new Missile.Builder()
												.setNow(
														SystemClock
																.elapsedRealtime())
												.setPixelsPerSecond(
														BULLET_SPEED)
												.setAngle(Math.PI * 7 / 4)
												.setX(launcher.mX)
												.setY(launcher.getTop())
												.setIsFrom(Missile.FROM_STINGER)
												.setIsReversing(true)
												.setPaint(mPaint)
												.setContext(mContext)
												.setBitmapRid(R.drawable.sam_l)
												.setBitmapRRid(R.drawable.sam_r)
												.create();

										launcher.mIsFiring = false;

										// if even number
										if (i % 2 == 0) {
											mLauncher2 = launcher;
											mHandler.postDelayed(
													mSetStingerFiringRunnable2,
													5000);
										} else {
											mLauncher = launcher;
											mHandler.postDelayed(
													mSetStingerFiringRunnable,
													5000);
										}

									} else {
										missile2 = new Missile.Builder()
												.setNow(
														SystemClock
																.elapsedRealtime())
												.setPixelsPerSecond(
														BULLET_SPEED)
												.setAngle(Math.PI * 5 / 4)
												.setX(launcher.mX)
												.setY(launcher.getTop())
												.setIsFrom(Missile.FROM_STINGER)
												.setIsReversing(false)
												.setPaint(mPaint)
												.setContext(mContext)
												.setBitmapRid(R.drawable.sam_l)
												.setBitmapRRid(R.drawable.sam_r)
												.create();

										launcher.mIsFiring = false;

										// if even number
										if (i % 2 == 0) {
											mLauncher2 = launcher;
											mHandler.postDelayed(
													mSetStingerFiringRunnable2,
													5000);
										} else {
											mLauncher = launcher;
											mHandler.postDelayed(
													mSetStingerFiringRunnable,
													5000);
										}
									}

									if (missile2 != null) {
										ArrayList<Missile> stingerMissiles = mRegion.mStingerMissiles;

										missile2.mRegion = mRegion;
										stingerMissiles.add(missile2);

										missile2 = null;
									}
								}
							}
						}

						// fighters
						ArrayList<Fighter> fighters = mRegion.mFighters;

						if (!fighters.isEmpty()) {
							size = fighters.size();

							for (int i = 0; i < size; i++) {
								final Fighter fighter = fighters.get(i);

								if (fighter != null && fighter.mIsFiring) {
									Missile missile2 = null;
									// fighter is going right
									if (fighter.IsReversing()) {
										missile2 = new Missile.Builder()
												.setNow(
														SystemClock
																.elapsedRealtime())
												.setPixelsPerSecond(
														BULLET_SPEED * 2)
												.setAngle(0)
												.setX(fighter.mX + 30)
												.setY(fighter.mY)
												.setIsFrom(Missile.FROM_FIGHTER)
												.setIsReversing(true)
												.setPaint(mPaint)
												.setContext(mContext)
												.setBitmapRid(
														R.drawable.sparrow_missle)
												.setBitmapRRid(
														R.drawable.sparrow_missle_r)
												.create();

										fighter.mIsFiring = false;
									} else {
										missile2 = new Missile.Builder()
												.setNow(
														SystemClock
																.elapsedRealtime())
												.setPixelsPerSecond(
														BULLET_SPEED * 2)
												.setAngle(Math.PI)
												.setX(fighter.mX - 30)
												.setY(fighter.mY)
												.setIsFrom(Missile.FROM_FIGHTER)
												.setIsReversing(false)
												.setPaint(mPaint)
												.setContext(mContext)
												.setBitmapRid(
														R.drawable.sparrow_missle)
												.setBitmapRRid(
														R.drawable.sparrow_missle_r)
												.create();

										fighter.mIsFiring = false;
									}

									if (missile2 != null) {
										missile2.mRegion = mRegion;
										missiles.add(missile2);

										missile2 = null;
									}
								}
							}

						}
					} else {
						// reset bullets and bogeys last update time - pause
						List<Bullet> bullets = mRegion.getBullets();

						for (Bullet bullet : bullets) {
							bullet.setLastUpdate(now);
						}

						List<Bogey> bogeys = mRegion.getBogeys();

						for (Bogey bogey : bogeys) {
							bogey.setLastUpdate(now);
						}

						List<Tank> tanks = mRegion.getTanks();

						for (Tank tank : tanks) {
							tank.setLastUpdate(now);
						}

						ArrayList<Bomber> bombers = mRegion.mBombers;

						for (Bomber bomber : bombers) {
							bomber.setLastUpdate(now);
						}

						ArrayList<StingerLauncher> launchers = mRegion.mLaunchers;

						for (StingerLauncher launcher : launchers) {
							launcher.setLastUpdate(now);
						}

						ArrayList<Fighter> fighters = mRegion.mFighters;

						for (Fighter fighter : fighters) {
							fighter.setLastUpdate(now);
						}
					}

					drawRegion(canvas, mRegion);
					if (mPrimaryHelicopter != null) {
						// only updates x,y if the state is running, ie not
						// paused
						if (mMode == STATE_RUNNING) {
							mPrimaryHelicopter.update(now);
						}
						mPrimaryHelicopter.draw(canvas, mPaint);
					}

					try {
						// the X-plosions
						for (int i = 0; i < mExplosions.size(); i++) {
							final Explosion explosion = mExplosions.get(i);
							explosion.update(now);
						}

						for (int i = 0; i < mExplosions.size(); i++) {
							final Explosion explosion = mExplosions.get(i);
							explosion.draw(canvas, mPaint);
							// TODO prune explosions that are done
						}
					} catch (Exception e) {
						Log.e(TAG + "Explosions", e.getMessage()
								+ e.fillInStackTrace());
					}
				}
			} catch (Exception e) {
				Log.e(TAG + "doDraw", e.getMessage() + e.getStackTrace()
						+ e.fillInStackTrace());
			}
		}

		/**
		 * Draw a game region, with the balls and bogeys included. Keep in mind
		 * the actual drawing is being done.
		 */
		private void drawRegion(Canvas canvas, GameRegion region) {

			try {
				// draw each bullet
				for (Bullet bullet : region.getBullets()) {
					if (!bullet.mIsDone) {
						if (bullet.getIsFromHelicopter()) {
							canvas.drawBitmap(mBulletBitmap, bullet.mX
									- mBulletBitmapRadius, bullet.mY
									- mBulletBitmapRadius, mPaint);
						} else {
							canvas.drawBitmap(mBogeyBulletBitmap, bullet.mX
									- mBogeyBulletBitmapRadius, bullet.mY
									- mBogeyBulletBitmapRadius, mPaint);

						}
					}
				}

				// draw each bomb
				for (Bomb bomb : region.getBombs()) {
					if (!bomb.mIsDone) {
						bomb.draw(canvas, mPaint);
					}
				}

				// draw each missile
				for (Missile missile : region.getMissiles()) {
					if (!missile.mIsDone) {
						missile.draw(canvas, mPaint);
					}
				}

				// draw each bogey
				for (Bogey bogey : region.getBogeys()) {
					bogey.draw(canvas, mPaint);
				}

				// draw each tank
				for (Tank tank : region.getTanks()) {
					if (!tank.getIsDone())
						tank.draw(canvas, mPaint);
				}

				// draw each bomber
				ArrayList<Bomber> bombers = region.mBombers;
				int numBombers = bombers.size();

				for (int i = 0; i < numBombers; i++) {
					final Bomber bomber = bombers.get(i);
					if (!bomber.getIsDone())
						bomber.draw(canvas, mPaint);
				}

				// draw each launcher
				ArrayList<StingerLauncher> launchers = region.mLaunchers;
				int numLaunchers = launchers.size();

				for (int i = 0; i < numLaunchers; i++) {
					final StingerLauncher launcher = launchers.get(i);
					if (!launcher.getIsDone() && launcher.mX > -1
							&& launcher.mY > -1)
						launcher.draw(canvas, mPaint);
				}

				// draw each stinger missile
				ArrayList<Missile> stingerMissiles = region.mStingerMissiles;
				int size = stingerMissiles.size();

				for (int i = 0; i < size; i++) {
					final Missile missile = stingerMissiles.get(i);

					if (!missile.mIsDone) {
						// if(missile.getDirection() == Missile.DIRECTION_RIGHT)
						// {
						// canvas.drawBitmap(
						// mSAMRightBitmap,
						// missile.mX - mSAMBitmapRadius,
						// missile.mY - mSAMBitmapRadius,
						// mPaint);
						// }
						// else
						// {
						// canvas.drawBitmap(
						// mSAMLeftBitmap,
						// missile.mX - mSAMBitmapRadius,
						// missile.mY - mSAMBitmapRadius,
						// mPaint);
						// }
						missile.draw(canvas, mPaint);
					}
				}

				// draw each fighter
				ArrayList<Fighter> fighters = region.mFighters;
				size = fighters.size();

				for (int i = 0; i < size; i++) {
					final Fighter fighter = fighters.get(i);
					if (!fighter.getIsDone())
						fighter.draw(canvas, mPaint);
				}
			} catch (Exception e) {
				Log.e(TAG + "drawRegion", e.getMessage());
			}

		}

		public void onBulletHitsBogey(long when, Bullet bullet, Bogey bogey) {

			try {
				bullet.mIsDone = true;
				bogey.mIsDone = true;

				mExplosions.add(new Explosion(when, bogey.mX, bogey.mY,
						mExplosion1, mExplosion4, mExplosion8));

				mCallback.onBulletHitsBogey(mEngine, bullet.mX, bullet.mY,
						bogey.mX, bogey.mY);

			} catch (Exception e) {
				Log.e(TAG + "onBulletHitsBogey", e.getMessage());
			}

		}

		public void onBulletHitsBomber(long when, Bullet bullet, Bomber bomber) {

			try {
				bullet.mIsDone = true;
				bomber.mIsDone = true;

				mExplosions.add(new Explosion(when, bomber.mX, bomber.mY,
						mBigExplosion1, mBigExplosion4, mBigExplosion8));

				mCallback.onBulletHitsBomber(mEngine, bullet.mX, bullet.mY,
						bomber.mX, bomber.mY);

			} catch (Exception e) {
				Log.e(TAG + "onBulletHitsBomber", e.getMessage());
			}

		}

		public void onBulletHitsVehicle(long when, Bullet bullet,
				Vehicle vehicle, boolean isBigExplosion) {

			try {
				bullet.mIsDone = true;
				vehicle.mIsDone = true;

				if (isBigExplosion) {
					mExplosions.add(new Explosion(when, vehicle.mX, vehicle.mY,
							mBigExplosion1, mBigExplosion4, mBigExplosion8));
				} else {
					mExplosions.add(new Explosion(when, vehicle.mX, vehicle.mY,
							mExplosion1, mExplosion4, mExplosion8));
				}

				mCallback.onBulletHitsVehicle(mEngine, bullet.mX, bullet.mY,
						vehicle.mX, vehicle.mY);

			} catch (Exception e) {
				Log.e(TAG + "onBulletHitsBomber", e.getMessage());
			}

		}

		public void onBogeyHitsHeli(long when, Bogey bogey) {
			try {
				// if we are just at the initial screen before play button is
				// hit
				if (mMode == STATE_RUNNING) {
					if (mPrimaryHelicopter != null
							&& !mPrimaryHelicopter.mIsDone) {
						mPrimaryHelicopter.mOldX = mPrimaryHelicopter.mX;
						mPrimaryHelicopter.mOldY = mPrimaryHelicopter.mY;

						mPrimaryHelicopter.mX = HELI_OFFSCREEN_COORD;
						mPrimaryHelicopter.mY = HELI_OFFSCREEN_COORD;

						mPrimaryHelicopter.mIsDone = true;
						bogey.mIsDone = true;

						mExplosions
								.add(new Explosion(when, bogey.mX, bogey.mY,
										mBigExplosion1, mBigExplosion4,
										mBigExplosion8));

						mCallback.onBogeyHitsHelicopter(mEngine, bogey.mX,
								bogey.mY);
					}
				}
			} catch (Exception e) {
				Log.e(TAG + "onBogeyHitsHeli", e.getMessage());
			}
		}

		public void onTankHitsHeli(long when, Tank tank) {
			try {
				// if we are just at the initial screen before play button is
				// hit
				if (mMode == STATE_RUNNING) {
					if (mPrimaryHelicopter != null
							&& !mPrimaryHelicopter.mIsDone) {
						mPrimaryHelicopter.mOldX = mPrimaryHelicopter.mX;
						mPrimaryHelicopter.mOldY = mPrimaryHelicopter.mY;

						mPrimaryHelicopter.mX = HELI_OFFSCREEN_COORD;
						mPrimaryHelicopter.mY = HELI_OFFSCREEN_COORD;

						mPrimaryHelicopter.mIsDone = true;
						tank.mIsDone = true;

						mExplosions
								.add(new Explosion(when, tank.mX, tank.mY,
										mBigExplosion1, mBigExplosion4,
										mBigExplosion8));

						mCallback.onTankHitsHelicopter(mEngine, tank.mX,
								tank.mY);
					}
				}
			} catch (Exception e) {
				Log.e(TAG + "onTankHitsHeli", e.getMessage());
			}
		}

		public void onBomberHitsHeli(long when, Bomber bomber) {
			try {
				// if we are just at the initial screen before play button is
				// hit
				if (mMode == STATE_RUNNING) {
					if (mPrimaryHelicopter != null
							&& !mPrimaryHelicopter.mIsDone) {
						mPrimaryHelicopter.mOldX = mPrimaryHelicopter.mX;
						mPrimaryHelicopter.mOldY = mPrimaryHelicopter.mY;

						mPrimaryHelicopter.mX = HELI_OFFSCREEN_COORD;
						mPrimaryHelicopter.mY = HELI_OFFSCREEN_COORD;

						mPrimaryHelicopter.mIsDone = true;
						bomber.mIsDone = true;

						mExplosions.add(new Explosion(when, bomber.mX,
								bomber.mY, mBigExplosion1, mBigExplosion4,
								mBigExplosion8));

						mCallback.onBomberHitsHelicopter(mEngine, bomber.mX,
								bomber.mY);
					}
				}
			} catch (Exception e) {
				Log.e(TAG + "onBomberHitsHeli", e.getMessage());
			}
		}

		public void onFighterHitsHeli(long when, Fighter fighter) {
			try {
				// if we are just at the initial screen before play button is
				// hit
				if (mMode == STATE_RUNNING) {
					if (mPrimaryHelicopter != null
							&& !mPrimaryHelicopter.mIsDone) {
						mPrimaryHelicopter.mOldX = mPrimaryHelicopter.mX;
						mPrimaryHelicopter.mOldY = mPrimaryHelicopter.mY;

						mPrimaryHelicopter.mX = HELI_OFFSCREEN_COORD;
						mPrimaryHelicopter.mY = HELI_OFFSCREEN_COORD;

						mPrimaryHelicopter.mIsDone = true;
						fighter.mIsDone = true;

						mExplosions.add(new Explosion(when, fighter.mX,
								fighter.mY, mBigExplosion1, mBigExplosion4,
								mBigExplosion8));

						mCallback.onFighterHitsHelicopter(mEngine, fighter.mX,
								fighter.mY);
					}
				}
			} catch (Exception e) {
				Log.e(TAG + "onFighterHitsHeli", e.getMessage());
			}
		}

		/**
		 * create new heli for situations when it hits a bogey, but there are
		 * still lives left
		 */
		public void CreateNewHelicopter() {
			/*
			 * we need to check for mIsDone in case the when it hits a bogey
			 * it's also leveling up, in which case the heli's coordinate is
			 * reset in AttackHelicopter class' onActivityLevelUp(), which is
			 * called first, so you don't want the coordinates to be set again
			 */
			if (mPrimaryHelicopter != null && mPrimaryHelicopter.mIsDone) {
				long now = SystemClock.elapsedRealtime();

				mPrimaryHelicopter.mLastUpdate = now;
				mPrimaryHelicopter.mIsDone = false;

				// is heli on the left side of the screen
				boolean isHeliOnLeftSide = true;
				isHeliOnLeftSide = mPrimaryHelicopter.mOldX <= (mViewWidth / 2);

				if (isHeliOnLeftSide) {
					mPrimaryHelicopter.mX = mHelicopterWidth / 2;
					mPrimaryHelicopter.mY = mHelicopterHeight / 2;
					mPrimaryHelicopter.mIsReversing = false;
				} else {
					mPrimaryHelicopter.mX = mViewWidth - mHelicopterWidth / 2;
					mPrimaryHelicopter.mY = mHelicopterHeight / 2;
					mPrimaryHelicopter.mIsReversing = true;
				}

				if (mEngine != null) {
					mEngine.mHeli = mPrimaryHelicopter;

					if (!mEngine.mRegions.isEmpty()) {
						GameRegion region = mEngine.mRegions.get(0);

						if (region != null) {
							region.mHeli = mPrimaryHelicopter;
						}
					}
				}
			}
		}

		public void onBombHitsTank(long when, Bomb bomb, Tank tank) {
			try {
				bomb.mIsDone = true;
				tank.mIsDone = true;

				mExplosions.add(new Explosion(when, tank.mX, tank.mY,
						mBigExplosion1, mBigExplosion4, mBigExplosion8));

				mCallback.onBombHitsTank(mEngine);

			} catch (Exception e) {
				Log.e(TAG + "onBombHitsTank", e.getMessage());
			}

		}

		public void onBombHitsLauncher(long when, Bomb bomb,
				StingerLauncher launcher) {
			try {
				bomb.mIsDone = true;
				launcher.mIsDone = true;

				mExplosions.add(new Explosion(when, launcher.mX, launcher.mY,
						mBigExplosion1, mBigExplosion4, mBigExplosion8));

				mCallback.onBombHitsStingerLauncher(mEngine);
			} catch (Exception e) {
				Log.e(TAG + "onBombHitsLauncher", e.getMessage());
			}

		}

		public void onMissileHitsHeli(long when, Missile missile,
				Helicopter heli) {
			try {
				if (mMode == STATE_RUNNING) {
					if (mPrimaryHelicopter != null
							&& !mPrimaryHelicopter.mIsDone) {
						mPrimaryHelicopter.mOldX = mPrimaryHelicopter.mX;
						mPrimaryHelicopter.mOldY = mPrimaryHelicopter.mY;

						mPrimaryHelicopter.mX = HELI_OFFSCREEN_COORD;
						mPrimaryHelicopter.mY = HELI_OFFSCREEN_COORD;

						mPrimaryHelicopter.mIsDone = true;
						missile.mIsDone = true;

						mExplosions.add(new Explosion(when, missile.mX,
								missile.mY, mExplosion1, mExplosion4,
								mExplosion8));

						mCallback.onMissileHitsHelicopter(mEngine);
					}
				}
			} catch (Exception e) {
				Log.e(TAG + "onMissileHitsHeli", e.getMessage());
			}
		}

		public void doAccelerationChanged(float x, float y, float z) {
			synchronized (mSurfaceHolder) {
				Log.d(TAG + "doAccelerationChanged", "in sync");

				if (mMode == STATE_RUNNING) {
					if (mPrimaryHelicopter != null
							&& !mPrimaryHelicopter.mIsDone) {
						// going up
						if (mPrevTiltX - x >= PRESET_THRESHOLDX_MIN
								&& mPrevTiltX - x <= PRESET_THRESHOLDX_MAX) {
							int height1 = mHelicopterHeight / 2;

							mPrimaryHelicopter.mY -= DISTANCE_INCREMENT_ACCEL_Y
									* 1.5 * mTiltSensitivityMultiplier;
							// }

							if (mPrimaryHelicopter.mY <= height1) {
								mPrimaryHelicopter.mY = height1;
							}

						}
						// going down
						else if (x - mPrevTiltX >= PRESET_THRESHOLDX_MIN
								&& x - mPrevTiltX <= PRESET_THRESHOLDX_MAX) {

							int height1 = getHeight() - mTankBitmapHeight;

							// slow down when near bottom
							if (mPrimaryHelicopter.mY >= height1
									- COLLISION_ALERT_DISTANTCE_Y) {
								mPrimaryHelicopter.mY += DISTANCE_INCREMENT_ACCEL_Y
										/ 40 * mTiltSensitivityMultiplier;
							} else {
								mPrimaryHelicopter.mY += DISTANCE_INCREMENT_ACCEL_Y
										* mTiltSensitivityMultiplier;
							}

							if (mPrimaryHelicopter.mY >= height1)
								mPrimaryHelicopter.mY = height1;
						}

						mPrevTiltX = x;

						// going right
						if (mPitch < 0 && mPitch >= -65) {
							int width1 = getWidth() - mHelicopterWidth / 2;

							mPrimaryHelicopter.mX += DISTANCE_INCREMENT_ACCEL_X
									* mTiltSensitivityMultiplier;
							// }

							if (mPrimaryHelicopter.mX > width1) {
								mPrimaryHelicopter.mX = width1;
							}
						}

						// going left
						else if (mPitch > 0 && mPitch <= 65) {
							int width1 = mHelicopterWidth / 2;

							mPrimaryHelicopter.mX -= DISTANCE_INCREMENT_ACCEL_X
									* mTiltSensitivityMultiplier;

							if (mPrimaryHelicopter.mX < width1) {
								mPrimaryHelicopter.mX = width1;
							}
						}
					}
				}
			}
		}

		public void doAccelerationChanged1(float x, float y, float z) {
			synchronized (mSurfaceHolder) {
				Log.d(TAG + "doAccelerationChanged", "in sync");

				if (mMode == STATE_RUNNING) {
					if (mPrimaryHelicopter != null
							&& !mPrimaryHelicopter.mIsDone) {
						/* if the phone is tilted too much, then no movement */
						if (!mIsTooTilted) {
							// going up
							if (mPrevTiltX - x >= PRESET_THRESHOLDX_MIN
									&& mPrevTiltX - x <= PRESET_THRESHOLDX_MAX) {
								// if too tilted towards bottom, but the heli is
								// on the top half of
								// screen. going up means you are actually
								// trying to level the phone, not
								// really going up, so not action
								if (mIsTooTiltedToBottom
										&& mPrimaryHelicopter.mY <= mViewHeight / 2) {

								} else {
									int height1 = mHelicopterHeight / 2;

									// // slow down when near top
									// if(mPrimaryHelicopter.mY <= height1 +
									// COLLISION_ALERT_DISTANTCE_Y)
									// {
									// mPrimaryHelicopter.mY -=
									// DISTANCE_INCREMENT_ACCEL/5;
									// }
									// else
									// {
									mPrimaryHelicopter.mY -= DISTANCE_INCREMENT_ACCEL_Y
											* 1.5 * mTiltSensitivityMultiplier;
									// }

									if (mPrimaryHelicopter.mY <= height1) {
										mPrimaryHelicopter.mY = height1;
									}
								}
							}
							// going down
							else if (x - mPrevTiltX >= PRESET_THRESHOLDX_MIN
									&& x - mPrevTiltX <= PRESET_THRESHOLDX_MAX) {
								// if too tilted towards top, but the heli is on
								// the bottom half of
								// screen. going down means you are actually
								// trying to level the phone, not
								// really going down, so not action
								if (mIsTooTiltedToTop
										&& mPrimaryHelicopter.mY > mViewHeight / 2) {

								} else {
									int height1 = getHeight()
											- mTankBitmapHeight;

									// slow down when near bottom
									if (mPrimaryHelicopter.mY >= height1
											- COLLISION_ALERT_DISTANTCE_Y) {
										mPrimaryHelicopter.mY += DISTANCE_INCREMENT_ACCEL_Y
												/ 40
												* mTiltSensitivityMultiplier;
									} else {
										mPrimaryHelicopter.mY += DISTANCE_INCREMENT_ACCEL_Y
												* mTiltSensitivityMultiplier;
									}

									if (mPrimaryHelicopter.mY >= height1)
										mPrimaryHelicopter.mY = height1;
								}
							}

							mPrevTiltX = x;

							// going right
							if (y - mPrevTiltY >= PRESET_THRESHOLDY_MIN
									&& y - mPrevTiltY <= PRESET_THRESHOLDY_MAX) {
								// if too tilted towards left, but the heli is
								// on the right half of
								// screen. going right means you are actually
								// trying to level the phone, not
								// really going right, so not action
								if (mIsTooTiltedToLeft
										&& mPrimaryHelicopter.mX > mViewWidth / 2) {

								} else {
									int width1 = getWidth() - mHelicopterWidth
											/ 2;

									// // slow down when get close to right side
									// if(mPrimaryHelicopter.mX > width1 -
									// COLLISION_ALERT_DISTANTCE_X)
									// {
									// mPrimaryHelicopter.mX +=
									// DISTANCE_INCREMENT_ACCEL/5;
									// }
									// else
									// {
									mPrimaryHelicopter.mX += DISTANCE_INCREMENT_ACCEL_X
											* mTiltSensitivityMultiplier;
									// }

									if (mPrimaryHelicopter.mX > width1) {
										mPrimaryHelicopter.mX = width1;
									}
								}
							}
							// going left
							else if (mPrevTiltY - y >= PRESET_THRESHOLDY_MIN
									&& mPrevTiltY - y <= PRESET_THRESHOLDY_MAX) {
								// if too tilted towards right, but the heli is
								// on the left half of
								// screen. going left means you are actually
								// trying to level the phone, not
								// really going left, so not action
								if (mIsTooTiltedToRight
										&& mPrimaryHelicopter.mX <= mViewWidth / 2) {

								} else {
									int width1 = mHelicopterWidth / 2;

									// slow down when get close to left side
									// if(mPrimaryHelicopter.mX < width1 +
									// COLLISION_ALERT_DISTANTCE_X)
									// {
									// mPrimaryHelicopter.mX -=
									// DISTANCE_INCREMENT_ACCEL/5;
									// }
									// else
									// {
									mPrimaryHelicopter.mX -= DISTANCE_INCREMENT_ACCEL_X
											* mTiltSensitivityMultiplier;
									// }

									if (mPrimaryHelicopter.mX < width1) {
										mPrimaryHelicopter.mX = width1;
									}
								}
							}
							mPrevTiltY = y;

							mIsTooTilted = false;
						} else {
							// reset
							mIsTooTilted = false;
							mIsTooTiltedToLeft = false;
							mIsTooTiltedToRight = false;
							mIsTooTiltedToTop = false;
							mIsTooTiltedToBottom = false;
						}
					}
				}
			}
		}

	}

	/**
	 * Callback notifying of events related to the ball engine.
	 */
	static interface BallEngineCallBack {

		/**
		 * The engine has its dimensions and is ready to go.
		 * 
		 * @param ballEngine
		 *            The ball engine.
		 */
		void onEngineReady(GameEngine gameEngine);

		void onBombHitsTank(GameEngine mEngine);

		void onBombHitsStingerLauncher(GameEngine mEngine);

		void onBulletHitsBogey(GameEngine gameEngine, float x, float y,
				float bogeyX, float bogeyY);

		void onBulletHitsBomber(GameEngine gameEngine, float x, float y,
				float bomberX, float bomberY);

		void onBulletHitsVehicle(GameEngine gameEngine, float x, float y,
				float vehicleX, float vehicleY);

		void onBulletHitsHelicopter(GameEngine gameEngine);

		void onBogeyHitsHelicopter(GameEngine gameEngine, float bogeyX,
				float bogeyY);

		void onTankHitsHelicopter(GameEngine gameEngine, float tankX,
				float tankY);

		void onBomberHitsHelicopter(GameEngine gameEngine, float bomberX,
				float bomberY);

		void onFighterHitsHelicopter(GameEngine gameEngine, float fighterX,
				float fighterY);

		void onHelicopterFiring();

		void onActivityLevelUp(GameEngine gameEngine);

		void onMissileHitsHelicopter(GameEngine gameEngine);

		void onMenuButtonClicked();

		void onLoseGame();
	}

	public AttackHelicopterView(Context context, AttributeSet attrs) {

		super(context, attrs);

		try {
			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setStrokeWidth(2);
			mPaint.setColor(Color.BLACK);

			// register our interest in hearing about changes to our surface
			SurfaceHolder holder = getHolder();
			holder.addCallback(this);

			mBulletBitmap = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.ball);

			mHeliBombsBitmap = BitmapFactory.decodeResource(context
					.getResources(), R.drawable.bombs2);

			mBombBitmapRadius = (mHeliBombsBitmap.getWidth()) / 2;

			mBogeyBulletBitmap = BitmapFactory.decodeResource(context
					.getResources(), R.drawable.missile);

			mBogeyBitmap = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.bogey);

			mTankBitmap = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.tank);

			mSAMRightBitmap = BitmapFactory.decodeResource(context
					.getResources(), R.drawable.sam_r);

			mBulletBitmapRadius = (mBulletBitmap.getWidth()) / 2;

			mBogeyBulletBitmapRadius = (mBogeyBulletBitmap.getHeight()) / 2;

			mBogeyBitmapRadius = (mBogeyBitmap.getWidth()) / 2;

			mTankBitmapRadius = (mTankBitmap.getWidth()) / 2;

			mTankBitmapHeight = (mTankBitmap.getHeight());

			mExplosion1 = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.explosion0004);
			mExplosion4 = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.explosion0016);
			mExplosion8 = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.explosion0032);

			mBigExplosion1 = BitmapFactory.decodeResource(context
					.getResources(), R.drawable.bigexplosion0001);
			mBigExplosion4 = BitmapFactory.decodeResource(context
					.getResources(), R.drawable.bigexplosion0004);
			mBigExplosion8 = BitmapFactory.decodeResource(context
					.getResources(), R.drawable.bigexplosion0008);

			mBomberBitmap = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.bomber);

			mBomberBitmapRadius = (mBomberBitmap.getWidth()) / 2;

			mBomberBitmapHeight = (mBomberBitmap.getHeight());

			mLauncherBitmap = BitmapFactory.decodeResource(context
					.getResources(), R.drawable.stinger_launcher);

			mLauncherBitmapRadius = (mLauncherBitmap.getWidth()) / 2;

			mLauncherBitmapHeight = (mLauncherBitmap.getHeight());

			mFighterBitmap = BitmapFactory.decodeResource(context
					.getResources(), R.drawable.fighter);

			mFighterBitmapRadius = (mFighterBitmap.getWidth()) / 2;

			mFighterBitmapHeight = (mFighterBitmap.getHeight());

			mDifficulty = DIFFICULTY_MEDIUM;
			mNumLives = NUM_LIVES_TO_START_MEDIUM;

			mTiltSensitivity = NORMAL_TILT_SENSITIVITY;
			mTiltSensitivityMultiplier = 1;

			// createInputObjectPool();

			mPrevTiltX = 0;
			mPrevTiltY = 0;
			mPrevTiltZ = 0;

			mIsTooTilted = false;
			mIsTooTiltedToLeft = false;
			mIsTooTiltedToRight = false;
			mIsTooTiltedToTop = false;
			mIsTooTiltedToBottom = false;

			// create thread only; it's started in surfaceCreated()
			thread = new AttackHelicopterThread(holder, context, new Handler() {
				@Override
				public void handleMessage(Message m) {
					mStatusText.setVisibility(m.getData().getInt("viz"));
					mStatusText.setText(m.getData().getString("text"));
				}
			});

			setFocusable(true); // make sure we get key events
		} catch (Exception ex) {
			Log.e(TAG + "Ctor", ex.getMessage());
		}
	}

	private void createInputObjectPool() {
		inputObjectPool = new ArrayBlockingQueue<InputObject>(INPUT_QUEUE_SIZE);
		for (int i = 0; i < INPUT_QUEUE_SIZE; i++) {
			inputObjectPool.add(new InputObject(inputObjectPool));
		}
	}

	/**
	 * Fetches the animation thread corresponding to this View.
	 * 
	 * @return the animation thread
	 */
	public AttackHelicopterThread getThread() {
		return thread;
	}

	public int getNumStartLives() {
		return mNumLives;
	}

	@Override
	public boolean onTouchEvent(MotionEvent motionEvent) {
		requestFocusFromTouch();
		return thread.doTouchEvent(motionEvent);
	}

	public void onAccelerationChanged(float x, float y, float z) {
		thread.doAccelerationChanged(x, y, z);
	}

	/**
	 * Standard override to get key-press events.
	 */
	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent msg)
	// {
	// return thread.doKeyDown(keyCode, msg);
	// }

	/**
	 * Standard override for key-up. We actually care about these, so we can
	 * turn off the engine or stop rotating.
	 */
	// @Override
	// public boolean onKeyUp(int keyCode, KeyEvent msg) {
	// return thread.doKeyUp(keyCode, msg);
	// }

	/**
	 * Standard window-focus override. Notice focus lost so we can pause on
	 * focus lost. e.g. user switches to take a call.
	 */
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		if (!hasWindowFocus) {
			thread.pause();
		}
	}

	/**
	 * Installs a pointer to the text view used for messages.
	 */
	public void setStatusTextView(TextView textView) {
		mStatusText = textView;
	}

	/* Callback invoked when the surface dimensions change. */
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		thread.setSurfaceSize(width, height);
	}

	/*
	 * Callback invoked when the Surface has been created and is ready to be
	 * used.
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		// start the thread here so that we don't busy-wait in run()
		// waiting for the surface to be created
		thread.mRun = true;
		thread.start();

		// soundThread.setRunning(true);
		// soundThread.start();
	}

	/*
	 * Callback invoked when the Surface has been destroyed and must no longer
	 * be touched. WARNING: after this method returns, the Surface/Canvas must
	 * never be touched again!
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		// we have to tell thread to shut down & wait for it to finish

		boolean retry = true;
		thread.mRun = false;
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
				Log.e(TAG + "surfaceDestroyed", e.getMessage());
			}
		}
	}

	@Override
	protected void onSizeChanged(int i, int i1, int i2, int i3) {
		super.onSizeChanged(i, i1, i2, i3);

		mViewWidth = getWidth();
		mViewHeight = getHeight();

		// this should only happen once when the activity is first launched.
		// we could be smarter about saving / restoring across activity
		// lifecycles, but for now, this is good enough to handle in game play,
		// and most cases of navigating away with the home key and coming back.
		mEngine = new GameEngine(mContext, mPrimaryHelicopter, 0, mViewWidth,
				0, mViewHeight, BULLET_SPEED, mBulletBitmapRadius, BOGEY_SPEED,
				mBogeyBitmapRadius, TANK_SPEED, mTankBitmapRadius,
				mTankBitmapHeight, mBomberBitmapRadius, mBomberBitmapHeight,
				mLauncherBitmapRadius, mLauncherBitmapHeight,
				mFighterBitmapRadius, mFighterBitmapHeight);

		mEngine.setCallBack(this);
		mEngine.setBogeyCallBack(this);
		mEngine.setBombCallBack(this);
		mEngine.setMissileCallBack(this);
		mEngine.setTankCallBack(this);
		mEngine.setBomberCallBack(this);
		mEngine.setFighterCallBack(this);
		mCallback.onEngineReady(mEngine);
	}

	/**
	 * Called on new game and game reset. calls GameEngine.reset()
	 */
	public void resetEngine() {
		try {
			switch (mDifficulty) {
			case DIFFICULTY_EASY:
				mNumLives = NUM_LIVES_TO_START_EASY;
				break;
			case DIFFICULTY_MEDIUM:
				mNumLives = NUM_LIVES_TO_START_MEDIUM;
				break;
			case DIFFICULTY_HARD:
				mNumLives = NUM_LIVES_TO_START_HARD;
				break;
			default:
				mNumLives = NUM_LIVES_TO_START_MEDIUM;
				break;
			}

			switch (mTiltSensitivity) {
			case LEAST_TILT_SENSITIVITY:
				mTiltSensitivityMultiplier = 0.25F;
				break;
			case LESS_TILT_SENSITIVITY:
				mTiltSensitivityMultiplier = 0.5F;
				break;
			case NORMAL_TILT_SENSITIVITY:
				mTiltSensitivityMultiplier = 1;
				break;
			case MORE_TILT_SENSITIVITY:
				mTiltSensitivityMultiplier = 1.25F;
				break;
			case MOST_TILT_SENSITIVITY:
				mTiltSensitivityMultiplier = 1.5F;
				break;
			default:
				mTiltSensitivityMultiplier = 1;
				break;
			}

			long now = SystemClock.elapsedRealtime();

			mHeliRids = new ArrayList<Integer>();
			mHeliRids.add(R.drawable.heli);
			mHeliRids.add(R.drawable.heli2);
			mHeliRids.add(R.drawable.heli3);
			mHeliRids.add(R.drawable.heli_r);
			mHeliRids.add(R.drawable.heli2_r);
			mHeliRids.add(R.drawable.heli3_r);

			// set heli center
			mPrimaryHelicopter = new Helicopter.Builder().setNow(now).setAngle(
					Math.PI).setX(mHelicopterWidth / 2).setY(
					mHelicopterHeight / 2).setContext(mContext).setIsFiring(
					false).setBitmapRids(mHeliRids).create();

			mPrimaryHelicopter.mLastTimeHeliWasHit = now;

			mEngine = new GameEngine(mContext, mPrimaryHelicopter, 0,
					getWidth(), 0, getHeight(), BULLET_SPEED,
					mBulletBitmapRadius, BOGEY_SPEED, mBogeyBitmapRadius,
					TANK_SPEED, mTankBitmapRadius, mTankBitmapHeight,
					mBombBitmapRadius, mBomberBitmapHeight,
					mLauncherBitmapRadius, mLauncherBitmapHeight,
					mFighterBitmapRadius, mFighterBitmapHeight);

			mEngine.setCallBack(this);
			mEngine.setBogeyCallBack(this);
			mEngine.setBombCallBack(this);
			mEngine.setMissileCallBack(this);
			mEngine.setTankCallBack(this);
			mEngine.setBomberCallBack(this);
			mEngine.setFighterCallBack(this);
			mCallback.onEngineReady(mEngine);

			mIsBogeyFiring = true;
			mIsTankFiring = true;
			mIsBomberDropping = true;
		} catch (Exception e) {
			Log.e(TAG + "resetEngine", e.getMessage());
		}
	}

	public GameEngine getEngine() {
		return mEngine;
	}

	/**
	 * Set the callback that will be notified of events related to the ball
	 * engine.
	 * 
	 * @param callback
	 *            The callback.
	 */
	public void setCallback(BallEngineCallBack callback) {
		mCallback = callback;
	}

	public void setIsHeliBombing(boolean isBombing) {
		mIsHeliBombing = isBombing;
	}

	/** {@inheritDoc} */
	public void onBulletHitsBogey(long when, Bullet bullet, Bogey bogey) {
		thread.onBulletHitsBogey(when, bullet, bogey);
	}

	/** {@inheritDoc} */
	public void onBulletHitsBomber(long when, Bullet bullet, Bomber bomber) {
		thread.onBulletHitsBomber(when, bullet, bomber);
	}

	/** {@inheritDoc} */
	public void onBulletHitsVehicle(long when, Bullet bullet, Vehicle vehicle,
			boolean isBigExplosion) {
		thread.onBulletHitsVehicle(when, bullet, vehicle, isBigExplosion);
	}

	/** {@inheritDoc} */
	public void onMissileHitsHeli(long when, Missile missile, Helicopter heli) {
		thread.onMissileHitsHeli(when, missile, heli);
	}

	/** {@inheritDoc} */
	public void onBogeyHitsHeli(long when, Bogey bogey) {
		thread.onBogeyHitsHeli(when, bogey);
	}

	/** {@inheritDoc} */
	public void onTankHitsHeli(long when, Tank tank) {
		thread.onTankHitsHeli(when, tank);
	}

	/** {@inheritDoc} */
	public void onBomberHitsHeli(long when, Bomber bomber) {
		thread.onBomberHitsHeli(when, bomber);
	}

	/** {@inheritDoc} */
	public void onFighterHitsHeli(long when, Fighter fighter) {
		thread.onFighterHitsHeli(when, fighter);
	}

	public void onBombHitsTank(long when, Bomb bomb, Tank tank) {
		thread.onBombHitsTank(when, bomb, tank);
	}

	public void onBombHitsLauncher(long when, Bomb bomb,
			StingerLauncher launcher) {
		thread.onBombHitsLauncher(when, bomb, launcher);
	}

	/**
	 * Update the display showing the number of lives left.
	 * 
	 * @param numLives
	 *            The number of lives left.
	 */
	void updateLivesDisplay(int numLives) {
		String text = (numLives == 1) ? mContext
				.getString(R.string.one_life_left) : mContext.getString(
				R.string.lives_left, numLives);
		mLivesLeftInfo.setText(text);
	}

	public void onOrientationChangedTooMuch(float pitch, float roll,
			float azimuth) {
		mIsTooTilted = true;

		if (pitch < -55) {
			mIsTooTiltedToRight = true;
		}
		if (pitch > 55) {
			mIsTooTiltedToLeft = true;
		}
		if (roll < -40) {
			mIsTooTiltedToTop = true;
		}
		if (roll > 40) {
			mIsTooTiltedToBottom = true;
		}

	}

	public void onOrientationChanged(float pitch, float roll, float azimuth) {
		mPitch = pitch;
		this.mAzimuth = azimuth;
		this.mRoll = roll;

	}
}
