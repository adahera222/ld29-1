package de.bitowl.ld28.objects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;

import de.bitowl.ld28.AnimAction;
import de.bitowl.ld28.screens.IngameScreen;

public class Slime extends Enemy {

	public Slime(IngameScreen pScreen) {
		super(pScreen, pScreen.atlas.findRegion("slime"));
		Animation wobble=new Animation(0.08f, screen.atlas.findRegions("slime"));
		wobble.setPlayMode(Animation.LOOP_PINGPONG);
		addAction(new AnimAction(wobble));
		defSpeedX=0.2f*(MathUtils.randomBoolean()?1:-1);
	}
	@Override
	public void hitLeft(){
		defSpeedX=0.2f;
	}
	@Override
	public void hitRight() {
		defSpeedX=-0.2f;
	}
}
