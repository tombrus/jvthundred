package com.tombrus.vthundred.terminal.types;

public class AnsiGraphics {
    public static final char   ULCORNER                     = 0x250C;
    public static final char   URCORNER                     = 0x2510;
    public static final char   LLCORNER                     = 0x2514;
    public static final char   LRCORNER                     = 0x2518;
    public static final char   HLINE                        = 0x2500;
    public static final char   VLINE                        = 0x2502;

    public static final char   FACE_WHITE                   = 0x263A;
    public static final char   FACE_BLACK                   = 0x263B;
    public static final char   HEART                        = 0x2665;
    public static final char   CLUB                         = 0x2663;
    public static final char   DIAMOND                      = 0x2666;
    public static final char   SPADES                       = 0x2660;
    public static final char   DOT                          = 0x2022;

    public static final char   ARROW_UP                     = 0x2191;
    public static final char   ARROW_DOWN                   = 0x2193;
    public static final char   ARROW_RIGHT                  = 0x2192;
    public static final char   ARROW_LEFT                   = 0x2190;
    public static final char   BLOCK_SOLID                  = 0x2588;
    public static final char   BLOCK_DENSE                  = 0x2593;
    public static final char   BLOCK_MIDDLE                 = 0x2592;
    public static final char   BLOCK_SPARSE                 = 0x2591;

    public static final char   SINGLE_LINE_HORIZONTAL       = HLINE;
    public static final char   DOUBLE_LINE_HORIZONTAL       = 0x2550;
    public static final char   SINGLE_LINE_VERTICAL         = VLINE;
    public static final char   DOUBLE_LINE_VERTICAL         = 0x2551;

    public static final char   SINGLE_LINE_UP_LEFT_CORNER   = ULCORNER;
    public static final char   DOUBLE_LINE_UP_LEFT_CORNER   = 0x2554;
    public static final char   SINGLE_LINE_UP_RIGHT_CORNER  = URCORNER;
    public static final char   DOUBLE_LINE_UP_RIGHT_CORNER  = 0x2557;

    public static final char   SINGLE_LINE_LOW_LEFT_CORNER  = LLCORNER;
    public static final char   DOUBLE_LINE_LOW_LEFT_CORNER  = 0x255A;
    public static final char   SINGLE_LINE_LOW_RIGHT_CORNER = LRCORNER;
    public static final char   DOUBLE_LINE_LOW_RIGHT_CORNER = 0x255D;

    public static final char   SINGLE_LINE_CROSS            = 0x253C;
    public static final char   DOUBLE_LINE_CROSS            = 0x256C;

    public static final char   SINGLE_LINE_T_UP             = 0x2534;
    public static final char   SINGLE_LINE_T_DOWN           = 0x252C;
    public static final char   SINGLE_LINE_T_RIGHT          = 0x251c;
    public static final char   SINGLE_LINE_T_LEFT           = 0x2524;

    public static final char   SINGLE_LINE_T_DOUBLE_UP      = 0x256B;
    public static final char   SINGLE_LINE_T_DOUBLE_DOWN    = 0x2565;
    public static final char   SINGLE_LINE_T_DOUBLE_RIGHT   = 0x255E;
    public static final char   SINGLE_LINE_T_DOUBLE_LEFT    = 0x2561;

    public static final char   DOUBLE_LINE_T_UP             = 0x2569;
    public static final char   DOUBLE_LINE_T_DOWN           = 0x2566;
    public static final char   DOUBLE_LINE_T_RIGHT          = 0x2560;
    public static final char   DOUBLE_LINE_T_LEFT           = 0x2563;

    public static final char   DOUBLE_LINE_T_SINGLE_UP      = 0x2567;
    public static final char   DOUBLE_LINE_T_SINGLE_DOWN    = 0x2564;
    public static final char   DOUBLE_LINE_T_SINGLE_RIGHT   = 0x255F;
    public static final char   DOUBLE_LINE_T_SINGLE_LEFT    = 0x2562;

    public static char toGraph (int c) {
        int lo = c       &0xff;
        int hi = (c >>8) &0xff;
        return GRAPHS[hi]==null ? 0: GRAPHS[hi][lo];
    }

    private static final char[][] GRAPHS = new char[256][];

    static {
        enter('<', ARROW_LEFT                  );
        enter('>', ARROW_RIGHT                 );
        enter('?', CLUB                        );
        enter('?', DIAMOND                     );
        enter('?', HEART                       );
        enter('?', SPADES                      );
        enter('^', ARROW_UP                    );
        enter('`', FACE_BLACK                  );
        enter('`', FACE_WHITE                  );
        enter('a', BLOCK_DENSE                 );
        enter('a', BLOCK_MIDDLE                );
        enter('a', BLOCK_SOLID                 );
        enter('a', BLOCK_SPARSE                );
        enter('f', DOT                         );
        enter('j', DOUBLE_LINE_LOW_RIGHT_CORNER);
        enter('j', SINGLE_LINE_LOW_RIGHT_CORNER);
        enter('k', DOUBLE_LINE_UP_RIGHT_CORNER );
        enter('k', SINGLE_LINE_UP_RIGHT_CORNER );
        enter('l', DOUBLE_LINE_UP_LEFT_CORNER  );
        enter('l', SINGLE_LINE_UP_LEFT_CORNER  );
        enter('m', DOUBLE_LINE_LOW_LEFT_CORNER );
        enter('m', SINGLE_LINE_LOW_LEFT_CORNER );
        enter('n', DOUBLE_LINE_CROSS           );
        enter('n', SINGLE_LINE_CROSS           );
        enter('q', DOUBLE_LINE_HORIZONTAL      );
        enter('q', SINGLE_LINE_HORIZONTAL      );
        enter('t', DOUBLE_LINE_T_RIGHT         );
        enter('t', DOUBLE_LINE_T_SINGLE_RIGHT  );
        enter('t', SINGLE_LINE_T_DOUBLE_RIGHT  );
        enter('t', SINGLE_LINE_T_RIGHT         );
        enter('u', DOUBLE_LINE_T_LEFT          );
        enter('u', DOUBLE_LINE_T_SINGLE_LEFT   );
        enter('u', SINGLE_LINE_T_DOUBLE_LEFT   );
        enter('u', SINGLE_LINE_T_LEFT          );
        enter('v', ARROW_DOWN                  );
        enter('v', DOUBLE_LINE_T_SINGLE_UP     );
        enter('v', DOUBLE_LINE_T_UP            );
        enter('v', SINGLE_LINE_T_DOUBLE_UP     );
        enter('v', SINGLE_LINE_T_UP            );
        enter('w', DOUBLE_LINE_T_DOWN          );
        enter('w', DOUBLE_LINE_T_SINGLE_DOWN   );
        enter('w', SINGLE_LINE_T_DOUBLE_DOWN   );
        enter('w', SINGLE_LINE_T_DOWN          );
        enter('x', DOUBLE_LINE_VERTICAL        );
        enter('x', SINGLE_LINE_VERTICAL        );
    }

    private static void enter (char c, char g) {
        int lo = g       &0xff;
        int hi = (g >>8) &0xff;
        if (GRAPHS[hi] ==null) {
            GRAPHS[hi] = new char[256];
        }
        if (GRAPHS[hi][lo] !=0 && GRAPHS[hi][lo] !=c) {
            throw new IllegalArgumentException(String.format("double define of graph character: 0x%04x => %c and %c", (int) g, GRAPHS[hi][lo], c));
        }
        GRAPHS[hi][lo] = c;
    }
}
