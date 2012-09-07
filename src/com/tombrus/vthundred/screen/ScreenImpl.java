package com.tombrus.vthundred.screen;

import com.tombrus.vthundred.terminal.*;
import com.tombrus.vthundred.terminal.CharProps.*;
import com.tombrus.vthundred.terminal.input.*;
import com.tombrus.vthundred.util.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

public class ScreenImpl implements Screen {
    private final          ScreenCharacter             SPACE                  = new ScreenCharacter(' ');
    private final          ReentrantLock               lock                   = new ReentrantLock();
    private final          Condition                   refreshNeededCondition = lock.newCondition();
    private       volatile boolean                     running;
    private final          Terminal                    terminal;

    private final          List<ResizeHandler> resizeHandlers = new ArrayList<ResizeHandler>();
    private                int                         currentScreenSizeX;
    private                int                         currentScreenSizeY;
    private final          AtomicReference<TerminalXY> newScreenSize          = new AtomicReference<TerminalXY>();
    private                TerminalXY                  userCursor             = new TerminalXY(0, 0);
    private                Color                       backgroundColor        = Color.DEFAULT;
    private                ScreenCharacter[][]         currentScreen;
    private                ScreenCharacter[][]         requestedScreen;
    private                boolean[][]                 dirtyChars;
    private                boolean[]                   dirtyLine;
    private                int[]                       dirtyLowChar;
    private                int[]                       dirtyHighChar;
    private                int                         dirtyLowLine;
    private                int                         dirtyHighLine;
    private                boolean                     dirtySomething;
    private                boolean                     dirtyAll;
    private                Refresher                   refresher              = new Refresher    ();

    public ScreenImpl (Terminal terminal) {
        try {
            this.terminal = terminal;
            TerminalXY currentScreenSize = terminal.getTerminalSize();
            currentScreenSizeX = currentScreenSize.getX();
            currentScreenSizeY = currentScreenSize.getY();
            currentScreen      = new ScreenCharacter[currentScreenSizeY][currentScreenSizeX];
            requestedScreen    = new ScreenCharacter[currentScreenSizeY][currentScreenSizeX];
            dirtyLine          = new boolean[currentScreenSizeY];
            dirtyChars         = new boolean[currentScreenSizeY][currentScreenSizeX];
            dirtyLowChar       = new int[currentScreenSizeY];
            dirtyHighChar      = new int[currentScreenSizeY];
            Arrays.fill(dirtyLowChar, Integer.MAX_VALUE );
            Arrays.fill(dirtyHighChar, Integer.MIN_VALUE);
            dirtyLowLine  = Integer.MAX_VALUE;
            dirtyHighLine = Integer.MIN_VALUE;
            terminal.addResizeHandler(new TerminalResizeHandler());
        } catch (RuntimeException rt) {
            DB.error(rt);
            throw rt;
        }
    }

    @Override
    public TerminalXY getScreenSize () {
        lock.lock();
        try {
            startScreen();
            return new TerminalXY(currentScreenSizeX, currentScreenSizeY);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void startScreen () {
        if (!running) {
            lock.lock();
            try {
                if (!running) {
                    running = true;
                    refresher.start();
                }
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    @SuppressWarnings({"UnusedDeclaration"})
    public void stopScreen () {
        lock.lock();
        try {
            if (running) {
                terminal.stopTerminal();
                running = false;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setUserCursor (TerminalXY loc) {
        if (loc ==null) {
            throw new IllegalArgumentException("user cursor location may not be null");
        }
        lock.lock();
        try {
            startScreen();
            if (!userCursor.equals(loc)) {
                userCursor = loc;
                requestRefresh(false);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setBackgroundColor (Color color) {
        if (color ==null) {
            throw new IllegalArgumentException("background color may not be null");
        }
        lock.lock();
        try {
            startScreen();
            backgroundColor = color;
            requestRefresh(true);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Erases all the characters on the screen, effectively giving you a blank
     * area. The default background color will be used, if you want to fill the
     * screen with a different color you will need to do this manually.
     */
    @Override
    public void clear () {
        lock.lock();
        try {
            startScreen();
            for (int y = 0; y<currentScreenSizeY; y++) {
                Arrays.fill(requestedScreen[y], null);
            }
            requestRefresh(true);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ScreenWriter getNewScreenWriter () {
        return new ScreenWriterImpl();
    }

    @Override
    public void run (Runnable runnable) {
        lock.lock();
        try {
            startScreen();
            runnable.run();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void addResizeListener (ResizeHandler handler) {
        synchronized (resizeHandlers) {
            resizeHandlers.remove(handler);
            resizeHandlers.add(handler);
        }
    }

    @Override
    public void removeResizeListener (ResizeHandler handler) {
        synchronized (resizeHandlers) {
            resizeHandlers.remove(handler);
        }
    }

    public void addKeyHandler (KeyHandler h) {
        terminal.addKeyHandler(h);
    }

    public void removeKeyHandler (KeyHandler h) {
        terminal.removeKeyHandler(h);
    }

    private void resizeDetected (TerminalXY newSize) {
        List<ResizeHandler> clone;
        synchronized (resizeHandlers) {
            clone = new ArrayList<ResizeHandler>(resizeHandlers);
        }
        for (ResizeHandler resizeHandler : clone) {
            resizeHandler.handleResize(newSize);
        }
    }

    public class ScreenWriterImpl implements ScreenWriter {
        private int          reqX;
        private int          reqY;
        private CharProps    reqProps     = CharProps.DEFAULT;
        private TabBehaviour tabBehaviour = TabBehaviour.ALIGN_8;

        @Override
        public void setTabBehaviour (TabBehaviour tabBehaviour) {
            if (tabBehaviour ==null) {
                throw new IllegalArgumentException("tabBehaviour may not be null");
            }
            this.tabBehaviour = tabBehaviour;
        }

        @Override
        public void write (Object... a) {
            lock.lock();
            try {
                startScreen();
                boolean wasDirty = dirtySomething;
                boolean intIsX   = true;
                for (Object o : a) {
                    if (o instanceof Integer) {
                        int xy = (Integer) o;
                        if (intIsX) {
                            reqX = xy;
                        }else {
                            reqY = xy;
                        }
                        intIsX = !intIsX;
                    }else if (o instanceof TerminalXY) {
                        TerminalXY xy = (TerminalXY) o;
                        reqX   = xy.getX();
                        reqY   = xy.getY();
                        intIsX = true;
                    }else if (o instanceof CharProps) {
                        reqProps = (CharProps) o;
                        intIsX   = true;
                    }else if (o instanceof CharPropsChanger) {
                        reqProps = ((CharPropsChanger) o).change(reqProps);
                        intIsX   = true;
                    }else if (o ==SET_USER_CURSOR) {
                        setUserCursor(new TerminalXY(reqX, reqY));
                    }else {
                        String str = o ==null ? "null" : o.toString();
                        str = tabBehaviour.replaceTabs(str, reqX);
                        for (char c : str.toCharArray()) {
                            setReqChar(reqX, reqY, c, reqProps);
                            reqX++;
                        }
                        intIsX = true;
                    }
                }
                if (!wasDirty && dirtySomething) {
                    requestRefresh(false);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private void setReqChar (int x, int y, char c, CharProps p) {
        if (y>=0 && y<currentScreenSizeY && x >=0 && x <currentScreenSizeX) {
            ScreenCharacter req = requestedScreen[y][x];
            if (!ScreenCharacter.safeEquals(req, c, p)) {
                // different from requested...
                ScreenCharacter cur = currentScreen[y][x];
                if (!ScreenCharacter.safeEquals(cur, c, p)) {
                    // ...and different from current
                    requestedScreen[y][x] = SPACE.equals(c, p) ? null : new ScreenCharacter(c, p);
                    dirtyChars[y][x]      = true;
                    dirtyLine[y]          = true;
                    dirtyLowChar[y]       = Math.min(dirtyLowChar[y], x);
                    dirtyHighChar[y]      = Math.max(dirtyHighChar[y], x);
                    dirtyLowLine          = Math.min(dirtyLowLine, y   );
                    dirtyHighLine         = Math.max(dirtyHighLine, y   );
                    dirtySomething        = true;
                }else {
                    // ...back to currently displayed char:
                    requestedScreen[y][x] = cur;
                    dirtyChars[y][x]      = false;
                }
            }
        }
    }

    private void refresh () {
        if (running && dirtySomething) {
            terminal.run(new Runnable() {
                @Override
                public void run () {
                    resizeScreenIfNeeded();
                    terminal.write(false);
                    if (dirtyAll) {
                        DB.t("\n<refresh-all>\n");
                        terminal.clearScreen(backgroundColor);
                        for (int y = 0; y<currentScreenSizeY; y++) {
                            final ScreenCharacter[] reqLine = requestedScreen[y];
                            for (int x = 0; x<currentScreenSizeX; x++) {
                                ScreenCharacter req = reqLine[x];
                                if (req !=null) {
                                    terminal.write(x, y, req.getProps(), req.getCharacter());
                                }
                            }
                            System.arraycopy(reqLine, 0, currentScreen[y], 0, currentScreenSizeX);
                            Arrays.fill(dirtyChars[y], false);
                        }
                        Arrays.fill(dirtyLine, false                );
                        Arrays.fill(dirtyLowChar, Integer.MAX_VALUE );
                        Arrays.fill(dirtyHighChar, Integer.MIN_VALUE);
                    }else {
                        DB.t("\n<refresh>\n");
                        for (int y = dirtyLowLine; y<=dirtyHighLine; y++) {
                            final ScreenCharacter[] curLine       = currentScreen[y];
                            final ScreenCharacter[] reqLine       = requestedScreen[y];
                            final boolean[]         dirtyCharLine = dirtyChars[y];
                            for (int x = dirtyLowChar[y]; x<=dirtyHighChar[y]; x++) {
                                if (dirtyCharLine[x]) {
                                    ScreenCharacter req = reqLine[x];
                                    ScreenCharacter cur = curLine[x];
                                    if (req !=null && !req.equals(cur)) {
                                        terminal.write(x, y, req.getProps(), req.getCharacter());
                                    }else if (req ==null && cur !=null) {
                                        terminal.write(x, y, SPACE.getProps(), SPACE.getCharacter());
                                    }
                                }
                                dirtyCharLine[x] = false;
                            }
                            System.arraycopy(reqLine, 0, curLine, 0, currentScreenSizeX);
                            dirtyLine[y]     = false;
                            dirtyLowChar[y]  = Integer.MAX_VALUE;
                            dirtyHighChar[y] = Integer.MIN_VALUE;
                        }
                    }
                    dirtyLowLine   = Integer.MAX_VALUE;
                    dirtyHighLine  = Integer.MIN_VALUE;
                    dirtyAll       = false;
                    dirtySomething = false;
                    terminal.write(userCursor, true);
                }
            });
        }
    }

    private void resizeScreenIfNeeded () {
        TerminalXY newSize = newScreenSize.getAndSet(null);
        if (newSize !=null && (newSize.getX()!=currentScreenSizeX || newSize.getY()!=currentScreenSizeY)) {
            DB.t("\n<screen-resize:["+currentScreenSizeX+","+currentScreenSizeY+"]=>"+newSize+">  ");
            final int                 numLines           = newSize.getY();
            final int                 numChars           = newSize.getX();
            final int                 numCharsToCopy     = Math.min(currentScreenSizeX, numChars);
            final int                 numLinesToCopy     = Math.min(currentScreenSizeY, numLines);

            final ScreenCharacter[][] newRequestedScreen = new ScreenCharacter[numLines][numChars];
            final ScreenCharacter[][] newCurrentScreen   = new ScreenCharacter[numLines][numChars];
            for (int y = 0; y<numLinesToCopy; y++) {
                System.arraycopy(requestedScreen[y], 0, newRequestedScreen[y], 0, numCharsToCopy);
                System.arraycopy(currentScreen[y], 0, newCurrentScreen[y], 0, numCharsToCopy    );
            }
            requestedScreen    = newRequestedScreen;
            currentScreen      = newCurrentScreen;

            currentScreenSizeX = numChars;
            currentScreenSizeY = numLines;

            dirtyChars         = new boolean[numLines][numChars];
            dirtyLine          = new boolean[numLines];
            dirtyLowChar       = new int[numLines];
            dirtyHighChar      = new int[numLines];
            Arrays.fill(dirtyLowChar, Integer.MAX_VALUE );
            Arrays.fill(dirtyHighChar, Integer.MIN_VALUE);
            dirtyLowLine  = Integer.MAX_VALUE;
            dirtyHighLine = Integer.MIN_VALUE;

            requestRefresh(true);
            
            resizeDetected(newSize);
        }
    }

    private class TerminalResizeHandler implements ResizeHandler {
        public void handleResize (TerminalXY newSize) {
            lock.lock();
            try {
                newScreenSize.set(newSize);
                requestRefresh(true);
            } finally {
                lock.unlock();
            }
        }
    }

    private void requestRefresh (boolean all) {
        dirtyAll       = all;
        dirtySomething = true;
        refreshNeededCondition.signalAll();
    }

    private class Refresher extends Thread {
        private Refresher () {
            super("Screen-Refresher");
            setDaemon(true);
        }

        @Override
        public void run () {
            lock.lock();
            try {
                if (running && dirtySomething) {
                    refresh();
                }
                while (running) {
                    // wait until there is a refresh needed (this will free the lock!):
                    refreshNeededCondition.await();

                    // wait for a quite period but prevent being locked out
                    int starvationPrevention = 0;
                    while (starvationPrevention >0 && refreshNeededCondition.await(10, TimeUnit.MILLISECONDS)) {
                        starvationPrevention--;
                    }

                    // and refresh the screen
                    refresh();
                }
            } catch (InterruptedException e) {
                DB.error("Refresher interrupted");
            } finally {
                lock.unlock();
            }
        }
    }
}
