package com.lam.android.attackhelicopter2;

public interface OrientationListener {

	/** when the roll and pitch changed too much*/
	public void onOrientationChangedTooMuch(float pitch, float roll, 
			float azimuth);
	
	public void onOrientationChanged(float pitch, float roll, 
			float azimuth);
}
