package com.chenwei666.netserial.ai;

import java.net.URI;

interface ModelCatalogHttpTransport {
    ChatHttpResponse get(
            URI endpoint,
            char[] credential,
            CredentialHeaderMode headerMode,
            HttpExecutionPolicy policy,
            RequestCancellation cancellation
    );
}
