package com.github.nirmalc.es;

import org.elasticsearch.action.Action;


public class    TermStatsAction extends Action<TermStatsResponse> {

    public static final TermStatsAction INSTANCE = new TermStatsAction();
    public static final String NAME = "indices:info:nirmalc/termstats";


    protected TermStatsAction() {
        super(NAME);
    }


    @Override
    public TermStatsResponse newResponse() {
        return new TermStatsResponse();
    }
}
