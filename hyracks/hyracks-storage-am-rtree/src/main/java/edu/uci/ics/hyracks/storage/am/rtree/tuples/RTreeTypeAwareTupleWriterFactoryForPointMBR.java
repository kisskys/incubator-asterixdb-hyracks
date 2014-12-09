package edu.uci.ics.hyracks.storage.am.rtree.tuples;

import edu.uci.ics.hyracks.api.dataflow.value.ITypeTraits;
import edu.uci.ics.hyracks.storage.am.common.api.ITreeIndexTupleWriter;
import edu.uci.ics.hyracks.storage.am.common.tuples.TypeAwareTupleWriterFactory;

public class RTreeTypeAwareTupleWriterFactoryForPointMBR extends TypeAwareTupleWriterFactory {

    private static final long serialVersionUID = 1L;
    private final int keyFieldCount;
    private final int valueFieldCount;

    public RTreeTypeAwareTupleWriterFactoryForPointMBR(ITypeTraits[] typeTraits, int keyFieldCount, int valueFieldCount) {
        super(typeTraits);
        this.keyFieldCount = keyFieldCount;
        this.valueFieldCount = valueFieldCount;
    }

    @Override
    public ITreeIndexTupleWriter createTupleWriter() {
        return new RTreeTypeAwareTupleWriterForPointMBR(typeTraits, keyFieldCount, valueFieldCount);
    }
}