package de.bitowl.ld28.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import de.bitowl.ld28.LDGame;
import de.bitowl.ld28.StageInputProcessor;

public class MenuScreen extends AbstractScreen{

	TextureAtlas atlas;
	public MenuScreen(LDGame pGame) {
		super(pGame);
		atlas=game.assets.get("textures/textures.pack",TextureAtlas.class);
		
		Table table = new Table();
		table.setSize(stage.getWidth(), stage.getHeight());
		
		Image title = new Image(atlas.findRegion("title_main"));
		table.add(title).pad(30).row();
		
		Image start = new Image(atlas.findRegion("button_start"));
		start.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(game.mapUsed){
					IngameScreen.resetMap(game);
				}
				game.setScreen(new IngameScreen(game));
			}
		});
		table.add(start).pad(30).row();
		
		Image credits = new Image(atlas.findRegion("button_credits"));
		credits.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				game.setScreen(new CreditsScreen(game));
			}
		});
		table.add(credits).pad(10).row();
		
		Image quit = new Image(atlas.findRegion("button_quit"));
		quit.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Gdx.app.exit();
			}
		});
		table.add(quit).pad(30).row();
		
		stage.addActor(table);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		super.render(delta);
	}
	@Override
	public void show() {
		Gdx.input.setInputProcessor(new StageInputProcessor(this));
	}
}
