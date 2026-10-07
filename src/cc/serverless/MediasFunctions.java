package cc.serverless;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import cc.data.MediaDAO;
import cc.ops.MediaOps;
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

public class MediasFunctions {
    @FunctionName("media-upload")
    public HttpResponseMessage upload(@HttpTrigger(name = "req",
                                              methods = { HttpMethod.POST },
                                              authLevel = AuthorizationLevel.ANONYMOUS,
                                              dataType = "binary",
                                              route = "media")
                                      HttpRequestMessage<Optional<byte[]>> request,
                                      final ExecutionContext context) {
        context.getLogger().info("POST media called");

        String contentType = request.getHeaders().get("Content-Type");

        if (!request.getBody().isPresent()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("No Data to upload.")
                    .build();
        }

        byte[] contents = request.getBody().get();

        Result<String> r = MediaOps.getInstance().upload(contentType, contents);
        return r.isOK()
                ? request.createResponseBuilder(HttpStatus.OK).body(r.value()).build()
                : request.createResponseBuilder(HttpStatus.valueOf(r.error())).body("...").build();
    }

    @FunctionName("media-download")
    public HttpResponseMessage download(@HttpTrigger(name = "req",
                                                methods = { HttpMethod.GET },
                                                authLevel = AuthorizationLevel.ANONYMOUS,
                                                route = "media/{id}")
                                        HttpRequestMessage<Optional<String>> request,
                                        @BindingName("id") String id,
                                        final ExecutionContext context) {
        context.getLogger().info("media called");

        if(id == null || id.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("No id given.")
                    .build();
        }

        Result<MediaDAO> r = MediaOps.getInstance().download(id);
        return r.isOK()
                ? request.createResponseBuilder(HttpStatus.OK).body(r.value()).build()
                : request.createResponseBuilder(HttpStatus.valueOf(r.error())).build();
    }
}
