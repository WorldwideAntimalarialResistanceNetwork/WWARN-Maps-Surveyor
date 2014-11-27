package org.wwarn.surveyor.client.core;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
/**
 * Wrapper for the following https://github.com/atonparker/bitterset impl
 */
public class BitSet {
    private JavaScriptObject bitset;

    public BitSet() {
        // create a new array
        initialise();
    }

    public BitSet(int nbits) {
        this();

        // throw an exception to be consistent
        // but (do we want to be consistent?)
        if (nbits < 0) {
            throw new NegativeArraySizeException("nbits < 0: " + nbits);
        }

        // even though the array's length is loosely kept to that of Sun's "logical
        // length," this might help in some cases where code uses size() to fill in
        // bits after constructing a BitSet, or after having one passed in as a
        // parameter.
//        setLengthWords(array, wordIndex(nbits + 31));
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
     * @return
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
