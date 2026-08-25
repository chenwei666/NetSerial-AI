package com.chenwei666.netserial.completion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.Vendor;

import org.junit.Test;

public class CompletionEngineTest {

    @Test
    public void h3cUserViewCompletesDisplayOffline() {
        CompletionEngine engine = OfflineCompletionEngine.createDefault();
        CompletionRequest request = new CompletionRequest(
                Vendor.H3C_COMWARE,
                CliMode.USER_VIEW,
                "dis",
                10
        );

        CompletionResult result = engine.complete(request);

        assertFalse(result.getSuggestions().isEmpty());
        assertEquals("display", result.getSuggestions().get(0).getInsertion());
        assertEquals(CompletionSource.LOCAL_COMMAND_PACK,
                result.getSuggestions().get(0).getSource());
    }

    @Test
    public void h3cSystemViewCompletesInterfaceWithoutLeakingIntoUserView() {
        CompletionEngine engine = OfflineCompletionEngine.createDefault();

        CompletionResult systemResult = engine.complete(new CompletionRequest(
                Vendor.H3C_COMWARE,
                CliMode.SYSTEM_VIEW,
                "int",
                10
        ));
        CompletionResult userResult = engine.complete(new CompletionRequest(
                Vendor.H3C_COMWARE,
                CliMode.USER_VIEW,
                "int",
                10
        ));

        assertEquals("interface", systemResult.getSuggestions().get(0).getInsertion());
        assertEquals(0, userResult.getSuggestions().size());
    }

    @Test
    public void supportsAllFourPriorityVendors() {
        CompletionEngine engine = OfflineCompletionEngine.createDefault();
        assertFalse(engine.complete(new CompletionRequest(
                Vendor.HUAWEI_VRP, CliMode.USER_VIEW, "display int", 5)).getSuggestions().isEmpty());
        assertFalse(engine.complete(new CompletionRequest(
                Vendor.CISCO_IOS, CliMode.USER_VIEW, "show run", 5)).getSuggestions().isEmpty());
        assertFalse(engine.complete(new CompletionRequest(
                Vendor.RUIJIE_RGOS, CliMode.INTERFACE_VIEW, "switchport access", 5)).getSuggestions().isEmpty());
    }
}
