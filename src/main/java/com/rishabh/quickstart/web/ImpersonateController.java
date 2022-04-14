package com.rishabh.quickstart.web;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


@RestController
@EnableConfigurationProperties(KeycloakSpringBootProperties.class)
public class ImpersonateController {
    private @Autowired HttpServletRequest request;

    private final KeycloakSpringBootProperties properties;


    public ImpersonateController(KeycloakSpringBootProperties properties) {
        this.properties = properties;

    }

    @GetMapping(value = "/impersonate")
    public String  impersonateUser(KeycloakAuthenticationToken keycloakAuthenticationToken, @RequestParam String user) throws IOException {

        @SuppressWarnings("unchecked")
        KeycloakPrincipal<KeycloakSecurityContext> principal = (KeycloakPrincipal<KeycloakSecurityContext>) keycloakAuthenticationToken
                .getPrincipal();

        StringBuffer html = new StringBuffer();
        html.append("Principal: "+principal.getName());
        html.append("<br />");


        /**Keycloak keycloakService = KeycloakBuilder.builder()
         .serverUrl("http://localhost:8080/auth")
         .realm("quickstart") //
         .grantType(OAuth2Constants.PASSWORD) //
         .username("rishabh")
         .password("password")
         .clientId("clientId") //
         .clientSecret("client_secret") //
         .build();**/

        BasicCookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();


        System.out.println("Logged in user : " + principal.getKeycloakSecurityContext().getTokenString());
        HttpUriRequest reqBuild = RequestBuilder.post()
                .setUri(properties.getAuthServerUrl() + "/realms/" + properties.getRealm() + "/protocol/openid-connect/token")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addParameter("client_id", properties.getResource())
                .addParameter("client_secret", (String) properties.getCredentials().get("secret")) //
                .addParameter("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
                .addParameter("subject_token", principal.getKeycloakSecurityContext().getTokenString())
                .addParameter("requested_token_type", "urn:ietf:params:oauth:token-type:access_token")
                .addParameter("requested_subject", user)
                .build();

        HttpResponse res = httpClient.execute(reqBuild);
        String resBody = EntityUtils.toString(res.getEntity());
        System.out.println("Impersonated User : " + resBody);
        return html.toString();
    }

}
