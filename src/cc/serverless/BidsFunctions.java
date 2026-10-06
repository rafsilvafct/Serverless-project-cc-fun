package cc.serverless;

import java.util.Optional;

import cc.data.Bid;
import cc.ops.BidOps;
import cc.utils.Result;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import static cc.serverless.UserFunctions.getSessionId;

public class BidsFunctions {
    @FunctionName("bids-create")
    public HttpResponseMessage create(@HttpTrigger(name = "req",
                                              methods = { HttpMethod.POST },
                                              authLevel = AuthorizationLevel.ANONYMOUS,
                                              route = "auction/{auctionId}/bid")
                                      HttpRequestMessage<Optional<Bid>> request,
                                      @BindingName("auctionId") String auctionId,
                                      final ExecutionContext context) {
        context.getLogger().info("auction/{auctionId}/bid called");

        if (request.getBody().isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("No auction data given to create.")
                    .build();
        }
        Bid bid = request.getBody().get();
        String sessionId = getSessionId(request);

        Result<Bid> r = BidOps.getInstance().create(sessionId, auctionId, bid);
        return r.isOK()
                ? request.createResponseBuilder(HttpStatus.OK).body(r.value()).build()
                : request.createResponseBuilder(HttpStatus.valueOf(r.error())).build();
    }


    @FunctionName("bids-list")
    public HttpResponseMessage list(@HttpTrigger(name = "req",
                                            methods = { HttpMethod.GET },
                                            authLevel = AuthorizationLevel.ANONYMOUS,
                                            route = "auction/{auctionId}/bid")
                                    HttpRequestMessage<Optional<String>> request,
                                    @BindingName("auctionId") String auctionId,
                                    final ExecutionContext context) {
        context.getLogger().info("auction/{auctionId}/bid called");

        int offset = Integer.parseInt(request.getQueryParameters().getOrDefault("st", "0"));
        int limit = Integer.parseInt(request.getQueryParameters().getOrDefault("len", "20"));

        Result<Bid[]> r = BidOps.getInstance().list(auctionId, offset, limit);
        return r.isOK()
                ? request.createResponseBuilder(HttpStatus.OK).body(r.value()).build()
                : request.createResponseBuilder(HttpStatus.valueOf(r.error())).build();
    }

    @FunctionName("bids-get")
    public HttpResponseMessage get(@HttpTrigger(name = "req",
                                           methods = { HttpMethod.GET },
                                           authLevel = AuthorizationLevel.ANONYMOUS,
                                           route = "auction/{auctionId}/bid/{bidId}")
                                   HttpRequestMessage<Optional<String>> request,
                                   @BindingName("auctionId") String auctionId,
                                   @BindingName("bidId") String bidId,
                                   final ExecutionContext context) {
        context.getLogger().info("auction/{auctionId}/bid/{bidId} called");

        Result<Bid> r = BidOps.getInstance().get(auctionId, bidId);
        return r.isOK()
                ? request.createResponseBuilder(HttpStatus.OK).body(r.value()).build()
                : request.createResponseBuilder(HttpStatus.valueOf(r.error())).build();
    }
}
