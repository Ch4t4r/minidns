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
package org.minidns.dnssec.algorithms;

import org.junit.Before;
import org.minidns.constants.DnssecConstants.SignatureAlgorithm;
import org.minidns.dnsname.DnsName;
import org.minidns.dnssec.DnssecValidationFailedException;
import org.minidns.record.DNSKEY;
import org.minidns.record.RRSIG;

import java.security.PrivateKey;
import java.util.Random;

import static org.minidns.dnssec.DnssecWorld.generatePrivateKey;
import static org.minidns.dnssec.DnssecWorld.publicKey;
import static org.minidns.dnssec.DnssecWorld.sign;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SignatureVerifierTest extends AlgorithmTest {
    private static Random RANDOM = new Random();
    protected byte[] sample;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        sample = new byte[1024];
        RANDOM.nextBytes(sample);
    }

    protected void verifierTest(int length, SignatureAlgorithm algorithm) throws DnssecValidationFailedException {
        verifierTest(generatePrivateKey(algorithm, length), algorithm);
    }

    protected void verifierTest(PrivateKey privateKey, SignatureAlgorithm algorithm) throws DnssecValidationFailedException {
        assertSignatureValid(publicKey(algorithm, privateKey), algorithm, sign(privateKey, algorithm, sample));
    }

    protected void assertSignatureValid(byte[] publicKey, SignatureAlgorithm algorithm, byte[] signature) throws DnssecValidationFailedException {
        assertTrue(verify(publicKey, algorithm, signature));
    }

    protected void assertSignatureInvalid(byte[] publicKey, SignatureAlgorithm algorithm, byte[] signature) throws DnssecValidationFailedException {
        assertFalse(verify(publicKey, algorithm, signature));
    }

    private boolean verify(byte[] publicKey, SignatureAlgorithm algorithm, byte[] signature) throws DnssecValidationFailedException {
        DNSKEY key = new DNSKEY((short) 0, (byte) 0, algorithm, publicKey);
        RRSIG rrsig = new RRSIG(null, algorithm, (byte) 0, (long) 0, null, null, 0, DnsName.ROOT, signature);

        boolean res = algorithmMap.getSignatureVerifier(algorithm).verify(sample, rrsig, key);
        return res;
    }
}
