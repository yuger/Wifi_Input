/**
 * WiFi Keyboard - Remote Keyboard for Android.
 * Copyright (C) 2011 Ivan Volosyuk
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.wxy.wifiinput;

import android.view.KeyEvent;

final class KeycodeConvertor {
    static int convertKey(int code) {
        if (code >= 65 && code <= 90) {
            return code - 65 + KeyEvent.KEYCODE_A;
        }
        if (code >= 48 && code <= 57) {
            return code - 48 + KeyEvent.KEYCODE_0;
        }
        switch (code) {
            case 9:
                return KeyEvent.KEYCODE_TAB;
            case 32:
                return KeyEvent.KEYCODE_SPACE;
            case 188:
                return KeyEvent.KEYCODE_COMMA;
            case 190:
                return KeyEvent.KEYCODE_PERIOD;
            case 13:
                return KeyEvent.KEYCODE_ENTER;
            case 219:
                return KeyEvent.KEYCODE_LEFT_BRACKET;
            case 221:
                return KeyEvent.KEYCODE_RIGHT_BRACKET;
            case 220:
                return KeyEvent.KEYCODE_BACKSLASH;
            case 186:
                return KeyEvent.KEYCODE_SEMICOLON;
            case 222:
                return KeyEvent.KEYCODE_APOSTROPHE;
            case 8:
                return KeyEvent.KEYCODE_DEL;
            case 189:
                return KeyEvent.KEYCODE_MINUS;
            case 187:
                return KeyEvent.KEYCODE_EQUALS;
            case 191:
                return KeyEvent.KEYCODE_SLASH;
            case 18:
                return KeyEvent.KEYCODE_ALT_LEFT;
            case 16:
                return KeyEvent.KEYCODE_SHIFT_LEFT;
            // public static final int KEYCODE_DPAD_UP = 19;
            // public static final int KEYCODE_DPAD_DOWN = 20;
            // public static final int KEYCODE_DPAD_LEFT = 21;
            // public static final int KEYCODE_DPAD_RIGHT = 22;
            // public static final int KEYCODE_DPAD_CENTER = 23;
            // arrow keys
            case 38:
                return KeyEvent.KEYCODE_DPAD_UP;
            case 40:
                return KeyEvent.KEYCODE_DPAD_DOWN;
            case 37:
                return KeyEvent.KEYCODE_DPAD_LEFT;
            case 39:
                return KeyEvent.KEYCODE_DPAD_RIGHT;
            // Insert
            case 112:
                return KeyEvent.KEYCODE_DPAD_CENTER;
            case 45:
                return KeyEvent.KEYCODE_DPAD_CENTER;
            // ESC
            case 27:
                return KeyEvent.KEYCODE_BACK;
            case 116:
                return KeyEvent.KEYCODE_BACK;
            // Home
            case 113:
                return KeyEvent.KEYCODE_MENU;
            case 114:
                return KeyEvent.KEYCODE_SEARCH;
            case 121:
                return KeyEvent.KEYCODE_VOLUME_UP;
            case 120:
                return KeyEvent.KEYCODE_VOLUME_DOWN;
            case KeyboardHttpServer.FOCUS:
                return KeyboardHttpServer.FOCUS;
            case 36:
                return WiFiInputMethod.KEY_HOME;
            case 35:
                return WiFiInputMethod.KEY_END;
            case 17:
                return WiFiInputMethod.KEY_CONTROL;
            case 46:
                return WiFiInputMethod.KEY_DEL;
            default:
                return -1;
        }
    }
}
