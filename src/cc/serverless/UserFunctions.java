package cc.serverless;

import java.util.Optional;

import cc.data.Auction;
import cc.data.Login;
import cc.data.User;
import cc.ops.UserOps;
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

import java.util.Arrays;

public class UserFunctions {
    public static final String COOKIE_NAME = "cc:session";

    @FunctionName("user-auth")
    public HttpResponseMessage auth(@HttpTrigger(name = "req",
                                            methods = { HttpMethod.POST },
                                            authLevel = AuthorizationLevel.ANONYMOUS,
                                            route = "user/auth")
                                    HttpRequestMessage<Optional<Login>> request,
                                    final ExecutionContext context) {
        context.getLogger().info("user/auth called");

        if (request.getBody().isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("No login data given.")
                    .build();
        }
        Login login = request.getBody().get();

        Result<String> r = UserOps.getInstance().auth(login);
        if(!r.isOK()) {
            return request.createResponseBuilder(HttpStatus.valueOf(r.error())).build();
        } else {
            String cookieSession = String.format("%s=%s; Path=/; Max-Age=3600; HttpOnly", COOKIE_NAME, r.value());
            return request.createResponseBuilder(HttpStatus.OK).header("set-cookie", cookieSession).build();
        }
    }

    @FunctionName("user-create")
    public HttpResponseMessage create(@HttpTrigger(name = "req",
                                              methods = { HttpMethod.POST },
                                              authLevel = AuthorizationLevel.ANONYMOUS,
                                              route = "user")
                                      HttpRequestMessage<Optional<User>> request,
                                      final ExecutionContext context) {
        context.getLogger().info("POST user called");

        if(request.getBody().isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("No user data given.")
                    .build();
        }

        User user = request.getBody().get();
        Result<User> r = UserOps.getInstance().create(user);
        return r.isOK()
                ? request.createResponseBuilder(HttpStatus.OK).body(r.value()).build()
                : request.createResponseBuilder(HttpStatus.valueOf(r.error())).build();
    }

    @FunctionName("user-get")
    public HttpResponseMessage get(@HttpTrigger(name = "req",
                                           methods = { HttpMethod.GET },
                                           authLevel = AuthorizationLevel.ANONYMOUS,
                                           route = "user/{id}")
                                   HttpRequestMessage<Optional<User>> request,
                                   @BindingName("id") String id,
                                   final ExecutionContext context) {
        context.getLogger().info("user/{id} called");

        Result<User> r = UserOps.getInstance().get(id);
        return r.isOK()
                ? request.createResponseBuilder(HttpStatus.OK).body(r.value()).build()
                : request.createResponseBuilder(HttpStatus.valueOf(r.error())).build();
    }

    @FunctionName("user-auctions")
    public HttpResponseMessage auctions(@HttpTrigger(name = "req",
                                                methods = { HttpMethod.GET },
                                                authLevel = AuthorizationLevel.ANONYMOUS,
                                                route = "user/{id}/auctions")
                                        HttpRequestMessage<Optional<User>> request,
                                        @BindingName("id") String id,
                                        final ExecutionContext context) {
        context.getLogger().info("user/{id}/auctions called");

        String status = request.getQueryParameters().getOrDefault("status", "");
        int offset = Integer.parseInt(request.getQueryParameters().getOrDefault("st", "0"));
        int limit = Integer.parseInt(request.getQueryParameters().getOrDefault("len", "20"));

        Result<Auction[]> r = UserOps.getInstance().auctions(id, status, offset, limit);
        return r.isOK()
                ? request.createResponseBuilder(HttpStatus.OK).body(r.value()).build()
                : request.createResponseBuilder(HttpStatus.valueOf(r.error())).build();
    }

    @FunctionName("user-update")
    public HttpResponseMessage update(@HttpTrigger(name = "req",
                                              methods = { HttpMethod.PUT },
                                              authLevel = AuthorizationLevel.ANONYMOUS,
                                              route = "user/{id}")
                                      HttpRequestMessage<Optional<User>> request,
                                      @BindingName("id") String id,
                                      final ExecutionContext context) {
        context.getLogger().info("PUT user/{id} called");

        if (request.getBody().isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("No data given to update.")
                    .build();
        }
        User user = request.getBody().get();

        Result<User> r = UserOps.getInstance().update(getSessionId(request), id, user);
        return r.isOK()
                ? request.createResponseBuilder(HttpStatus.OK).body(r.value()).build()
                : request.createResponseBuilder(HttpStatus.valueOf(r.error())).body(user).build();
    }

    @FunctionName("user-delete")
    public HttpResponseMessage delete(@HttpTrigger(name = "req",
                                              methods = { HttpMethod.DELETE },
                                              authLevel = AuthorizationLevel.ANONYMOUS,
                                              route = "user/{id}")
                                      HttpRequestMessage<Optional<String>> request,
                                      @BindingName("id") String id,
                                      final ExecutionContext context) {
        context.getLogger().info("DELETE user/{id} called");

        Result<Void> r = UserOps.getInstance().delete(getSessionId(request), id);
        return r.isOK()
                ? request.createResponseBuilder(HttpStatus.OK).body(r.value()).build()
                : request.createResponseBuilder(HttpStatus.valueOf(r.error())).build();
    }

    public static <T> String getSessionId(HttpRequestMessage<T> request) {
        String[] cookies = request.getHeaders().get("cookie").split(";");
        String session = Arrays.stream(cookies).filter(cookie -> cookie.contains(COOKIE_NAME)).findFirst().orElse("not fund");

        String sessionId = session.split("=")[1];
        //This if is where if you want to test with postman
        if (sessionId.startsWith("\"") && sessionId.endsWith("\"") && sessionId.length() > 1) {
            sessionId = sessionId.substring(1, sessionId.length() - 1);
        }
        return sessionId;
    }
}
