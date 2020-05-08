package com.github.nirmalc.es;

import org.elasticsearch.action.support.DefaultShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.BroadcastResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class TermStatsResponse  extends BroadcastResponse{

    private Map<String,TermStat> results;
    private long tookInNanoSeconds;
    public TermStatsResponse(){
        super();
    }
    public TermStatsResponse(int totalShards, int successfulShards, int failedShards,
                             List<DefaultShardOperationFailedException> shardFailures, Map<String,TermStat> termStatMap){
        super(totalShards,successfulShards,failedShards,shardFailures);
        this.results = termStatMap;
    }

    public void setTookInNanoSeconds(long took){
        tookInNanoSeconds = took;
    }
    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        int n = in.readInt();
        results = new LinkedHashMap<String, TermStat>();
        for (int i = 0; i < n; i++) {
            String text = in.readString();
            TermStat t = new TermStat();
            t.readFrom(in);
            results.put(text, t);
        }
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeInt(results.size());
        for (Map.Entry<String, TermStat> t : results.entrySet()) {
            out.writeString(t.getKey());
            t.getValue().writeTo(out);
        }
    }

    @Override
    protected void addCustomXContentFields(XContentBuilder builder, Params params) throws IOException {
        builder.field("took",tookInNanoSeconds);
        builder.startArray("termstats");
        for (Map.Entry<String, TermStat> t : this.results.entrySet()) {
            builder.startObject().field("term", t.getKey());
            t.getValue().toXContent(builder, ToXContent.EMPTY_PARAMS);
            builder.endObject();
        }
        builder.endArray();
    }
}
