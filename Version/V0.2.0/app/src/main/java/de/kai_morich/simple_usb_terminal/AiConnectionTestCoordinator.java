package de.kai_morich.simple_usb_terminal;

import com.chenwei666.netserial.ai.AiDraftPlan;
import com.chenwei666.netserial.ai.AiProviderError;
import com.chenwei666.netserial.ai.AiProviderException;
import com.chenwei666.netserial.ai.AiRequest;
import com.chenwei666.netserial.ai.CredentialVaultException;
import com.chenwei666.netserial.ai.AiProviderFactory;
import com.chenwei666.netserial.ai.ProviderCredentialService;
import com.chenwei666.netserial.ai.ProviderProfile;
import com.chenwei666.netserial.ai.RequestCancellation;
import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.Vendor;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class AiConnectionTestCoordinator implements AutoCloseable {
    interface Listener {
        void onSuccess(int stepCount);

        void onProviderFailure(AiProviderError error);

        void onCredentialFailure();

        void onUnexpectedFailure();
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private RequestCancellation activeCancellation;
    private boolean closed;

    synchronized boolean isRunning() {
        return activeCancellation != null;
    }

    synchronized void start(
            ProviderProfile profile,
            ProviderCredentialService credentialService,
            Listener listener
    ) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(credentialService, "credentialService");
        Objects.requireNonNull(listener, "listener");
        if (closed) {
            throw new IllegalStateException("connection test coordinator is closed");
        }
        if (activeCancellation != null) {
            throw new IllegalStateException("connection test already running");
        }
        RequestCancellation cancellation = new RequestCancellation();
        activeCancellation = cancellation;
        executor.submit(() -> execute(profile, credentialService, listener, cancellation));
    }

    synchronized void cancel() {
        if (activeCancellation != null) {
            activeCancellation.cancel();
        }
    }

    private void execute(
            ProviderProfile profile,
            ProviderCredentialService credentialService,
            Listener listener,
            RequestCancellation cancellation
    ) {
        try {
            AiDraftPlan plan = AiProviderFactory.create(
                    profile,
                    credentialService
            ).propose(
                    new AiRequest(
                            "Return exactly one read-only command that displays device version.",
                            Vendor.GENERIC,
                            CliMode.USER_VIEW,
                            ""
                    )
            );
            listener.onSuccess(plan.getSteps().size());
        } catch (AiProviderException exception) {
            listener.onProviderFailure(exception.getError());
        } catch (CredentialVaultException exception) {
            listener.onCredentialFailure();
        } catch (RuntimeException exception) {
            listener.onUnexpectedFailure();
        } catch (Exception exception) {
            listener.onUnexpectedFailure();
        } finally {
            synchronized (this) {
                if (activeCancellation == cancellation) {
                    activeCancellation = null;
                }
            }
        }
    }

    @Override
    public synchronized void close() {
        closed = true;
        if (activeCancellation != null) {
            activeCancellation.cancel();
        }
        executor.shutdownNow();
    }
}
