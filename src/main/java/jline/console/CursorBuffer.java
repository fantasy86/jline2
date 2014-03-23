/*
 * Copyright (c) 2002-2012, the original author or authors.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package jline.console;

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

    private int cursor = 0;

    private final StringBuilder buffer = new StringBuilder();
    
    public CursorBuffer copy () {
        CursorBuffer that = new CursorBuffer();
        that.overTyping = this.overTyping;
        that.cursor = this.cursor;
        that.buffer.append (this.toString());
        
        return that;
    }

    public boolean isOverTyping() {
        return overTyping;
    }

    public void setOverTyping(final boolean b) {
        overTyping = b;
    }

    public int length() {
        return buffer.length();
    }

    // TODO convert char idx to cell coord
    public int getCursor() {
        return cursor;
    }
    
    public void setCursor(int pos) {
        cursor = pos;
    }
    
    public void advanceCursor(int d) {
        cursor += d;
    }
    
    public char charAt(int idx) {
        return buffer.charAt(idx);
    }
    
    public String substring(int start) {
        return buffer.substring(start);
    }

    public String substring(int start, int end) {
        return buffer.substring(start, end);
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

    @Override
    public String toString() {
        return buffer.toString();
    }
}
