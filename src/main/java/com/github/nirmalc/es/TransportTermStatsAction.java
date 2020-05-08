package com.github.nirmalc.es;


import org.apache.lucene.index.IndexReader;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.search.FieldComparator;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.DefaultShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.BroadcastShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.TransportBroadcastAction;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.routing.GroupShardsIterator;
import org.elasticsearch.cluster.routing.ShardIterator;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class TransportTermStatsAction extends TransportBroadcastAction<TermStatsRequest, TermStatsResponse, ShardTermStatsRequest, ShardTermStatsResponse> {


    private final IndicesService indicesService;

    @Inject
    public TransportTermStatsAction(String actionName, ClusterService clusterService, IndicesService indicesService, TransportService transportService, ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver, String shardExecutor) {
        super(actionName, clusterService, transportService, actionFilters, indexNameExpressionResolver, TermStatsRequest::new, ShardTermStatsRequest::new, ThreadPool.Names.GENERIC);
        this.indicesService = indicesService;
    }


    private void addTermStats(Map<String, TermStat> result, Map<String, TermStat> toAdd) {
        for (Map.Entry<String, TermStat> entry : toAdd.entrySet()) {
            if (!result.containsKey(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            } else {
                TermStat termStat = entry.getValue();
                termStat.add(result.get(entry.getKey()));
                result.replace(entry.getKey(), termStat);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private final Map<String, TermStat> sortFinal(Map<String, TermStat> termStatMap, boolean sortByTF, int size) {
        List<Map.Entry<String, TermStat>> finalList = new LinkedList<>(termStatMap.entrySet());
        Collections.sort(finalList, (Comparator<Object>) (o1, o2) -> {
            if (sortByTF) {
                return Long.compare(((Map.Entry<String, TermStat>) o1).getValue().getTermFrequency(), ((Map.Entry<String, TermStat>) o2).getValue().getTermFrequency());
            } else {
                return Long.compare(((Map.Entry<String, TermStat>) o1).getValue().getDocFrequency(), ((Map.Entry<String, TermStat>) o2).getValue().getDocFrequency());
            }
        });
        Map<String, TermStat> result = new LinkedHashMap<>();
        int i = 0;
        for (Iterator<Map.Entry<String, TermStat>> it = finalList.iterator(); it.hasNext(); ) {
            if (i > size)
                break;
            Map.Entry<String, TermStat> entry = it.next();
            result.put(entry.getKey(), entry.getValue());
            i++;
        }
        return termStatMap;
    }

    @Override
    protected TermStatsResponse newResponse(TermStatsRequest request, AtomicReferenceArray shardsResponses, ClusterState clusterState) {
        int successfulShards = 0;
        int failedShards = 0;
        List<DefaultShardOperationFailedException> shardFailures = null;
        Map<String, TermStat> map = new LinkedHashMap<>();
        for (int i = 0; i < shardsResponses.length(); i++) {
            Object shardResponse = shardsResponses.get(i);
            if (shardResponse instanceof BroadcastShardOperationFailedException) {
                BroadcastShardOperationFailedException e = (BroadcastShardOperationFailedException) shardResponse;
                logger.error(e.getMessage(), e);
                failedShards++;
                if (shardFailures == null) {
                    shardFailures = new ArrayList<DefaultShardOperationFailedException>();
                }
                shardFailures.add(new DefaultShardOperationFailedException(e));
            } else {
                if (shardResponse instanceof ShardTermStatsResponse) {

                    successfulShards++;
                    ShardTermStatsResponse resp = (ShardTermStatsResponse) shardResponse;
                    addTermStats(map, resp.getResults());
                }
            }
        }
        if (shardsResponses.length() == 0) {
            return new TermStatsResponse(shardsResponses.length(), successfulShards, failedShards, shardFailures, map);
        } else {
            return new TermStatsResponse(shardsResponses.length(), successfulShards, failedShards, shardFailures, sortFinal(map, request.getSortByTermFrequency(), request.getSize()));
        }
    }

    @Override
    protected ShardTermStatsRequest newShardRequest(int numShards, ShardRouting shard, TermStatsRequest request) {
        return new ShardTermStatsRequest(shard.index(), shard.shardId(), request);
    }

    @Override
    protected ShardTermStatsResponse newShardResponse() {
        return new ShardTermStatsResponse(new LinkedHashMap<>());
    }

    @Override
    protected ShardTermStatsResponse shardOperation(ShardTermStatsRequest request, Task task) throws IOException {
        IndexShard indexShard = indicesService.indexServiceSafe(request.getIndex()).getShard(request.shardId().id());
        Engine.Searcher searcher = indexShard.acquireSearcher("termstats");
        Map<String, TermStat> stringTermStatMap = new LinkedHashMap<>();
        try {
            IndexReader indexReader = searcher.reader();
            Comparator<TermStats> termStatsComparator = request.getSortByTermFrequency() ? (Comparator<TermStats>) new HighFreqTerms.TotalTermFreqComparator() : new HighFreqTerms.DocFreqComparator();

            TermStats[] highFreqTerms = HighFreqTerms.getHighFreqTerms(indexReader, request.getSize(), request.getField(), termStatsComparator);
            for (TermStats termStats : highFreqTerms) {
                TermStat termStat = new TermStat(termStats.termtext.utf8ToString(), termStats.totalTermFreq, termStats.docFreq);
                stringTermStatMap.put(termStats.termtext.utf8ToString(), termStat);

            }
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new ElasticsearchException(ex.getMessage(), ex);
        } finally {
            searcher.close();
        }
        return new ShardTermStatsResponse(stringTermStatMap);

    }

    @Override
    protected GroupShardsIterator<ShardIterator> shards(ClusterState clusterState, TermStatsRequest request, String[] concreteIndices) {
        return clusterState.routingTable().activePrimaryShardsGrouped(concreteIndices, true);
    }

    @Override
    protected ClusterBlockException checkGlobalBlock(ClusterState state, TermStatsRequest request) {
        return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA_READ);
    }

    @Override
    protected ClusterBlockException checkRequestBlock(ClusterState state, TermStatsRequest request, String[] concreteIndices) {
        return state.blocks().indicesBlockedException(ClusterBlockLevel.METADATA_READ, concreteIndices);
    }

}