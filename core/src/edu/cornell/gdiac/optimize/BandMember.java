package edu.cornell.gdiac.optimize;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.optimize.entity.Note;



public class BandMember {


    /**
     * Bottom left corner
     */
    Vector2 BL;

    /**
     * The height of separator lines from the top of the lane
     */
    float lineHeight;
    /**
     * Width of the lane
     */
    float width;

    /**
     * Total height
     */
    float height;

    /**
     * Number of lines this band member has
     */
    int numLines;

    /**
     * The color of the border
     */
    Color borderColor;

    /**
     * Active array of beat and held notes
     */
    Array<Note> hitNotes;

    /**
     * Active array of switch notes
     */
    Array<Note> switchNotes;

    /**
     * Queue to hold all the notes for this band member across the entire level
     */
    Queue<Note> allNotes;

    /**
     * backing array used for garbage collection
     */
    Array<Note> backing;

    /**
     * Maximum competency
     */

    int maxComp;

    /**
     * Current competency
     */
    int curComp;

    /**
     * Constructor
     */
    public BandMember(){
        //instantiate everything to evade NULL POINTERS
        BL = new Vector2();
        hitNotes = new Array<>();
        switchNotes = new Array<>();
        allNotes = new Queue<>();
        backing = new Array<>();
    }

    /**
     * Updates according to frame
     * @param frame
     */
    public void updateNotes(int frame){
        //Update both switchNotes and hit notes no matter what
        for(Note n : switchNotes){
            n.update(frame);
        }
        for(Note n : hitNotes){
            n.update(frame);
        }
    }

    /**
     * Draw the hit bar in a certain color according to if we triggered the line. Pass in an array for the active
     * lane
     */
    public void drawHitBar(GameCanvas canvas, float yval, Color hitColor, boolean[] hits){
        //If we get passed an array we must draw 4 hit bars
        for(int i = 0; i < numLines; ++i){
            canvas.drawLine(BL.x + i * width/numLines, yval, BL.x +(i+1) * width/numLines, yval, 3, hits[i] ? hitColor : Color.BLACK);
        }
    }
    /**
     * Draw the hit bar in a certain color according to if we triggered the line. Pass in a value for a switchable lane
     */
    public void drawHitBar(GameCanvas canvas, float yval, Color hitColor, boolean hit){
        //If we get passed a single value then we're in a switch lane
        canvas.drawLine(BL.x, yval, BL.x + width, yval, 3, hit ? hitColor : Color.BLACK);
    }

    /**
     * Add notes from the queue to the correct active array
     * @param frame
     */
    public void spawnNotes(int frame){
        //add everything at the front of the queue that's supposed to start on this frame
        while(!allNotes.isEmpty() && allNotes.first().startFrame == frame){
            Note n = allNotes.removeFirst();
            if(n.nt == Note.NType.SWITCH){
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
            if(!n.destroyed){
                backing.add(n);
            }
        }
        Array<Note> temp = backing;
        backing = switchNotes;
        switchNotes = temp;
        backing.clear();

        for(Note n : hitNotes){
            if(!n.destroyed){
                backing.add(n);
            }
        }
        temp = backing;
        backing = hitNotes;
        hitNotes = temp;
        backing.clear();
    }

    /**
     * Update competency by the specified amount but do not go below 0 or exceed the max
     */

    public void compUpdate(int amount){
        curComp = Math.min(Math.max(0, curComp + amount), maxComp);
    }

    /**
     * Draw the switch notes
     */
    public void drawSwitchNotes(GameCanvas canvas){
        for(Note n : switchNotes){
            if(!n.destroyed){
                //Switch notes should just appear in the middle of the lane
                n.x = BL.x + width/2;
                n.draw(canvas, 3*width/4, 3*width/4);
            }
        }
    }

    /**
     * Draw the held and beat notes
     */
    public void drawHitNotes(GameCanvas canvas){
        for(Note n : hitNotes){
            if(!n.destroyed){
                //Hitnotes will be based on what line we are on
                n.x = BL.x + width/(2*numLines) + n.line*(width/numLines);
                n.tail_thickness = width/(4f*numLines);
                n.draw(canvas, 3*width/(4*numLines), 3*width/(4*numLines));
            }

        }
    }

    /**
     * Draw the border
     */
    public void drawBorder(GameCanvas canvas){
        canvas.drawRect(BL, width, height, borderColor, false);
    }

    /**
     * Draw separation lines to divide each line within this lane
     */
    public void drawLineSeps(GameCanvas canvas){
        Color lColor = Color.BLACK;
        for(int i = 1; i < numLines; ++i){
            canvas.drawLine(BL.x + i * (width/numLines), BL.y + height, BL.x + i * (width/numLines), BL.y + height - lineHeight, 3, lColor);
        }
    }


}
