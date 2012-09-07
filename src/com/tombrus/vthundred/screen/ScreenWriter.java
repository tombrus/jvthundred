package com.tombrus.vthundred.screen;

/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: 04-09-12
 * Time: 22:14
 * To change this template use File | Settings | File Templates.
 */
public interface ScreenWriter {
    void setTabBehaviour (TabBehaviour tabBehaviour);
    void write (Object... a);
}
