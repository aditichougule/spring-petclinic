/*
 * Copyright 2012-2025 the original author or authors.
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
package org.springframework.samples.petclinic.featureflag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FeatureFlagController.class)
@DisabledInNativeImage
@DisabledInAotMode
class FeatureFlagControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private FeatureFlagService service;

	private FeatureFlag testFlag;

	@BeforeEach
	void setup() {
		testFlag = new FeatureFlag();
		testFlag.setId(1);
		testFlag.setName("test_flag");
		testFlag.setEnabled(true);
		testFlag.setDescription("Test flag");
		testFlag.setCreatedAt(LocalDateTime.now());
		testFlag.setUpdatedAt(LocalDateTime.now());
	}

	@Test
	void shouldGetAllFlags() throws Exception {
		List<FeatureFlag> flags = Arrays.asList(testFlag);
		given(service.findAll()).willReturn(flags);

		mockMvc.perform(get("/api/feature-flags"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$[0].id").value(1))
			.andExpect(jsonPath("$[0].name").value("test_flag"))
			.andExpect(jsonPath("$[0].enabled").value(true));
	}

	@Test
	void shouldGetFlagById() throws Exception {
		given(service.findById(1)).willReturn(Optional.of(testFlag));

		mockMvc.perform(get("/api/feature-flags/1"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.name").value("test_flag"))
			.andExpect(jsonPath("$.enabled").value(true));
	}

	@Test
	void shouldReturnNotFoundWhenFlagNotFoundById() throws Exception {
		given(service.findById(999)).willReturn(Optional.empty());

		mockMvc.perform(get("/api/feature-flags/999")).andExpect(status().isNotFound());
	}

	@Test
	void shouldGetFlagByName() throws Exception {
		given(service.findByName("test_flag")).willReturn(Optional.of(testFlag));

		mockMvc.perform(get("/api/feature-flags/name/test_flag"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.name").value("test_flag"));
	}

	@Test
	void shouldReturnNotFoundWhenFlagNotFoundByName() throws Exception {
		given(service.findByName("non_existent")).willReturn(Optional.empty());

		mockMvc.perform(get("/api/feature-flags/name/non_existent")).andExpect(status().isNotFound());
	}

	@Test
	void shouldCheckIfFlagIsEnabled() throws Exception {
		given(service.isEnabled("test_flag")).willReturn(true);

		mockMvc.perform(get("/api/feature-flags/check/test_flag"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").value(true));
	}

	@Test
	void shouldCheckIfFlagIsDisabled() throws Exception {
		given(service.isEnabled("disabled_flag")).willReturn(false);

		mockMvc.perform(get("/api/feature-flags/check/disabled_flag"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").value(false));
	}

	@Test
	void shouldCreateNewFlag() throws Exception {
		FeatureFlag newFlag = new FeatureFlag();
		newFlag.setName("new_flag");
		newFlag.setEnabled(true);
		newFlag.setDescription("New flag");

		given(service.create(any(FeatureFlag.class))).willReturn(newFlag);

		String jsonContent = "{\"name\":\"new_flag\",\"enabled\":true,\"description\":\"New flag\"}";

		mockMvc.perform(post("/api/feature-flags").contentType(MediaType.APPLICATION_JSON).content(jsonContent))
			.andExpect(status().isCreated())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.name").value("new_flag"));
	}

	@Test
	void shouldReturnBadRequestWhenCreatingDuplicateFlag() throws Exception {
		given(service.create(any(FeatureFlag.class)))
			.willThrow(new IllegalArgumentException("Feature flag with name 'existing_flag' already exists"));

		String jsonContent = "{\"name\":\"existing_flag\",\"enabled\":true}";

		mockMvc.perform(post("/api/feature-flags").contentType(MediaType.APPLICATION_JSON).content(jsonContent))
			.andExpect(status().isBadRequest());
	}

	@Test
	void shouldUpdateFlag() throws Exception {
		FeatureFlag updatedFlag = new FeatureFlag();
		updatedFlag.setName("updated_flag");
		updatedFlag.setEnabled(false);
		updatedFlag.setDescription("Updated");

		given(service.update(eq(1), any(FeatureFlag.class))).willReturn(updatedFlag);

		String jsonContent = "{\"name\":\"updated_flag\",\"enabled\":false,\"description\":\"Updated\"}";

		mockMvc.perform(put("/api/feature-flags/1").contentType(MediaType.APPLICATION_JSON).content(jsonContent))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("updated_flag"))
			.andExpect(jsonPath("$.enabled").value(false));
	}

	@Test
	void shouldReturnNotFoundWhenUpdatingNonExistentFlag() throws Exception {
		given(service.update(eq(999), any(FeatureFlag.class)))
			.willThrow(new IllegalArgumentException("Feature flag not found with id: 999"));

		String jsonContent = "{\"name\":\"updated_flag\"}";

		mockMvc.perform(put("/api/feature-flags/999").contentType(MediaType.APPLICATION_JSON).content(jsonContent))
			.andExpect(status().isNotFound());
	}

	@Test
	void shouldDeleteFlag() throws Exception {
		mockMvc.perform(delete("/api/feature-flags/1")).andExpect(status().isNoContent());
	}

	@Test
	void shouldReturnNotFoundWhenDeletingNonExistentFlag() throws Exception {
		doThrow(new IllegalArgumentException("Feature flag not found with id: 999")).when(service).delete(999);

		mockMvc.perform(delete("/api/feature-flags/999")).andExpect(status().isNotFound());
	}

	@Test
	void shouldToggleFlag() throws Exception {
		FeatureFlag toggledFlag = new FeatureFlag();
		toggledFlag.setId(1);
		toggledFlag.setName("test_flag");
		toggledFlag.setEnabled(false);

		given(service.toggle(1)).willReturn(toggledFlag);

		mockMvc.perform(patch("/api/feature-flags/1/toggle"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.enabled").value(false));
	}

	@Test
	void shouldReturnNotFoundWhenTogglingNonExistentFlag() throws Exception {
		given(service.toggle(999)).willThrow(new IllegalArgumentException("Feature flag not found with id: 999"));

		mockMvc.perform(patch("/api/feature-flags/999/toggle")).andExpect(status().isNotFound());
	}

}
