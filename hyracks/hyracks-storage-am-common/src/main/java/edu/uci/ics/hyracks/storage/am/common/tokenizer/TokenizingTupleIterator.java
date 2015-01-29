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

package edu.uci.ics.hyracks.storage.am.common.tokenizer;

import java.io.IOException;

import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
import edu.uci.ics.hyracks.dataflow.common.comm.io.ArrayTupleBuilder;
import edu.uci.ics.hyracks.dataflow.common.comm.io.ArrayTupleReference;
import edu.uci.ics.hyracks.dataflow.common.data.accessors.ITupleReference;
import edu.uci.ics.hyracks.storage.am.common.api.IBinaryTokenizer;
import edu.uci.ics.hyracks.storage.am.common.api.IToken;
import edu.uci.ics.hyracks.storage.am.common.api.ITokenizingTupleIterator;

// TODO: We can possibly avoid copying the data into a new tuple here.
public class TokenizingTupleIterator implements ITokenizingTupleIterator {
    // Field that is expected to be tokenized.
    protected final int FILED_TO_BE_TOKENIZED = 0;

    protected final int tokensFieldCount;
    protected final int attachedFieldCount; //ex. primary key fields count
    protected final ArrayTupleBuilder tupleBuilder;
    protected final ArrayTupleReference tupleReference;
    protected final IBinaryTokenizer tokenizer;
    protected ITupleReference inputTuple;

    public TokenizingTupleIterator(int tokensFieldCount, int attachedFieldCount, IBinaryTokenizer tokenizer) {
        this.tokensFieldCount = tokensFieldCount; //This is always 1. We don't support tokenizing more than 1 field in a tuple
        this.attachedFieldCount = attachedFieldCount;
        this.tupleBuilder = new ArrayTupleBuilder(tokensFieldCount + attachedFieldCount);
        this.tupleReference = new ArrayTupleReference();
        this.tokenizer = tokenizer;
    }

    @Override
    public void reset(ITupleReference inputTuple) throws HyracksDataException {
        this.inputTuple = inputTuple;
        tokenizer.reset(inputTuple.getFieldData(FILED_TO_BE_TOKENIZED), inputTuple.getFieldStart(FILED_TO_BE_TOKENIZED),
                inputTuple.getFieldLength(FILED_TO_BE_TOKENIZED));
    }

    @Override
    public boolean hasNext() {
        return tokenizer.hasNext();
    }

    @Override
    public void next() throws HyracksDataException {
        tokenizer.next();
        IToken token = tokenizer.getToken();
        tupleBuilder.reset();
        // Add token field.
        try {
            token.serializeToken(tupleBuilder.getFieldData());
        } catch (IOException e) {
            throw new HyracksDataException(e);
        }
        tupleBuilder.addFieldEndOffset();
        // append attached fields.
        for (int i = 0; i < attachedFieldCount; i++) {
            tupleBuilder.addField(inputTuple.getFieldData(i + 1), inputTuple.getFieldStart(i + 1),
                    inputTuple.getFieldLength(i + 1));
        }
        // Reset tuple reference for insert operation.
        tupleReference.reset(tupleBuilder.getFieldEndOffsets(), tupleBuilder.getByteArray());
    }

    @Override
    public ITupleReference getTuple() {
        return tupleReference;
    }
}
