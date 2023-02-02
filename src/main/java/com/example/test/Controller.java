package com.example.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.test.forauthentication.User;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;


@RestController
public class Controller {

    @Autowired
    RestTemplate restTemplate;

    private final WebClient.Builder webClient;

    private static final String AUTHENTICATION_URL = "http://localhost:8080/user/authenticate";
    private static final String HELLO_URL = "http://localhost:8080/user/3";

    public Controller(WebClient.Builder webClient) {
        this.webClient = webClient;
    }

    @RequestMapping(value = "/getResponse", method = RequestMethod.GET)
    public String getResponse(@RequestBody User user) {

        User response = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            String userString = mapper.writeValueAsString(user);
            HttpHeaders authenticationHeaders = getHeaders();
            HttpEntity<String> authenticationEntity = new HttpEntity<>(userString,
                    authenticationHeaders);

            System.out.println(authenticationEntity);

//            HttpEntity<User> request = new HttpEntity<>(new User(user.getUserName(),user.getPassword()),authenticationHeaders);
//            System.out.println(request);
//            tok.next();
//
//            System.out.println(tok);
            // Authenticate User and get JWT
            ResponseEntity<String> authenticationResponse = restTemplate.exchange(AUTHENTICATION_URL,
                    HttpMethod.POST, authenticationEntity, String.class);

            // if the authentication is successful
            String token = "Bearer " + Objects.requireNonNull(authenticationResponse.getBody());
            HttpHeaders headers = getHeaders();
            headers.set("Authorization", token);
            HttpEntity<String> jwtEntity = new HttpEntity<String>(headers);
            // Use Token to get Response
            ResponseEntity<User> helloResponse = restTemplate.exchange(HELLO_URL, HttpMethod.GET, jwtEntity,
                    User.class);

//            User user1 = mapper.readValue((DataInput) helloResponse, User.class);


            if (helloResponse.getStatusCode().equals(HttpStatus.OK)) {
                response = helloResponse.getBody();

//                response = user1.getUserName();
            }
        } catch (Exception ex) {
            ex.printStackTrace();

        }

        /**
         * Using web Client to retrieve data
         * */
        String tok = webClient.build()
                .post()
                .uri(AUTHENTICATION_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(user))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println(tok);

        //String token1 = "Bearer " + tok;

        String userName = Objects.requireNonNull(webClient.build()
                .get()
                .uri(HELLO_URL)
                .headers(headers -> headers.setBearerAuth(tok))
                .retrieve()
                .bodyToMono(User.class)
                .block()).getUserName();


        System.out.println(userName);
/**
 * WebClient task complete
 * */


        assert response != null;
        return response.getUserName();

    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

}