package cc.serverless;

import java.util.Optional;

import cc.data.Auction;
import cc.data.Login;
import cc.data.User;
import cc.ops.AuctionOps;
import cc.serverless.UserFunctions;
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

public class AuctionsFunctions {
    @FunctionName("auction-create")
    public HttpResponseMessage create(@HttpTrigger(name = "req",
                                              methods = { HttpMethod.POST },
                                              authLevel = AuthorizationLevel.ANONYMOUS,
                                              route = "auction")
                                      HttpRequestMessage<Optional<Auction>> request,
                                      final ExecutionContext context) {
        context.getLogger().info("post/auction called");

        if (request.getBody().isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("No auction data given to create.")
                    .build();
        }
        Auction auction = request.getBody().get();
        String sessionId = UserFunctions.getSessionId(request);

        Result<Auction> r = AuctionOps.getInstance().create(sessionId, auction);
        return r.isOK()
                ? request.createResponseBuilder(HttpStatus.OK).body(r.value()).build()
                : request.createResponseBuilder(HttpStatus.valueOf(r.error())).build();
    }

    @FunctionName("auction-get")
    public HttpResponseMessage get(@HttpTrigger(name = "req",
                                           methods = { HttpMethod.GET },
                                           authLevel = AuthorizationLevel.ANONYMOUS,
                                           route = "auction/{id}")
                                   HttpRequestMessage<Optional<Auction>> request,
                                   @BindingName("id") String id,
                                   final ExecutionContext context) {
        context.getLogger().info("auction called");

        if (id == null || id.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("No id given.")
                    .build();
        }

        Result<Auction> r = AuctionOps.getInstance().get(id);
        return r.isOK()
                ? request.createResponseBuilder(HttpStatus.OK).body(r.value()).build()
                : request.createResponseBuilder(HttpStatus.valueOf(r.error())).build();
    }

    @FunctionName("auction-update")
    public HttpResponseMessage update(@HttpTrigger(name = "req",
                                              methods = { HttpMethod.PUT },
                                              authLevel = AuthorizationLevel.ANONYMOUS,
                                              route = "auction/{id}")
                                      HttpRequestMessage<Optional<Auction>> request,
                                      @BindingName("id") String id,
                                      final ExecutionContext context) {
        context.getLogger().info("PUT auction/{id} called");

        if (request.getBody().isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("No auction data given to create.")
                    .build();
        }
        Auction auction = request.getBody().get();
        String sessionId = UserFunctions.getSessionId(request);

        Result<Auction> r = AuctionOps.getInstance().update(sessionId, id, auction);
        return r.isOK()
                ? request.createResponseBuilder(HttpStatus.OK).body(r.value()).build()
                : request.createResponseBuilder(HttpStatus.valueOf(r.error())).build();
    }

    @FunctionName("auction-list")
    public HttpResponseMessage list(@HttpTrigger(name = "req",
                                            methods = { HttpMethod.GET },
                                            authLevel = AuthorizationLevel.ANONYMOUS,
                                            route = "auction")
                                    HttpRequestMessage<Optional<Auction>> request,
                                    final ExecutionContext context) {
        context.getLogger().info("auction/{st}/{len} called");

        int offset = Integer.parseInt(request.getQueryParameters().getOrDefault("st", "0"));
        int length = Integer.parseInt(request.getQueryParameters().getOrDefault("len", "20"));

        Result<Auction[]> r = AuctionOps.getInstance().list(offset, length);
        return r.isOK()
                ? request.createResponseBuilder(HttpStatus.OK).body(r.value()).build()
                : request.createResponseBuilder(HttpStatus.valueOf(r.error())).build();
    }

    @FunctionName("auction-recent")
    public HttpResponseMessage recent(@HttpTrigger(name = "req",
                                              methods = { HttpMethod.GET },
                                              authLevel = AuthorizationLevel.ANONYMOUS,
                                              route = "auction/any/recent")
                                      HttpRequestMessage<Optional<Auction>> request,
                                      final ExecutionContext context) {
        context.getLogger().info("auction/any/recent called");

        int offset = Integer.parseInt(request.getQueryParameters().getOrDefault("st", "0"));
        int length = Integer.parseInt(request.getQueryParameters().getOrDefault("len", "20"));

        Result<Auction[]> r = AuctionOps.getInstance().popular(offset, length);
        return r.isOK()
                ? request.createResponseBuilder(HttpStatus.OK).body(r.value()).build()
                : request.createResponseBuilder(HttpStatus.valueOf(r.error())).build();
    }

    @FunctionName("auction-about-to-close")
    public HttpResponseMessage aboutToClose(@HttpTrigger(name = "req",
                                                    methods = { HttpMethod.GET },
                                                    authLevel = AuthorizationLevel.ANONYMOUS,
                                                    route = "auction/any/about-to-close")
                                            HttpRequestMessage<Optional<Auction>> request,
                                            final ExecutionContext context) {
        context.getLogger().info("auction/any/about-to-close called");

        int offset = Integer.parseInt(request.getQueryParameters().getOrDefault("st", "0"));
        int length = Integer.parseInt(request.getQueryParameters().getOrDefault("len", "20"));

        Result<Auction[]> r = AuctionOps.getInstance().aboutToClose(offset, length);
        return r.isOK()
                ? request.createResponseBuilder(HttpStatus.OK).body(r.value()).build()
                : request.createResponseBuilder(HttpStatus.valueOf(r.error())).build();
    }

    @FunctionName("auction-popular")
    public HttpResponseMessage popular(@HttpTrigger(name = "req",
                                               methods = { HttpMethod.GET },
                                               authLevel = AuthorizationLevel.ANONYMOUS,
                                               route = "auction/any/popular")
                                       HttpRequestMessage<Optional<Auction>> request,
                                       final ExecutionContext context) {
        context.getLogger().info("auction/any/popular called");

        int offset = Integer.parseInt(request.getQueryParameters().getOrDefault("st", "0"));
        int length = Integer.parseInt(request.getQueryParameters().getOrDefault("len", "20"));

        Result<Auction[]> r = AuctionOps.getInstance().popular(offset, length);
        return r.isOK()
                ? request.createResponseBuilder(HttpStatus.OK).body(r.value()).build()
                : request.createResponseBuilder(HttpStatus.valueOf(r.error())).build();
    }

    @FunctionName("auction-search")
    public HttpResponseMessage search(@HttpTrigger(name = "req",
                                              methods = { HttpMethod.GET },
                                              authLevel = AuthorizationLevel.ANONYMOUS,
                                              route = "auction/any/search")
                                      HttpRequestMessage<Optional<Auction>> request,
                                      final ExecutionContext context) {
        context.getLogger().info("GET auction/search called");

        String query = request.getQueryParameters().getOrDefault("q", "");
        int offset = Integer.parseInt(request.getQueryParameters().getOrDefault("st", "0"));
        int length = Integer.parseInt(request.getQueryParameters().getOrDefault("len", "20"));

        Result<Auction[]> r = AuctionOps.getInstance().search(query, offset, length);
        return r.isOK()
                ? request.createResponseBuilder(HttpStatus.OK).body(r.value()).build()
                : request.createResponseBuilder(HttpStatus.valueOf(r.error())).build();
    }
}
