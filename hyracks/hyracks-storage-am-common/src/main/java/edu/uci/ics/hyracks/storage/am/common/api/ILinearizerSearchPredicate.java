package edu.uci.ics.hyracks.storage.am.common.api;

import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;

public interface ILinearizerSearchPredicate extends ISearchPredicate {
    public enum LinearizerSearchComparisonType {
      HILBERT_ORDER_BASED_RELATIVE_COMPARISON_BETWEETN_TWO_OBJECTS,
      COMPARISON_BETWEEN_HILBERT_VALUES_OF_TWO_OBJECTS
    };
    
    public ILinearizerSearchHelper getLinearizerSearchHelper() throws HyracksDataException;
    public LinearizerSearchComparisonType getComparisonType();
}
