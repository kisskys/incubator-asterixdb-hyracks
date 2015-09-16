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
package edu.uci.ics.hyracks.storage.am.lsm.common.impls;

import edu.uci.ics.hyracks.storage.am.lsm.common.api.ILSMIOOperation;

public abstract class AbstractLSMMergeOperation implements ILSMIOOperation {

    protected final Object mergePolicyInfo;
    
    public AbstractLSMMergeOperation(Object mergePolicyInfo) {
        this.mergePolicyInfo = mergePolicyInfo;
    }
    
    public Object getMergePolicyInfo() {
        return mergePolicyInfo;
    }

}
