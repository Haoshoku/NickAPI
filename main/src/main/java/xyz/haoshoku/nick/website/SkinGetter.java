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

package xyz.haoshoku.nick.website;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class SkinGetter {

    private final LoadingCache<UUID, String[]> cache = CacheBuilder.newBuilder().expireAfterWrite( 11, TimeUnit.MINUTES ).build( new CacheLoader<UUID, String[]>() {

        @Override
        public String[] load( UUID minecraftUUID ) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL( "https://sessionserver.mojang.com/session/minecraft/profile/"
                        + minecraftUUID.toString().replace( "-", "" ) + "?unsigned=false" ).openConnection();
                connection.setReadTimeout( 3000 );
                BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );
                JsonElement element = new JsonParser().parse( reader );
                JsonElement propertiesElement = element.getAsJsonObject().get( "properties" );
                JsonObject skinData = propertiesElement.getAsJsonArray().get( 0 ).getAsJsonObject();

                return new String[] { skinData.get( "value" ).toString().replace( "\"", "" ), skinData.get( "signature" )
                        .toString().replace( "\"", "" ) };
            } catch ( Exception e ) {
                return new String[] { "", "" };
            }
        }
    } );

    public String[] uuidToSkinData( UUID minecraftUUID ) {
        try {
            return this.cache.get( minecraftUUID );
        } catch ( ExecutionException e ) {
            return new String[] { "", "" };
        }
    }

}
