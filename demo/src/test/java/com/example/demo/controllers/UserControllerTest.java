package com.example.demo.controllers;

import com.example.demo.Controllers.UserController;
import com.example.demo.Entities.User;
import com.example.demo.Entities.UserInfo;
import com.example.demo.Services.UserService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by jt on 11/17/15.
 */
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    public void testShow() throws Exception {
        Integer id = 1;
        User testUser = new User();
        UserInfo testUserInfo = new UserInfo();
        testUserInfo.setFirstName("testFirstName");
        testUserInfo.setLastName("testLastName");
        testUser.setUsername("testName");
        testUser.setRole("testRole");

        when(userService.getUserById(id)).thenReturn(new User());

        mockMvc.perform(get("/user/show/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/show"))
                .andExpect(model().attribute("user", instanceOf(User.class)));
    }

    @Test
    public void testEdit() throws Exception {
        Integer id = 1;

        when(userService.getById(id)).thenReturn(new user());

        mockMvc.perform(get("/user/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/userform"))
                .andExpect(model().attribute("user", instanceOf(user.class)));
    }

    @Test
    public void testNewuser() throws Exception {
        verifyZeroInteractions(userService);

        mockMvc.perform(get("/user/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/userform"))
                .andExpect(model().attribute("user", instanceOf(user.class)));
    }

    @Test
    public void testSaveOrUpdate() throws Exception {
        Integer id = 1;
        user returnuser = new user();
        String firstName = "Micheal";
        String lastName = "Weston";
        String addressLine1 = "1 Main St";
        String addressLine2 = "Apt 301";
        String city = "Miami";
        String state = "Florida";
        String zipCode = "33101";
        String email = "micheal@burnnotice.com";
        String phoneNumber = "305.333.0101";

        returnuser.setId(id);
        returnuser.setFirstName(firstName);
        returnuser.setLastName(lastName);
        returnuser.setBillingAddress(new Address());
        returnuser.getBillingAddress().setAddressLine1(addressLine1);
        returnuser.getBillingAddress().setAddressLine2(addressLine2);
        returnuser.getBillingAddress().setCity(city);
        returnuser.getBillingAddress().setState(state);
        returnuser.getBillingAddress().setZipCode(zipCode);
        returnuser.setEmail(email);
        returnuser.setPhoneNumber(phoneNumber);

        when(userService.saveOrUpdate(Matchers.<user>any())).thenReturn(returnuser);

        mockMvc.perform(post("/user")
                .param("id", "1")
                .param("firstName", firstName)
                .param("lastName", lastName)
                .param("shippingAddress.addressLine1", addressLine1)
                .param("shippingAddress.addressLine2", addressLine2)
                .param("shippingAddress.city", city)
                .param("shippingAddress.state", state)
                .param("shippingAddress.zipCode", zipCode)
                .param("email", email)
                .param("phoneNumber", phoneNumber))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:user/show/1"))
                .andExpect(model().attribute("user", instanceOf(user.class)))
                .andExpect(model().attribute("user", hasProperty("firstName", is(firstName))))
                .andExpect(model().attribute("user", hasProperty("lastName", is(lastName))))
                .andExpect(model().attribute("user", hasProperty("shippingAddress", hasProperty("addressLine1", is(addressLine1)))))
                .andExpect(model().attribute("user", hasProperty("shippingAddress", hasProperty("addressLine2", is(addressLine2)))))
                .andExpect(model().attribute("user", hasProperty("shippingAddress", hasProperty("city", is(city)))))
                .andExpect(model().attribute("user", hasProperty("shippingAddress", hasProperty("state", is(state)))))
                .andExpect(model().attribute("user", hasProperty("shippingAddress", hasProperty("zipCode", is(zipCode)))))
                .andExpect(model().attribute("user", hasProperty("email", is(email))))
                .andExpect(model().attribute("user", hasProperty("phoneNumber", is(phoneNumber))));

        ArgumentCaptor<user> userCaptor = ArgumentCaptor.forClass(user.class);
        verify(userService).saveOrUpdate(userCaptor.capture());

        user bounduser = userCaptor.getValue();

        assertEquals(id, bounduser.getId());
        assertEquals(firstName, bounduser.getFirstName());
        assertEquals(lastName, bounduser.getLastName());
        assertEquals(addressLine1, bounduser.getShippingAddress().getAddressLine1());
        assertEquals(addressLine2, bounduser.getShippingAddress().getAddressLine2());
        assertEquals(city, bounduser.getShippingAddress().getCity());
        assertEquals(state, bounduser.getShippingAddress().getState());
        assertEquals(zipCode, bounduser.getShippingAddress().getZipCode());
        assertEquals(email, bounduser.getEmail());
        assertEquals(phoneNumber, bounduser.getPhoneNumber());


    }
}
