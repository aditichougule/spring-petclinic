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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import org.springframework.samples.petclinic.featureflag.FeatureFlagService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OwnerController.class)
@DisabledInNativeImage
@DisabledInAotMode
class OwnerControllerWithFeatureFlagTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	@MockitoBean
	private FeatureFlagService featureFlagService;

	private Owner testOwner;

	@BeforeEach
	void setup() {
		testOwner = new Owner();
		testOwner.setId(1);
		testOwner.setFirstName("George");
		testOwner.setLastName("Franklin");
		testOwner.setAddress("110 W. Liberty St.");
		testOwner.setCity("Madison");
		testOwner.setTelephone("6085551023");
	}

	@Test
	void shouldAllowSearchWhenFlagIsEnabled() throws Exception {
		given(featureFlagService.isEnabled("owner_search")).willReturn(true);
		Page<Owner> page = new PageImpl<>(Arrays.asList(testOwner, testOwner, testOwner));
		given(owners.findByLastNameStartingWith(anyString(), any(Pageable.class))).willReturn(page);

		mockMvc.perform(get("/owners").param("lastName", "Franklin").param("page", "1"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"));
	}

	@Test
	void shouldBlockSearchWhenFlagIsDisabled() throws Exception {
		given(featureFlagService.isEnabled("owner_search")).willReturn(false);

		mockMvc.perform(get("/owners").param("lastName", "Franklin"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/findOwners"))
			.andExpect(model().attributeHasErrors("owner"));
	}

}
