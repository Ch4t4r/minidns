/*
 * Copyright 2015-2018 the original author or authors
 *
 * This software is licensed under the Apache License, Version 2.0,
 * the GNU Lesser General Public License version 2 or later ("LGPL")
 * and the WTFPL.
 * You may choose either license to govern your use of this software only
 * upon the condition that you accept all of the terms of either
 * the Apache License 2.0, the LGPL 2.1+ or the WTFPL.
 */
package org.minidns.iterative;

import java.io.IOException;
import java.util.List;

import org.minidns.cache.LruCache;
import org.minidns.dnsmessage.DnsMessage;
import org.minidns.dnsqueryresult.DnsQueryResult;
import org.minidns.record.A;
import org.minidns.record.Data;
import org.minidns.record.Record;
import org.minidns.record.Record.TYPE;
import org.junit.Test;

import static org.minidns.DnsWorld.a;
import static org.minidns.DnsWorld.applyZones;
import static org.minidns.DnsWorld.ns;
import static org.minidns.DnsWorld.record;
import static org.minidns.DnsWorld.rootZone;
import static org.minidns.DnsWorld.zone;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class IterativeDnsClientTest {

    @SuppressWarnings("unchecked")
    @Test
    public void basicIterativeTest() throws IOException {
        IterativeDnsClient client = new IterativeDnsClient(new LruCache(0));
        applyZones(client,
                rootZone(
                        record("com", ns("ns.com")),
                        record("ns.com", a("1.1.1.1"))
                ), zone("com", "ns.com", "1.1.1.1",
                        record("example.com", ns("ns.example.com")),
                        record("ns.example.com", a("1.1.1.2"))
                ), zone("example.com", "ns.example.com", "1.1.1.2",
                        record("www.example.com", a("1.1.1.3"))
                )
        );
        DnsQueryResult result = client.query("www.example.com", TYPE.A);
        DnsMessage message = result.response;
        List<Record<? extends Data>> answers = message.answerSection;
        assertEquals(1, answers.size());
        assertEquals(TYPE.A, answers.get(0).type);
        assertArrayEquals(new byte[]{1, 1, 1, 3}, ((A) answers.get(0).payloadData).getIp());
    }

    @SuppressWarnings("unchecked")
    @Test(expected = IterativeClientException.LoopDetected.class)
    public void loopIterativeTest() throws IOException {
        IterativeDnsClient client = new IterativeDnsClient(new LruCache(0));
        applyZones(client,
                rootZone(
                        record("a", ns("a.ns")),
                        record("b", ns("b.ns")),
                        record("a.ns", a("1.1.1.1")),
                        record("b.ns", a("1.1.1.2"))
                ), zone("a", "a.ns", "1.1.1.1",
                        record("test.a", ns("a.test.b"))
                ), zone("b", "b.ns", "1.1.1.2",
                        record("test.b", ns("b.test.a"))
                )
        );
        assertNull(client.query("www.test.a", TYPE.A));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void notGluedNsTest() throws IOException {
        IterativeDnsClient client = new IterativeDnsClient(new LruCache(0));
        applyZones(client,
                rootZone(
                        record("com", ns("ns.com")),
                        record("net", ns("ns.net")),
                        record("ns.com", a("1.1.1.1")),
                        record("ns.net", a("1.1.2.1"))
                ), zone("com", "ns.com", "1.1.1.1",
                        record("example.com", ns("example.ns.net"))
                ), zone("net", "ns.net", "1.1.2.1",
                        record("example.ns.net", a("1.1.2.2"))
                ), zone("example.com", "example.ns.net", "1.1.2.2",
                        record("www.example.com", a("1.1.1.3"))
                )
        );
        DnsQueryResult result = client.query("www.example.com", TYPE.A);
        DnsMessage message = result.response;
        List<Record<? extends Data>> answers = message.answerSection;
        assertEquals(1, answers.size());
        assertEquals(TYPE.A, answers.get(0).type);
        assertArrayEquals(new byte[]{1, 1, 1, 3}, ((A) answers.get(0).payloadData).getIp());
    }
}
