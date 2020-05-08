package com.github.nirmalc.es;

import org.elasticsearch.action.Action;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

public class TermStatsRequestBuilder extends ActionRequestBuilder<TermStatsRequest,TermStatsResponse> {
    protected TermStatsRequestBuilder(ElasticsearchClient client, Action<TermStatsResponse> action, TermStatsRequest request) {
        super(client, action, request);
    }

    public TermStatsRequestBuilder setSortByTF(boolean sortByTF){
        request.setSortByDocFrequency(sortByTF);
        return this;
    }

    public TermStatsRequestBuilder setSortByDF(boolean sortByDF){
        request.setSortByDocFrequency(sortByDF);
        return this;
    }

    public TermStatsRequestBuilder setField(String field){
        request.setField(field);
        return this;
    }

    public TermStatsRequestBuilder setSize(int size){
        request.setSize(size);
        return this;
    }
}
