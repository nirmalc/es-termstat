package com.github.nirmalc.es;

import org.elasticsearch.action.support.broadcast.BroadcastRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

public class TermStatsRequest extends BroadcastRequest<TermStatsRequest> {
    private String field;
    private boolean sortByTermFrequency;
    private boolean sortByDocFrequency;
    private int size = 10;

    public TermStatsRequest(String... indices) {
        super(indices);
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setSortByTermFrequency(boolean sortByDocFrequency) {
        this.sortByTermFrequency = sortByDocFrequency;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setSortByDocFrequency(boolean sortByDocFrequency) {
        this.sortByDocFrequency = sortByDocFrequency;
    }

    public int getSize() {
        return size;
    }

    public boolean getSortByTermFrequency() {
        return sortByTermFrequency;
    }

    public boolean getSortByDocFrequency() {
        return sortByDocFrequency;
    }

    public String getField() {
        return field;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeString(field);
        out.writeInt(size);
        out.writeBoolean(sortByTermFrequency);
        out.writeBoolean(sortByDocFrequency);
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        field = in.readString();
        size = in.readInt();
        sortByTermFrequency = in.readBoolean();
        sortByDocFrequency = in.readBoolean();

    }


}
