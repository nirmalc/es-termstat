package com.github.nirmalc.es;

import org.elasticsearch.action.support.broadcast.BroadcastShardRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.shard.ShardId;

import java.io.IOException;

public class ShardTermStatsRequest extends BroadcastShardRequest {
    private Index index;
    private TermStatsRequest request;

    public ShardTermStatsRequest() {
    }

    public ShardTermStatsRequest(Index index, ShardId shardId, TermStatsRequest request) {
        super(shardId, request);
        this.index = index;
        this.request = request;
    }

    public int getSize() {
        return request.getSize();
    }

    public boolean getSortByTermFrequency() {
        return request.getSortByTermFrequency();
    }

    public boolean getSortByDocFrequency() {
        return request.getSortByDocFrequency();
    }

    public String getField() {
        return request.getField();
    }

    public Index getIndex() {
        return index;
    }

    public TermStatsRequest getRequest() {
        return request;
    }

   @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        index = new Index(in);
        request = new TermStatsRequest();
        request.readFrom(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        index.writeTo(out);
        request.writeTo(out);
    }
}
