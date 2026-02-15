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

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
class FeatureFlagInitializer implements CommandLineRunner {

	private final FeatureFlagService featureFlagService;

	FeatureFlagInitializer(FeatureFlagService featureFlagService) {
		this.featureFlagService = featureFlagService;
	}

	@Override
	public void run(String... args) {
		createIfNotExists("add_new_pet", true, "Enable adding new pets");
		createIfNotExists("add_visit", true, "Enable adding visits");
		createIfNotExists("owner_search", true, "Enable owner search");
	}

	private void createIfNotExists(String name, boolean enabled, String description) {
		if (!featureFlagService.findByName(name).isPresent()) {
			FeatureFlag flag = new FeatureFlag();
			flag.setName(name);
			flag.setEnabled(enabled);
			flag.setDescription(description);
			featureFlagService.create(flag);
		}
	}

}

