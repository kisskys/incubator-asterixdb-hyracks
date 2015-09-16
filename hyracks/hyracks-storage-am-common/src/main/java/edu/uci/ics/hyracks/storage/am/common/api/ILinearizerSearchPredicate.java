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

public interface ILinearizerSearchPredicate extends ISearchPredicate {
    public enum LinearizerSearchComparisonType {
      HILBERT_ORDER_BASED_RELATIVE_COMPARISON_BETWEETN_TWO_OBJECTS,
      COMPARISON_BETWEEN_HILBERT_VALUES_OF_TWO_OBJECTS
    };
    
    public ILinearizerSearchHelper getLinearizerSearchHelper() throws HyracksDataException;
    public LinearizerSearchComparisonType getComparisonType();
}
