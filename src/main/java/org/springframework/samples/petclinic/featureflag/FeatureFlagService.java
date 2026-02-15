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

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FeatureFlagService {

	private final FeatureFlagRepository repository;

	public FeatureFlagService(FeatureFlagRepository repository) {
		this.repository = repository;
	}

	@Cacheable(value = "featureFlags", key = "#flagName")
	@Transactional(readOnly = true)
	public boolean isEnabled(String flagName) {
		return repository.findByName(flagName).map(FeatureFlag::getEnabled).orElse(false);
	}

	@Transactional(readOnly = true)
	public List<FeatureFlag> findAll() {
		return repository.findAll();
	}

	@Transactional(readOnly = true)
	public Optional<FeatureFlag> findById(Integer id) {
		return repository.findById(id);
	}

	@Transactional(readOnly = true)
	public Optional<FeatureFlag> findByName(String name) {
		return repository.findByName(name);
	}

	@CacheEvict(value = "featureFlags", key = "#featureFlag.name")
	public FeatureFlag create(FeatureFlag featureFlag) {
		if (repository.existsByName(featureFlag.getName())) {
			throw new IllegalArgumentException(
					"Feature flag with name '" + featureFlag.getName() + "' already exists");
		}
		return repository.save(featureFlag);
	}

	@CacheEvict(value = "featureFlags", allEntries = true)
	public FeatureFlag update(Integer id, FeatureFlag featureFlag) {
		FeatureFlag existing = repository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Feature flag not found with id: " + id));

		if (!existing.getName().equals(featureFlag.getName())
				&& repository.existsByName(featureFlag.getName())) {
			throw new IllegalArgumentException(
					"Feature flag with name '" + featureFlag.getName() + "' already exists");
		}

		existing.setName(featureFlag.getName());
		existing.setEnabled(featureFlag.getEnabled());
		existing.setDescription(featureFlag.getDescription());

		return repository.save(existing);
	}

	@CacheEvict(value = "featureFlags", allEntries = true)
	public void delete(Integer id) {
		if (!repository.existsById(id)) {
			throw new IllegalArgumentException("Feature flag not found with id: " + id);
		}
		repository.deleteById(id);
	}

	@CacheEvict(value = "featureFlags", key = "#result.name", condition = "#result != null")
	public FeatureFlag toggle(Integer id) {
		FeatureFlag flag = repository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Feature flag not found with id: " + id));
		flag.setEnabled(!flag.getEnabled());
		return repository.save(flag);
	}

}

