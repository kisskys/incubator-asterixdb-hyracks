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

package edu.uci.ics.hyracks.storage.am.rtree.tuples;

import edu.uci.ics.hyracks.api.dataflow.value.ITypeTraits;
import edu.uci.ics.hyracks.dataflow.common.data.accessors.ITupleReference;
import edu.uci.ics.hyracks.storage.am.common.api.ITreeIndexTupleReference;

/**
 * This class writes a point mbr as many double values as the number of dimension of the point.
 * For instance, 2d point mbr is stored as two double values (instead of four double values).
 * We assume that a rtree index tuple logically consists of consecutive secondary key fields and then value fields.
 * For instance, conceptually, if a tuple to be written by this class instance consists of
 * 1 mbr field (which is 4 double key fields) and 1 value field as
 * [0.4, 0.3, 0.4, 0.3, 1]
 * the tuple is stored as
 * [0.4, 0.3, 1]
 * where, inputKeyFieldCount, storedKeyFieldCount, valueFieldCount member variables are set to 4, 2, and 1, respectively.
 * Similarly, the associated class RTreeTypeAwareTupleRefereceForPoiintMBR instance reads
 * the stored point MBR [0.4, 0.3, 1] and generates a tuple reference which is externally shown as [0.4, 0.3, 0.4, 0.3, 1].
 * 
 * @author kisskys
 */

public class RTreeTypeAwareTupleWriterForPointMBR extends RTreeTypeAwareTupleWriter {
    private final int inputKeyFieldCount; //double field count for mbr secondary key of an input tuple
    private final int valueFieldCount; //value(or payload or primary key) field count (same for an input tuple and a stored tuple)
    private final int inputTotalFieldCount; //total field count (key + value fields) of an input tuple.
    private final int storedKeyFieldCount; //double field count to be stored for the mbr secondary key
    private final int storedTotalFieldCount; //total field count (key + value fields) of a stored tuple.

    public RTreeTypeAwareTupleWriterForPointMBR(ITypeTraits[] typeTraits, int keyFieldCount, int valueFieldCount) {
        super(typeTraits);
        this.inputKeyFieldCount = keyFieldCount;
        this.valueFieldCount = valueFieldCount;
        this.inputTotalFieldCount = keyFieldCount + valueFieldCount;
        this.storedKeyFieldCount = keyFieldCount / 2;
        this.storedTotalFieldCount = storedKeyFieldCount + valueFieldCount;
    }

    @Override
    public int bytesRequired(ITupleReference tuple) {
        int bytes = getNullFlagsBytes(tuple) + getFieldSlotsBytes(tuple);
        //key field
        for (int i = 0; i < storedKeyFieldCount; i++) {
            bytes += tuple.getFieldLength(i);
        }
        //value field
        for (int i = inputKeyFieldCount; i < inputTotalFieldCount; i++) {
            bytes += tuple.getFieldLength(i);
        }
        return bytes;
    }

    @Override
    public ITreeIndexTupleReference createTupleReference() {
        return new RTreeTypeAwareTupleReferenceForPointMBR(typeTraits, inputKeyFieldCount, valueFieldCount);
    }

    @Override
    public int writeTuple(ITupleReference tuple, byte[] targetBuf, int targetOff) {
        int runner = targetOff;
        int nullFlagsBytes = getNullFlagsBytes(tuple);
        // write null indicator bits
        for (int i = 0; i < nullFlagsBytes; i++) {
            targetBuf[runner++] = (byte) 0;
        }

        // write field slots for variable length fields which applies only to value fields in RTree 
        encDec.reset(targetBuf, runner);
        for (int i = inputKeyFieldCount; i < inputTotalFieldCount; i++) {
            if (!typeTraits[i].isFixedLength()) {
                encDec.encode(tuple.getFieldLength(i));
            }
        }
        runner = encDec.getPos();

        // write key fields
        for (int i = 0; i < storedKeyFieldCount; i++) {
            System.arraycopy(tuple.getFieldData(i), tuple.getFieldStart(i), targetBuf, runner, tuple.getFieldLength(i));
            runner += tuple.getFieldLength(i);
        }
        // write value fields
        for (int i = inputKeyFieldCount; i < inputTotalFieldCount; i++) {
            System.arraycopy(tuple.getFieldData(i), tuple.getFieldStart(i), targetBuf, runner, tuple.getFieldLength(i));
            runner += tuple.getFieldLength(i);
        }

        return runner - targetOff;
    }

    @Override
    public int writeTupleFields(ITupleReference tuple, int startField, int numFields, byte[] targetBuf, int targetOff) {
        //*interior frame tuple writer method*
        //this method is used to write only key fields of an tuple for interior frames
        throw new UnsupportedOperationException(
                "writeTupleFields(ITupleReference, int, int, byte[], int) not implemented for RTreeTypeAwareTupleWriterForPointMBR class.");
    }

    protected int getNullFlagsBytes(ITupleReference tuple) {
        return (int) Math.ceil((double) (storedTotalFieldCount) / 8.0);
    }

    protected int getFieldSlotsBytes(ITupleReference tuple) {
        int fieldSlotBytes = 0;
        for (int i = inputKeyFieldCount; i < inputTotalFieldCount; i++) {
            if (!typeTraits[i].isFixedLength()) {
                fieldSlotBytes += encDec.getBytesRequired(tuple.getFieldLength(i));
            }
        }
        return fieldSlotBytes;
    }

    @Override
    public int getCopySpaceRequired(ITupleReference tuple) {
        return bytesRequired(tuple);
    }
}
