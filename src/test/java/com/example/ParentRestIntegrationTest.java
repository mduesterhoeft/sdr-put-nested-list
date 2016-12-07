/*     / \____  _    _  ____   ______  / \ ____  __    _______
 *    /  /    \/ \  / \/    \ /  /\__\/  //    \/  \  //  /\__\   JΛVΛSLΛNG
 *  _/  /  /\  \  \/  /  /\  \\__\\  \  //  /\  \ /\\/ \ /__\ \   Copyright 2014-2016 Javaslang, http://javaslang.io
 * /___/\_/  \_/\____/\_/  \_/\__\/__/\__\_/  \_//  \__/\_____/   Licensed under the Apache License, Version 2.0
 */
package com.example;

import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.EntityLinks;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ParentRestIntegrationTest {
    
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityLinks entityLinks;

    @Autowired
    private ParentRepository parentRepository;

    private URI parentsUri;
    private URI parentUri;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        parentsUri = entityLinks.linkFor(Parent.class).toUri();
        parentRepository.deleteAll();
    }

    @Test
    public void should_update_existing_item() throws Exception {

        givenParent();

        Map<String, Object> jsonMap = ImmutableMap.of(
                "child", ImmutableMap.of(
                        "items",ImmutableList.of(
                                ImmutableMap.of("some", "test123")
                        )
                )
        );
        then(parentRepository.findAll().get(0).getChild().getItems()).hasSize(1);
        String json = objectMapper.writeValueAsString(jsonMap);
        System.out.println(json);
        mockMvc
                .perform(put(parentUri)
                        .content(json))
                .andDo(print())
                .andExpect(status().isNoContent())
        ;

        then(parentRepository.count()).isEqualTo(1);
        then(parentRepository.findAll().get(0).getChild().getItems()).hasSize(1);
        then(parentRepository.findAll().get(0).getChild().getItems().get(0).getSome()).isEqualTo("test123");
    }

    @Test
    public void should_add_new_item() throws Exception {

        givenParent();

        Map<String, Object> jsonMap = ImmutableMap.of(
                "child", ImmutableMap.of(
                        "items",ImmutableList.of(
                                ImmutableMap.of("some", "test123"),
                                ImmutableMap.of("some", "test1231")
                        )
                )
        );
        then(parentRepository.findAll().get(0).getChild().getItems()).hasSize(1);
        String json = objectMapper.writeValueAsString(jsonMap);
        System.out.println(json);
        mockMvc
                .perform(put(parentUri)
                        .content(json))
                .andDo(print())
                .andExpect(status().isNoContent())
        ;

        then(parentRepository.count()).isEqualTo(1);
        then(parentRepository.findAll().get(0).getChild().getItems()).hasSize(2);
        then(parentRepository.findAll().get(0).getChild().getItems().get(0).getSome()).isEqualTo("test123");
        then(parentRepository.findAll().get(0).getChild().getItems().get(1).getSome()).isEqualTo("test1231");
    }


    private void givenParent() throws Exception {
        Map<String, Object> jsonMap = ImmutableMap.of(
                "child", ImmutableMap.of(
                        "items",ImmutableList.of(
                                ImmutableMap.of("some", "test")
                        )
                )
        );
        String location = mockMvc
                .perform(post(parentsUri)
                        .content(objectMapper.writeValueAsString(jsonMap)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string(LOCATION, org.hamcrest.Matchers.startsWith(parentsUri.toString())))
                .andReturn().getResponse().getHeader(LOCATION)
                ;
        parentUri = URI.create(location);

    }
}
