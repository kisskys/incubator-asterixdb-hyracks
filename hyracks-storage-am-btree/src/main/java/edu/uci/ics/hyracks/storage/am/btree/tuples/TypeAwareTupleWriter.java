/*
 * Copyright 2009-2010 by The Regents of the University of California
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

package edu.uci.ics.hyracks.storage.am.btree.tuples;

import java.nio.ByteBuffer;

import edu.uci.ics.hyracks.api.dataflow.value.ITypeTrait;
import edu.uci.ics.hyracks.dataflow.common.data.accessors.ITupleReference;
import edu.uci.ics.hyracks.storage.am.btree.api.IBTreeTupleReference;
import edu.uci.ics.hyracks.storage.am.btree.api.IBTreeTupleWriter;

public class TypeAwareTupleWriter implements IBTreeTupleWriter {

    private ITypeTrait[] typeTraits;
    private VarLenIntEncoderDecoder encDec = new VarLenIntEncoderDecoder();

    public TypeAwareTupleWriter(ITypeTrait[] typeTraits) {
        this.typeTraits = typeTraits;
    }

    @Override
    public int bytesRequired(ITupleReference tuple) {
        int bytes = getNullFlagsBytes(tuple) + getFieldSlotsBytes(tuple);
        for (int i = 0; i < tuple.getFieldCount(); i++) {
            bytes += tuple.getFieldLength(i);
        }
        return bytes;
    }

    @Override
    public int bytesRequired(ITupleReference tuple, int startField, int numFields) {
        int bytes = getNullFlagsBytes(tuple, startField, numFields) + getFieldSlotsBytes(tuple, startField, numFields);
        for (int i = startField; i < startField + numFields; i++) {
            bytes += tuple.getFieldLength(i);
        }
        return bytes;
    }

    @Override
    public IBTreeTupleReference createTupleReference() {
        return new TypeAwareTupleReference(typeTraits);
    }

    @Override
    public int writeTuple(ITupleReference tuple, ByteBuffer targetBuf, int targetOff) {
        int runner = targetOff;
        int nullFlagsBytes = getNullFlagsBytes(tuple);
        // write null indicator bits
        for (int i = 0; i < nullFlagsBytes; i++) {
            targetBuf.put(runner++, (byte) 0);
        }

        // write field slots for variable length fields
        encDec.reset(targetBuf.array(), runner);
        for (int i = 0; i < tuple.getFieldCount(); i++) {
            if (typeTraits[i].getStaticallyKnownDataLength() == ITypeTrait.VARIABLE_LENGTH) {
                encDec.encode(tuple.getFieldLength(i));
            }
        }
        runner = encDec.getPos();

        // write data fields
        for (int i = 0; i < tuple.getFieldCount(); i++) {
            System.arraycopy(tuple.getFieldData(i), tuple.getFieldStart(i), targetBuf.array(), runner, tuple
                    .getFieldLength(i));
            runner += tuple.getFieldLength(i);
        }

        return runner - targetOff;
    }

    @Override
    public int writeTupleFields(ITupleReference tuple, int startField, int numFields, ByteBuffer targetBuf,
            int targetOff) {
        int runner = targetOff;
        int nullFlagsBytes = getNullFlagsBytes(tuple, startField, numFields);
        // write null indicator bits
        for (int i = 0; i < nullFlagsBytes; i++) {
            targetBuf.put(runner++, (byte) 0);
        }

        // write field slots for variable length fields
        encDec.reset(targetBuf.array(), runner);
        for (int i = startField; i < startField + numFields; i++) {
            if (typeTraits[i].getStaticallyKnownDataLength() == ITypeTrait.VARIABLE_LENGTH) {
                encDec.encode(tuple.getFieldLength(i));
            }
        }
        runner = encDec.getPos();

        for (int i = startField; i < startField + numFields; i++) {
            System.arraycopy(tuple.getFieldData(i), tuple.getFieldStart(i), targetBuf.array(), runner, tuple
                    .getFieldLength(i));
            runner += tuple.getFieldLength(i);
        }

        return runner - targetOff;
    }

    private int getNullFlagsBytes(ITupleReference tuple) {
        return (int) Math.ceil((double) tuple.getFieldCount() / 8.0);
    }

    private int getFieldSlotsBytes(ITupleReference tuple) {
        int fieldSlotBytes = 0;
        for (int i = 0; i < tuple.getFieldCount(); i++) {
            if (typeTraits[i].getStaticallyKnownDataLength() == ITypeTrait.VARIABLE_LENGTH) {
                fieldSlotBytes += encDec.getBytesRequired(tuple.getFieldLength(i));
            }
        }
        return fieldSlotBytes;
    }

    private int getNullFlagsBytes(ITupleReference tuple, int startField, int numFields) {
        return (int) Math.ceil((double) numFields / 8.0);
    }

    private int getFieldSlotsBytes(ITupleReference tuple, int startField, int numFields) {
        int fieldSlotBytes = 0;
        for (int i = startField; i < startField + numFields; i++) {
            if (typeTraits[i].getStaticallyKnownDataLength() == ITypeTrait.VARIABLE_LENGTH) {
                fieldSlotBytes += encDec.getBytesRequired(tuple.getFieldLength(i));
            }
        }
        return fieldSlotBytes;
    }

    public ITypeTrait[] getTypeTraits() {
        return typeTraits;
    }

    public void setTypeTraits(ITypeTrait[] typeTraits) {
        this.typeTraits = typeTraits;
    }
}