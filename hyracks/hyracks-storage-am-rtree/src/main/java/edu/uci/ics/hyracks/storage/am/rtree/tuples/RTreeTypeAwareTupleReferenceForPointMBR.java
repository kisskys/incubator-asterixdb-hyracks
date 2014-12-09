package edu.uci.ics.hyracks.storage.am.rtree.tuples;

import java.nio.ByteBuffer;

import edu.uci.ics.hyracks.api.dataflow.value.ITypeTraits;
import edu.uci.ics.hyracks.storage.am.common.api.ITreeIndexFrame;
import edu.uci.ics.hyracks.storage.am.common.api.ITreeIndexTupleReference;
import edu.uci.ics.hyracks.storage.am.common.tuples.VarLenIntEncoderDecoder;

public class RTreeTypeAwareTupleReferenceForPointMBR implements ITreeIndexTupleReference {
    private final int inputKeyFieldCount; //double field count for mbr secondary key of an input tuple
    private final int inputTotalFieldCount; //total field count (key + value fields) of an input tuple.
    private final int storedKeyFieldCount; //double field count to be stored for the mbr secondary key
    private final int storedTotalFieldCount; //total field count (key + value fields) of a stored tuple.

    private final ITypeTraits[] typeTraits;
    private final int nullFlagsBytes;
    private final int[] decodedFieldSlots;
    private final VarLenIntEncoderDecoder encDec;

    private ByteBuffer buf;
    private int tupleStartOff;
    private int dataStartOff;

    public RTreeTypeAwareTupleReferenceForPointMBR(ITypeTraits[] typeTraits, int keyFieldCount, int valueFieldCount) {
        this.inputKeyFieldCount = keyFieldCount;
        this.inputTotalFieldCount = keyFieldCount + valueFieldCount;
        this.storedKeyFieldCount = keyFieldCount / 2;
        this.storedTotalFieldCount = storedKeyFieldCount + valueFieldCount;

        this.typeTraits = typeTraits;
        this.nullFlagsBytes = getNullFlagsBytes();
        decodedFieldSlots = new int[inputTotalFieldCount];
        encDec = new VarLenIntEncoderDecoder();
    }

    @Override
    public void resetByTupleOffset(ByteBuffer buf, int tupleStartOff) {
        this.buf = buf;
        this.tupleStartOff = tupleStartOff;

        // decode field slots in three steps
        int field = 0;
        int cumul = 0;
        //step1. decode field slots for stored key
        for (int i = 0; i < storedKeyFieldCount; i++) {
            //key or value fields
            cumul += typeTraits[i].getFixedLength();
            decodedFieldSlots[field++] = cumul;
        }
        //step2. decode field slots for non-stored (duplicated point) key
        // this simply copies the field slots for stored key.
        for (int i = 0; i < storedKeyFieldCount; i++) {
            decodedFieldSlots[field++] = decodedFieldSlots[i];
        }
        //step3. decode field slots for value field
        encDec.reset(buf.array(), tupleStartOff + nullFlagsBytes);
        for (int i = inputKeyFieldCount; i < inputTotalFieldCount; i++) {
            if (!typeTraits[i].isFixedLength()) {
                //value fields
                cumul += encDec.decode();
                decodedFieldSlots[field++] = cumul;
            } else {
                //key or value fields
                cumul += typeTraits[i].getFixedLength();
                decodedFieldSlots[field++] = cumul;
            }
        }

        dataStartOff = encDec.getPos();
    }

    @Override
    public void resetByTupleIndex(ITreeIndexFrame frame, int tupleIndex) {
        resetByTupleOffset(frame.getBuffer(), frame.getTupleOffset(tupleIndex));
    }

    @Override
    public void setFieldCount(int fieldCount) {
        //no op
    }

    @Override
    public void setFieldCount(int fieldStartIndex, int fieldCount) {
        //no op
    }

    @Override
    public int getFieldCount() {
        return inputTotalFieldCount;
    }

    @Override
    public byte[] getFieldData(int fIdx) {
        return buf.array();
    }

    @Override
    public int getFieldLength(int fIdx) {
        if (getInternalFieldIdx(fIdx) == 0) {
            return decodedFieldSlots[0];
        } else {
            return decodedFieldSlots[getInternalFieldIdx(fIdx)] - decodedFieldSlots[getInternalFieldIdx(fIdx) - 1];
        }
    }

    @Override
    public int getFieldStart(int fIdx) {
        if (getInternalFieldIdx(fIdx) == 0) {
            return dataStartOff;
        } else {
            return dataStartOff + decodedFieldSlots[getInternalFieldIdx(fIdx) - 1];
        }
    }

    private int getInternalFieldIdx(int fIdx) {
        if (fIdx >= storedKeyFieldCount && fIdx < inputKeyFieldCount) {
            return fIdx % storedKeyFieldCount;
        } else {
            return fIdx;
        }
    }

    private int getNullFlagsBytes() {
        return (int) Math.ceil(inputTotalFieldCount / 8.0);
    }

    @Override
    public int getTupleSize() {
        return dataStartOff - tupleStartOff + decodedFieldSlots[inputTotalFieldCount - 1];
    }
}
