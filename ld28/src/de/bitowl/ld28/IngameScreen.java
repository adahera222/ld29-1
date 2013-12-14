package de.bitowl.ld28;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Keys;
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
import com.badlogic.gdx.scenes.scene2d.Group;

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
	
	Group enemies;
	
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
		
		
		player = new Player(this);
		player.setY(colLayer.getHeight()*colLayer.getTileHeight()-player.getHeight());
		stage.addActor(player);
		
			
		// handle input
		Gdx.input.setInputProcessor(new GameInputProcessor());
		
		
		
		
		// have the tiles properties?
		TiledMapTileSet tileset=map.getTileSets().getTileSet("tileset");
		
		
		int tssize=0;
		Iterator<TiledMapTile> it = map.getTileSets().getTileSet("tileset").iterator();
		while(it.hasNext()){ // count how many tiles there are
			TiledMapTile tile =it.next();
			if(tile.getId()>tssize){
				tssize=tile.getId();
			}
			
		}
		destroyable = new int[tssize+1];
		it = map.getTileSets().getTileSet("tileset").iterator();
		
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
					Enemy enemy = new Enemy(this);
					enemy.setPosition(x*enemyLay.getTileWidth(), y*enemyLay.getTileHeight());
					enemies.addActor(enemy);
				}
			}
		}
		
		stage.addActor(enemies);

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
					player.speedX= Gdx.input.isKeyPressed(Keys.RIGHT)?1:0-(Gdx.input.isKeyPressed(Keys.LEFT)?1:0);
					player.walk();
					break;
				case Keys.UP:
				case Keys.SPACE:
					player.jump();
					break;
				case Keys.S: // dig down
					digTile(player.getStandingX(),player.getStandingY(), 1);
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
				/*	// Boombs
					int deployX=player.getStandingX();
					int deployY=player.getStandingY();
					
					for(int y = deployY - 2;y<=deployY+2;y++){
						for(int x = deployX-2;x<deployX+3;x++){
							digTile(x,y);
						}
					}*/
					break;
				case Keys.E:
					// swing your sword, mighty hero!
					player.sword();
					break;
			}
			
			return super.keyDown(keycode);
		}
		@Override
		public boolean keyUp(int keycode) {
			switch(keycode){
				case Keys.LEFT:
				case Keys.RIGHT:
					player.speedX= Gdx.input.isKeyPressed(Keys.RIGHT)?1:0-(Gdx.input.isKeyPressed(Keys.LEFT)?1:0);
					player.walk();
					break;
			}
			return super.keyUp(keycode);
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
