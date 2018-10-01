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
package org.minidns.dnssec;

import org.minidns.dnsmessage.Question;
import org.minidns.dnsname.DnsName;
import org.minidns.dnssec.algorithms.JavaSecDigestCalculator;
import org.minidns.record.NSEC;
import org.minidns.record.NSEC3;
import org.minidns.record.Record;
import org.minidns.record.Record.TYPE;
import org.junit.Test;

import java.math.BigInteger;

import static org.minidns.DnsWorld.nsec;
import static org.minidns.DnsWorld.nsec3;
import static org.minidns.DnsWorld.record;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class VerifierTest {

    @Test
    public void testNsecMatches() {
        assertTrue(Verifier.nsecMatches("example.com", "com", "com"));
        assertTrue(Verifier.nsecMatches("example.com", "e.com", "f.com"));
        assertTrue(Verifier.nsecMatches("example.com", "be", "de"));
        assertTrue(Verifier.nsecMatches("nsec.example.com", "example.com", "www.example.com"));
        assertFalse(Verifier.nsecMatches("example.com", "a.com", "example.com"));
        assertFalse(Verifier.nsecMatches("example.com", "example1.com", "example2.com"));
        assertFalse(Verifier.nsecMatches("example.com", "test.com", "xxx.com"));
        assertFalse(Verifier.nsecMatches("example.com", "xxx.com", "test.com"));
        assertFalse(Verifier.nsecMatches("example.com", "aaa.com", "bbb.com"));
        assertFalse(Verifier.nsecMatches("www.example.com", "example2.com", "example3.com"));
        assertFalse(Verifier.nsecMatches("test.nsec.example.com", "nsec.example.com", "a.nsec.example.com"));
        assertFalse(Verifier.nsecMatches("test.nsec.example.com", "test.nsec.example.com", "a.example.com"));
        assertFalse(Verifier.nsecMatches("www.example.com", "example.com", "nsec.example.com"));
        assertFalse(Verifier.nsecMatches("example.com", "nsec.example.com", "www.example.com"));
    }

    @Test
    public void testVerifyNsec() {
        Record<NSEC> nsecRecord = record("example.com", nsec("www.example.com", TYPE.A, TYPE.NS, TYPE.SOA, TYPE.TXT, TYPE.AAAA, TYPE.RRSIG, TYPE.NSEC, TYPE.DNSKEY)).as(NSEC.class);
        assertNull(Verifier.verifyNsec(nsecRecord, new Question("nsec.example.com", TYPE.A)));
        assertNull(Verifier.verifyNsec(nsecRecord, new Question("example.com", TYPE.PTR)));
        assertNotNull(Verifier.verifyNsec(nsecRecord, new Question("www.example.com", TYPE.A)));
        assertNotNull(Verifier.verifyNsec(nsecRecord, new Question("example.com", TYPE.NS)));
    }

    @Test
    public void testVerifyNsec3() {
        byte[] bytes = new byte[]{0x3f, (byte) 0xb1, (byte) 0xd0, (byte) 0xaa, 0x27, (byte) 0xe2, 0x5f, (byte) 0xda, 0x40, 0x75, (byte) 0x92, (byte) 0x95, 0x5a, 0x1c, 0x7f, (byte) 0x98, (byte) 0xdb, 0x5b, 0x79, (byte) 0x91};
        Record<NSEC3> nsec3Record = record("7UO4LIHALHHLNGLJAFT7TBIQ6H1SL1CN.net", nsec3((byte) 1, (byte) 1, 0, new byte[0], bytes, TYPE.NS, TYPE.SOA, TYPE.RRSIG, TYPE.DNSKEY, TYPE.NSEC3PARAM)).as(NSEC3.class);
        DnsName zone = DnsName.from("net");
        assertNull(Verifier.verifyNsec3(zone, nsec3Record, new Question("x.net", TYPE.A)));
        assertNotNull(Verifier.verifyNsec3(zone, nsec3Record, new Question("example.net", TYPE.A)));
    }

    @Test
    public void testNsec3hash() throws Exception {
        JavaSecDigestCalculator digestCalculator = new JavaSecDigestCalculator("SHA-1");
        assertEquals("6e8777855bcd60d7b45fc51893776dde75bf6cd4", new BigInteger(1, Verifier.nsec3hash(digestCalculator, new byte[]{42}, new byte[]{88}, 5)).toString(16));
    }
}
