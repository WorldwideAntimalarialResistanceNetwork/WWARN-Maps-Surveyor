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
/**
 * The MIT License (MIT)

 Copyright (c) 2013 Chris Parker

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 the Software, and to permit persons to whom the Software is furnished to do so,
 subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
    //com_github.sample
var some_externally_sourced_code = {};
(function() {
    var ADDR_BITS, BitterSet, HAMMING_TABLE, WORD_BITS, bits, bstring, weight;

    WORD_BITS = 32;

    ADDR_BITS = 5;

    HAMMING_TABLE = [0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4];

    weight = function(word) {
        return HAMMING_TABLE[(word >> 0x00) & 0xF] + HAMMING_TABLE[(word >> 0x04) & 0xF] + HAMMING_TABLE[(word >> 0x08) & 0xF] + HAMMING_TABLE[(word >> 0x0C) & 0xF] + HAMMING_TABLE[(word >> 0x10) & 0xF] + HAMMING_TABLE[(word >> 0x14) & 0xF] + HAMMING_TABLE[(word >> 0x18) & 0xF] + HAMMING_TABLE[(word >> 0x1C) & 0xF];
    };

    bits = function(word, offset) {
        var bit, _i, _ref, _results;
        if (offset == null) {
            offset = 0;
        }
        _results = [];
        for (bit = _i = 0, _ref = WORD_BITS - 1; 0 <= _ref ? _i <= _ref : _i >= _ref; bit = 0 <= _ref ? ++_i : --_i) {
            if ((word & (1 << bit)) !== 0x0) {
                _results.push(bit + offset * WORD_BITS);
            }
        }
        return _results;
    };

    bstring = function(word) {
        return (word >>> 0).toString(2);
    };

    some_externally_sourced_code.BitSet = BitterSet = (function() {
        function BitterSet() {
            this.store = [];
        }

        BitterSet.prototype.get = function(bit) {
            return (this.store[bit >> ADDR_BITS] & (1 << bit)) !== 0x0;
        };

        BitterSet.prototype.set = function(bit) {
            this.store[bit >> ADDR_BITS] |= 1 << bit;
        };

        BitterSet.prototype.clear = function(bit) {
            if (bit != null) {
                this.store[bit >> ADDR_BITS] &= ~(1 << bit);
            } else {
                this.store = [];
            }
        };

        BitterSet.prototype.wordLength = function() {
            var length, pos, _i, _ref;
            length = this.store.length;
            for (pos = _i = _ref = this.store.length - 1; _ref <= 0 ? _i <= 0 : _i >= 0; pos = _ref <= 0 ? ++_i : --_i) {
                if (this.store[pos] !== 0) {
                    break;
                }
                length--;
            }
            return length;
        };

        BitterSet.prototype.flip = function(bit) {
            this.store[bit >> ADDR_BITS] ^= 1 << bit;
        };

        BitterSet.prototype.next = function(value, from) {
            var length;
            length = this.length();
            while (this.store[from >> ADDR_BITS] == null) {
                from += WORD_BITS;
            }
            while (from < length) {
                if (this.get(from) === value) {
                    return from;
                }
                from += 1;
            }
            if (value === false) {
                return from;
            } else {
                return -1;
            }
        };

        BitterSet.prototype.previous = function(value, from) {
            while (this.store[from >> ADDR_BITS] == null) {
                from -= WORD_BITS;
            }
            while (!(from < 0)) {
                if (this.get(from) === value) {
                    return from;
                }
                from -= 1;
            }
            return -1;
        };

        BitterSet.prototype.length = function() {
            var fill, tail;
            this.cull();
            if (this.store.length === 0) {
                return 0;
            }
            fill = WORD_BITS * (this.store.length - 1);
            tail = bstring(this.store[this.store.length - 1]).length;
            return fill + tail;
        };

        BitterSet.prototype.cardinality = function() {
            var reducer;
            reducer = function(sum, word) {
                return sum + weight(word);
            };
            return this.store.reduce(reducer, 0);
        };

        BitterSet.prototype.cull = function() {
            var tail;
            while (this.store.length > 0) {
                tail = this.store[this.store.length - 1];
                if ((tail == null) || tail === 0x0) {
                    this.store.pop();
                } else {
                    break;
                }
            }
        };

        BitterSet.prototype.or = function(set) {
            var i, _i, _ref;
            if (set === this) {
                return;
            }
            for (i = _i = 0, _ref = set.store.length - 1; 0 <= _ref ? _i <= _ref : _i >= _ref; i = 0 <= _ref ? ++_i : --_i) {
                this.store[i] |= set.store[i];
            }
        };

        BitterSet.prototype.and = function(set) {
            var i, _i, _ref;
            if (set === this) {
                return;
            }
            for (i = _i = 0, _ref = this.store.length - 1; 0 <= _ref ? _i <= _ref : _i >= _ref; i = 0 <= _ref ? ++_i : --_i) {
                this.store[i] &= set.store[i] || 0;
            }
            this.cull();
        };

        BitterSet.prototype.andnot = function(set) {
            var i, _i, _ref;
            if (set === this) {
                this.clear();
            } else {
                for (i = _i = 0, _ref = this.store.length - 1; 0 <= _ref ? _i <= _ref : _i >= _ref; i = 0 <= _ref ? ++_i : --_i) {
                    this.store[i] &= ~(set.store[i] || 0);
                }
                this.cull();
            }
        };

        BitterSet.prototype.xor = function(set) {
            var i, _i, _ref;
            if (set === this) {
                this.clear();
            } else {
                for (i = _i = 0, _ref = set.store.length; 0 <= _ref ? _i <= _ref : _i >= _ref; i = 0 <= _ref ? ++_i : --_i) {
                    this.store[i] ^= set.store[i];
                }
                this.cull();
            }
        };

        BitterSet.prototype.toString = function() {
            var index, word;
            this.cull();
            return "\{" + ((function() {
                var _i, _len, _ref, _results;
                _ref = this.store;
                _results = [];
                for (index = _i = 0, _len = _ref.length; _i < _len; index = ++_i) {
                    word = _ref[index];
                    if ((word != null) && word !== 0) {
                        _results.push(bits(word, index));
                    }
                }
                return _results;
            }).call(this)) + "\}";
        };

        BitterSet.prototype.toBinaryString = function() {
            var reducer;
            this.cull();
            reducer = function(string, word, index) {
                var fill;
                fill = index > 0 ? Array(index * WORD_BITS - string.length + 1).join('0') : '';
                return bstring(word) + fill + string;
            };
            return this.store.reduce(reducer, '');
        };

        return BitterSet;

    })();

}).call(this);