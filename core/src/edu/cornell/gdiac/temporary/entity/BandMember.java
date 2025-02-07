package edu.cornell.gdiac.temporary.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.temporary.*;
import edu.cornell.gdiac.util.FilmStrip;

import java.util.PriorityQueue;


public class BandMember {

    FilmStrip DF;
    FilmStrip JK;
    FilmStrip ACTIVE_IDLE;
    FilmStrip MISS;
    FilmStrip INACTIVE_NOTES;
    FilmStrip INACTIVE_NO_NOTES;
    FilmStrip INACTIVE_LOW;

    public void setLoomFilmStrip(FilmStrip f){
        ghostLoomSprite = f;
    }

    public void setFilmStrips(FilmStrip[] f){
        INACTIVE_NOTES = f[0];
        INACTIVE_NO_NOTES = f[1];
        INACTIVE_LOW = f[2];
        DF = f[3];
        JK = f[4];
        ACTIVE_IDLE = f[5];
        MISS = f[6];
    }
    boolean done;
    float sample;
    float samplesPerBeat;
    float start;
    float end;
    private int mode;
    public void setMode(int k){
        assert  k == -1 || k ==1;
        mode = k;
    }

    private boolean gameOver;
    public void setGameOver(boolean value){
        gameOver = value;
    }
    private boolean DFprev;
    private boolean JKprev;

    boolean held;
    public boolean hasHold(){
        return held;
    }

    public void setSPB(float s){
        samplesPerBeat = s;
    }


    private enum ACTIVE_STATE{
        IDLE,MISS,DF,JK
    }

    ACTIVE_STATE AS;
    public void changeMode(){
        mode *= -1;
    }


    /**
     * Always recieve sample first before handling interrupts
     * @param s
     */
    public void recieveSample(float s){
        sample = s;
    }


    private void beginProcess(ACTIVE_STATE  nextState, float s, float e){
        start = s;
        end = e;
        done = false;
        AS = nextState;
    }
    /**
     * Respond to flags
     * @param df
     * @param jk
     * @param miss
     */
    public void recieveFlags(boolean df, boolean jk, boolean miss){
        if(AS == ACTIVE_STATE.IDLE){
            if(miss){
                beginProcess(ACTIVE_STATE.MISS, sample, sample + samplesPerBeat);
            }
            else if((!DFprev && df) && (!JKprev && jk)){
                beginProcess(RandomController.rollInt(0,1) > 0 ? ACTIVE_STATE.JK : ACTIVE_STATE.DF, sample, sample + samplesPerBeat);
            }
            else if(!DFprev && df){
                beginProcess(ACTIVE_STATE.DF, sample, sample + samplesPerBeat);
            }
            else if(!JKprev && jk){
                beginProcess(ACTIVE_STATE.JK, sample, sample + samplesPerBeat);
            }
        }
        else if(AS == ACTIVE_STATE.MISS){
            if(sample >= end){
                if(df && jk){
                    int x = RandomController.rollInt(0,1);
                    beginProcess(x > 0 ? ACTIVE_STATE.JK : ACTIVE_STATE.DF, sample, sample + samplesPerBeat);
                }
                else if(df){
                    beginProcess(ACTIVE_STATE.DF, sample, sample + samplesPerBeat);
                }
                else if(jk){
                    beginProcess(ACTIVE_STATE.JK, sample, sample + samplesPerBeat);
                }
                else{
                    AS = ACTIVE_STATE.IDLE;
                }
            }
        }
        else if(AS == ACTIVE_STATE.DF){
            if(miss){
                beginProcess(ACTIVE_STATE.MISS, sample, sample + samplesPerBeat);
            }
            else if(!JKprev && jk){
                beginProcess(ACTIVE_STATE.JK, sample, sample + samplesPerBeat);
            }
            else if(DFprev && !df){
                if(done){
                    if(jk){
                        beginProcess(ACTIVE_STATE.JK, sample, sample + samplesPerBeat);
                    }
                    else{
                        done = false;
                        AS = ACTIVE_STATE.IDLE;
                    }
                }
            }
            else if(!DFprev && df){
                beginProcess(ACTIVE_STATE.DF, sample, sample + samplesPerBeat);
            }
            else if(sample >= end){
                if(df){
                    beginProcess(ACTIVE_STATE.DF, sample, sample + samplesPerBeat);
                    done = true;
                }
                else if(jk){
                    beginProcess(ACTIVE_STATE.JK, sample, sample + samplesPerBeat);

                }
                else{
                    done = false;
                    AS = ACTIVE_STATE.IDLE;
                }
            }
        }
        else if(AS == ACTIVE_STATE.JK){
            if(miss){
                beginProcess(ACTIVE_STATE.MISS, sample, sample + samplesPerBeat);
            }
            else if(!DFprev && df){
                beginProcess(ACTIVE_STATE.DF, sample, sample + samplesPerBeat);
            }
            else if(JKprev && !jk){
                if(done){
                    if(df){
                        beginProcess(ACTIVE_STATE.DF, sample, sample + samplesPerBeat);
                    }
                    else{
                        done = false;
                        AS = ACTIVE_STATE.IDLE;
                    }
                }
            }
            else if(!JKprev && jk){
                beginProcess(ACTIVE_STATE.JK, sample, sample + samplesPerBeat);
            }
            else if(sample >= end){
                if(jk){
                    beginProcess(ACTIVE_STATE.JK, sample, sample + samplesPerBeat);
                    done = true;
                }
                else if(df){
                    beginProcess(ACTIVE_STATE.DF, sample, sample + samplesPerBeat);
                }
                else{
                    done = false;
                    AS = ACTIVE_STATE.IDLE;
                }
            }
        }
        held = (DFprev && df) || (JKprev && jk);
        DFprev = df;
        JKprev = jk;
    }

    public void pickFrame(){
        if(mode == 1 || gameOver){
            if(AS == ACTIVE_STATE.MISS || gameOver){
                characterSprite = MISS;
                int total = characterSprite.getSize();
                int cur = (int)(total*(( (sample - start) % samplesPerBeat)/samplesPerBeat));
                characterSprite.setFrame(cur);
            } else if(AS == ACTIVE_STATE.IDLE){
                characterSprite = ACTIVE_IDLE;
                int total = characterSprite.getSize();
                int cur = (int)(total*(sample % samplesPerBeat)/samplesPerBeat);
                characterSprite.setFrame(cur);
            }
            else if(AS == ACTIVE_STATE.JK){
                characterSprite = JK;
                int total = characterSprite.getSize();
                int cur = (int)(total*(( (sample - start) % samplesPerBeat)/samplesPerBeat));
                characterSprite.setFrame(cur);
            }
            else if(AS == ACTIVE_STATE.DF){
                characterSprite = DF;
                int total = characterSprite.getSize();
                int cur = (int)(total*(( (sample - start) % samplesPerBeat)/samplesPerBeat));
                characterSprite.setFrame(cur);
            }
        }
        else{
            if(hitNotes.isEmpty()){
                characterSprite = INACTIVE_NO_NOTES;
                int total = characterSprite.getSize();
                int cur = (int)(total*(sample % samplesPerBeat)/samplesPerBeat);
                characterSprite.setFrame(cur);
            }
            else{
                if(curComp/maxComp < 14f/47){
                    characterSprite = INACTIVE_LOW;
                    int total = characterSprite.getSize();
                    int cur = (int)(total*(sample % samplesPerBeat)/samplesPerBeat);
                    characterSprite.setFrame(cur);
                }
                else{
                    characterSprite = INACTIVE_NOTES;
                    int total = characterSprite.getSize();
                    int cur = (int)(total*(sample % samplesPerBeat)/samplesPerBeat);
                    characterSprite.setFrame(cur);
                }
            }
        }
    }

    private static final float NOTE_SIZE_SCALE = 0.7f;

    FilmStrip hpbar;
    /**
     * Number of frames in HP bar animation
     */
    int hpbarFrames;

    /**
     * Bottom left corner
     */
    private Vector2 bottomLeftCorner;

    /** Textures */
    private Texture noteIndicator;
    private Texture noteIndicatorHit;

    private Texture perfectHitIndicator;
    private Texture okHitIndicator;

    private Texture missIndicator;

    private FilmStrip characterSprite;

    private FilmStrip ghostLoomSprite;

    public void setBottomLeft(Vector2 V){
        bottomLeftCorner = V;
    }

    public Vector2 getBottomLeft(){
        return bottomLeftCorner;
    }

    /**
     * The height of separator lines from the top of the lane
     */
    private float lineHeight;

    private float laneTint;

    public void setLaneTint(float c){
        laneTint = c;
    }

    public void setLineHeight(float l){
        lineHeight = l;
    }

    public float getLineHeight(){
        return lineHeight;
    }
    /**
     * Width of the lane
     */
    private float width;
    public void setWidth(float l){
        width = l;
    }

    public float getWidth(){
        return width;
    }
    /**
     * Total height
     */
    private float height;
    public void setHeight(float l){
        height = l;
    }

    public float getHeight(){
        return height;
    }

    private float hitY;
    public void setHitY(float t){
        hitY = t;
    }
    public float getHitY(){
        return hitY;
    }

    /**
     * Number of lines this band member has
     */
    private final int numLines = 4;

    public int getNumLines(){
        return numLines;
    }

    /**
     * The color of the border
     */
    private Color borderColor = Color.BLACK;
    public void setBColor(Color l){
        borderColor = l;
    }

    public Color getBorderColor(){
        return borderColor;
    }
    /**
     * Active array of beat and held notes
     */
    private Array<Note> hitNotes;
    public void setHitNotes(Array<Note> l){
        hitNotes = l;
    }

    public Array<Note> getHitNotes(){
        return hitNotes;
    }
    /**
     * Active array of switch notes
     */
    private Array<Note> switchNotes;
    public void setSwitchNotes(Array<Note> l){
        switchNotes = l;
    }

    public boolean hasMoreNotes(){
        return !switchNotes.isEmpty() || !hitNotes.isEmpty() || !allNotes.isEmpty();
    }
    public Array<Note> getSwitchNotes(){
        return switchNotes;
    }
    /**
     * Queue to hold all the notes for this band member across the entire level
     */
    private Queue<Note> allNotes;

    public void setAllNotes(Queue<Note> l){
        allNotes = l;
    }

    public Queue<Note> getAllNotes(){
        return allNotes;
    }

    /**
     * backing array used for garbage collection
     */
    private Array<Note> backing;


    private int gainRate;
    private int lossRate;
    private PriorityQueue<Long[]> compData;
    public void setCompData(PriorityQueue<Long[]> compData) {
        this.compData = compData;
    }
    public PriorityQueue<Long[]> getCompData() {
        return compData;
    }
    public void setLossRate(int t){
        lossRate = t;
    }
    public int getLossRate(){
        return lossRate;
    }
    public void setGainRate(int t) { gainRate = t; }
    public int getGainRate() { return gainRate; }
    /**
     * Maximum competency
     */

    private float maxComp;

    public void setMaxComp(int p){
        maxComp = p;
    }
    public float getMaxComp(){
        return maxComp;
    }

    /**
     * Current competency
     */
    private float curComp;
    public void setCurComp(int p){
        curComp = p;
    }
    public float getCurComp(){
        return curComp;
    }

    /**
     * Constructor
     */
    public BandMember(){
        laneTint = 1f;
        bottomLeftCorner = new Vector2();
        hitNotes = new Array<>();
        switchNotes = new Array<>();
        allNotes = new Queue<>();
        compData = new PriorityQueue<>();
        backing = new Array<>();
        done = false;
        mode = -1;
        gameOver = false;
        AS = ACTIVE_STATE.IDLE;
        sample = 0f;
        DFprev = false;
        JKprev = false;

    }

    public void setHpBarFilmStrip(Texture t, int numFrames){
        hpbarFrames = numFrames;
        hpbar = new FilmStrip(t,1,hpbarFrames,hpbarFrames);
        hpbar.setFrame(0);
    }

    public void setStartingState(int comp, Queue<Note> notes){
        curComp = comp;
        maxComp = comp;
        allNotes = notes;
    }

    /**
     * Update animations
     */
    public void updateNotes(float spawnY, long currentSample){
        //Update both switchNotes and hit notes no matter what
        for(Note n : switchNotes){
            n.setY(spawnY + (float)(currentSample - n.getStartSample())/(n.getHitSample() - n.getStartSample()) *(hitY - spawnY));
            n.update();
        }
        for(Note n : hitNotes){
            if(n.getNoteType() == Note.NoteType.HELD){
                //Y coordinates based on formula mentioned in discord.
                float bottomYCalc = spawnY + (float)(currentSample - n.getStartSample())/(n.getHitSample() - n.getStartSample()) *(hitY - spawnY);
                if (!n.isHolding()) {
                    n.setBottomY(bottomYCalc);
                }
                if (n.isHolding() && n.getY() <= n.getBottomY()) {
                    n.setY(n.getBottomY());
                } else {
                    n.setHoldMiddleBottomY(bottomYCalc);
                    n.setY(spawnY + Math.max(0, (float) (currentSample - n.getStartSample() - n.getHoldSamples()) / (n.getHitSample() - n.getStartSample())) * (hitY - spawnY));
                }
            }
            else{
                n.setY(spawnY + (float)(currentSample - n.getStartSample())/(n.getHitSample() - n.getStartSample())*(hitY - spawnY));
            }
            n.update();
        }
    }

    /**
     * Add notes from the queue to the correct active array
     * @param currentSample
     */
    public void spawnNotes(long currentSample){
        //add everything at the front of the queue that's supposed to start on this frame
        while(!allNotes.isEmpty() && allNotes.first().getStartSample() <= currentSample){
            Note n = allNotes.removeFirst();
            if(n.getNoteType() == Note.NoteType.SWITCH){
                switchNotes.add(n);
            }
            else{
                hitNotes.add(n);
            }
        }
    }

    public void garbageCollect(){
        //Stop and copy both the switch and hit notes
        for(Note n : switchNotes){
            if(!n.isDestroyed()){
                backing.add(n);
            }
        }
        Array<Note> temp = backing;
        backing = switchNotes;
        switchNotes = temp;
        backing.clear();

        for(Note n : hitNotes){
            if(!n.isDestroyed()){
                backing.add(n);
            }
        }
        temp = backing;
        backing = hitNotes;
        hitNotes = temp;
        backing.clear();
    }

    /**
     * Update competency by the specified amount but will not go below 0 or exceed the max
     */
    public void compUpdate(float amount){
        curComp = Math.min(Math.max(0, curComp + amount), maxComp);
        hpbar.setFrame(Math.min((int)((1 - curComp/maxComp)*(hpbarFrames)), hpbarFrames - 1));
    }

    // DRAWING METHODS

    /**
     * Draw the indicator in a certain color according to if we triggered the line. Pass in an array for the active lane
     */
    public void drawIndicator(GameCanvas canvas, Texture noteIndicator, Texture noteIndicatorHit, boolean[] hits){
        //If we get passed an array we must draw 4 hit bars
        float scale = NOTE_SIZE_SCALE*(width/4)/noteIndicatorHit.getWidth();
        for(int i = 0; i < numLines; ++i){
            canvas.draw(hits[i] ? noteIndicatorHit : noteIndicator, Color.WHITE, noteIndicatorHit.getWidth() / 2, noteIndicatorHit.getHeight() / 2,
                    ((bottomLeftCorner.x + i * width/numLines) + (bottomLeftCorner.x +(i+1) * width/numLines)) / 2 - 5, hitY,
                    0.0f, scale, scale);
//            canvas.drawText(InputController.triggerKeyBinds()[i], displayFont, (bottomLeftCorner.x + i * width/numLines + bottomLeftCorner.x +(i+1) * width/numLines) / 2, hitY - 80);
        }
    }

    public void drawPawIndicator(GameCanvas canvas, Texture pawIndicator, int line) {
        float scale = Math.min(0.2f * height/pawIndicator.getHeight(), 0.85f* width/pawIndicator.getWidth());
        if (line >= 0) {
            canvas.draw(pawIndicator, Color.WHITE, pawIndicator.getWidth() / 2, pawIndicator.getHeight() / 2,
                    ((bottomLeftCorner.x + line * width / numLines) + (bottomLeftCorner.x + (line + 1) * width / numLines)) / 2 - 5, 0.65f*height + bottomLeftCorner.y,
                    0.0f, scale, scale);
        }
    }

    public void drawControlBox(GameCanvas canvas, Texture backgroundBox){
        if(mode == -1){
            float scale = Math.min(0.2f * height/backgroundBox.getHeight(), 0.85f* width/backgroundBox.getWidth());
            canvas.draw(backgroundBox, Color.WHITE, backgroundBox.getWidth()/2, backgroundBox.getHeight()/2, bottomLeftCorner.x + width/2f, 0.85f*height + bottomLeftCorner.y, 0f, scale, scale);
        }
        else{
            float scale = Math.min(0.15f * height/backgroundBox.getHeight(), 0.85f* width/(4f*backgroundBox.getWidth()));
            for(int i = 0; i < 4; ++i){
                canvas.draw(backgroundBox, Color.WHITE, backgroundBox.getWidth()/2, backgroundBox.getHeight()/2, bottomLeftCorner.x + width/8f+i*width/4f, 0.85f*height + bottomLeftCorner.y, 0f, scale, scale);

            }
        }

    }

    public void drawPawIndicator(GameCanvas canvas, Texture pawIndicator) {
        float scale = Math.min(0.2f * height/pawIndicator.getHeight(), 0.85f* width/pawIndicator.getWidth());
            canvas.draw(pawIndicator, Color.WHITE, pawIndicator.getWidth() / 2, pawIndicator.getHeight() / 2,
                    bottomLeftCorner.x + width/2, 0.65f*height + bottomLeftCorner.y,
                    0.0f, scale, scale);
    }

    /**
     * Draw the hit bar in a certain color according to if we triggered the line. Pass in a value for a switchable lane
     * also draw the keyBind
     */
    public void drawIndicator(GameCanvas canvas, Texture noteIndicator, Texture noteIndicatorHit, boolean hit){
       float scale = width/noteIndicatorHit.getWidth();
        canvas.draw(hit ? noteIndicatorHit : noteIndicator, Color.WHITE, noteIndicatorHit.getWidth() / 2, noteIndicatorHit.getHeight() / 2,
                bottomLeftCorner.x + width/2, hitY,
                0.0f,scale, scale);
//         TODO: fix these values
//        canvas.drawText(InputController.switchKeyBinds(4)[i], displayFont, ( bottomLeftCorner.x + bottomLeftCorner.x + width) / 2f, hitY - 80);
    }

    /**
     * Draw the switch notes
     */
    public void drawSwitchNotes(GameCanvas canvas){
        for(Note n : switchNotes){
            if(!n.isDestroyed()){
                //Switch notes should just appear in the middle of the lane
                n.setX(bottomLeftCorner.x + width/2);
                //Set the Y coordinate according to sampleProgression
                //Calculate the spawning y coordinate to be high enough such that none of the note is
                //visible
                n.draw(canvas, width, width, bottomLeftCorner.y + height, bottomLeftCorner.y);
            }
        }
    }

    /**
     * Draw the held and beat notes
     * Updates the notes
     */
    public void drawHitNotes(GameCanvas canvas){
        for(Note n : hitNotes){
            if(!n.isDestroyed()){
                //Hitnotes will be based on what line we are on
                n.setX(bottomLeftCorner.x + width/(2*numLines) + n.getLine()*(width/numLines));
                n.draw(canvas, NOTE_SIZE_SCALE*width/(numLines), NOTE_SIZE_SCALE*width/(numLines), bottomLeftCorner.y + height, bottomLeftCorner.y);
            }

        }
    }

    private float borderthickness;
    /**
     * Draw the border
     */
    public void drawBorder(GameCanvas canvas, Texture HorizontalUnit, Texture VerticalUnit, Texture Corner, float thickness){
        borderthickness = thickness;
        Vector2 HOrigin = new Vector2(HorizontalUnit.getWidth()/2f, HorizontalUnit.getHeight()/2f);
        Vector2 VOrigin = new Vector2(VerticalUnit.getWidth()/2f, VerticalUnit.getHeight()/2f);
        Vector2 COrigin = new Vector2(Corner.getWidth()/2f, Corner.getHeight()/2f);

        //First, calculate 4 borderline coordinates
        float xLocBottom = bottomLeftCorner.x + width/2;
        float yLocBottom = bottomLeftCorner.y - thickness/2;
        float xScaleBottom = width/HorizontalUnit.getWidth();
        float yScaleBottom = thickness/VerticalUnit.getHeight();

        float xLocTop = bottomLeftCorner.x + width/2;
        float yLocTop = bottomLeftCorner.y + height + thickness/2;
        float xScaleTop = width/HorizontalUnit.getWidth();
        float yScaleTop = thickness/VerticalUnit.getHeight();

        float xLocLeft = bottomLeftCorner.x - thickness/2;
        float yLocLeft = bottomLeftCorner.y + height/2;
        float xScaleLeft = thickness/HorizontalUnit.getWidth();
        float yScaleLeft = height/VerticalUnit.getHeight();

        float xLocRight = bottomLeftCorner.x + width + thickness/2;
        float yLocRight = bottomLeftCorner.y + height/2;
        float xScaleRight = thickness/HorizontalUnit.getWidth();
        float yScaleRight = height/VerticalUnit.getHeight();

        //Now, draw the 4 corners

        canvas.draw(Corner, Color.WHITE, COrigin.x, COrigin.y, xLocLeft, yLocBottom, 0.0f, xScaleLeft, yScaleBottom);

        canvas.draw(Corner, Color.WHITE, COrigin.x, COrigin.y, xLocRight, yLocBottom, 90.0f, xScaleRight, yScaleBottom);

        canvas.draw(Corner, Color.WHITE, COrigin.x, COrigin.y, xLocRight, yLocTop, 180.0f, xScaleRight, yScaleTop);

        canvas.draw(Corner, Color.WHITE, COrigin.x, COrigin.y, xLocLeft, yLocTop, 270.0f, xScaleLeft, yScaleTop);

        //Now, draw the sidebars such that they don't intersect the corners
        float barHeight = (yLocTop - (Corner.getHeight()*yScaleTop/2)) - (yLocBottom + (Corner.getHeight()*yScaleBottom/2));
        float barWidth = (xLocRight - (Corner.getWidth()*xScaleRight/2)) - (xLocLeft + (Corner.getWidth()*xScaleLeft/2));
        float lengthScale = barWidth/HorizontalUnit.getWidth();
        canvas.draw(HorizontalUnit, Color.WHITE, HOrigin.x, HOrigin.y, xLocBottom, yLocBottom, 0.0f, lengthScale, yScaleBottom);
        canvas.draw(HorizontalUnit, Color.WHITE, HOrigin.x, HOrigin.y, xLocTop, yLocTop, 0.0f, lengthScale, yScaleTop);

        float heightScale = barHeight/VerticalUnit.getWidth();
        canvas.draw(VerticalUnit, Color.WHITE, VOrigin.x, VOrigin.y, xLocLeft, yLocLeft, 0.0f, xScaleLeft, heightScale);
        canvas.draw(VerticalUnit, Color.WHITE, VOrigin.x, VOrigin.y, xLocRight, yLocRight, 0.0f, xScaleRight, heightScale);
    }

    /**
     * Draw the background of this bandMember
     * @param canvas

     */
    public void drawBackground(GameCanvas canvas, Texture activeBackground, Texture inactiveBackground){
        float xScale = width/activeBackground.getWidth();
        float yScale = height/activeBackground.getHeight();

        float ixScale = width/inactiveBackground.getWidth();
        float iyScale = height/inactiveBackground.getHeight();
        Color tint = Color.WHITE;
        tint.a = laneTint;
        canvas.draw(activeBackground, tint, 0, 0, bottomLeftCorner.x, bottomLeftCorner.y, 0.0f, xScale, yScale);
        Color itint = Color.WHITE;
        itint.a = 1f - laneTint;
        canvas.draw(inactiveBackground, tint, 0, 0, bottomLeftCorner.x, bottomLeftCorner.y, 0.0f, ixScale, iyScale);
    }

    public void drawHPBar(GameCanvas canvas){
        float scale = (bottomLeftCorner.y*4/5)/hpbar.getRegionHeight();
        float trueHeight = scale*hpbar.getRegionHeight();
        canvas.draw(hpbar, Color.WHITE, 0, 0, bottomLeftCorner.x + width/10, (bottomLeftCorner.y - trueHeight)/2,
                0.0f, scale, scale);
    }

    public void drawArrow(GameCanvas canvas, Texture arrow) {
        float scale = Math.min((bottomLeftCorner.y*1/4)/arrow.getHeight(), 1.5f*width/arrow.getWidth());
        float trueHeight = scale*arrow.getHeight();
        canvas.draw(arrow, Color.WHITE, arrow.getWidth()/2, arrow.getHeight()/2, bottomLeftCorner.x + width/5, (bottomLeftCorner.y - trueHeight),
                90f, scale, scale);
    }

    public void drawCharacterSprite(GameCanvas canvas) {
        float scale = Math.min((bottomLeftCorner.y*4/5)/characterSprite.getRegionHeight(), 1.5f*width/characterSprite.getRegionWidth());
        float trueHeight = scale*characterSprite.getRegionHeight();
        canvas.draw(characterSprite, Color.WHITE, characterSprite.getRegionWidth()/ 2f, characterSprite.getRegionHeight()/2f,
                bottomLeftCorner.x + width/2, (bottomLeftCorner.y - borderthickness - trueHeight/2), 0.0f, scale, scale);
    }

    public void drawGhostLoom(GameCanvas canvas){
        float scale = Math.min((bottomLeftCorner.y*4/5)/ghostLoomSprite.getRegionHeight(), 1.5f*width/ghostLoomSprite.getRegionWidth());
        float trueHeight = scale*characterSprite.getRegionHeight();
        float timeStep = (sample % (2f*samplesPerBeat))/(2f*samplesPerBeat);
        float hover = scale*characterSprite.getRegionHeight()/3f + (float) ((scale*characterSprite.getRegionHeight()/20f)*(Math.sin(2*Math.PI*((double) timeStep))));
        canvas.draw(ghostLoomSprite, Color.WHITE, ghostLoomSprite.getRegionWidth()/2f, ghostLoomSprite.getRegionHeight()/2f,
                bottomLeftCorner.x + width/2, bottomLeftCorner.y - borderthickness - trueHeight/2 + hover, 0.0f, scale, scale);
    }

    /**
     * Draw separation lines to divide each line within this lane
     */
    public void drawLineSeps(GameCanvas canvas, Texture lineSep){
        float thickness = width/50f;
        float xScale = thickness/lineSep.getWidth();
        float trueHeight = 0.95f*lineHeight;
        float yScale = trueHeight/lineSep.getHeight();
        float yCoord = ((bottomLeftCorner.y + height) + (bottomLeftCorner.y + height - lineHeight))/2f;
        for(int i = 1; i < numLines; ++i){
            canvas.draw(lineSep, Color.WHITE, lineSep.getWidth()/2f, lineSep.getHeight()/2f, bottomLeftCorner.x + i * (width/numLines), yCoord, 0.0f, xScale, yScale);
        }
    }

    public void setIndicatorTextures(Texture texture, Texture textureHit) {
        noteIndicator = texture;
        noteIndicatorHit = textureHit;
    }
    private int characterFrames;
    public int getCharacterFrames(){
        return characterFrames;
    }

    private int frame;
    public int getFrame(){
        return frame;
    }
    public void setFrame(int frame){
        this.frame = frame;
        characterSprite.setFrame(frame);
    }

}
