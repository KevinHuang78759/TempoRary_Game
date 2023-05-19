package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.AudioEngine;
import edu.cornell.gdiac.audio.AudioSource;
import edu.cornell.gdiac.audio.AudioStream;
import edu.cornell.gdiac.audio.MusicQueue;
import edu.cornell.gdiac.temporary.entity.BandMember;
import edu.cornell.gdiac.temporary.entity.Note;
import edu.cornell.gdiac.util.FilmStrip;

import javax.swing.plaf.TextUI;
import java.nio.ByteBuffer;

public class Level {

    public BandMember[] getBandMembers() {
        return bandMembers;
    }

    public Texture getHitNoteTexture() {
        return hitNoteTexture;
    }

    public Texture getSwitchNoteTexture() {
        return switchNoteTexture;
    }

    private Texture switchIndicator;

    private Texture switchIndicatorHit;

    private Texture bkgTexture;

    public Texture getHoldNoteTexture() {
        return holdNoteTexture;
    }

    public String getLevelName() {
        return levelName;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public int getMaxCompetency() {
        return maxCompetency;
    }

    public int getSpawnOffset() {
        return spawnOffset;
    }

    public MusicQueue getMusic() {
        return music;
    }

    public void setBandMembers(BandMember[] bandMembers) {
        this.bandMembers = bandMembers;
    }

    public void setHitNoteTexture(Texture hitNoteTexture) {
        this.hitNoteTexture = hitNoteTexture;
    }

    public void setSwitchNoteTexture(Texture switchNoteTexture) {
        this.switchNoteTexture = switchNoteTexture;
    }

    public void setHoldNoteTexture(Texture holdNoteTexture) {
        this.holdNoteTexture = holdNoteTexture;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public void setMaxCompetency(int maxCompetency) {
        this.maxCompetency = maxCompetency;
    }

    public void setSpawnOffset(int spawnOffset) {
        this.spawnOffset = spawnOffset;
    }

    public void setMusic(MusicQueue music) {
        this.music = music;
    }

    public int getBpm() { return this.bpm; }

    public long getaThreshold() {
        return aThreshold;
    }

    public void setaThreshold(long aThreshold) {
        this.aThreshold = aThreshold;
    }

    public long getbThreshold() {
        return bThreshold;
    }

    public void setbThreshold(long bThreshold) {
        this.bThreshold = bThreshold;
    }

    public long getcThreshold() {
        return cThreshold;
    }

    public void setcThreshold(long cThreshold) {
        this.cThreshold = cThreshold;
    }

    public long getsThreshold() {
        return sThreshold;
    }

    public void setsThreshold(long sThreshold) {
        this.sThreshold = sThreshold;
    }

    private BandMember[] bandMembers;

    // TEXTURES
    private Texture hitNoteTexture;
    private Texture switchNoteTexture;
    private Texture holdNoteTexture;
    private Texture holdEndTexture;
    private Texture holdTrailTexture;
    private Texture hpbar;
    private Texture noteIndicator;
    private Texture noteIndicatorHit;
    private FilmStrip violinSprite;
    private FilmStrip drummerSprite;
    private FilmStrip voiceSprite;
    private FilmStrip synthSprite;
    private FilmStrip backSplash;
    private FilmStrip frontSplash;

    // PROPERTIES
    private String levelName;
    private int levelNumber;
    private int maxCompetency;
    private int spawnOffset;
    private long spawnOffsetSwitch;
    private int bpm;
    private long aThreshold;
    private long bThreshold;
    private long cThreshold;
    private long sThreshold;
    private MusicQueue music;

    /**
     * The last sample that health was decremented due to continuous decay
     */
    private long lastDec;

    /**
     * Horizontal slice of border
     */
    private Texture HUnit;

    /**
     * set to true if music has been started
     */
    private boolean musicInitialized = false;
    /**
     * Vertical slice of border
     */
    private Texture VUnit;
    /**
     * Corner Texture
     */
    private Texture CUnit;

    /**
     * Line separation texture
     */
    private Texture sepLine;

    /**
     * background of each lane
     */
    private Texture activeLane;
    private Texture inactiveLane;
    AudioSource songSource;
    JsonValue data;

    float maxSample;
    public Level(JsonValue data, AssetDirectory directory) {
        JsonReader jr = new JsonReader();
        JsonValue assets = jr.parse(Gdx.files.internal("assets.json"));
        bkgTexture = directory.getEntry(data.getString("background"), Texture.class);
        // load all related level textures
        hitNoteTexture = directory.getEntry("hit", Texture.class);
        switchNoteTexture = directory.getEntry("switch", Texture.class);
        holdNoteTexture = directory.getEntry("hold-start", Texture.class);
        holdTrailTexture = directory.getEntry("hold-trail", Texture.class);
        hpbar = directory.getEntry("hp-bar", Texture.class);
        holdTrailTexture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.Repeat);
        holdEndTexture = directory.getEntry("hold-end", Texture.class);
        noteIndicator = directory.getEntry("note-indicator", Texture.class);
        noteIndicatorHit = directory.getEntry("note-indicator-hit", Texture.class);
        switchIndicator = directory.getEntry("switch-indicator", Texture.class);
        switchIndicatorHit = directory.getEntry("switch-indicator-hit", Texture.class);
        violinSprite = new FilmStrip(directory.getEntry("violin-cat", Texture.class), 2, 5, 6);
        voiceSprite = new FilmStrip(directory.getEntry("singer-cat", Texture.class), 2, 5, 6);
        drummerSprite = new FilmStrip(directory.getEntry("drummer-cat", Texture.class), 2, 5, 6);
        synthSprite = new FilmStrip(directory.getEntry("piano-cat", Texture.class), 2, 5, 10);
        backSplash = new FilmStrip(directory.getEntry("back-splash", Texture.class), 5, 5, 23);
        frontSplash = new FilmStrip(directory.getEntry("front-splash", Texture.class), 5, 5, 21);
        this.data = data;
        //Read in Json  Value and populate asset textures
        lastDec = 0;
        levelName = data.getString("levelName");
        levelNumber = data.getInt("levelNumber");
        maxCompetency = data.getInt("maxCompetency");
        aThreshold = data.get("a-threshold").asLong();
        bThreshold = data.get("b-threshold").asLong();
        cThreshold = data.get("c-threshold").asLong();
        sThreshold = data.get("s-threshold").asLong();
        bpm = data.getInt("bpm");
        String song = data.getString("song");
        music = ((AudioEngine) Gdx.audio).newMusic(Gdx.files.internal(assets.get("samples").getString(song)));
        songSource = music.getSource(0);
        music.setVolume(0.8f);
        maxSample = songSource.getDuration() * songSource.getSampleRate();
        HUnit = directory.getEntry("borderHUnit", Texture.class);
        VUnit = directory.getEntry("borderVUnit", Texture.class);
        CUnit = directory.getEntry("borderCorner", Texture.class);
        activeLane = directory.getEntry("activeLane", Texture.class);
        inactiveLane = directory.getEntry("inactiveLane", Texture.class);
        sepLine = directory.getEntry("separationLine", Texture.class);

        // preallocate band members
        bandMembers = new BandMember[data.get("bandMembers").size];
        spawnOffset = music.getSampleRate();
        // switch note is twice as slow
        spawnOffsetSwitch = 2L * spawnOffset;
        System.out.println(spawnOffset);
        float samplesPerBeat = songSource.getSampleRate() * 60f/bpm;
        for(int i = 0; i < bandMembers.length; i++){
            bandMembers[i] = new BandMember();
            JsonValue bandMemberData = data.get("bandMembers").get(i);
            Queue<Note> notes = new Queue<>();
            JsonValue noteData = bandMemberData.get("notes");
            for(int j = 0; j < noteData.size; ++j){
                JsonValue thisNote = noteData.get(j);
                Note n;

                if (thisNote.getString("type").equals("beat")){
                    if(thisNote.getLong("position") > maxSample){
                        continue;
                    }
                    n = new Note(thisNote.getInt("line"), Note.NoteType.BEAT, thisNote.getLong("position") - spawnOffset, hitNoteTexture);
                }
                else if (thisNote.getString("type").equals("switch")){
                    if(thisNote.getLong("position") > maxSample){
                        continue;
                    }
                    n = new Note(thisNote.getInt("line"), Note.NoteType.SWITCH, thisNote.getLong("position") - spawnOffsetSwitch, switchNoteTexture);
                }
                else {
                    if(thisNote.getLong("position") + thisNote.getLong("duration")> maxSample){
                        continue;
                    }
                    n = new Note(thisNote.getInt("line"), Note.NoteType.HELD, thisNote.getLong("position") - spawnOffset, holdNoteTexture);
                    n.setHoldTextures(holdTrailTexture,1,holdEndTexture,1, backSplash, frontSplash, getAnimationRateFromBPM(bpm));
                    n.setHoldSamples(thisNote.getLong("duration"));
                }
                n.setHitSample(thisNote.getLong("position"));
                notes.addLast(n);
            }
            bandMembers[i].setAllNotes(notes);
            bandMembers[i].setCurComp(maxCompetency);
            bandMembers[i].setMaxComp(maxCompetency);
            // TODO: FIX THIS SO THAT IT FITS THE LEVEL JSON
            bandMembers[i].setLossRate(bandMemberData.getInt("competencyLossRate"));
            bandMembers[i].setHpBarFilmStrip(hpbar, 47);
            bandMembers[i].setIndicatorTextures(noteIndicator, noteIndicatorHit);
            bandMembers[i].setSPB(samplesPerBeat);
            switch (bandMemberData.getString("instrument")) {
                case "violin":
                    bandMembers[i].setCharacterFilmstrip(violinSprite);
                    break;
                case "piano":
                    bandMembers[i].setCharacterFilmstrip(synthSprite);
                    break;
                case "drum":
                    bandMembers[i].setCharacterFilmstrip(drummerSprite);
                    break;
                case "voice":
                    bandMembers[i].setCharacterFilmstrip(voiceSprite);
                    break;
            }
        }
    }

    // TODO: document and multiply by the base music volume
    public void setMusicVolume(float vol) {
        music.setVolume(vol);
    }

    // TODO: REMOVE THIS AND REPLACE WITH ACTUAL ANIMATION BASED ON SAMPLE
    /**
     * Takes BPM and converts it to rate of animation (as all animations should be on beat)
     * @param bpm BPM of song in level
     * @return animation rate
     */
    public float getAnimationRateFromBPM(int bpm) {
        return bpm * 2 / 60f / 60f;
    }

    public int getSamplesPerBeat(){
        return music.getSampleRate()*60/bpm;
    }

    /**
     * Sets all the bottom left corners of the BandMembers. Call this after setting width and height properties
     * @param windowBL - Bottom Left of the play area
     * @param inBetweenWidth - Width between each band member
     */
    public void setBandMemberBl(Vector2 windowBL, float inBetweenWidth){
        float xCoord = windowBL.x;
        float yCoord = windowBL.y;
        for (BandMember bandMember : bandMembers) {
            bandMember.setBottomLeft(new Vector2(xCoord, yCoord));
            xCoord += inBetweenWidth + bandMember.getWidth();
        }
    }

    public void setBandMemberHitY(float hity){
        for(BandMember bandMember : bandMembers){
            bandMember.setHitY(hity);
        }
    }

    /**
     * Sets width and height of BandMembers outside of transition. Call once after transitioning is finished
     * @param activeBandMember - index of active BandMember
     * @param large_width - width of active BandMember
     * @param short_width - width of inactive BandMember
     */
    public void setActiveProperties(int activeBandMember, float large_width, float short_width, float maxLineHeight){
        for (BandMember bandMember : bandMembers) {
            bandMember.setWidth(short_width);
            bandMember.setLineHeight(0f);
            bandMember.setHeight(maxLineHeight);
            bandMember.setLaneTint(0.1f);
        }
        bandMembers[activeBandMember].setWidth(large_width);
        bandMembers[activeBandMember].setLineHeight(maxLineHeight);
        bandMembers[activeBandMember].setLaneTint(1f);
    }

    /**
     * Sets the width and line height of BandMembers during transition call at the start and during transition
     * @param previousBM - BandMember we are transitioning away from
     * @param nextBM - BandMember we are transitioning into
     * @param large_width - width of fully active BandMember
     * @param short_width - with of fully in active Band Member
     * @param t_progress - transition progress, should be between 0 and 1. 1 means transition completed.
     * */
    public void setTransitionProperties(int previousBM, int nextBM, float large_width, float short_width, float maxLineHeight, float t_progress){
        //set default widths and heights, which means everyone with no lines and everyone has no lines
        for (BandMember bandMember : bandMembers) {
            bandMember.setWidth(short_width);
            bandMember.setLineHeight(0f);
            bandMember.setHeight(maxLineHeight);
        }

        //set the width and line heights of the active and goal bandmembers accordingly
        bandMembers[previousBM].setWidth(large_width - (large_width - short_width)*t_progress);
        bandMembers[nextBM].setWidth(short_width + (large_width - short_width)*t_progress);
        bandMembers[nextBM].setLaneTint(findTintColorProgress(t_progress, new SingleOperator() {
            @Override
            public float op(float x) {
                return 0.9f*(float)Math.sin(x * Math.PI/2) + 0.1f;
            }
        }));
        bandMembers[previousBM].setLaneTint(findTintColorProgress(1f - t_progress, new SingleOperator() {
            @Override
            public float op(float x) {
                return 0.9f*x * x + 0.1f;
            }
        }));
        bandMembers[previousBM].setLineHeight(maxLineHeight*(1f-t_progress));
        bandMembers[nextBM].setLineHeight(maxLineHeight*t_progress);
    }

    private float findTintColorProgress(float t_progress, SingleOperator op){
        return op.op(t_progress);
    }

    private long sample = 0;

    public void recieveInterrupt(int BM_id, boolean DFflag, boolean JKflag, boolean MISSflag){
        bandMembers[BM_id].recieveSample(sample);
        bandMembers[BM_id].recieveFlags(DFflag, JKflag, MISSflag);
    }

    public void setActive(int id){
        bandMembers[id].setMode(1);
    }
    public void swapActive(int prev, int next){
        bandMembers[prev].changeMode();
        bandMembers[next].changeMode();
    }

    /**
     * Spawns new notes according to what sample we are at. Also decrements bandmembers' competency
     * for some amount about once a second. It also updates the frame of the bandmember.
     */
    public void updateBandMemberNotes(float spawnY, int mode, int ticks, int introLength){
        //First get the sample we at
        int rate = music.getSampleRate();
        if (mode == 1)       {
            sample = getCurrentSample();
        } else if (mode == 2){
            sample += (int) ((((float) rate)/60f)*(0.8f*(1f - (((float) ticks)/120f))));
        } else if (mode == 0){
            int startSample = -(int) (((float) rate/60f)*introLength);
            sample = startSample + (int) (((float) rate/60f)*ticks);
        }


        for (int i = 0; i < bandMembers.length; ++i) {
            BandMember bandMember = bandMembers[i];
            if (mode==1) {
                bandMember.recieveSample(sample);
                bandMember.pickFrame();
            }

            //spawn new notes accordingly
            bandMember.spawnNotes(sample);
            //update the note frames
            bandMember.updateNotes(spawnY, sample);

            if (!bandMember.getHitNotes().isEmpty() && mode == 1) {
                float loss = -1f * bandMember.getLossRate() * (sample - lastDec) / ((float) music.getSampleRate());
                // TODO: ONLY UPDATE IF YOU ARE NOT HOLDING
                bandMember.compUpdate(loss);
            }
        }
        lastDec = sample;
    }

    /**
     * Starts the music
     */
    public void startmusic(){
        musicInitialized = true;
        music.play();
    }

    public boolean hasMoreNotes(){
        boolean tog = false;
        for(BandMember bm : bandMembers){
            tog = tog ||  bm.hasMoreNotes();
        }
        return tog;
    }

    public void resetLevel(){
        float oldVolume = music.getVolume();
        lastDec = 0;
        music.stop();
        music.reset();
        music.clearSources();
        music = ((AudioEngine) Gdx.audio).newMusicBuffer( songSource.getChannels() == 1, songSource.getSampleRate() );
        music.addSource(songSource);
        // reset volume
        music.setVolume(oldVolume);
        bandMembers = new BandMember[data.get("bandMembers").size];
        spawnOffset = music.getSampleRate();
        spawnOffsetSwitch = spawnOffset * 2L;
        for(int i = 0; i < bandMembers.length; i++){
            bandMembers[i] = new BandMember();
            JsonValue bandMemberData = data.get("bandMembers").get(i);
            Queue<Note> notes = new Queue<>();
            JsonValue noteData = bandMemberData.get("notes");
            for(int j = 0; j < noteData.size; ++j){
                JsonValue thisNote = noteData.get(j);
                Note n;
                if (thisNote.getString("type").equals("beat")){
                    if(thisNote.getLong("position") > maxSample){
                        continue;
                    }
                    n = new Note(thisNote.getInt("line"), Note.NoteType.BEAT, thisNote.getLong("position") - spawnOffset, hitNoteTexture);
                }
                else if (thisNote.getString("type").equals("switch")){
                    if(thisNote.getLong("position") > maxSample){
                        continue;
                    }
                    n = new Note(thisNote.getInt("line"), Note.NoteType.SWITCH, thisNote.getLong("position") - spawnOffsetSwitch, switchNoteTexture);
                }
                else {
                    if(thisNote.getLong("position") + thisNote.getLong("duration")> maxSample){
                        continue;
                    }
                    n = new Note(thisNote.getInt("line"), Note.NoteType.HELD, thisNote.getLong("position") - spawnOffset, holdNoteTexture);
                    n.setHoldTextures(holdTrailTexture,1,holdEndTexture,1, backSplash, frontSplash, getAnimationRateFromBPM(bpm));
                    n.setHoldSamples(thisNote.getLong("duration"));
                }
                n.setHitSample(thisNote.getLong("position"));
                notes.addLast(n);
            }
            bandMembers[i].setAllNotes(notes);
            bandMembers[i].setCurComp(maxCompetency);
            bandMembers[i].setMaxComp(maxCompetency);
            bandMembers[i].setLossRate(bandMemberData.getInt("competencyLossRate"));
            bandMembers[i].setHpBarFilmStrip(hpbar, 47);
            switch (bandMemberData.getString("instrument")) {
                case "violin":
                    bandMembers[i].setCharacterFilmstrip(violinSprite);
                    break;
                case "piano":
                    bandMembers[i].setCharacterFilmstrip(synthSprite);
                    break;
                case "drum":
                    bandMembers[i].setCharacterFilmstrip(drummerSprite);
                    break;
                case "voice":
                    bandMembers[i].setCharacterFilmstrip(voiceSprite);
                    break;
            }
        }
    }

    public void stopMusic() {
        music.stop();
    }

    public void pauseMusic() {
        music.pause();
    }

    /**
     * returns true if music is playing
     */
    public boolean isMusicPlaying(){

        return Math.abs(music.getPosition()-songSource.getDuration()) > 0.001*songSource.getDuration();
    }

    /**
     * Gets the current sample of the song
     * @return
     */
    public long getCurrentSample(){
        return (long)(music.getPosition()*music.getSampleRate());
    }

    public long getLevelSample(){
        return sample;
    }

    /**
     * Returns true if the player has unlocked this level
     * @return
     */
    public boolean hasUnlocked(){ return true; };

    /**
     * this draws everything the level needs to display on the given canvas
     * @param canvas - what are we drawing on
     * @param active - which band member is active
     * @param goal - which band member are we/have we transitioned to?
     * @param triggers - which triggers are pressed?
     * @param switches - which switches are pressed?
     */
    public void drawEverything(GameCanvas canvas, int active, int goal, boolean[] triggers, boolean[] switches, float borderThickness){
        canvas.drawBackground(bkgTexture,0,0);
        for(int i = 0; i < bandMembers.length; ++i){
            //Draw the border of each band member
            bandMembers[i].drawBackground(canvas, activeLane, inactiveLane);
            bandMembers[i].drawBorder(canvas, HUnit, VUnit, CUnit, borderThickness);
            //draw the character sprite and the comp bar
            bandMembers[i].drawCharacterSprite(canvas);
            bandMembers[i].drawHPBar(canvas);
            //If we are the goal of the active lane we need to draw separation lines and held/beat notes
            //We also need to draw a separate hit bar for each line
            if(active == i || goal == i){
                bandMembers[i].drawHitNotes(canvas);
                bandMembers[i].drawLineSeps(canvas, sepLine);
                bandMembers[i].drawIndicator(canvas, noteIndicator, noteIndicatorHit, triggers);
            }
            //Otherwise just draw the switch notes, and we only have 1 hit bar to draw
            else{
                bandMembers[i].drawSwitchNotes(canvas);
                bandMembers[i].drawIndicator(canvas, switchIndicator, switchIndicatorHit, switches[i]);
            }
        }

    }

    public void dispose(){
        music.dispose();
        activeLane.dispose();
        inactiveLane.dispose();
        hitNoteTexture.dispose();
        holdTrailTexture.dispose();
        holdNoteTexture.dispose();
        switchNoteTexture.dispose();
        holdEndTexture.dispose();
        noteIndicator.dispose();
        noteIndicatorHit.dispose();
        hpbar.dispose();
        music.dispose();
    }
}
