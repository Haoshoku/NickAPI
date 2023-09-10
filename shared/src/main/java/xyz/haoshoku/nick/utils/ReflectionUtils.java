/*
 * MIT License
 *
 * Copyright (c) 2023 Haoshoku
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package xyz.haoshoku.nick.utils;

import com.mojang.authlib.GameProfile;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class ReflectionUtils {

    public static GameProfile getProfile( Player player ) {
        try {
            return (GameProfile) player.getClass().getMethod( "getProfile" ).invoke( player );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    public static Object getField( Object instance, String fieldName ) {
        try {
            Field field = instance.getClass().getDeclaredField( fieldName );
            field.setAccessible( true );
            return field.get( instance );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    public static void setField( Object instance, String fieldName, Object value ) {
        try {
            Field field = instance.getClass().getDeclaredField( fieldName );
            field.setAccessible( true );
            field.set( instance, value );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }



}
