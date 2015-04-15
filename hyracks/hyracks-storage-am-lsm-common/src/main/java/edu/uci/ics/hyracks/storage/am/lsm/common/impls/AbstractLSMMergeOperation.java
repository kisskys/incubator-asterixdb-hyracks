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
