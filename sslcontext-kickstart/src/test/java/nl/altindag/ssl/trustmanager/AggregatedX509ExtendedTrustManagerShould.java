/*
 * Copyright 2019 Thunderberry.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.altindag.ssl.trustmanager;

import nl.altindag.ssl.util.KeyStoreUtils;
import nl.altindag.ssl.util.TrustManagerUtils;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 * @author Hakan Altindag
 */
class AggregatedX509ExtendedTrustManagerShould {

    private static final String TRUSTSTORE_FILE_NAME = "truststore.jks";
    private static final char[] TRUSTSTORE_PASSWORD = new char[]{'s', 'e', 'c', 'r', 'e', 't'};
    private static final String KEYSTORE_FILE_NAME = "identity.jks";
    private static final char[] KEYSTORE_PASSWORD = new char[]{'s', 'e', 'c', 'r', 'e', 't'};
    private static final String KEYSTORE_LOCATION = "keystore/";

    private static final Socket SOCKET = new Socket();
    private static final SSLEngine SSL_ENGINE = new MockedSSLEngine();

    @Test
    void checkClientTrusted() throws KeyStoreException {
        KeyStore trustStore = KeyStoreUtils.loadKeyStore(KEYSTORE_LOCATION + TRUSTSTORE_FILE_NAME, TRUSTSTORE_PASSWORD);
        X509ExtendedTrustManager trustManager = TrustManagerUtils.createTrustManager(trustStore);
        X509Certificate[] trustedCerts = KeyStoreTestUtils.getTrustedX509Certificates(trustStore);

        AggregatedX509ExtendedTrustManager aggregatedX509ExtendedTrustManager = new AggregatedX509ExtendedTrustManager(Collections.singletonList(trustManager));
        assertThat(trustManager).isNotNull();
        assertThat(trustManager.getAcceptedIssuers()).hasSize(1);
        assertThat(trustedCerts).hasSize(1);
        int amountOfTrustManagers = aggregatedX509ExtendedTrustManager.getInnerTrustManagers().size();
        assertThat(amountOfTrustManagers).isEqualTo(1);

        assertThatCode(() -> aggregatedX509ExtendedTrustManager.checkClientTrusted(trustedCerts, "RSA"))
                .doesNotThrowAnyException();
    }

    @Test
    void checkClientTrustedWithSslEngine() throws KeyStoreException {
        KeyStore trustStore = KeyStoreUtils.loadKeyStore(KEYSTORE_LOCATION + TRUSTSTORE_FILE_NAME, TRUSTSTORE_PASSWORD);
        X509ExtendedTrustManager trustManager = TrustManagerUtils.createTrustManager(trustStore);
        X509Certificate[] trustedCerts = KeyStoreTestUtils.getTrustedX509Certificates(trustStore);

        AggregatedX509ExtendedTrustManager aggregatedX509ExtendedTrustManager = new AggregatedX509ExtendedTrustManager(Collections.singletonList(trustManager));
        assertThat(trustManager).isNotNull();
        assertThat(trustManager.getAcceptedIssuers()).hasSize(1);
        assertThat(trustedCerts).hasSize(1);

        assertThatCode(() -> aggregatedX509ExtendedTrustManager.checkClientTrusted(trustedCerts, "RSA", SSL_ENGINE))
                .doesNotThrowAnyException();
    }

    @Test
    void checkClientTrustedWithSocket() throws KeyStoreException {
        KeyStore trustStore = KeyStoreUtils.loadKeyStore(KEYSTORE_LOCATION + TRUSTSTORE_FILE_NAME, TRUSTSTORE_PASSWORD);
        X509ExtendedTrustManager trustManager = TrustManagerUtils.createTrustManager(trustStore);
        X509Certificate[] trustedCerts = KeyStoreTestUtils.getTrustedX509Certificates(trustStore);

        AggregatedX509ExtendedTrustManager aggregatedX509ExtendedTrustManager = new AggregatedX509ExtendedTrustManager(Collections.singletonList(trustManager));
        assertThat(trustManager).isNotNull();
        assertThat(trustManager.getAcceptedIssuers()).hasSize(1);
        assertThat(trustedCerts).hasSize(1);

        assertThatCode(() -> aggregatedX509ExtendedTrustManager.checkClientTrusted(trustedCerts, "RSA", SOCKET))
                .doesNotThrowAnyException();
    }

    @Test
    void checkServerTrusted() throws KeyStoreException {
        KeyStore trustStore = KeyStoreUtils.loadKeyStore(KEYSTORE_LOCATION + "truststore-containing-dummy-client.jks", TRUSTSTORE_PASSWORD);
        X509Certificate[] trustedCerts = KeyStoreTestUtils.getTrustedX509Certificates(KeyStoreUtils.loadKeyStore(KEYSTORE_LOCATION + KEYSTORE_FILE_NAME, KEYSTORE_PASSWORD));

        AggregatedX509ExtendedTrustManager trustManager = new AggregatedX509ExtendedTrustManager(Collections.singletonList(TrustManagerUtils.createTrustManager(trustStore)));
        assertThat(trustedCerts).hasSize(1);
        assertThat(trustManager.getAcceptedIssuers()).hasSize(1);

        assertThatCode(() -> trustManager.checkServerTrusted(trustedCerts, "RSA"))
                .doesNotThrowAnyException();
    }

    @Test
    void checkServerTrustedWithSslEngine() throws KeyStoreException {
        KeyStore trustStore = KeyStoreUtils.loadKeyStore(KEYSTORE_LOCATION + "truststore-containing-dummy-client.jks", TRUSTSTORE_PASSWORD);
        X509Certificate[] trustedCerts = KeyStoreTestUtils.getTrustedX509Certificates(KeyStoreUtils.loadKeyStore(KEYSTORE_LOCATION + KEYSTORE_FILE_NAME, KEYSTORE_PASSWORD));

        AggregatedX509ExtendedTrustManager trustManager = new AggregatedX509ExtendedTrustManager(Collections.singletonList(TrustManagerUtils.createTrustManager(trustStore)));
        assertThat(trustedCerts).hasSize(1);
        assertThat(trustManager.getAcceptedIssuers()).hasSize(1);

        assertThatCode(() -> trustManager.checkServerTrusted(trustedCerts, "RSA", SSL_ENGINE))
                .doesNotThrowAnyException();
    }

    @Test
    void checkServerTrustedWithSocket() throws KeyStoreException {
        KeyStore trustStore = KeyStoreUtils.loadKeyStore(KEYSTORE_LOCATION + "truststore-containing-dummy-client.jks", TRUSTSTORE_PASSWORD);
        X509Certificate[] trustedCerts = KeyStoreTestUtils.getTrustedX509Certificates(KeyStoreUtils.loadKeyStore(KEYSTORE_LOCATION + KEYSTORE_FILE_NAME, KEYSTORE_PASSWORD));

        AggregatedX509ExtendedTrustManager trustManager = new AggregatedX509ExtendedTrustManager(Collections.singletonList(TrustManagerUtils.createTrustManager(trustStore)));
        assertThat(trustedCerts).hasSize(1);
        assertThat(trustManager.getAcceptedIssuers()).hasSize(1);

        assertThatCode(() -> trustManager.checkServerTrusted(trustedCerts, "RSA", SOCKET))
                .doesNotThrowAnyException();
    }

    @Test
    void throwsExceptionWhenCheckServerTrustedDoesNotTrustTheSuppliedCertificate() throws KeyStoreException {
        KeyStore trustStore = KeyStoreUtils.loadKeyStore(KEYSTORE_LOCATION + TRUSTSTORE_FILE_NAME, TRUSTSTORE_PASSWORD);
        X509Certificate[] trustedCerts = KeyStoreTestUtils.getTrustedX509Certificates(KeyStoreUtils.loadKeyStore(KEYSTORE_LOCATION + KEYSTORE_FILE_NAME, KEYSTORE_PASSWORD));

        AggregatedX509ExtendedTrustManager trustManager = new AggregatedX509ExtendedTrustManager(Collections.singletonList(TrustManagerUtils.createTrustManager(trustStore)));
        assertThat(trustedCerts).hasSize(1);
        assertThat(trustManager.getAcceptedIssuers()).hasSize(1);

        assertThatThrownBy(() -> trustManager.checkServerTrusted(trustedCerts, "RSA"))
                .isInstanceOf(CertificateException.class)
                .hasMessage("The certificate chain is not trusted");
    }

    @Test
    void throwsExceptionWhenCheckServerTrustedWithSslEngineDoesNotTrustTheSuppliedCertificate() throws KeyStoreException {
        KeyStore trustStore = KeyStoreUtils.loadKeyStore(KEYSTORE_LOCATION + TRUSTSTORE_FILE_NAME, TRUSTSTORE_PASSWORD);
        X509Certificate[] trustedCerts = KeyStoreTestUtils.getTrustedX509Certificates(KeyStoreUtils.loadKeyStore(KEYSTORE_LOCATION + KEYSTORE_FILE_NAME, KEYSTORE_PASSWORD));

        AggregatedX509ExtendedTrustManager trustManager = new AggregatedX509ExtendedTrustManager(Collections.singletonList(TrustManagerUtils.createTrustManager(trustStore)));
        assertThat(trustedCerts).hasSize(1);
        assertThat(trustManager.getAcceptedIssuers()).hasSize(1);

        assertThatThrownBy(() -> trustManager.checkServerTrusted(trustedCerts, "RSA", SSL_ENGINE))
                .isInstanceOf(CertificateException.class)
                .hasMessage("The certificate chain is not trusted");
    }

    @Test
    void throwsExceptionWhenCheckServerTrustedWithSocketDoesNotTrustTheSuppliedCertificate() throws KeyStoreException {
        KeyStore trustStore = KeyStoreUtils.loadKeyStore(KEYSTORE_LOCATION + TRUSTSTORE_FILE_NAME, TRUSTSTORE_PASSWORD);
        X509Certificate[] trustedCerts = KeyStoreTestUtils.getTrustedX509Certificates(KeyStoreUtils.loadKeyStore(KEYSTORE_LOCATION + KEYSTORE_FILE_NAME, KEYSTORE_PASSWORD));

        AggregatedX509ExtendedTrustManager trustManager = new AggregatedX509ExtendedTrustManager(Collections.singletonList(TrustManagerUtils.createTrustManager(trustStore)));
        assertThat(trustedCerts).hasSize(1);
        assertThat(trustManager.getAcceptedIssuers()).hasSize(1);

        assertThatThrownBy(() -> trustManager.checkServerTrusted(trustedCerts, "RSA", SOCKET))
                .isInstanceOf(CertificateException.class)
                .hasMessage("The certificate chain is not trusted");
    }

    @Test
    void throwsExceptionWhenCheckClientTrustedDoesNotTrustTheSuppliedCertificate() throws KeyStoreException {
        KeyStore trustStore = KeyStoreUtils.loadKeyStore(KEYSTORE_LOCATION + TRUSTSTORE_FILE_NAME, TRUSTSTORE_PASSWORD);
        X509ExtendedTrustManager trustManager = TrustManagerUtils.createTrustManager(trustStore);
        X509Certificate[] trustedCerts = KeyStoreTestUtils.getTrustedX509Certificates(KeyStoreUtils.loadKeyStore(KEYSTORE_LOCATION + "truststore-containing-github.jks", TRUSTSTORE_PASSWORD));

        AggregatedX509ExtendedTrustManager aggregatedX509ExtendedTrustManager = new AggregatedX509ExtendedTrustManager(Collections.singletonList(trustManager));
        assertThat(trustManager).isNotNull();
        assertThat(trustManager.getAcceptedIssuers()).hasSize(1);
        assertThat(trustedCerts).hasSize(1);

        assertThatThrownBy(() -> aggregatedX509ExtendedTrustManager.checkClientTrusted(trustedCerts, "RSA"))
                .isInstanceOf(CertificateException.class)
                .hasMessage("The certificate chain is not trusted");
    }

    @Test
    void throwsExceptionWhenCheckClientTrustedWithSslEngineDoesNotTrustTheSuppliedCertificate() throws KeyStoreException {
        KeyStore trustStore = KeyStoreUtils.loadKeyStore(KEYSTORE_LOCATION + TRUSTSTORE_FILE_NAME, TRUSTSTORE_PASSWORD);
        X509ExtendedTrustManager trustManager = TrustManagerUtils.createTrustManager(trustStore);
        X509Certificate[] trustedCerts = KeyStoreTestUtils.getTrustedX509Certificates(KeyStoreUtils.loadKeyStore(KEYSTORE_LOCATION + "truststore-containing-github.jks", TRUSTSTORE_PASSWORD));

        AggregatedX509ExtendedTrustManager aggregatedX509ExtendedTrustManager = new AggregatedX509ExtendedTrustManager(Collections.singletonList(trustManager));
        assertThat(trustManager).isNotNull();
        assertThat(trustManager.getAcceptedIssuers()).hasSize(1);
        assertThat(trustedCerts).hasSize(1);

        assertThatThrownBy(() -> aggregatedX509ExtendedTrustManager.checkClientTrusted(trustedCerts, "RSA", SSL_ENGINE))
                .isInstanceOf(CertificateException.class)
                .hasMessage("The certificate chain is not trusted");
    }

    @Test
    void throwsExceptionWhenCheckClientTrustedWithSocketDoesNotTrustTheSuppliedCertificate() throws KeyStoreException {
        KeyStore trustStore = KeyStoreUtils.loadKeyStore(KEYSTORE_LOCATION + TRUSTSTORE_FILE_NAME, TRUSTSTORE_PASSWORD);
        X509ExtendedTrustManager trustManager = TrustManagerUtils.createTrustManager(trustStore);
        X509Certificate[] trustedCerts = KeyStoreTestUtils.getTrustedX509Certificates(KeyStoreUtils.loadKeyStore(KEYSTORE_LOCATION + "truststore-containing-github.jks", TRUSTSTORE_PASSWORD));

        AggregatedX509ExtendedTrustManager aggregatedX509ExtendedTrustManager = new AggregatedX509ExtendedTrustManager(Collections.singletonList(trustManager));
        assertThat(trustManager).isNotNull();
        assertThat(trustManager.getAcceptedIssuers()).hasSize(1);
        assertThat(trustedCerts).hasSize(1);

        assertThatThrownBy(() -> aggregatedX509ExtendedTrustManager.checkClientTrusted(trustedCerts, "RSA", SOCKET))
                .isInstanceOf(CertificateException.class)
                .hasMessage("The certificate chain is not trusted");
    }

    @Test
    void wrapCauseOfRuntimeExceptionContainingInvalidAlgorithmParameterExceptionIntoSuppressedCertificateException() throws KeyStoreException {
        KeyStore emptyTrustStore = KeyStoreUtils.createKeyStore();
        X509ExtendedTrustManager emptyTrustManager = TrustManagerUtils.createTrustManager(emptyTrustStore);

        KeyStore trustStore = KeyStoreUtils.loadKeyStore(KEYSTORE_LOCATION + TRUSTSTORE_FILE_NAME, TRUSTSTORE_PASSWORD);
        X509Certificate[] certificateChain = KeyStoreTestUtils.getTrustedX509Certificates(trustStore);

        AggregatedX509ExtendedTrustManager aggregatedX509ExtendedTrustManager = new AggregatedX509ExtendedTrustManager(Collections.singletonList(emptyTrustManager));
        assertThat(emptyTrustManager).isNotNull();
        assertThat(emptyTrustManager.getAcceptedIssuers()).isEmpty();
        assertThat(certificateChain).hasSize(1);
        int amountOfTrustManagers = aggregatedX509ExtendedTrustManager.getInnerTrustManagers().size();
        assertThat(amountOfTrustManagers).isEqualTo(1);

        CertificateException certificateException = catchThrowableOfType(() -> aggregatedX509ExtendedTrustManager.checkClientTrusted(certificateChain, "RSA"), CertificateException.class);
        assertThat(certificateException.getSuppressed()).hasSize(1);
        Throwable suppressedException = certificateException.getSuppressed()[0];

        assertThat(suppressedException.getCause()).isInstanceOf(InvalidAlgorithmParameterException.class);
        assertThat(suppressedException.getCause().getMessage()).isEqualTo("the trustAnchors parameter must be non-empty");
    }

    @Test
    void notWrapRuntimeExceptionWhichDoesNotContainACauseOfInvalidAlgorithmParameterExceptionIntoSuppressedCertificateExceptionAndJustRethrow() throws KeyStoreException, CertificateException {
        KeyStore trustStore = KeyStoreUtils.loadKeyStore(KEYSTORE_LOCATION + TRUSTSTORE_FILE_NAME, TRUSTSTORE_PASSWORD);
        X509Certificate[] certificateChain = KeyStoreTestUtils.getTrustedX509Certificates(trustStore);

        X509ExtendedTrustManager shadyTrustManager = mock(X509ExtendedTrustManager.class);
        doThrow(new RuntimeException("KABOOM!!!")).when(shadyTrustManager).checkClientTrusted(certificateChain, "RSA");

        AggregatedX509ExtendedTrustManager aggregatedX509ExtendedTrustManager = new AggregatedX509ExtendedTrustManager(Collections.singletonList(shadyTrustManager));
        assertThat(certificateChain).hasSize(1);
        int amountOfTrustManagers = aggregatedX509ExtendedTrustManager.getInnerTrustManagers().size();
        assertThat(amountOfTrustManagers).isEqualTo(1);

        assertThatThrownBy(() -> aggregatedX509ExtendedTrustManager.checkClientTrusted(certificateChain, "RSA"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("KABOOM!!!");
    }

    static class MockedSSLEngine extends SSLEngine {
        @Override
        public SSLEngineResult wrap(ByteBuffer[] byteBuffers, int i, int i1, ByteBuffer byteBuffer) {
            return null;
        }

        @Override
        public SSLEngineResult unwrap(ByteBuffer byteBuffer, ByteBuffer[] byteBuffers, int i, int i1) {
            return null;
        }

        @Override
        public Runnable getDelegatedTask() {
            return null;
        }

        @Override
        public void closeInbound() {

        }

        @Override
        public boolean isInboundDone() {
            return false;
        }

        @Override
        public void closeOutbound() {

        }

        @Override
        public boolean isOutboundDone() {
            return false;
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return new String[0];
        }

        @Override
        public String[] getEnabledCipherSuites() {
            return new String[0];
        }

        @Override
        public void setEnabledCipherSuites(String[] strings) {

        }

        @Override
        public String[] getSupportedProtocols() {
            return new String[0];
        }

        @Override
        public String[] getEnabledProtocols() {
            return new String[0];
        }

        @Override
        public void setEnabledProtocols(String[] strings) {

        }

        @Override
        public SSLSession getSession() {
            return null;
        }

        @Override
        public void beginHandshake() {

        }

        @Override
        public SSLEngineResult.HandshakeStatus getHandshakeStatus() {
            return null;
        }

        @Override
        public SSLSession getHandshakeSession() {
            return new SSLSession() {
                @Override
                public byte[] getId() {
                    return new byte[0];
                }

                @Override
                public SSLSessionContext getSessionContext() {
                    return null;
                }

                @Override
                public long getCreationTime() {
                    return 0;
                }

                @Override
                public long getLastAccessedTime() {
                    return 0;
                }

                @Override
                public void invalidate() {

                }

                @Override
                public boolean isValid() {
                    return false;
                }

                @Override
                public void putValue(String s, Object o) {

                }

                @Override
                public Object getValue(String s) {
                    return null;
                }

                @Override
                public void removeValue(String s) {

                }

                @Override
                public String[] getValueNames() {
                    return new String[0];
                }

                @Override
                public Certificate[] getPeerCertificates() {
                    return new Certificate[0];
                }

                @Override
                public Certificate[] getLocalCertificates() {
                    return new Certificate[0];
                }

                @Override
                public javax.security.cert.X509Certificate[] getPeerCertificateChain() {
                    return new javax.security.cert.X509Certificate[0];
                }

                @Override
                public Principal getPeerPrincipal() {
                    return null;
                }

                @Override
                public Principal getLocalPrincipal() {
                    return null;
                }

                @Override
                public String getCipherSuite() {
                    return null;
                }

                @Override
                public String getProtocol() {
                    return "TLSv1.2";
                }

                @Override
                public String getPeerHost() {
                    return null;
                }

                @Override
                public int getPeerPort() {
                    return 0;
                }

                @Override
                public int getPacketBufferSize() {
                    return 0;
                }

                @Override
                public int getApplicationBufferSize() {
                    return 0;
                }
            };
        }

        @Override
        public void setUseClientMode(boolean b) {

        }

        @Override
        public boolean getUseClientMode() {
            return false;
        }

        @Override
        public void setNeedClientAuth(boolean b) {

        }

        @Override
        public boolean getNeedClientAuth() {
            return false;
        }

        @Override
        public void setWantClientAuth(boolean b) {

        }

        @Override
        public boolean getWantClientAuth() {
            return false;
        }

        @Override
        public void setEnableSessionCreation(boolean b) {

        }

        @Override
        public boolean getEnableSessionCreation() {
            return false;
        }
    }

}
