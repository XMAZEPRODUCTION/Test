package com.itm.space.backendresources.controller;

import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "MODERATOR")
public class UserControllerTest extends BaseIntegrationTest {
    @Autowired
    private UserService userService;
    @Autowired
    private Keycloak keycloak;
    @Test
    void createUserTest() throws Exception{
        // Тест на создания User
        MockHttpServletRequestBuilder requestBuilder =
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "username": "User",
                                "firstName": "User",
                                "lastName": "User",
                                "email": "user@mail.com",
                                "password": "12345"
                                }
                                """);
        mvc.perform(requestBuilder).andExpect(status().isOk());
    }
    @Test
    void createUserTestNegative() throws Exception{
        // Тест на создания User с неправильным email
        MockHttpServletRequestBuilder requestBuilder =
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "username": "test",
                                "firstname": "test",
                                "lastname": "test",
                                "email": "test",
                                "password": "test"
                                }
                                """);
        mvc.perform(requestBuilder).andExpect(status().isBadRequest());
    }
    @Test
    void helloTest() throws Exception{
        // Тест на доступ к странице Hello
        MockHttpServletRequestBuilder requestBuilder =
                get("/api/users/hello");
        mvc.perform(requestBuilder).andExpect(status().isOk());
    }
    @Test
    @WithMockUser(roles = "USER")
    void helloTestNegative() throws Exception{
        // Тест на допступ к странице Hello с другой ролью
        MockHttpServletRequestBuilder requestBuilder =
                get("/api/users/hello");
        mvc.perform(requestBuilder).andExpect(status().is4xxClientError());
    }
    @Test
    void getUserByIdTest() throws Exception{
        //Тест на получения User
        userService.createUser(new UserRequest("User","User@mail.com","12345","User",
                "User"));
        String testUUID = keycloak.realm("ITM").users().search("User").get(0).getId();
        MockHttpServletRequestBuilder requestBuilder =
                get("/api/users/"+ testUUID);
        mvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                        {
                         "firstName": "User",
                         "lastName": "User",
                         "email": "user@mail.com",
                         "roles": ["default-roles-itm"],
                         "groups": []
                        }
                        """));
        keycloak.realm("ITM").users().get(testUUID).remove();
    }
    @Test
    void getUserByIdTestNegative() throws Exception{
        //Тест на получение не существующего User
        String testUUID = String.valueOf(UUID.randomUUID());
        MockHttpServletRequestBuilder requestBuilder =
                get("/api/users/"+testUUID);
        mvc.perform(requestBuilder).andExpect(status().is5xxServerError());
    }
    @AfterEach
    void clean(){
        List<UserRepresentation> userRepresentationsList = keycloak.realm("ITM").users().search("User");
        if (!(userRepresentationsList.isEmpty())){
            UserRepresentation userRepresentation = userRepresentationsList.get(0);
            keycloak.realm("ITM").users().get(userRepresentation.getId()).remove();
        }
    }
}
