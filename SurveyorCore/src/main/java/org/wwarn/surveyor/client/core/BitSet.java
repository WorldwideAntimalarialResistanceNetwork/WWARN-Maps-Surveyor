package org.wwarn.surveyor.client.core;

/*
 * #%L
 * SurveyorCore
 * %%
 * Copyright (C) 2013 - 2014 University of Oxford
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the University of Oxford nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ResourceCallback;
import com.google.gwt.resources.client.ResourceException;
import com.google.gwt.resources.client.TextResource;
import org.wwarn.surveyor.client.resources.Resources;

import java.util.Set;

/**
 * Wrapper for the following https://github.com/atonparker/bitterset impl
 * TODO make this an external package and add to github
 */
public class BitSet  {
    static{
        if (!isLoaded()) {
            String text = Resources.IMPL.bitSetScript().getText();
            ScriptInjector.fromString(text).setWindow(ScriptInjector.TOP_WINDOW).inject();
        }
    }


    public static native boolean isLoaded()/*-{
        if (typeof $wnd.some_externally_sourced_code === "undefined" || typeof $wnd.some_externally_sourced_code.BitSet === "undefined"  || $wnd.some_externally_sourced_code.BitSet === null) {
            return false;
        }
        return true;
    }-*/;

    private JavaScriptObject bitset;

    public BitSet() {
        initialise();
    }


    /**
     * Sets the bit on position pos to true
     * @param pos
     */
    public void set(int pos){
        setPos(pos);
    }

    /**
     * Returns whether the bit on position pos is set
     * @param pos
     * @return
     */
    public boolean get(int pos){
        return getPos(pos);
    }

    /**
     * clears the bit on position pos
     * @param pos
     */
    public void clear(int pos) {
        clearPos(pos);
    }

    /**
     * Get the logical length of the bitset
     * @return
     */
    public int length() {
        return getLength();
    }

    /**
     * Get the word-length of the bitset - a performance feature
     * @return
     */
    public int wordLength() {
        return getWordLength();
    }

    /**
     * returns how many bits are set to true in the bitset
     * @return
     */
    public int cardinality(){
        return getCardinality();
    }

    /**
     * returns a string representation of the bitset
     * @return
     */
    public String toString() {
        return getStringRepresentation();
    }

    /**
     * BitSet#toBinaryString - returns a binary string representation of the bitset
     */
    public String toBinaryString(){
        return getBinaryString();
    }

    /**
     * BitSet#or(bitset) - OR's this bitset with the argument bitset
     * @param bitSet
     */
    public void or(BitSet bitSet){
        getOr(bitSet.bitset);
    }

    /**
     *  BitSet#and(bitset) - AND's this bitset with the argument bitset
     * @param bitSet
     */
    public void and(BitSet bitSet){
        getAnd(bitSet.bitset);
    }

    /**
     * BitSet#andNot(bitset) - ANDNOT's this bitset with the argument bitset
     * @param bitSet
     */
    public void andNot(BitSet bitSet){
        getAndNot(bitSet.bitset);
    }

    /**
     * BitSet#xor(bitset) - XOR's this bitset with the argument bitset
     */
    public void xor(BitSet bitSet){
        applyXOR(bitSet.bitset);
    }

    private native void applyXOR(JavaScriptObject bitSet) /*-{
        this.@org.wwarn.surveyor.client.core.BitSet::bitset.xor(bitSet);
    }-*/;

    private native void getAndNot(JavaScriptObject bitSet) /*-{
        this.@org.wwarn.surveyor.client.core.BitSet::bitset.andnot(bitSet);
    }-*/;

    private native void getAnd(JavaScriptObject bitSet) /*-{
        this.@org.wwarn.surveyor.client.core.BitSet::bitset.and(bitSet);
    }-*/;

    public JavaScriptObject getBitset() {
        return bitset;
    }

    private native void getOr(JavaScriptObject bitSet) /*-{
        this.@org.wwarn.surveyor.client.core.BitSet::bitset.or(bitSet);
    }-*/;

    private native String getBinaryString() /*-{
        return this.@org.wwarn.surveyor.client.core.BitSet::bitset.toBinaryString();
    }-*/;

    private native String getStringRepresentation() /*-{
        return this.@org.wwarn.surveyor.client.core.BitSet::bitset.toString();
    }-*/;

    private native int getCardinality() /*-{
        return this.@org.wwarn.surveyor.client.core.BitSet::bitset.cardinality();
    }-*/;

    private native int getWordLength() /*-{
        return this.@org.wwarn.surveyor.client.core.BitSet::bitset.wordLength();
    }-*/;

    private native int getLength() /*-{
        return this.@org.wwarn.surveyor.client.core.BitSet::bitset.length();
    }-*/;

    private native void clearPos(int pos) /*-{
        return this.@org.wwarn.surveyor.client.core.BitSet::bitset.clear(pos);
    }-*/;

    private native boolean getPos(int pos) /*-{
        return this.@org.wwarn.surveyor.client.core.BitSet::bitset.get(pos);
    }-*/;

    private native void initialise() /*-{
        this.@org.wwarn.surveyor.client.core.BitSet::bitset = new $wnd.some_externally_sourced_code.BitSet();
    }-*/;

    private native void setPos(int pos) /*-{
        this.@org.wwarn.surveyor.client.core.BitSet::bitset.set(pos);
    }-*/;

    private BitSet(JsArrayInteger array) {
//        this.array = array;
    }

}
