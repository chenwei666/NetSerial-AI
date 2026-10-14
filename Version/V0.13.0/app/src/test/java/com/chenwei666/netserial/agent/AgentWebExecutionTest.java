package com.chenwei666.netserial.agent;

import com.chenwei666.netserial.ai.RequestCancellation;
import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.web.WebAccessPlan;
import com.chenwei666.netserial.web.WebAccessPlanFactory;
import com.chenwei666.netserial.web.WebAccessRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import static org.junit.Assert.*;

/** Actual local templates and bridge with synthetic transport, not a device/Android runner test. */
public final class AgentWebExecutionTest {
    @Test public void localVendorTemplatesUseOneSensitiveSendAndNeverExposePassword() throws Exception {
        Vendor[] vendors = {Vendor.H3C_COMWARE, Vendor.H3C_COMWARE, Vendor.HUAWEI_VRP,
                Vendor.CISCO_IOS, Vendor.RUIJIE_RGOS};
        String[] platforms = {"Comware 5", "Comware 7", "VRP", "IOS", "RGOS"};
        for (int vendor = 0; vendor < vendors.length; vendor++) {
            for (boolean http : new boolean[] {false, true}) {
                char[] password = "Fixture-Secret-042".toCharArray();
                AtomicReference<AgentSessionBridge> ref = new AtomicReference<>();
                List<String> sent = new ArrayList<>();
                StringBuilder visible = new StringBuilder();
                AgentSessionBridge bridge = new AgentSessionBridge(500, 1);
                ref.set(bridge);
                bridge.register("review", "synthetic-only", command -> {
                    sent.add(command);
                    String token = ref.get().sessionToken("review");
                    String echo = command + "\r\nSW1#";
                    // A regex applied separately to individual chunks cannot hide this password.
                    for (int index = 0; index < echo.length(); index += 3) {
                        String chunk = echo.substring(index, Math.min(index + 3, echo.length()));
                        ref.get().receiveToken(token, chunk);
                        visible.append(ref.get().maskIncomingToken(token, chunk));
                    }
                });
                bridge.receive("review", "SW1#");
                try (WebAccessRequest request = new WebAccessRequest(vendors[vendor], platforms[vendor],
                             "reviewadmin", password, true, http);
                     WebAccessPlan plan = new WebAccessPlanFactory().create(request);
                     AgentSessionBridge.Lease lease = bridge.acquire(bridge.sessionToken("review"))) {
                    List<String> raw = plan.getCommands();
                    List<String> safe = plan.getRedactedCommands();
                    int sensitive = 0;
                    for (int index = 0; index < raw.size(); index++) {
                        String result;
                        if (!raw.get(index).equals(safe.get(index))) {
                            sensitive++;
                            result = lease.executeSensitive(raw.get(index), safe.get(index), password,
                                    new RequestCancellation());
                        } else result = lease.execute(raw.get(index), new RequestCancellation());
                        assertFalse(result.contains("Fixture-Secret-042"));
                    }
                    for (String verification : plan.getVerificationCommands()) {
                        assertEquals(AgentSessionBridge.SENSITIVE_OUTPUT_WITHHELD,
                                lease.execute(verification, new RequestCancellation()));
                    }
                    assertEquals(1, sensitive);
                    assertTrue(lease.isOutputSuppressed());
                    assertFalse(lease.initialOutput().contains("Fixture"));
                    assertFalse(visible.toString().contains("Fixture"));
                    assertEquals(raw.size() + plan.getVerificationCommands().size(), sent.size());
                    assertEquals(raw, sent.subList(0, raw.size()));
                    assertEquals(plan.getVerificationCommands(), sent.subList(raw.size(), sent.size()));
                    assertFalse(plan.redactedBatch().contains("Fixture"));
                } finally {
                    Arrays.fill(password, '\0');
                    bridge.disconnect("review");
                }
            }
        }
    }
}
