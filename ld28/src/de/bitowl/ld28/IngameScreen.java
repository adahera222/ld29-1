package de.bitowl.ld28;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class IngameScreen extends AbstractScreen{

	
	TextureAtlas atlas;
	
	// map stuff
	TiledMap map;
	OrthogonalTiledMapRenderer renderer;
	TiledMapTileLayer colLayer;
	TiledMapTileLayer destLayer; // layer that is containing destroyable stuff
	
	TiledMapTileLayer fgLayer;
	TiledMapTileLayer bgLayer;
	
	Player player;
	
	int resized; //TODO remove this workaround and really fix it (e.g. with a menu :P)
	
	/**
	 * how hard is a tile to destroy
	 */
	int[] destroyable;
	
	Group items;
	Group enemies;
	Group ladders;
	
	enum Weapon{
		SWORD, SHOVEL, PICKAXE, BOMBS, BOW
	}
	Weapon weapon;
	

	
	WeaponBar weaponbar;
	
	public IngameScreen(LDGame pGame) {
		super(pGame);
		
		
		map = game.assets.get("maps/map1.tmx",TiledMap.class);
		atlas = game.assets.get("textures/textures.pack", TextureAtlas.class);
		
		renderer = new OrthogonalTiledMapRenderer(map);
		renderer.setView((OrthographicCamera) stage.getCamera());
		colLayer = (TiledMapTileLayer) map.getLayers().get("collision");
		colLayer.setVisible(false);
		destLayer = (TiledMapTileLayer) map.getLayers().get("destroyable");
		
		bgLayer = (TiledMapTileLayer) map.getLayers().get("background");
		fgLayer = (TiledMapTileLayer) map.getLayers().get("foreground");
		

		ladders=new Group(); // ladders should be behind the player
		stage.addActor(ladders);
		
		items = new Group();
		stage.addActor(items);
		
		
		player = new Player(this);
		player.setY(colLayer.getHeight()*colLayer.getTileHeight()-player.getHeight());
		stage.addActor(player);

		weapon = Weapon.SHOVEL;

			
		// handle input
		Gdx.input.setInputProcessor(new GameInputProcessor());
		
		
		
		
		// have the tiles properties?
		TiledMapTileSet tileset=map.getTileSets().getTileSet("tileset");
		
		
		int tssize=0;
		Iterator<TiledMapTile> it =tileset.iterator();
		while(it.hasNext()){ // count how many tiles there are
			TiledMapTile tile =it.next();
			if(tile.getId()>tssize){
				tssize=tile.getId();
			}
			
		}
		destroyable = new int[tssize+1];
		it = tileset.iterator();
		
		while(it.hasNext()){
			TiledMapTile tile = it.next();
			MapProperties properties = tile.getProperties();
			if(properties.get("dst")!=null){
				System.out.println(tile.getId()+": "+properties.get("dst"));
				try{
					destroyable[tile.getId()]=Integer.parseInt((String) properties.get("dst"));
				}catch(NumberFormatException e){
					destroyable[tile.getId()]=1;
				}
			}
			/*Iterator<String> it =properties.getKeys();
			System.out.println(tile.getId());
			while(it.hasNext()){
				String key=it.next();
				System.out.println(key+":"+properties.get(key));
			}*/
		}
		
		
		enemies=new Group();
		
		// place enemies
		TiledMapTileLayer enemyLay=(TiledMapTileLayer) map.getLayers().get("enemies");
		for(int y=0;y<enemyLay.getHeight();y++){
			for(int x=0;x<enemyLay.getWidth();x++){
				if(enemyLay.getCell(x, y)!=null){
					// System.out.println(enemyLay.getCell(x, y).getTile().getId());
					Enemy enemy;
					switch(enemyLay.getCell(x, y).getTile().getId()){
						case 3:
							enemy = new Worm(this);
							break;
						case 4:
							enemy = new Slime(this);
							break;
						case 5:
							enemy = new Spider(this);
							break;
						case 6:
							enemy = new Spike(this);
							break;
						case 7:
							Chest chest=new Chest(this);
							chest.setPosition(x*enemyLay.getTileWidth(), y*enemyLay.getTileHeight());
							items.addActor(chest);
							continue;
						case 8:
							Jug jug = new Jug(this);
							jug.setPosition(x*enemyLay.getTileWidth(), y*enemyLay.getTileHeight());
							items.addActor(jug);
							continue;
						case 9:
							HealthBottle hb = new HealthBottle(this);
							hb.setPosition(x*enemyLay.getTileWidth()+4, y*enemyLay.getTileHeight());
							items.addActor(hb);		
							continue;
						default:
							enemy = new Enemy(this);
							break;
					} 
					enemy.setPosition(x*enemyLay.getTileWidth(), y*enemyLay.getTileHeight());
					enemies.addActor(enemy);
				}
			}
		}
		
		stage.addActor(enemies);
		
		
		
		// HUD
		LifeBar lifebar=new LifeBar(this);
		stage.addActor(lifebar);
		
		weaponbar = new WeaponBar(this);
		stage.addActor(weaponbar);
		
		
	}
	@Override
	public void render(float delta) {
		// TODO remove this i3-resize workaround
		if(resized < 10){

			// resize at the beginning to set the letterbox right
			resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			resized++;
		}
		
		// move all the stuff around and stuff
		stage.act(delta);
		
		// scroll camera to the player
		stage.getCamera().position.set(player.getX(),player.getY(),0);
		stage.getCamera().update();
		
		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glViewport((int) viewport.x, (int) viewport.y,
                (int) viewport.width, (int) viewport.height);  // TODO move this into an own method in AbstractScreen
		
		renderer.setView((OrthographicCamera) stage.getCamera());
		renderer.getSpriteBatch().begin();
		renderer.renderTileLayer(bgLayer);
		renderer.renderTileLayer(destLayer);
		renderer.getSpriteBatch().end();
		stage.draw();
		renderer.getSpriteBatch().begin();
		renderer.renderTileLayer(fgLayer);
		renderer.getSpriteBatch().end();
		// super.render(delta);
	}

	@Override
	public void dispose() {
		super.dispose();
		renderer.dispose();
	}
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		renderer.setView((OrthographicCamera)stage.getCamera());
	}
	
	class GameInputProcessor extends InputAdapter{
				
		@Override
		public boolean keyDown(int keycode) {
			switch(keycode){
				case Keys.LEFT:
				case Keys.RIGHT:
				case Keys.A:
				case Keys.D:
					player.speedX= Gdx.input.isKeyPressed(Keys.RIGHT)?1:0-(Gdx.input.isKeyPressed(Keys.LEFT)?1:0)+(Gdx.input.isKeyPressed(Keys.D)?1:0)-(Gdx.input.isKeyPressed(Keys.A)?1:0);
					player.walk();
					break;
				case Keys.UP:
				case Keys.SPACE:
				case Keys.W:
					player.jump();
					break;
				case Keys.DOWN:
				case Keys.S:
					player.descend();
					break;
			/*	case Keys.S: // dig down
					digTile(player.getStandingX(),player.getStandingY(), 1);
					player.dig();
					//colLayer.getCell((int)(player.getX()/colLayer.getTileWidth()), (int)(player.getY()/colLayer.getTileHeight())).setTile();
					break;
				case Keys.A: // dig left
					digTile(player.getStandingX()-1,player.getMiddleY(), 1);
					break;
				case Keys.D: // dig right
					digTile(player.getStandingX()+1,player.getMiddleY(), 1);
					break;
				case Keys.Q: // deploy a bomb
					Bomb bomb =new Bomb(IngameScreen.this);
					bomb.setPosition(player.getX()+player.getWidth()/2-bomb.getWidth()/2, player.getY()+player.getHeight()/2-bomb.getHeight()/2);
					bomb.speedX = player.speedX*2;
					bomb.speedY = player.speedY;
					stage.addActor(bomb);
	*/
				case Keys.E:
					// swing your sword, mighty hero!
					player.sword(player.isFlipped);
					break;
				case Keys.Y:
					if(player.onGround || player.onLadder){
						// place a ladder						
						Ladder ladder=new Ladder(IngameScreen.this);
						ladder.setPosition(player.getStandingX()*destLayer.getTileWidth(), player.getY()+player.getHeight()/2);
						
						if(ladder.notOnLadder()){ // only place it, if there is no other ladder
							ladders.addActor(ladder);
						}
					}
					
				case Keys.NUM_1:
					weaponbar.selectId(0);
					break;
				case Keys.NUM_2:
					weaponbar.selectId(1);
					break;
				case Keys.NUM_3:
					weaponbar.selectId(2);
					break;
				case Keys.NUM_4:
					weaponbar.selectId(3);
					break;
				case Keys.NUM_5:
					weaponbar.selectId(4);
					break;
			}
			
			return super.keyDown(keycode);
		}
		@Override
		public boolean keyUp(int keycode) {
			switch(keycode){
				case Keys.LEFT:
				case Keys.RIGHT:
				case Keys.A:
				case Keys.D:
					player.speedX= Gdx.input.isKeyPressed(Keys.RIGHT)?1:0-(Gdx.input.isKeyPressed(Keys.LEFT)?1:0)+(Gdx.input.isKeyPressed(Keys.D)?1:0)-(Gdx.input.isKeyPressed(Keys.A)?1:0);
					player.walk();
					break;
				case Keys.UP:
				case Keys.DOWN:
				case Keys.W:
				case Keys.S:
					player.stopClimbing();
					break;
			}
			return super.keyUp(keycode);
		}
		
		@Override
		public boolean touchDown(int screenX, int screenY, int pointer,
				int button) {		
			
			if(Gdx.input.getX()>viewport.x&&Gdx.input.getX()<viewport.x+viewport.width&&Gdx.input.getY()>viewport.y&&Gdx.input.getY()<viewport.y+viewport.height){
				Vector3 touchPos = new Vector3();
				touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
				stage.getCamera().unproject(touchPos,viewport.x,viewport.y,viewport.width,viewport.height);

				
				
				
				if(touchPos.x>stage.getCamera().position.x + stage.getWidth()/2 - 64){
					if(weaponbar.open){
						weaponbar.select(touchPos.y-stage.getCamera().position.y-stage.getHeight()/2);
						weaponbar.close();
					}else{
						weaponbar.open();
					}
					return false;
				}
				if(weaponbar.open){ // the user clicked somewhere else
					weaponbar.close();
				}
				
				
				
				System.out.println("touch: "+touchPos.x+","+touchPos.y);
				System.out.println("playa: "+player.getX()+","+player.getY());
				float SPAN_X=40;
				float SPAN_Y=40;
				
				switch (weapon){
					case SHOVEL:
						int digX=player.getStandingX();
						int digY=player.getMiddleY();
						
	
						if(touchPos.x<player.getX() - SPAN_X){
							digX=player.getStandingX()-1;
						}else if(touchPos.x>player.getX()+player.getWidth() + SPAN_X){
							digX=player.getStandingX()+1;
						}
						
						if(touchPos.y<player.getY() - SPAN_Y){
							digY=player.getStandingY();
						}else if(touchPos.y>player.getY()+player.getHeight() + SPAN_Y){
							digY=player.getMiddleY()+1;
						}
						
						
						digTile(digX,digY, 1);
						player.dig();
					break;
					case BOMBS:
						Bomb bomb =new Bomb(IngameScreen.this);
						bomb.setPosition(player.getX()+player.getWidth()/2-bomb.getWidth()/2, player.getY()+player.getHeight()/2-bomb.getHeight()/2);
						bomb.speedX = 0;
						bomb.speedY = 0;
						
						if(touchPos.x<player.getX() - SPAN_X){
							bomb.speedX=-2;
						}else if(touchPos.x>player.getX()+player.getWidth() + SPAN_X){
							bomb.speedX=2;
						}
						
						if(touchPos.y<player.getY() - SPAN_Y){
							bomb.speedY=-3;
						}else if(touchPos.y>player.getY()+player.getHeight() + SPAN_Y){
							bomb.speedY=3;
						}
								
						stage.addActor(bomb);
						break;
					case SWORD:
						player.sword(touchPos.x<player.getX()+player.getWidth()/2);
						break;
					case BOW:
						player.bow(touchPos.x<player.getX()+player.getWidth()/2);
						
						// arrow
						float xdiff = touchPos.x - player.getX()+player.getHeight()/2;
						float ydiff =touchPos.y - player.getY()+player.getHeight()/2;
						
						float degree = MathUtils.atan2(ydiff, xdiff);
						float dist=(float) Math.sqrt(xdiff*xdiff + ydiff*ydiff);
						
						Arrow arrow= new Arrow(IngameScreen.this);
						arrow.setPosition(player.getX()+player.getOriginX()-arrow.getWidth()/2, player.getY()+player.getHeight()/2 -arrow.getHeight()/2);
						float ARROW_SPEED=Math.min(dist/32,5);
						arrow.speedX = MathUtils.cos(degree)*ARROW_SPEED;
						arrow.speedY = MathUtils.sin(degree)*ARROW_SPEED;
						stage.addActor(arrow);
						break;
				
				}
				
			}
			return false;
		}
	}
	
	public void digTile(int x,int y, int power){
		// TODO ask tile if diggable
		Cell dst=destLayer.getCell(x, y);
		if(dst != null){
			TiledMapTile tile = dst.getTile();
			if(tile != null){
				System.err.println("tile: "+tile.getId());
				
			/*	Iterator<String> it =tile.getProperties().getKeys();
				
				while(it.hasNext()){
					System.out.println(it.next());
				}*/
				if(destroyable[tile.getId()]>power){
					// dah. we don't have enough power
					return;
				}
				
				/*if(tile.getProperties().get("dst")!=null && tile.getProperties().get("dst").equals("no")){
					return;
				}*/
			}
		}
		colLayer.setCell(x, y, null);
		destLayer.setCell(x, y, null);
	}
	
}
