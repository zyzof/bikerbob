package game;

import game.bikerbob.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainMenu extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    // Start game on click
    public void onClickStartGame(View v) {
    	startActivity(new Intent(this, GameActivity.class));
    }
}
