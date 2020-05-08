package com.github.nirmalc.es;

import org.elasticsearch.test.ESTestCase;
import org.junit.Assert;

import static org.junit.Assert.*;

public class TermStatsRequestBuilderTests extends ESTestCase {

    public void testOptions(){
        TermStatsRequest termStatsRequest = new TermStatsRequest("foo");
        termStatsRequest.setSortByDocFrequency(true);
        assertEquals(termStatsRequest.getSortByTermFrequency(),false);
        assertEquals(termStatsRequest.getSortByDocFrequency(),true);
        assertEquals(termStatsRequest.getSize(),10);
        termStatsRequest.setSize(100);
        assertEquals(termStatsRequest.getSize(),100);
        termStatsRequest.setField("bar");
        assertEquals(termStatsRequest.getField(),"bar");


    }

}