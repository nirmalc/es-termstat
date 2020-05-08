package com.github.nirmalc.es;

import org.apache.lucene.util.LuceneTestCase;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.test.ESTestCase;
import org.junit.Assert;


public class ShardTermStatsRequestTests extends ESTestCase {

    // Test serialization ( and deserialization) for internal actions
    public void testReadWrite() throws Exception {
        Index index = new Index("foo", "bar");
        ShardId shardId = new ShardId(index, 0);
        TermStatsRequest termStatsRequest = new TermStatsRequest("foo");
        termStatsRequest.setField("field1");
        termStatsRequest.setSize(100);
        termStatsRequest.setSortByDocFrequency(true);
        termStatsRequest = termStatsRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
        ShardTermStatsRequest shardTermStatsRequest = new ShardTermStatsRequest(index, shardId, termStatsRequest);
        BytesStreamOutput bytesStreamOutput = new BytesStreamOutput();
        shardTermStatsRequest.writeTo(bytesStreamOutput);
        ShardTermStatsRequest shardTermStatsRequest1 = new ShardTermStatsRequest();
        shardTermStatsRequest1.readFrom(bytesStreamOutput.bytes().streamInput());
        assertEquals("expected to be equal", shardTermStatsRequest.getSize(), shardTermStatsRequest1.getSize());
    }

}