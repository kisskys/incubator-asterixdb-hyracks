package edu.uci.ics.hyracks.storage.am.lsm.rtree.tuples;

import edu.uci.ics.hyracks.api.dataflow.value.ITypeTraits;
import edu.uci.ics.hyracks.storage.am.common.api.ITreeIndexTupleWriter;
import edu.uci.ics.hyracks.storage.am.common.tuples.TypeAwareTupleWriterFactory;

public class LSMRTreeTupleWriterFactoryForPointMBR extends TypeAwareTupleWriterFactory {

    private static final long serialVersionUID = 1L;
    private final int keyFieldCount;
    private final int valueFieldCount;
    private final boolean antimatterAware;

    public LSMRTreeTupleWriterFactoryForPointMBR(ITypeTraits[] typeTraits, int keyFieldCount,
            int valueFieldCount, boolean antimatterAware) {
        super(typeTraits);
        this.keyFieldCount = keyFieldCount;
        this.valueFieldCount = valueFieldCount;
        this.antimatterAware = antimatterAware;
    }

    @Override
    public ITreeIndexTupleWriter createTupleWriter() {
        return new LSMRTreeTupleWriterForPointMBR(typeTraits, keyFieldCount, valueFieldCount, antimatterAware);
    }
}