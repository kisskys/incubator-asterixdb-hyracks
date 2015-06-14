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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
import edu.uci.ics.hyracks.storage.am.common.api.IndexException;
import edu.uci.ics.hyracks.storage.am.common.impls.NoOpOperationCallback;
import edu.uci.ics.hyracks.storage.am.lsm.common.api.ILSMComponent;
import edu.uci.ics.hyracks.storage.am.lsm.common.api.ILSMComponent.ComponentState;
import edu.uci.ics.hyracks.storage.am.lsm.common.api.ILSMIndex;
import edu.uci.ics.hyracks.storage.am.lsm.common.api.ILSMIndexAccessor;
import edu.uci.ics.hyracks.storage.am.lsm.common.api.ILSMMergePolicy;
import edu.uci.ics.hyracks.storage.am.lsm.common.api.ILSMOperationTracker;

public class PrefixMergePolicy implements ILSMMergePolicy {

    private long maxMergableComponentSize;
    private int maxToleranceComponentCount;

    @Override
    public void diskComponentAdded(final ILSMIndex index, boolean fullMergeIsRequested,
            AbstractDiskLSMComponent newComponent, boolean isMergeOp) throws HyracksDataException, IndexException {

        /**
         * [for flow-control purpose]
         * 
         * when merge operations are lagged, threads which flushed components will be blocked
         * until merge operations catch up, i.e, until the number of mergable immutable components <= maxToleranceComponentCount
         * example:
         * suppose that maxToleranceComponentCount = 3 and maxMergableComponentSize = 1GB
         * The following shows a set of events occurred in time ti with a brief description.
         * time
         * t40: c32-1(1GB, RU) c38-33(192MB, RU) c39-39(32MB, RU) c40-40(32MB, RU)
         * --> a thread which added c40-40 will trigger a merge including c38-33,c39-39,c40-40
         * t41: c32-1(1GB, RU) c38-33(192MB, RUM) c39-39(32MB, RUM) c40-40(32MB, RUM) c41-41(32MB, RU)
         * --> a thread which added c41-41 will not be blocked
         * t42: c32-1(1GB, RU) c38-33(192MB, RUM) c39-39(32MB, RUM) c40-40(32MB, RUM) c41-41(32MB, RU) c42-42(32MB, RU)
         * --> a thread which added c42-42 will not be blocked
         * t43: c32-1(1GB, RU) c38-33(192MB, RUM) c39-39(32MB, RUM) c40-40(32MB, RUM) c41-41(32MB, RU) c42-42(32MB, RU) c43-43(32MB, RU)
         * --> a thread which added c43-43 will not be blocked and will not trigger a merge since there is an ongoing merge triggered in t1.
         * t44: c32-1(1GB, RU) c38-33(192MB, RUM) c39-39(32MB, RUM) c40-40(32MB, RUM) c41-41(32MB, RU) c42-42(32MB, RU) c43-43(32MB, RU) c44-44(32MB, RU)
         * --> a thread which added c44-44 will be blocked until the number of RU components < maxToleranceComponentCount
         * t45: c32-1(1GB, RU) *c40-33(256MB, RU)* c41-41(32MB, RU) c42-42(32MB, RU) c43-43(32MB, RU) c44-44(32MB, RU)
         * --> a thread which completed the merge triggered in t1 added c40-33 and will go ahead and trigger the next merge with c40-33,c41-41,c42-42,c43-43,c44-44.
         * Still, the blocked thread will continue being blocked.
         * t46: c32-1(1GB, RU) c40-33(256MB, RUM) c41-41(32MB, RUM) c42-42(32MB, RUM) c43-43(32MB, RUM) c44-44(32MB, RUM)
         * --> the merge triggered in t6 is going on
         * t47: c32-1(1GB, RU) *c44-33(320MB, RU)*
         * --> a thread completed the merge triggered in t6 and added c44-33 and this will unblock the blocked thread in t5
         * t48: c32-1(1GB, RU) *c44-33(320MB, RU)* c48-48(32MB, RU)
         * --> a thread added c48-48 and will not be blocked.
         * ... continues ...
         * 
         * ----------------------------------------
         * legend:
         * For example, C32-1 represents a disk component, more specifically, disk component name, where 32-1 represents a timestamp range from t1 to time t32.
         * This means that the component C32-1 is a component resulting from a merge operation that merged components C1-1 to C32-32.
         * This also implies that if two timestamps in a component name are equal, the component has not been merged yet after it was created.
         * RU and RUM are possible state of disk components, where RU represents READABLE_UNWRITABLE and RUM represents READABLE_UNWRITABLE_MERGING.
         * Now, c32-1(1GB, RU) represents a disk component resulted from merging c1-1 ~ c32-32 and the component size is 1GB.
         * ----------------------------------------
         * 
         * The flow control allows at most (maxToleranceComponentCount + 1) mergable components,
         * where the mergable components are disk components whose i) state == RU and ii) size < maxMergableComponentSize.
         */

        ILSMOperationTracker opTracker = ((AbstractLSMIndex) index).getOperationTracker();
        if (isMergeOp) {
            opTracker.notifyAll();
        } else {
            if (newComponent.getComponentSize() <= maxMergableComponentSize) {
                boolean wasBlocked = false;
                while (true) {
                    List<ILSMComponent> immutableComponents = index.getImmutableComponents();
                    int totalImmutableComponentCount = immutableComponents.size();
                    int mergableImmutableComponentCount = 0;
                    int i = 0;
                    for (i = 0; i < totalImmutableComponentCount; i++) {
                        AbstractDiskLSMComponent c = (AbstractDiskLSMComponent) immutableComponents.get(i);
                        if (c.getComponentSize() > maxMergableComponentSize) {
                            break;
                        }
                        if (c.getState() != ComponentState.READABLE_UNWRITABLE) {
                            break;
                        }
                        ++mergableImmutableComponentCount;
                        if (mergableImmutableComponentCount > maxToleranceComponentCount) {
                            break;
                        }
                    }

                    if (mergableImmutableComponentCount > maxToleranceComponentCount) {
                        synchronized (opTracker) {
                            try {
                                wasBlocked = true;
                                opTracker.wait();
                            } catch (InterruptedException e) {
                                //ignore
                            }
                        }
                        continue;
                    } else {
                        break;
                    }
                }
                if (wasBlocked) {
                    return;
                }
            }
        }

        // 1.  Look at the candidate components for merging in oldest-first order.  If one exists, identify the prefix of the sequence of
        // all such components for which the sum of their sizes exceeds MaxMrgCompSz.  Schedule a merge of those components into a new component.
        // 2.  If a merge from 1 doesn't happen, see if the set of candidate components for merging exceeds MaxTolCompCnt.  If so, schedule
        // a merge all of the current candidates into a new single component.
        List<ILSMComponent> immutableComponents = new ArrayList<ILSMComponent>(index.getImmutableComponents());
        // Reverse the components order so that we look at components from oldest to newest.
        Collections.reverse(immutableComponents);

        for (ILSMComponent c : immutableComponents) {
            if (c.getState() != ComponentState.READABLE_UNWRITABLE) {
                return;
            }
        }
        if (fullMergeIsRequested) {
            ILSMIndexAccessor accessor = (ILSMIndexAccessor) index.createAccessor(NoOpOperationCallback.INSTANCE,
                    NoOpOperationCallback.INSTANCE);
            accessor.scheduleFullMerge(index.getIOOperationCallback());
            return;
        }
        long totalSize = 0;
        int startIndex = -1;
        for (int i = 0; i < immutableComponents.size(); i++) {
            ILSMComponent c = immutableComponents.get(i);
            long componentSize = ((AbstractDiskLSMComponent) c).getComponentSize();
            if (componentSize > maxMergableComponentSize) {
                startIndex = i;
                totalSize = 0;
                continue;
            }
            totalSize += componentSize;
            boolean isLastComponent = i + 1 == immutableComponents.size() ? true : false;
            if (totalSize > maxMergableComponentSize
                    || (isLastComponent && i - startIndex >= maxToleranceComponentCount)) {
                List<ILSMComponent> mergableComponents = new ArrayList<ILSMComponent>();
                for (int j = startIndex + 1; j <= i; j++) {
                    mergableComponents.add(immutableComponents.get(j));
                }
                // Reverse the components order back to its original order
                Collections.reverse(mergableComponents);
                ILSMIndexAccessor accessor = (ILSMIndexAccessor) index.createAccessor(NoOpOperationCallback.INSTANCE,
                        NoOpOperationCallback.INSTANCE);
                accessor.scheduleMerge(index.getIOOperationCallback(), mergableComponents, null);
                break;
            }
        }
    }

    @Override
    public void configure(Map<String, String> properties) {
        maxMergableComponentSize = Long.parseLong(properties.get("max-mergable-component-size"));
        maxToleranceComponentCount = Integer.parseInt(properties.get("max-tolerance-component-count"));
    }
}
