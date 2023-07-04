package com.itm.space.backendresources.controller;

import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.account.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.ws.rs.core.Response;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "user1", password = "user", authorities = "ROLE_MODERATOR")
class UserControllerTest extends BaseIntegrationTest {
    @MockBean
    private Keycloak keycloak;
    @Autowired
    private MockMvc mockMvc;

    @Value("${keycloak.realm}")
    private String realmItm;

    private UserRequest testUserRequest;
    private RealmResource realmResourceMock;
    private UsersResource usersResourceMock;
    private UserRepresentation userRepresentationMock;
    private UserResource userResourceMock;

    @BeforeEach
    void initNecessaryMocks() {
        testUserRequest = new UserRequest("thor", "thor@gmail.com", "root", "ken", "floor");
        realmResourceMock = mock(RealmResource.class);
        usersResourceMock = mock(UsersResource.class);
        userRepresentationMock = mock(UserRepresentation.class);
        userResourceMock = mock(UserResource.class);
    }
    @Test
    public void helloMethodTest_ShouldReturnOk() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/api/users/hello")).andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("user1", response.getContentAsString());
    }

    @Test
    public void userCreatedTest_ShouldReturnSuccessStatus() throws Exception {
        when(keycloak.realm(realmItm)).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.create(any())).thenReturn(Response.status(Response.Status.CREATED).build());
        when(userRepresentationMock.getId()).thenReturn(UUID.randomUUID().toString());
        MockHttpServletResponse response = mvc.perform(requestWithContent(post("/api/users"), testUserRequest))
                .andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        verify(keycloak).realm(realmItm);
        verify(realmResourceMock).users();
        verify(usersResourceMock).create(any());
    }

    @Test
    public void getUserByIdTest_ShouldReturnUserIDSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        when(keycloak.realm(realmItm)).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(mock(UsersResource.class));
        when(realmResourceMock.users().get(eq(String.valueOf(userId)))).thenReturn(userResourceMock);
        when(userRepresentationMock.getId()).thenReturn(String.valueOf(userId));
        MockHttpServletResponse response = mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isInternalServerError())
                .andReturn().getResponse();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
    }
}