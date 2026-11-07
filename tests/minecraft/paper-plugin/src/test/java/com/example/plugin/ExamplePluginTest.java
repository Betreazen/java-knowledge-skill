package com.example.plugin;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExamplePluginTest {
    private ServerMock server;

    @BeforeEach void setUp() { server = MockBukkit.mock(); }
    @AfterEach void tearDown() { MockBukkit.unmock(); }

    @Test void counterIncrementsPerCall() {
        PlayerMock p = server.addPlayer();
        assertEquals(1, VisitCounter.increment(p));
        assertEquals(2, VisitCounter.increment(p));
    }
}
