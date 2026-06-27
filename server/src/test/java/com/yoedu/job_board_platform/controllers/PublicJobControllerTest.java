package com.yoedu.job_board_platform.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoedu.job_board_platform.TestcontainersConfiguration;
import com.yoedu.job_board_platform.models.JobCategory;
import com.yoedu.job_board_platform.repositories.JobCategoryRepository;
import com.yoedu.job_board_platform.repositories.JobRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class PublicJobControllerTest {

        @Autowired
        MockMvc mockMvc;
        @Autowired
        JobCategoryRepository jobCategoryRepository;
        @Autowired
        JobRepository jobRepository;

        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void cleanup() {
                jobRepository.deleteAll();
                jobCategoryRepository.deleteAll();
                jobCategoryRepository.save(JobCategory.builder().name("IT").build());
        }

        // -----------------------------------------------------------------
        // GET /api/public/categories — getCategories
        // -----------------------------------------------------------------

        @Test
        void getCategories_returnsAllCategories() throws Exception {
                jobCategoryRepository.save(JobCategory.builder().name("Finance").build());

				var res = mockMvc.perform(get("/api/categories"))
                                .andExpect(status().isOk())
                                .andReturn();

                var json = objectMapper.readTree(res.getResponse().getContentAsString());
                assertThat(json).hasSize(2);
                assertThat(json.get(0).get("name").asText()).isEqualTo("IT");
                assertThat(json.get(1).get("name").asText()).isEqualTo("Finance");
        }

        @Test
        void getCategories_whenEmpty_returnsEmptyList() throws Exception {
                jobCategoryRepository.deleteAll();

				var res = mockMvc.perform(get("/api/categories"))
                                .andExpect(status().isOk())
                                .andReturn();

                var json = objectMapper.readTree(res.getResponse().getContentAsString());
                assertThat(json).isEmpty();
        }
}
