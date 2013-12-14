package de.bitowl.ld28;

import com.badlogic.gdx.graphics.g2d.Animation;

public class Player extends GameObject{
	public final static float JUMP_CONST=4f;
	AnimAction walking;
	AnimAction sword;
	boolean isWalking;
	boolean isFlipped;
	
	public Player(IngameScreen pScreen){
		super(pScreen,pScreen.atlas.findRegion("player"));
		doNotStopOnWalls = true; // so we can smoothly jump, when we stand at a wall
		Animation walk=new Animation(0.1f,screen.atlas.findRegions("player"));
		//walk.setPlayMode(Animation.LOOP_PINGPONG);
		walking = new AnimAction(walk,true);
		
		sword = new AnimAction(new Animation(0.05f,screen.atlas.findRegions("sword")));
		
		setOriginX(getWidth()/2);
	}
	
	public void jump(){
		if(onGround){
			speedY = JUMP_CONST;
			onGround = false;
		}
	}
	
	// what tile the player is standing on
	public int getStandingX(){
		return (int)( (getX() +getWidth()/2)/screen.colLayer.getTileWidth());
	}
	public int getStandingY(){
		return (int)(getY()/screen.colLayer.getTileHeight())-1;
	}
	public int getMiddleY(){
		return (int)((getY()+getHeight()/2)/screen.colLayer.getTileHeight());
	}

	public void walk() {
		if(speedX==0 && isWalking){
			removeAction(walking);
			isWalking=false;
			
		}else if(speedX!=0 && !isWalking){
			addAction(walking);
			isWalking=true;
		}
		if(speedX<0 && !isFlipped){
			setScaleX(-1);
			isFlipped=true;
		}else if(speedX>0 && isFlipped){
			setScaleX(1);
			isFlipped=false;				
		}
		
	}
	public void sword(){
		if(sword.getActor()==null){
			sword.reset();
			addAction(sword);
		}
	}
}
