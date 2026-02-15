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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FeatureFlagServiceTests {

	@Mock
	private FeatureFlagRepository repository;

	@InjectMocks
	private FeatureFlagService service;

	private FeatureFlag enabledFlag;

	private FeatureFlag disabledFlag;

	@BeforeEach
	void setup() {
		enabledFlag = new FeatureFlag();
		enabledFlag.setId(1);
		enabledFlag.setName("test_flag");
		enabledFlag.setEnabled(true);
		enabledFlag.setDescription("Test flag");

		disabledFlag = new FeatureFlag();
		disabledFlag.setId(2);
		disabledFlag.setName("disabled_flag");
		disabledFlag.setEnabled(false);
		disabledFlag.setDescription("Disabled flag");
	}

	@Test
	void shouldReturnTrueWhenFlagIsEnabled() {
		given(repository.findByName("test_flag")).willReturn(Optional.of(enabledFlag));

		boolean result = service.isEnabled("test_flag");

		assertThat(result).isTrue();
	}

	@Test
	void shouldReturnFalseWhenFlagIsDisabled() {
		given(repository.findByName("disabled_flag")).willReturn(Optional.of(disabledFlag));

		boolean result = service.isEnabled("disabled_flag");

		assertThat(result).isFalse();
	}

	@Test
	void shouldReturnFalseWhenFlagDoesNotExist() {
		given(repository.findByName("non_existent")).willReturn(Optional.empty());

		boolean result = service.isEnabled("non_existent");

		assertThat(result).isFalse();
	}

	@Test
	void shouldFindAllFlags() {
		List<FeatureFlag> flags = Arrays.asList(enabledFlag, disabledFlag);
		given(repository.findAll()).willReturn(flags);

		List<FeatureFlag> result = service.findAll();

		assertThat(result).hasSize(2);
		assertThat(result).contains(enabledFlag, disabledFlag);
	}

	@Test
	void shouldFindFlagById() {
		given(repository.findById(1)).willReturn(Optional.of(enabledFlag));

		Optional<FeatureFlag> result = service.findById(1);

		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(enabledFlag);
	}

	@Test
	void shouldReturnEmptyWhenFlagNotFoundById() {
		given(repository.findById(999)).willReturn(Optional.empty());

		Optional<FeatureFlag> result = service.findById(999);

		assertThat(result).isEmpty();
	}

	@Test
	void shouldFindFlagByName() {
		given(repository.findByName("test_flag")).willReturn(Optional.of(enabledFlag));

		Optional<FeatureFlag> result = service.findByName("test_flag");

		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(enabledFlag);
	}

	@Test
	void shouldCreateNewFlag() {
		FeatureFlag newFlag = new FeatureFlag();
		newFlag.setName("new_flag");
		newFlag.setEnabled(true);
		newFlag.setDescription("New flag");

		given(repository.existsByName("new_flag")).willReturn(false);
		given(repository.save(any(FeatureFlag.class))).willReturn(newFlag);

		FeatureFlag result = service.create(newFlag);

		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("new_flag");
		verify(repository).save(newFlag);
	}

	@Test
	void shouldThrowExceptionWhenCreatingDuplicateFlag() {
		FeatureFlag newFlag = new FeatureFlag();
		newFlag.setName("existing_flag");

		given(repository.existsByName("existing_flag")).willReturn(true);

		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> service.create(newFlag))
			.withMessageContaining("already exists");
		verify(repository, never()).save(any());
	}

	@Test
	void shouldUpdateExistingFlag() {
		FeatureFlag updatedFlag = new FeatureFlag();
		updatedFlag.setName("updated_flag");
		updatedFlag.setEnabled(false);
		updatedFlag.setDescription("Updated description");

		given(repository.findById(1)).willReturn(Optional.of(enabledFlag));
		given(repository.existsByName("updated_flag")).willReturn(false);
		given(repository.save(any(FeatureFlag.class))).willReturn(enabledFlag);

		FeatureFlag result = service.update(1, updatedFlag);

		assertThat(result).isNotNull();
		verify(repository).save(any(FeatureFlag.class));
	}

	@Test
	void shouldThrowExceptionWhenUpdatingNonExistentFlag() {
		FeatureFlag updatedFlag = new FeatureFlag();
		updatedFlag.setName("updated_flag");

		given(repository.findById(999)).willReturn(Optional.empty());

		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> service.update(999, updatedFlag))
			.withMessageContaining("not found");
	}

	@Test
	void shouldThrowExceptionWhenUpdatingWithDuplicateName() {
		FeatureFlag updatedFlag = new FeatureFlag();
		updatedFlag.setName("duplicate_name");

		given(repository.findById(1)).willReturn(Optional.of(enabledFlag));
		given(repository.existsByName("duplicate_name")).willReturn(true);

		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> service.update(1, updatedFlag))
			.withMessageContaining("already exists");
	}

	@Test
	void shouldDeleteFlag() {
		given(repository.existsById(1)).willReturn(true);

		service.delete(1);

		verify(repository).deleteById(1);
	}

	@Test
	void shouldThrowExceptionWhenDeletingNonExistentFlag() {
		given(repository.existsById(999)).willReturn(false);

		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> service.delete(999))
			.withMessageContaining("not found");
	}

	@Test
	void shouldToggleFlagFromEnabledToDisabled() {
		given(repository.findById(1)).willReturn(Optional.of(enabledFlag));
		given(repository.save(any(FeatureFlag.class))).willAnswer(invocation -> {
			FeatureFlag flag = invocation.getArgument(0);
			assertThat(flag.getEnabled()).isFalse();
			return flag;
		});

		FeatureFlag result = service.toggle(1);

		assertThat(result.getEnabled()).isFalse();
		verify(repository).save(enabledFlag);
	}

	@Test
	void shouldToggleFlagFromDisabledToEnabled() {
		given(repository.findById(2)).willReturn(Optional.of(disabledFlag));
		given(repository.save(any(FeatureFlag.class))).willAnswer(invocation -> {
			FeatureFlag flag = invocation.getArgument(0);
			assertThat(flag.getEnabled()).isTrue();
			return flag;
		});

		FeatureFlag result = service.toggle(2);

		assertThat(result.getEnabled()).isTrue();
		verify(repository).save(disabledFlag);
	}

	@Test
	void shouldThrowExceptionWhenTogglingNonExistentFlag() {
		given(repository.findById(999)).willReturn(Optional.empty());

		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> service.toggle(999))
			.withMessageContaining("not found");
	}

}
