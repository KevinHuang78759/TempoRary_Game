/*
 * InputController.java
 *
 * This class buffers in input from the devices and converts it into its
 * semantic meaning. If your game had an option that allows the player to
 * remap the control keys, you would store this information in this class.
 * That way, the main GameEngine does not have to keep track of the current
 * key mapping.
 *
 * This class is a singleton for this application, but we have not designed
 * it as one.  That is to give you some extra functionality should you want
 * to add multiple ships.
 *
 * Author: Walker M. White
 * Based on original Optimization Lab by Don Holden, 2007
 * LibGDX version, 2/2/2015
 */
package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.*;
import edu.cornell.gdiac.util.*;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

/**
 * Class for reading player input. 
 *
 * This supports both a keyboard and X-Box controller. In previous solutions, we only 
 * detected the X-Box controller on start-up.  This class allows us to hot-swap in
 * a controller via the new XBox360Controller class.
 */
public class InputController {
	// Fields to manage game state
	/** Whether the reset button was pressed. */
	protected boolean resetPressed;
	/** Whether the exit button was pressed. */
	protected boolean exitPressed;
	/** XBox Controller support */
	private XBoxController xbox;

	private Processor processor = new Processor();
	public float mouseX;
	public float mouseY;

	public float mouseMoveX;

	public boolean mouseMoved;

	public float mouseMoveY;
	public boolean mouseClicked;

	/**
	 * Returns true if the reset button was pressed.
	 *
	 * @return true if the reset button was pressed.
	 */
	public boolean didReset() {
		return resetPressed;
	}

	/**
	 * Returns true if the exit button was pressed.
	 *
	 * @return true if the exit button was pressed.
	 */
	public boolean didExit() { return exitPressed; }
	
	/**
	 * Creates a new input controller
	 * 
	 * The input controller attempts to connect to the X-Box controller at device 0,
	 * if it exists.  Otherwise, it falls back to the keyboard control.
	 */
	public InputController(int lanes, int lpl) {
		// If we have a game-pad for id, then use it.
		Array<XBoxController> controllers = Controllers.get().getXBoxControllers();
		if (controllers.size > 0) {
			xbox = controllers.get(0);
		} else {
			xbox = null;
		}
		clicking = false;
		triggerLast = new boolean[lpl];
		switchesLast = new boolean[lanes];
		triggers = new boolean[lpl];
		switches = new boolean[lanes];
		triggerLifted = new boolean[lpl];
	}

	/**
	 * Reads the input for the player and converts the result into game logic.
	 */
	public void readInput() {
		// Check to see if a GamePad is connected
		if (xbox != null && xbox.isConnected()) {
			readGamepad();
			readKeyboard(true); // Read as a back-up
		} else {
			readKeyboard(false);
		}
	}

	private boolean clicking;

	public class Processor implements InputProcessor {

		public boolean keyDown (int keycode){
			return false;
		}

		public boolean keyUp (int keycode){
			return false;
		}

		public boolean keyTyped (char character){
			return false;
		}

		public boolean touchDown (int x, int y, int pointer, int button){
			mouseClicked = true;
			mouseX = x;
			mouseY = y;
			return false;
		}

		public boolean touchUp (int x, int y, int pointer, int button){
			clicking = false;
			mouseClicked = false;
			mouseX = x;
			mouseY = y;
			return false;
		}

		public boolean touchDragged (int x, int y, int pointer){
			return false;
		}

		public boolean mouseMoved (int x, int y){
			mouseMoveX = x;
			mouseMoveY = y;
			mouseMoved = true;
			return false;
		}

		public boolean scrolled (float amountX, float amountY){
			return false;
		}
	}

	/**
	 * Reads input from an X-Box controller connected to this computer.
	 */
	private void readGamepad() {
		// TODO: Map gamepad inputs
		resetPressed = xbox.getA();
		exitPressed  = xbox.getBack();
	}


	/**
	 * Reads input from the keyboard.
	 *
	 * This controller reads from the keyboard regardless of whether or not an X-Box
	 * controller is connected.  However, if a controller is connected, this method
	 * gives priority to the X-Box controller.
	 *
	 * @param secondary true if the keyboard should give priority to a gamepad
	 */

	//Arrays to registering switch and trigger presses
	//We need to track their previous values so that we dont register a hold as repeated clicks
	private boolean[] triggers;
	private boolean[] triggerLast;
	private boolean[] switches;
	private boolean[] switchesLast;
	private boolean[] moves;
	private boolean erased;
	private boolean erasedPress;
	private boolean erasedLast;

	private boolean undid;
	private boolean undidPress;
	private boolean undidLast;

	private boolean redid;
	private boolean redidPress;
	private boolean redidLast;

	private boolean play;
	private boolean playPress;
	private boolean playLast;

	private boolean track;
	private boolean trackPress;
	private boolean trackLast;
	boolean[] triggerPress;
	boolean[] triggerLifted;
	/**
	 * Key for random indicator  on reset
	 */
	boolean rKey = false;
	private void readKeyboard(boolean secondary) {
		// Give priority to gamepad results, get input from keyboard
		resetPressed = (secondary && resetPressed) || (Gdx.input.isKeyPressed(Input.Keys.ENTER));
		exitPressed = (secondary && exitPressed) || (Gdx.input.isKeyPressed(Input.Keys.ESCAPE));
		rKey = resetPressed && Gdx.input.isKeyPressed(Input.Keys.H);

		triggerPress = new boolean[]{
				Gdx.input.isKeyPressed(Input.Keys.D),
				Gdx.input.isKeyPressed(Input.Keys.F),
				Gdx.input.isKeyPressed(Input.Keys.J),
				Gdx.input.isKeyPressed(Input.Keys.K)
		};

		boolean[] switchesPress = new boolean[]{
				Gdx.input.isKeyPressed(Input.Keys.E),
				Gdx.input.isKeyPressed(Input.Keys.R),
				Gdx.input.isKeyPressed(Input.Keys.U),
				Gdx.input.isKeyPressed(Input.Keys.I),
		};

		moves = new boolean[]{
				Gdx.input.isKeyPressed(Input.Keys.UP),
				Gdx.input.isKeyPressed(Input.Keys.DOWN),
				Gdx.input.isKeyPressed(Input.Keys.LEFT),
				Gdx.input.isKeyPressed(Input.Keys.RIGHT)
		};

		erasedPress = Gdx.input.isKeyPressed(Input.Keys.E);

		undidPress = Gdx.input.isKeyPressed(Input.Keys.U);

		redidPress = Gdx.input.isKeyPressed(Input.Keys.R);

		playPress = Gdx.input.isKeyPressed(Input.Keys.SPACE);

		trackPress = Gdx.input.isKeyPressed(Input.Keys.P) ;

		//Compute actual values by comparing with previous value. We only register a click if the trigger or switch
		// went from false to true. We only register a lift if the trigger went from true to false.
		for(int i = 0; i < Math.max(switchesPress.length, triggerPress.length); ++i){
			if(i < triggers.length){
				triggers[i] = !triggerLast[i] && triggerPress[i];
				triggerLifted[i] = triggerLast[i] && !triggerPress[i];
				triggerLast[i] = triggerPress[i];
			}
			if(i < switches.length){
				switches[i] = !switchesLast[i] && switchesPress[i];
				switchesLast[i] = switchesPress[i];
			}
		}

		erased = !erasedLast && erasedPress;
		erasedLast = erasedPress;

		undid = !undidLast && undidPress;
		undidLast = undidPress;

		redid = !redidLast && redidPress;
		redidLast = redidPress;

		play = !playLast && playPress;
		playLast = playPress;

		track = !trackLast && trackPress;
		trackLast = trackPress;
	}

	/*
	 * Returns an array that represents the if left switch or right switch key are being pressed
	 */
	public boolean[] switches(){
		return switches;
	}

	public boolean[] didTrigger(){
		return triggers;
	}

	public boolean[] getMoves(){
		return moves;
	}

	public boolean didClick() {
		if (!clicking && mouseClicked) {
			clicking = true;
			return true;
		}
		return false;
	}

	public float getMouseX() { return Gdx.input.getX(); }

	public float getMouseY() { return Gdx.input.getY(); }


	public boolean didMove() { return mouseMoved;}

	public boolean didErase() {return erased;}

	public boolean didSetQuarter() {return Gdx.input.isKeyPressed(Input.Keys.Q);}
	public boolean didSetThird() {return Gdx.input.isKeyPressed(Input.Keys.T);}
	public boolean didSetFree() {return Gdx.input.isKeyPressed(Input.Keys.F);}

	public boolean didPrecision1() {return Gdx.input.isKeyPressed(Input.Keys.NUM_1);}
	public boolean didPrecision2() {return Gdx.input.isKeyPressed(Input.Keys.NUM_2);}
	public boolean didPrecision3() {return Gdx.input.isKeyPressed(Input.Keys.NUM_3);}

	public boolean setSwitchNotes() {return Gdx.input.isKeyPressed(Input.Keys.S);}
	public boolean setBeatNotes() {return Gdx.input.isKeyPressed(Input.Keys.B);}
	public boolean setHeldNotes() {return Gdx.input.isKeyPressed(Input.Keys.H);}

	public boolean didPressPlay() {return play;}

	public boolean didPressTrack() {return track;}

	public boolean didResetSong() {return Gdx.input.isKeyPressed(Input.Keys.R);}

	public boolean didSpeedUp() {return Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);}

	public boolean didUndo() {return undid;}

	public boolean didRedo() {return redid;}

	public void setEditorProcessor() {
		Gdx.input.setInputProcessor(processor);
	}

}
