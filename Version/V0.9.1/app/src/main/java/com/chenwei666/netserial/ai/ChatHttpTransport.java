package com.chenwei666.netserial.ai;

import java.net.URI;

public interface ChatHttpTransport {
    ChatHttpResponse post(
            URI endpoint,
            byte[] requestBody,
            char[] credential,
            HttpExecutionPolicy policy,
            RequestCancellation cancellation
    );
}
