package cc.serverless;

import java.util.Optional;

import cc.data.Question;
import cc.ops.QuestionOps;
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

public class QuestionsFunctions {

    @FunctionName("question-ask")
    public HttpResponseMessage ask(@HttpTrigger(name = "req",
                                           methods = { HttpMethod.POST },
                                           authLevel = AuthorizationLevel.ANONYMOUS,
                                           route = "auction/{auctionId}/question")
                                   HttpRequestMessage<Optional<Question>> request,
                                   @BindingName("auctionId") String auctionId,
                                   final ExecutionContext context) {
        context.getLogger().info("auction/{auctionId}/question called");

        if(request.getBody().isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("No question data given.")
                    .build();
        }

        Question question = request.getBody().get();
        Result<Question> r = QuestionOps.getInstance().ask(auctionId, question);
        return r.isOK()
                ? request.createResponseBuilder(HttpStatus.OK).body(r.value()).build()
                : request.createResponseBuilder(HttpStatus.valueOf(r.error())).build();
    }

    @FunctionName("question-list")
    public HttpResponseMessage list(@HttpTrigger(name = "req",
                                            methods = { HttpMethod.GET },
                                            authLevel = AuthorizationLevel.ANONYMOUS,
                                            route = "auction/{auctionId}/question")
                                    HttpRequestMessage<Optional<String>> request,
                                    @BindingName("auctionId") String auctionId,
                                    final ExecutionContext context) {
        context.getLogger().info("get auction/{auctionId}/question called");

        int offset = Integer.parseInt(request.getQueryParameters().getOrDefault("st", "0"));
        int limit = Integer.parseInt(request.getQueryParameters().getOrDefault("len", "20"));

        Result<Question[]> r = QuestionOps.getInstance().list(auctionId, offset, limit);
        return r.isOK()
                ? request.createResponseBuilder(HttpStatus.OK).body(r.value()).build()
                : request.createResponseBuilder(HttpStatus.valueOf(r.error())).build();
    }

    @FunctionName("question-reply")
    public HttpResponseMessage reply(@HttpTrigger(name = "req",
                                            methods = { HttpMethod.POST },
                                            authLevel = AuthorizationLevel.ANONYMOUS,
                                            route = "auction/{auctionId}/question/{questionId}/reply")
                                    HttpRequestMessage<Optional<Question>> request,
                                    @BindingName("auctionId") String auctionId,
                                    @BindingName("questionId") String questionId,
                                    final ExecutionContext context) {
        context.getLogger().info("get auction/{auctionId}/question/st/len called");

        if(request.getBody().isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("No question data given.")
                    .build();
        }
        Question question = request.getBody().get();
        String sessionId = UserFunctions.getSessionId(request);

        Result<Question> r = QuestionOps.getInstance().reply(sessionId, auctionId, questionId, question);
        return r.isOK()
                ? request.createResponseBuilder(HttpStatus.OK).body(r.value()).build()
                : request.createResponseBuilder(HttpStatus.valueOf(r.error())).build();
    }
}
