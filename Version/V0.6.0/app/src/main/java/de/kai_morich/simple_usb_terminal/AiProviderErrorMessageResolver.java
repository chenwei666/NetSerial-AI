package de.kai_morich.simple_usb_terminal;

import androidx.annotation.StringRes;

import com.chenwei666.netserial.ai.AiProviderError;

final class AiProviderErrorMessageResolver {
    private AiProviderErrorMessageResolver() {
    }

    @StringRes
    static int resolve(AiProviderError error) {
        switch (error) {
            case AUTHENTICATION:
                return R.string.ai_test_error_authentication;
            case RATE_LIMIT:
                return R.string.ai_test_error_rate_limit;
            case TIMEOUT:
                return R.string.ai_test_error_timeout;
            case TLS:
                return R.string.ai_test_error_tls;
            case NETWORK:
                return R.string.ai_test_error_network;
            case SERVER:
                return R.string.ai_test_error_server;
            case INVALID_RESPONSE:
                return R.string.ai_test_error_response;
            case RESPONSE_TOO_LARGE:
                return R.string.ai_test_error_response_too_large;
            case CANCELLED:
                return R.string.ai_test_cancelled;
            case HTTP:
            default:
                return R.string.ai_test_error_http;
        }
    }
}
