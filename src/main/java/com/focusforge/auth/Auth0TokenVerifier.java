package com.focusforge.auth;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.*;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

import java.net.URL;
import java.util.Map;

public class Auth0TokenVerifier {

    private final String issuer;
    private final ConfigurableJWTProcessor<SecurityContext> processor;

    public Auth0TokenVerifier(String domain) throws Exception {

        this.issuer = "https://" + domain + "/";

        JWKSource<SecurityContext> keySource =
                new RemoteJWKSet<>(new URL(issuer + ".well-known/jwks.json"));

        // Tell Nimbus: This token uses RS256 and should be validated using Auth0’s keys
        JWSVerificationKeySelector<SecurityContext> keySelector =
                new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource);

        processor = new DefaultJWTProcessor<>();
        processor.setJWSKeySelector(keySelector);
    }

    public Map<String, Object> verify(String idToken) throws Exception {
        SignedJWT jwt = SignedJWT.parse(idToken);

        // Nimbus does signature verification + claim parsing for us:
        SecurityContext ctx = null;
        Map<String, Object> claims = processor.process(jwt, ctx).getClaims();

        // Validate issuer manually (Nimbus doesn't enforce this automatically)
        if (!claims.get("iss").equals(issuer)) {
            throw new RuntimeException("Invalid issuer in token");
        }

        return claims;
    }
}
