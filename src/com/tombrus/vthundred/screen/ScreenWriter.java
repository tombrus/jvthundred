package com.tombrus.vthundred.screen;

import com.tombrus.vthundred.terminal.*;

public interface ScreenWriter {
    void setTabBehaviour (TabBehaviour    tabBehaviour                              );
    void write           (Object...       a                                         );
    void clear           (                                                          );
    void clear           (int             x, int y, int w, int h                    );
    void fill            (ScreenCharacter sc                                        );
    void fill            (int             x, int y, int w, int h, ScreenCharacter sc);
    void scrollUp        (                                                          );
    void border          (CharProps       p                                         );
}
