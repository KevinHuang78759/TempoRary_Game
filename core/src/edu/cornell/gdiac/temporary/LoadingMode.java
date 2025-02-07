/*
 * LoadingMode.java
 *
 * Asset loading is a really tricky problem.  If you have a lot of sound or images,
 * it can take a long time to decompress them and load them into memory.  If you just
 * have code at the start to load all your assets, your game will look like it is hung
 * at the start.
 *
 * The alternative is asynchronous asset loading.  In asynchronous loading, you load a
 * little bit of the assets at a time, but still animate the game while you are loading.
 * This way the player knows the game is not hung, even though he or she cannot do 
 * anything until loading is complete. You know those loading screens with the inane tips 
 * that want to be helpful?  That is asynchronous loading.  
 *
 * This player mode provides a basic loading screen.  While you could adapt it for
 * between level loading, it is currently designed for loading all assets at the 
 * start of the game.
 *
 * Author: Walker M. White
 * Based on original Optimization Lab by Don Holden, 2007
 * LibGDX version, 2/2/2015
 */
package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.*;
import edu.cornell.gdiac.assets.*;
import edu.cornell.gdiac.util.*;

/**
 * Class that provides a loading screen for the state of the game.
 *
 * You still DO NOT need to understand this class for this lab.  We will talk about this
 * class much later in the course.  This class provides a basic template for a loading
 * screen to be used at the start of the game or between levels.  Feel free to adopt
 * this to your needs.
 *
 * You will note that this mode has some textures that are not loaded by the AssetManager.
 * You are never required to load through the AssetManager.  But doing this will block
 * the application.  That is why we try to have as few resources as possible for this
 * loading screen.
 */
public class LoadingMode implements Screen {
	// There are TWO asset managers.  One to load the loading screen.  The other to load the assets
	/** Internal assets for this loading screen */
	private AssetDirectory internal;
	/** The actual assets to be loaded */
	private AssetDirectory assets;

	private FilmStrip graciasCat;

	/** Background texture for start-up */
	private Texture background;

	// statusBar is a "texture atlas." Break it up into parts.
	/** Left cap to the status background (grey region) */
	private TextureRegion statusBkgLeft;
	/** Middle portion of the status background (grey region) */
	private TextureRegion statusBkgMiddle;
	/** Right cap to the status background (grey region) */
	private TextureRegion statusBkgRight;
	/** Left cap to the status forground (colored region) */
	private TextureRegion statusFrgLeft;
	/** Middle portion of the status forground (colored region) */
	private TextureRegion statusFrgMiddle;
	/** Right cap to the status forground (colored region) */
	private TextureRegion statusFrgRight;

	/** Texture for "Loading..." */
	private Texture loadingText;

	/** Default budget for asset loader (do nothing but load 60 fps) */
	private static int DEFAULT_BUDGET = 15;
	/** Standard window size (for scaling) */
	private static int STANDARD_WIDTH  = 1200;
	/** Standard window height (for scaling) */
	private static int STANDARD_HEIGHT = 800;
	/** Ratio of the bar width to the screen */
	private static float BAR_WIDTH_RATIO  = 0.3f;
	/** Ratio of the bar height to the screen */
	private static float BAR_HEIGHT_RATIO = 0.1f;
	/** Height of the progress bar */
	private static int PROGRESS_HEIGHT = 93;
	/** Height of the inner progress bar */
	private static int INNER_PROGRESS_HEIGHT = 77;
	/** Width of the rounded cap on left or right */
	private static int PROGRESS_CAP    = 42;
	/** Width of the rounded cap on the left or right for the inner bar */
	private static int INNER_PROGRESS_CAP    = 33;

	/** Reference to GameCanvas created by the root */
	private GameCanvas canvas;
	/** Listener that will update the player mode when we are done */
	private ScreenListener listener;

	/** The width of the progress bar */
	private int width;
	/** The y-coordinate of the center of the progress bar */
	private int centerY;
	/** The x-coordinate of the center of the progress bar */
	private int centerX;
	/** Scaling factor for when the student changes the resolution. */
	private float scale;

	private final int numAnimations = 50;
	/** Current progress (0 to 1) of the asset manager */
	private float progress;
	/** The amount of time to devote to loading assets (as opposed to on screen hints, etc.) */
	private int   budget;
	/** Whether or not this player mode is still active */
	private boolean active;

	private int innerWidth;

	/**
	 * Returns the budget for the asset loader.
	 *
	 * The budget is the number of milliseconds to spend loading assets each animation
	 * frame.  This allows you to do something other than load assets.  An animation 
	 * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to 
	 * do something else.  This is how game companies animate their loading screens.
	 *
	 * @return the budget in milliseconds
	 */
	public int getBudget() {
		return budget;
	}

	/**
	 * Sets the budget for the asset loader.
	 *
	 * The budget is the number of milliseconds to spend loading assets each animation
	 * frame.  This allows you to do something other than load assets.  An animation 
	 * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to 
	 * do something else.  This is how game companies animate their loading screens.
	 *
	 * @param millis the budget in milliseconds
	 */
	public void setBudget(int millis) {
		budget = millis;
	}
	
	/**
	 * Returns true if all assets are loaded and the player is ready to go.
	 *
	 * @return true if the player is ready to go
	 */
	public boolean isReady() {
		return progress >= 1.0f;
	}

	/**
	 * Returns the asset directory produced by this loading screen
	 *
	 * This asset loader is NOT owned by this loading scene, so it persists even
	 * after the scene is disposed.  It is your responsbility to unload the
	 * assets in this directory.
	 *
	 * @return the asset directory produced by this loading screen
	 */
	public AssetDirectory getAssets() {
		return assets;
	}

	/**
	 * Creates a LoadingMode with the default budget, size and position.
	 *
	 * @param file  	The asset directory to load in the background
	 * @param canvas 	The game canvas to draw to
	 */
	public LoadingMode(String file, GameCanvas canvas) {
		this(file, canvas, DEFAULT_BUDGET);
	}

	/**
	 * Creates a LoadingMode with the default size and position.
	 *
	 * The budget is the number of milliseconds to spend loading assets each animation
	 * frame.  This allows you to do something other than load assets.  An animation 
	 * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to 
	 * do something else.  This is how game companies animate their loading screens.
	 *
	 * @param file  	The asset directory to load in the background
	 * @param canvas 	The game canvas to draw to
	 * @param millis The loading budget in milliseconds
	 */
	public LoadingMode(String file, GameCanvas canvas, int millis) {
		this.canvas  = canvas;
		budget = millis;
		
		// Compute the dimensions from the canvas
		resize(canvas.getWidth(),canvas.getHeight());

		// We need these files loaded immediately
		internal = new AssetDirectory( "loading.json" );
		internal.loadAssets();
		internal.finishLoading();

		background = internal.getEntry( "background", Texture.class );
		background.setFilter( TextureFilter.Linear, TextureFilter.Linear );
		graciasCat = new FilmStrip(internal.getEntry("cat", Texture.class), 1, 10, 10);
		loadingText = internal.getEntry("loading", Texture.class);

		// Break up the status bar texture into regions
		statusBkgLeft = internal.getEntry( "progress.backleft", TextureRegion.class );
		statusBkgRight = internal.getEntry( "progress.backright", TextureRegion.class );
		statusBkgMiddle = internal.getEntry( "progress.background", TextureRegion.class );

		statusFrgLeft = internal.getEntry( "progress.foreleft", TextureRegion.class );
		statusFrgRight = internal.getEntry( "progress.foreright", TextureRegion.class );
		statusFrgMiddle = internal.getEntry( "progress.foreground", TextureRegion.class );

		// No progress so far.
		progress = 0;

		// Start loading the real assets
		assets = new AssetDirectory( file );
		assets.loadAssets();
		active = true;
	}

	/**
	 * Called when this screen should release all resources.
	 */
	public void dispose() {
		internal.unloadAssets();
		internal.dispose();
	}
	
	/**
	 * Update the status of this player mode.
	 *
	 * We prefer to separate update and draw from one another as separate methods, instead
	 * of using the single render() method that LibGDX does.  We will talk about why we
	 * prefer this in lecture.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	private void update(float delta) {
		assets.update(budget);
		this.progress = assets.getProgress();
		graciasCat.setFrame((int)(graciasCat.getSize() * (progress % (1f/numAnimations))/(1f/numAnimations)));
	}

	/**
	 * Draw the status of this player mode.
	 *
	 * We prefer to separate update and draw from one another as separate methods, instead
	 * of using the single render() method that LibGDX does.  We will talk about why we
	 * prefer this in lecture.
	 */
	private void draw() {
		canvas.begin();
		canvas.drawBackground(background, 0, 0);
		canvas.draw(loadingText, Color.WHITE, loadingText.getWidth()/2, loadingText.getHeight()/2, 4*canvas.getWidth()/5, 0.23f * canvas.getHeight(), 0, 1.5f * scale, 1.5f * scale);
		drawProgress(canvas);
		float catScale = Math.min((float)canvas.getWidth()/graciasCat.getRegionWidth(),(float)canvas.getHeight()/graciasCat.getRegionHeight());
		canvas.draw(graciasCat, Color.WHITE, graciasCat.getRegionWidth()/2f, graciasCat.getRegionHeight()/2f, 4*canvas.getWidth()/5f, canvas.getHeight()/2f, 0, catScale/3f, catScale/3f);
		canvas.end();
	}
	
	/**
	 * Updates the progress bar according to loading progress
	 *
	 * The progress bar is composed of parts: two rounded caps on the end, 
	 * and a rectangle in a middle.  We adjust the size of the rectangle in
	 * the middle to represent the amount of progress.
	 *
	 * @param canvas The drawing context
	 */	
	private void drawProgress(GameCanvas canvas) {	
		canvas.draw(statusBkgLeft,   centerX-width/2, centerY, scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);
		canvas.draw(statusBkgRight,  centerX+width/2-scale*PROGRESS_CAP, centerY, scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);
		canvas.draw(statusBkgMiddle, centerX-width/2+scale*PROGRESS_CAP, centerY, width-2*scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);

		float padding = (width - innerWidth) / 2f;
		canvas.draw(statusFrgLeft,   centerX-width/2+padding, centerY +(PROGRESS_HEIGHT - INNER_PROGRESS_HEIGHT)/4f+1, scale*INNER_PROGRESS_CAP, scale*INNER_PROGRESS_HEIGHT);
		if (progress > 0) {
			float span = progress*((innerWidth)-2*scale*INNER_PROGRESS_CAP);
			canvas.draw(statusFrgRight,  centerX-innerWidth/2+scale*INNER_PROGRESS_CAP+span, centerY+(PROGRESS_HEIGHT - INNER_PROGRESS_HEIGHT)/4f+1, scale*INNER_PROGRESS_CAP, scale*INNER_PROGRESS_HEIGHT);
			canvas.draw(statusFrgMiddle, centerX-innerWidth/2+scale*INNER_PROGRESS_CAP, centerY+(PROGRESS_HEIGHT - INNER_PROGRESS_HEIGHT)/4f+1, span, scale*INNER_PROGRESS_HEIGHT);
		} else {
			canvas.draw(statusFrgRight,  centerX-innerWidth/2+scale*INNER_PROGRESS_CAP+padding, centerY+(PROGRESS_HEIGHT - INNER_PROGRESS_HEIGHT)/4f+1, scale*INNER_PROGRESS_CAP, scale*INNER_PROGRESS_HEIGHT);
		}
	}

	// ADDITIONAL SCREEN METHODS
	/**
	 * Called when the Screen should render itself.
	 *
	 * We defer to the other methods update() and draw().  However, it is VERY important
	 * that we only quit AFTER a draw.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void render(float delta) {
		if (active) {
			update(delta);
			draw();

			// We are ready, notify our listener
			if (isReady() && listener != null) {
				listener.exitScreen(this, ExitCode.TO_MENU);
			}
		}
	}

	/**
	 * Called when the Screen is resized. 
	 *
	 * This can happen at any point during a non-paused state but will never happen 
	 * before a call to show().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		// Compute the drawing scale
		float sx = ((float)width)/STANDARD_WIDTH;
		float sy = ((float)height)/STANDARD_HEIGHT;
		scale = (Math.min(sx, sy)) * 0.6f;
		
		this.width = (int)(BAR_WIDTH_RATIO*width);
		innerWidth = (int)(BAR_WIDTH_RATIO*0.95*width);
		centerY = (int)(BAR_HEIGHT_RATIO*height);
		centerX = 4*width/5;
	}

	/**
	 * Called when the Screen is paused.
	 * 
	 * This is usually when it's not active or visible on screen. An Application is 
	 * also paused before it is destroyed.
	 */
	public void pause() {
		// TODO Auto-generated method stub
	}

	/**
	 * Called when the Screen is resumed from a paused state.
	 *
	 * This is usually when it regains focus.
	 */
	public void resume() {
		// TODO Auto-generated method stub
	}
	
	/**
	 * Called when this screen becomes the current screen for a Game.
	 */
	public void show() {
		// Useless if called in outside animation loop
		active = true;
	}

	/**
	 * Called when this screen is no longer the current screen for a Game.
	 */
	public void hide() {
		// Useless if called in outside animation loop
		active = false;
	}
	
	/**
	 * Sets the ScreenListener for this mode
	 *
	 * The ScreenListener will respond to requests to quit.
	 */
	public void setScreenListener(ScreenListener listener) {
		this.listener = listener;
	}
}