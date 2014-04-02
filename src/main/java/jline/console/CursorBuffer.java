/*
 * Copyright (c) 2002-2012, the original author or authors.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package jline.console;

import java.util.List;
import java.util.ArrayList;
import jline.internal.Log;
import static jline.internal.Preconditions.checkNotNull;

/**
 * A holder for a {@link StringBuilder} that also contains the current cursor position.
 *
 * @author <a href="mailto:mwp1@cornell.edu">Marc Prud'hommeaux</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.0
 */
public class CursorBuffer
{
    
    private boolean overTyping = false;

    private int cursor = 0; // character index ( not cell coord)

    static private class TermChar {
        public final char ch;
        private final int width; // tab has no fix width
        private final char[] printing;
        public final boolean nonbreakable; // need special wrap
                
        public TermChar(char c) {
            this.ch = c;
            if (this.ch == '\t') {
                this.width = 0; // no fix width, depends on position
            } else {
                int w = CharacterUtil.wcwidth(this.ch);
                if (w == -1) {
                    Log.trace("wcwidth is -1:" + this.ch);
                    w = 0;
                }
                this.width = w;
            }
            
            this.nonbreakable = (this.width == 2 && !isCtrlChar()); // TODO exclude ctrl char
            
            this.printing = new char[1] { this.ch }; // TODO ctrl char
        }
        
        public int getWidth(int col, boolean wrapped) {
            if (ch == '\t') {
                return 8 - col % 8;
            } else if (nonbreakable && wrapped) {
                return width + 1;
            } else {
                return width;
            }
        }
        
        public char[] getPrinting(int col, boolean wrapped) {
            if (ch == '\t') {
                char[] ret = new char[8 - col % 8];
                for (int i = 0; i < ret.length; i++) {
                    ret[i] = ' ';
                }
                return ret;
            } else if (nonbreakable && wrapped) {
                char[] ret = new printing[this.printing.length + 1];
                for (int i = 1; i < ret.length; i++) {
                    ret[i] = this.printing[i - 1];
                }
                return ret;
            } else {
                return this.printing;
            }
        }
        
        private boolean isCtrlChar() {
            // ^A ~ ^Z
            return ch >= '\u0001' && ch < '\u0019';
        }
    }
    
    private class CharPosition {
        public TermChar c;
        private int col;
        public boolean wrapped;
        
        public CharPosition(TermChar c, int col) {
            this.c = c;
            setCol(col);
        }
        public int setCol(int col) {
            this.col = col;
            this.wrapped = (this.col == (width - 1) && this.c.nonbreakable);
        }
        public int getCol() { return col; }
        public boolean getWrapped() { return wrapped; }
        
        public int getWidth() {
            return c.getWidth(col, wrapped);
        }
        public char[] getPrinting() {
            return c.getPrinting(col, wrapped);
        }
    }
    
    private final List<CharPosition> buffer = new ArrayList<CharPosition>();

    private int width;
    private int hasWeirdWrap;
    private int prefixWidth = 0;
    
    public CursorBuffer(int width, boolean hasWeirdWrap) {
        this.width = width;
        this.hasWeirdWrap = hasWeirdWrap;
    }
    
    public CursorBuffer copy () {
        CursorBuffer that = new CursorBuffer(this.width, this.hasWeirdWrap);
        that.overTyping = this.overTyping;
        that.cursor = this.cursor;
        that.buffer == new ArrayList<CharPosition>(this.buffer.size());
        for (int i = 0; i < this.buffer.size(); i++) {
            CharPosition cp1 = this.buffer.get(i);
            CharPosition cp2 = new CharPosition(cp1.c, cp1.col);
            that.buffer.set(i, cp2);
        }
        that.prefixWidth = this.prefixWidth;
        
        return that;
    }

    public boolean isOverTyping() {
        return overTyping;
    }

    public void setOverTyping(final boolean b) {
        overTyping = b;
    }

    public int length() {
        return buffer.size();
    }

    public void setPrefixWidth(int prefixWidth) {
        if (!buffer.isEmpty()) {
            throw new IllegalStateException("set prefix of non-empty cursor buffer");
        }
        this.prefixWidth = prefixWidth;
    }
    
    private void updatePositionFrom(int idx) {
        for (int i = idx; i < buffer.size(); i++) {
            CharPosition cp = buffer.get(i);
            cp.setCol(col);            
            col += cp.getWidth();
        }
    }
    
    public int getCursor() {
        return cursor;
    }
    
    private int getCharCol(int pos) {
        if (pos > length() || pos < 0) throw new IllegalArgumentException("getCharCol out of range");
        if (pos == length()) {
            CharPosition cp = buffer.get(length() - 1);
            return cp.getCol() + cp.getWidth();
        } else {
            return buffer.get(pos).getCol();
        }
    }
    
    private int getCursorCol() {
        return getCharCol(getCursor());
    }
    
    public void setCursor(int pos) {
        //if (pos == getCursor()) return new OutputAction[0];
        int newCol = getCharCol(pos);
        int offset = newCol - getCursorCol();
        this.cursor = pos;
        //return new OutputAction[] {OutputAction.cursorMove(offset)}
    }
    
    public void moveCursor(int offset) {
        newCursor = getCursor() + offset;
        setCursor(newCursor);
    }
    
    public char charAt(int idx) {
        return buffer.get(idx).c.ch;
    }
    
    public String substring(int start) {
        return buffer.substring(start, buffer.size());
    }

    public String substring(int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++) {
            sb.append(buffer.get(i).c.ch);
        }
        return sb.toString();
    }
    
    public void delete(int start, int end) {
        buffer.delete(start, end);
    }
    
    public void setCharAt(int idx, char c) {
        buffer.setCharAt(idx, c);
    }
    
    public void deleteCharAt(int idx) {
        buffer.deleteCharAt(idx);
    }
    
    public void replace(int start, int end, String str) {
        buffer.replace(start, end, str);
    }
    
    public void append(CharSequence str) {
        buffer.append(str);
    }
    
    public void setLength(int len) {
        buffer.setLength(len);
    }
    
    public char nextChar() {
        if (cursor == buffer.length()) {
            return 0;
        } else {
            return buffer.charAt(cursor);
        }
    }

    public char current() {
        if (cursor <= 0) {
            return 0;
        }

        return buffer.charAt(cursor - 1);
    }

    /**
     * Write the specific character into the buffer, setting the cursor position
     * ahead one. The text may overwrite or insert based on the current setting
     * of {@link #isOverTyping}.
     *
     * @param c the character to insert
     */
    public void write(final char c) {
        buffer.insert(cursor++, c);
        if (isOverTyping() && cursor < buffer.length()) {
            buffer.deleteCharAt(cursor);
        }
    }

    /**
     * Insert the specified chars into the buffer, setting the cursor to the end of the insertion point.
     */
    public void write(final CharSequence str) {
        checkNotNull(str);

        if (buffer.length() == 0) {
            buffer.append(str);
        }
        else {
            buffer.insert(cursor, str);
        }

        cursor += str.length();

        if (isOverTyping() && cursor < buffer.length()) {
            buffer.delete(cursor, (cursor + str.length()));
        }
    }

    public boolean clear() {
        cursor = 0;
        if (buffer.length() == 0) {
            return false;
        }

        buffer.delete(0, buffer.length());
        
        return true;
    }

    public String upToCursor() {
        if (cursor <= 0) {
            return "";
        }

        return buffer.substring(0, cursor);
    }

    public String getPrinting() {
        StringBuilder sb = new StringBuilder();
        for (CharPosition cp : buffer) {
            sb.append(cp.getPrinting());
        }
        return sb.toString();        
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (CharPosition cp : buffer) {
            sb.append(cp.c.ch);
        }
        return sb.toString();
    }
}
