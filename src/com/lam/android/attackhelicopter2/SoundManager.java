package com.lam.android.attackhelicopter2;

import java.util.HashMap;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

public class SoundManager {
	
	private  SoundPool mSoundPool; 
	private  HashMap<Integer, Integer> mSoundPoolMap; 
	private  AudioManager  mAudioManager;
	private  Context mContext;
	
	private String TAG = "SoundManager ";
	
	public SoundManager()
	{
		
	}
		
	public void initSounds(Context theContext) { 
		 mContext = theContext;
	     mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0); 
	     mSoundPoolMap = new HashMap<Integer, Integer>(); 
	     mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE); 	     
	} 
	
	public void addSound(int Index,int SoundID)
	{
		mSoundPoolMap.put(1, mSoundPool.load(mContext, SoundID, 1));
	}
	
	public void playSound(int index) { 
		try
		{
		    int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC); 
		    mSoundPool.play(mSoundPoolMap.get(index), streamVolume, streamVolume, 1, 0, 1f); 
		}
		catch(Exception e)
		{
			Log.e(TAG + "playSound", e.getMessage());
		}
	}
	
	public void playLoopedSound(int index) { 
		
	     int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC); 
	     mSoundPool.play(mSoundPoolMap.get(index), streamVolume, streamVolume, 1, -1, 1f); 
	}
	
}