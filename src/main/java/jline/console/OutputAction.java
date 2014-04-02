package jline.console;

public class OutputAction {
    public static final int CURSOR_MOVE = 1;
    public static final int APPEND = 2;
    public static final int REDRAW = 3;
    
    public int type;
    
    // for cursor move
    public int offset;
        
    // for append & redraw
    public char[] printing;
    
    // for redraw
    int origWidth; // for clear
    
    public static OutputAction cursorMove(int offset) {
        OutputAction a = new OutputAction();
        a.type = CURSOR_MOVE;
        a.offset = offset;
        return a;
    }
    public static OutputAction append(char[] printing) {
        OutputAction a = new OutputAction();
        a.type = APPEND;
        a.printing = printing;
        return a;        
    }
    public static OutputAction redraw(char[] printing, int origWidth) {
        OutputAction a = new OutputAction();
        a.type = REDRAW;
        a.printing = printing;
        a.origWidth = origWidth;
        return a;
    }
}