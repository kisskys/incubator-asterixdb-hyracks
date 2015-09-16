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
