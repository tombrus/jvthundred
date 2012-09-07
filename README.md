jvthundred
==========

This is a java library to control a vt100/ansi terminal (emulator). I only used it on a Mac, so it is only tested there ;-).

why
---
I needed something in this area and could not find anything suitable.
It needed to run on my raspberry pi so efficency was crucial.

features
---
* fully java (except stty call to set terminal in no-echo no-buffer mode!)
* _terminal_ level API
 * write to the screen at given coordinates
 * switch color and other attributes
 * get the screen size and listen for screen size changes
* _screen_ level API
 * the library keeps a copy of the screen in memory
 * you can write to it wherever you like
 * at refresh the whole screen is updated most efficiently to reflect the new off screen buffer
 * at screen resizes the library will update the whole screen to make it look good again

inspiration
---
I found something that was close but not close enough for me:
> [lanterna](http://code.google.com/p/lanterna)

Since I needed code to run on a raspberry pi,
I needed fast and efficient code and lanterna was not written with that in mind.
I took the lanterna code as an inspiration and started from there.
I managed to get condiderably more efficient code, so I decided to publish it here.
Ofcourse I do not want to claim any rights on works of others, that is why I mention this here.
If you find any resemblance in the code at certain points it is not a coincidence.

differences from _lanterna_
---
The main things I did differently from lanterna is:

* no gui things, only a _terminal_ and _screen_ level
* automatic start at the first use
* always run on a _private_ (meaning _separate_) screen
* dropped 'dos' compatability, only *nix is supported
* reading of the terminal stream and interpreting the keys (F1/ESC/...) is done in special threads
* screen refresh is done in a separet thread
* screen update is a lot more efficient
* and more...
