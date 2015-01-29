package edu.uci.ics.hyracks.storage.am.common.api;

import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
import edu.uci.ics.hyracks.dataflow.common.data.accessors.ITupleReference;

public interface ITokenizingTupleIterator {

    public abstract void reset(ITupleReference inputTuple) throws HyracksDataException;

    public abstract boolean hasNext();

    public abstract void next() throws HyracksDataException;

    public abstract ITupleReference getTuple();

}