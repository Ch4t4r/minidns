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

import org.minidns.constants.DnssecConstants.SignatureAlgorithm;
import org.minidns.dnssec.DnssecValidationFailedException;
import org.minidns.dnssec.DnssecValidationFailedException.DataMalformedException;
import org.junit.Test;

import static org.minidns.dnssec.DnssecWorld.generatePrivateKey;
import static org.minidns.dnssec.DnssecWorld.publicKey;
import static org.minidns.dnssec.DnssecWorld.sign;

public class DsaSingatureVerifierTest extends SignatureVerifierTest {
    private static final SignatureAlgorithm ALGORITHM = SignatureAlgorithm.DSA;

    @Test
    public void testDSA1024Valid() throws DnssecValidationFailedException {
        verifierTest(1024, ALGORITHM);
    }

    @Test
    public void testDSA512Valid() throws DnssecValidationFailedException {
        verifierTest(512, ALGORITHM);
    }


    @Test(expected = DataMalformedException.class)
    public void testDSAIllegalSignature() throws DnssecValidationFailedException {
        assertSignatureValid(publicKey(ALGORITHM, generatePrivateKey(ALGORITHM, 1024)), ALGORITHM, new byte[]{0x0});
    }

    @Test(expected = DataMalformedException.class)
    public void testDSAIllegalPublicKey() throws DnssecValidationFailedException {
        assertSignatureValid(new byte[]{0x0}, ALGORITHM, sign(generatePrivateKey(ALGORITHM, 1024), ALGORITHM, sample));
    }

    @Test
    public void testDSAWrongSignature() throws DnssecValidationFailedException {
        assertSignatureInvalid(publicKey(ALGORITHM, generatePrivateKey(ALGORITHM, 1024)), ALGORITHM, sign(generatePrivateKey(ALGORITHM, 1024), ALGORITHM, sample));
    }

}
