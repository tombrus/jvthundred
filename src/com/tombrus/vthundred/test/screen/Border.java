package com.tombrus.vthundred.test.screen;

import com.tombrus.vthundred.screen.*;
import com.tombrus.vthundred.terminal.*;
import com.tombrus.vthundred.terminal.types.*;
import com.tombrus.vthundred.test.util.*;

import java.util.*;

public class Border {
    private static Screen screen = new ScreenImpl(new UnixTerminal());

    public static void main (String[] args) {
        final Random r = new Random();
        final int dx = 40;
        final int dy = 10;
        final int maxX = screen.getScreenSize().getX()-dx;
        final int maxY = screen.getScreenSize().getY()-dy;
        for (int i=0;i<100; i++) {
            screen.getNewScreenWriter(r.nextInt(maxX), r.nextInt(maxY), 1+r.nextInt(dx), 1+r.nextInt(dy)).border(CharProps.DEFAULT);
            U.sleep(10);
        }
        U.waitForQ(screen);
    }
}
