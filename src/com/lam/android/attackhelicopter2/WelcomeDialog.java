package com.lam.android.attackhelicopter2;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

/**
 * When the game starts, the user is welcomed with a message, and buttons for
 * starting a new game, or getting instructions about the game.
 */
public class WelcomeDialog extends Dialog implements View.OnClickListener {

    private final NewGameCallback mCallback;

    private View mNewGame;

    public WelcomeDialog(Context context, NewGameCallback callback) {
        super(context);
        mCallback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.app_name);

        setContentView(R.layout.welcome_dialog);

        mNewGame = findViewById(R.id.newGame);
        mNewGame.setOnClickListener(this);

    }

    /** {@inheritDoc} */
    public void onClick(View v) {
        if (v == mNewGame) {
            mCallback.onNewGame();
            dismiss();
        }
    }
}
