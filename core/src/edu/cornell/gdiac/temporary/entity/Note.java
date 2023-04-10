/*
 * Note.java
 *
 *
 * This separation is very important for this class because it has a lot
 * of interactions with other classes.  When a note is hit, it emits stars.
 */
package edu.cornell.gdiac.temporary.entity;

import edu.cornell.gdiac.temporary.*;
import edu.cornell.gdiac.util.*;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;

/**
 * Model class for Notes.
 */
public class Note{
	public static final float descentSpeed = -4.5f;
	/** Rescale the size of a shell */
	private static final float SHELL_SIZE_MULTIPLE = 4.0f;
	/** How fast we change frames (one frame per 4 calls to update) */
	private static final float ANIMATION_SPEED = 0.25f;
	/** The number of animation frames in our filmstrip */
	private static final int   NUM_ANIM_FRAMES = 4;
	/** Current animation frame for this shell */
	private float animeframe;

	private int hitStatus;

	public int getHitStatus(){
		return hitStatus;
	}
	public void setHitStatus(int t){
		hitStatus = t;
	}

	/** line the note is one */
	private int line;
	public int getLine(){
		return line;
	}
	public void setLine(int t){
		line = t;
	}
	private int startFrame;
	public int getStartFrame(){
		return startFrame;
	}
	public void setStartFrame(int t){
		startFrame = t;
	}
	private int holdFrame;

	public int getHoldFrames(){
		return holdFrame;
	}
	public void setHoldFrames(int t){
		holdFrame = t;
	}

	public enum NoteType {
		SWITCH,
		HELD,
		BEAT
	}

	private NoteType nt;

	public NoteType getNoteType(){
		return nt;
	}
	public void setNoteType(NoteType t){
		nt = t;
	}
	private float w;

	public float getWidth(){
		return w;
	}
	public void setWidth(float t){
		w = t;
	}
	private float h;

	public float getHeight(){
		return h;
	}
	public void setHeight(float t){
		h = t;
	}
	private float x;

	public float getX(){
		return x;
	}
	public void setX(float t){
		x = t;
	}
	private float y;
	public float getY(){
		return y;
	}
	public void setY(float t){
		y = t;
	}
	private float vy;
	public float getYVel(){
		return vy;
	}
	public void setYVel(float t){
		vy = t;
	}
	private float by;
	public float getBottomY(){
		return by;
	}
	public void setBottomY(float y){
		by = y;
	}
	private boolean destroyed;
	public boolean isDestroyed(){
		return destroyed;
	}
	public void setDestroyed(boolean d){
		destroyed = d;
	}
	FilmStrip animator;
	Vector2 origin;

	/**
	 * Initialize shell with trivial starting position.
	 */
	public Note(int line, NoteType n, int frame, Texture t) {
		// Set minimum Y velocity for this shell
		this.line = line;
		hitStatus = 0;
		animeframe = 0.0f;
		nt = n;
		vy = n == NoteType.HELD? 0f : descentSpeed;
		startFrame = frame;
		animator = new FilmStrip(t,1,NUM_ANIM_FRAMES,NUM_ANIM_FRAMES);
		origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f);
		h = animator.getRegionHeight();
		w = animator.getRegionWidth();
	}

	public void setTexture(Texture texture) {
		animator = new FilmStrip(texture,1,NUM_ANIM_FRAMES,NUM_ANIM_FRAMES);
		origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f);
		h = animator.getRegionHeight();
		w = animator.getRegionWidth();
	}


	public void update(int frame) {

		if(nt == NoteType.HELD){
			by += descentSpeed;
			if(frame == (startFrame + holdFrame)){
				vy = descentSpeed;
			}

		}
		y += vy;
		// Increase animation frame
		animeframe += ANIMATION_SPEED;
		if (animeframe >= NUM_ANIM_FRAMES) {
			animeframe -= NUM_ANIM_FRAMES;
		}
	}

	private float tail_thickness = 5f;

	public float getTail_thickness(){
		return tail_thickness;
	}
	public void setTail_thickness(float t){
		tail_thickness = t;
	}
	/**
	 * Draws this shell to the canvas
	 *
	 * There is only one drawing pass in this application, so you can draw the objects
	 * in any order.
	 *
	 * @param canvas The drawing context
	 */
	public void draw(GameCanvas canvas, float widthConfine, float heightConfine) {
		if(nt == NoteType.HELD){
			canvas.drawRect(x - tail_thickness/2, by, x + tail_thickness/2, y, Color.BLUE, true);

			animator.setFrame(0);
			canvas.draw(animator, Color.WHITE, origin.x, origin.y, x, by,
					0.0f, widthConfine/w, heightConfine/h);
		}
		else{
			animator.setFrame((int)animeframe);
			canvas.draw(animator, Color.WHITE, origin.x, origin.y, x, y,
					0.0f, widthConfine/w, heightConfine/h);
		}

	}

}