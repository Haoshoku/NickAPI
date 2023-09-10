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

import java.util.UUID;

public class User {

    private String originalName, originalValue, originalSignature;
    private String nickedName, nickedValue, nickedSignature;

    private UUID originalUniqueId, nickedUniqueId;

    private String requestedSkinFromMinecraftName;

    private String requestedUUIDFromMinecraftName;
    private UUID requestedUUIDFromUUID;

    private boolean initialized, currentNicking;
    private long initializedNickTime;

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName( String originalName ) {
        this.originalName = originalName;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue( String originalValue ) {
        this.originalValue = originalValue;
    }

    public String getOriginalSignature() {
        return originalSignature;
    }

    public void setOriginalSignature( String originalSignature ) {
        this.originalSignature = originalSignature;
    }

    public String getNickedName() {
        return nickedName;
    }

    public void setNickedName( String nickedName ) {
        this.nickedName = nickedName;
    }

    public String getNickedValue() {
        return nickedValue;
    }

    public void setNickedValue( String nickedValue ) {
        this.nickedValue = nickedValue;
    }

    public String getNickedSignature() {
        return nickedSignature;
    }

    public void setNickedSignature( String nickedSignature ) {
        this.nickedSignature = nickedSignature;
    }

    public UUID getOriginalUniqueId() {
        return originalUniqueId;
    }

    public void setOriginalUniqueId( UUID originalUniqueId ) {
        this.originalUniqueId = originalUniqueId;
    }

    public UUID getNickedUniqueId() {
        return nickedUniqueId;
    }

    public void setNickedUniqueId( UUID nickedUniqueId ) {
        this.nickedUniqueId = nickedUniqueId;
    }

    public String getRequestedSkinFromMinecraftName() {
        return requestedSkinFromMinecraftName;
    }

    public void setRequestedSkinFromMinecraftName( String requestedSkinFromMinecraftName ) {
        this.requestedSkinFromMinecraftName = requestedSkinFromMinecraftName;
    }

    public String getRequestedUUIDFromMinecraftName() {
        return requestedUUIDFromMinecraftName;
    }

    public void setRequestedUUIDFromMinecraftName( String requestedUUIDFromMinecraftName ) {
        this.requestedUUIDFromMinecraftName = requestedUUIDFromMinecraftName;
    }

    public UUID getRequestedUUIDFromUUID() {
        return requestedUUIDFromUUID;
    }

    public void setRequestedUUIDFromUUID( UUID requestedUUIDFromUUID ) {
        this.requestedUUIDFromUUID = requestedUUIDFromUUID;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized( boolean initialized ) {
        this.initialized = initialized;
    }

    public boolean isCurrentNicking() {
        return currentNicking;
    }

    public void setCurrentNicking( boolean currentNicking ) {
        this.currentNicking = currentNicking;
    }

    public long getInitializedNickTime() {
        return initializedNickTime;
    }

    public void setInitializedNickTime( long initializedNickTime ) {
        this.initializedNickTime = initializedNickTime;
    }
}
