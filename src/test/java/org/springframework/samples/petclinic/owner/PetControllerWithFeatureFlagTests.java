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
package org.springframework.samples.petclinic.owner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import org.springframework.samples.petclinic.featureflag.FeatureFlagService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PetController.class)
@DisabledInNativeImage
@DisabledInAotMode
class PetControllerWithFeatureFlagTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	@MockitoBean
	private PetTypeRepository types;

	@MockitoBean
	private FeatureFlagService featureFlagService;

	private Owner testOwner;

	private PetType petType;

	@BeforeEach
	void setup() {
		testOwner = new Owner();
		testOwner.setId(1);
		testOwner.setFirstName("George");
		testOwner.setLastName("Franklin");
		testOwner.setAddress("110 W. Liberty St.");
		testOwner.setCity("Madison");
		testOwner.setTelephone("6085551023");

		petType = new PetType();
		petType.setId(1);
		petType.setName("dog");

		given(owners.findById(1)).willReturn(Optional.of(testOwner));
		given(types.findPetTypes()).willReturn(List.of(petType));
	}

	@Test
	void shouldShowPetFormWhenFlagIsEnabled() throws Exception {
		given(featureFlagService.isEnabled("add_new_pet")).willReturn(true);

		mockMvc.perform(get("/owners/1/pets/new"))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdatePetForm"));
	}

	@Test
	void shouldRedirectWhenFlagIsDisabled() throws Exception {
		given(featureFlagService.isEnabled("add_new_pet")).willReturn(false);

		mockMvc.perform(get("/owners/1/pets/new"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/owners/1"));
	}

	@Test
	void shouldBlockPetCreationWhenFlagIsDisabled() throws Exception {
		given(featureFlagService.isEnabled("add_new_pet")).willReturn(false);

		mockMvc.perform(post("/owners/1/pets/new").param("name", "Max").param("birthDate", "2020-01-01"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/owners/1"));
	}

}
