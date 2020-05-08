package com.github.nirmalc.es;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.action.RestBuilderListener;

import java.io.IOException;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.action.RestActions.buildBroadcastShardsHeader;

public class TermStatsRestHandler extends BaseRestHandler {

    public TermStatsRestHandler(Settings settings, RestController controller) {
        super(settings);
        controller.registerHandler(GET, "/{index}/_termstat", this);
    }

    @Override
    public String getName() {
        return null;
    }


    RestChannel restChannel;

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        TermStatsRequest termStatsRequest = new TermStatsRequest(Strings.splitStringByCommaToArray(request.param(
                "index")));
        termStatsRequest.setField(request.param("field"));
        boolean tfSort = request.paramAsBoolean("sortByTF", false);
        boolean dfSort = request.paramAsBoolean("sortByDF", false);
        tfSort = dfSort ?  false : true;
        termStatsRequest.setSortByDocFrequency(dfSort);
        termStatsRequest.setSortByTermFrequency(tfSort);
        termStatsRequest.setSize(request.paramAsInt("size",10));
        final long startTime = System.nanoTime();
        return channel -> client.execute(TermStatsAction.INSTANCE, termStatsRequest,
                new RestBuilderListener<TermStatsResponse>(channel) {
            @Override
            public RestResponse buildResponse(TermStatsResponse termStatsResponse, XContentBuilder builder) throws Exception {
                termStatsResponse.setTookInNanoSeconds((System.nanoTime() - startTime) / 1000000);
                termStatsResponse.toXContent(builder, ToXContent.EMPTY_PARAMS);
                return new BytesRestResponse(RestStatus.OK, builder);
            }
        });
    }
}
