/*
 * Copyright 2009-2013 by The Regents of the University of California
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License from
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.hyracks.storage.am.common.tokenizer;

import java.io.IOException;

import edu.uci.ics.hyracks.data.std.primitive.ByteArrayPointable;
import edu.uci.ics.hyracks.data.std.util.GrowableArray;
import edu.uci.ics.hyracks.dataflow.common.data.marshalling.ByteArraySerializerDeserializer;
import edu.uci.ics.hyracks.storage.am.common.api.IToken;

public class NonTaggedByteArrayToken implements IToken {
    
    private int length;
    private int tokenLength;
    private int start;
    private int tokenCount;
    private byte[] data;

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public int getTokenLength() {
        return tokenLength;
    }

    @Override
    public void reset(byte[] data, int start, int length, int tokenLength, int tokenCount) {
        this.data = data;
        this.start = start;
        this.length = length;
        this.tokenLength = tokenLength;
        this.tokenCount = tokenCount;
    }

    @Override
    public void serializeToken(GrowableArray out) throws IOException {
        out.getDataOutput().writeShort(length);
        ByteArraySerializerDeserializer.INSTANCE.serialize(data, start, length, out.getDataOutput());
        //out.getDataOutput().write(data, start, length);
    }

    @Override
    public void serializeTokenCount(GrowableArray out) throws IOException {
        return; //no op
    }
}
