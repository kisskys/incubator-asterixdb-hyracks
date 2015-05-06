package edu.uci.ics.hyracks.storage.am.common.api;

import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
import edu.uci.ics.hyracks.dataflow.common.comm.io.ArrayTupleBuilder;

public interface ILinearizerSearchHelper {
    public void convertPointField2TwoDoubles(byte[] in, int startOffset, double[] out) throws HyracksDataException;

    public void convertTwoDoubles2PointField(double[] in, ArrayTupleBuilder tupleBuilder) throws HyracksDataException;
    
    public void convertLong2Int64Field(long in, ArrayTupleBuilder tupleBuilder) throws HyracksDataException;
    
    public long convertInt64Field2Long(byte[] in, int startOffset) throws HyracksDataException;

    public double getQueryBottomLeftX();

    public double getQueryBottomLeftY();

    public double getQueryTopRightX();

    public double getQueryTopRightY();
}
