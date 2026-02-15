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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
class FeatureFlagAspect {

	private final FeatureFlagService featureFlagService;

	FeatureFlagAspect(FeatureFlagService featureFlagService) {
		this.featureFlagService = featureFlagService;
	}

	@Around("@annotation(featureFlagRequired)")
	public Object checkFeatureFlag(ProceedingJoinPoint joinPoint, FeatureFlagRequired featureFlagRequired)
			throws Throwable {
		String flagName = featureFlagRequired.value();
		boolean isEnabled = featureFlagService.isEnabled(flagName);

		if (!isEnabled) {
			if (featureFlagRequired.throwException()) {
				throw new FeatureFlagDisabledException("Feature flag '" + flagName + "' is disabled");
			}
			else {
				return getDefaultReturnValue(joinPoint);
			}
		}

		return joinPoint.proceed();
	}

	private Object getDefaultReturnValue(ProceedingJoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Class<?> returnType = signature.getReturnType();

		if (returnType == boolean.class || returnType == Boolean.class) {
			return false;
		}
		return null;
	}

}
