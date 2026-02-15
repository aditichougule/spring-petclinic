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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/feature-flags")
public class FeatureFlagController {

	private final FeatureFlagService service;

	public FeatureFlagController(FeatureFlagService service) {
		this.service = service;
	}

	@GetMapping
	public ResponseEntity<List<FeatureFlag>> getAll() {
		return ResponseEntity.ok(service.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<FeatureFlag> getById(@PathVariable Integer id) {
		return service.findById(id)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/name/{name}")
	public ResponseEntity<FeatureFlag> getByName(@PathVariable String name) {
		return service.findByName(name)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/check/{name}")
	public ResponseEntity<Boolean> check(@PathVariable String name) {
		return ResponseEntity.ok(service.isEnabled(name));
	}

	@PostMapping
	public ResponseEntity<FeatureFlag> create(@Valid @RequestBody FeatureFlag featureFlag) {
		try {
			FeatureFlag created = service.create(featureFlag);
			return ResponseEntity.status(HttpStatus.CREATED).body(created);
		}
		catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<FeatureFlag> update(@PathVariable Integer id,
			@Valid @RequestBody FeatureFlag featureFlag) {
		try {
			FeatureFlag updated = service.update(id, featureFlag);
			return ResponseEntity.ok(updated);
		}
		catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Integer id) {
		try {
			service.delete(id);
			return ResponseEntity.noContent().build();
		}
		catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@PatchMapping("/{id}/toggle")
	public ResponseEntity<FeatureFlag> toggle(@PathVariable Integer id) {
		try {
			FeatureFlag toggled = service.toggle(id);
			return ResponseEntity.ok(toggled);
		}
		catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}

}

