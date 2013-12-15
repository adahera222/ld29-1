package de.bitowl.ld28;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.AtlasTmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;

public class LDGame extends Game {
	AssetManager assets;
	boolean assetsLoaded;
	boolean mapUsed;
	
	
	@Override
	public void create() {
		assets = new AssetManager();
		
		assets.setLoader(TiledMap.class, new AtlasTmxMapLoader(new InternalFileHandleResolver()));
		
		assets.load("maps/map1.tmx",TiledMap.class);
		assets.load("textures/textures.pack",TextureAtlas.class);
		assets.load("fonts/numbers.fnt",BitmapFont.class);
		assets.load("fonts/dialog.fnt",BitmapFont.class);
	}
	
	public void render() {
		if(assetsLoaded){
			super.render();
		}else{
			if(assets.update()){
				setScreen(new GameOverScreen(this));
				assetsLoaded=true;
				System.out.println("finished loading");
			}else{
				// TODO draw loading bar
			}
		}
	}
	@Override
	public void dispose() {
		super.dispose();
		assets.dispose();
	}
	
}
