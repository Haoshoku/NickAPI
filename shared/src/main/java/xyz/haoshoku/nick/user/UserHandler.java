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

package xyz.haoshoku.nick.user;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserHandler {

    private static final ConcurrentHashMap<UUID, User> UUID_USER_CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();

    public static void createUser( UUID uuid ) {
        UserHandler.UUID_USER_CONCURRENT_HASH_MAP.put( uuid, new User() );
    }

    public static User getUser( UUID uuid ) {
        return UserHandler.UUID_USER_CONCURRENT_HASH_MAP.get( uuid );
    }

    public static void deleteUser( UUID uuid ) {
        UserHandler.UUID_USER_CONCURRENT_HASH_MAP.remove( uuid );
    }

    public static User[] getUsers() {
        User[] users = new User[ UserHandler.UUID_USER_CONCURRENT_HASH_MAP.size() ];
        int count = 0;
        for ( Map.Entry<UUID, User> userEntry : UserHandler.UUID_USER_CONCURRENT_HASH_MAP.entrySet() ) {
            users[count] = userEntry.getValue();
            count++;
        }
        return users;
    }

}
