package com.dnocturne.afflictions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

/**
 * Base class for Afflictions tests using MockBukkit.
 */
public abstract class AfflictionsTestBase {

    protected ServerMock server;
    protected Afflictions plugin;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Afflictions.class);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }
}
