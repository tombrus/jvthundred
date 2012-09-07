package com.tombrus.vthundred.screen;

import com.tombrus.vthundred.terminal.CharProps.*;
import com.tombrus.vthundred.terminal.*;
import com.tombrus.vthundred.terminal.input.*;

@SuppressWarnings({"UnusedDeclaration"})
public interface Screen {
    Object SET_USER_CURSOR = new Object();
    void         startScreen          (                      );
    void         stopScreen           (                      );

    TerminalXY   getScreenSize        (                      );
    void         setUserCursor        (TerminalXY    loc     );
    void         setBackgroundColor   (Color         color   );

    void         clear                (                      );
    ScreenWriter getNewScreenWriter   (                      );
    void         run                  (Runnable      runnable);

    void         addResizeListener    (ResizeHandler handler );
    void         removeResizeListener (ResizeHandler handler );

    void         addKeyHandler        (KeyHandler    h       );
    void         removeKeyHandler     (KeyHandler    h       );
}
