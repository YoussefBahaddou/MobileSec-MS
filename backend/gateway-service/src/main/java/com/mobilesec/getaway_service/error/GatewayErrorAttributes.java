package com.mobilesec.getaway_service.error;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

@Component
public class GatewayErrorAttributes extends DefaultErrorAttributes {

	@Override
	@NonNull
	public Map<String, Object> getErrorAttributes(@NonNull ServerRequest request, @NonNull ErrorAttributeOptions options) {
		Throwable error = getError(request);
		HttpStatus status = determineStatus(error);

		Map<String, Object> attributes = new LinkedHashMap<>();
		attributes.put("timestamp", OffsetDateTime.now());
		attributes.put("status", status.value());
		attributes.put("error", status.getReasonPhrase());
		attributes.put("message", buildMessage(error, status));
		attributes.put("path", request.path());

		return attributes;
	}

	private HttpStatus determineStatus(Throwable error) {
		if (error instanceof ResponseStatusException responseStatusException) {
			return resolveStatus(responseStatusException.getStatusCode().value());
		}

		if (error instanceof WebClientRequestException || hasCause(error, WebClientRequestException.class)) {
			return HttpStatus.SERVICE_UNAVAILABLE;
		}

		if (error instanceof NotFoundException notFoundException) {
			String message = notFoundException.getMessage();
			if (message != null && message.toLowerCase().contains("instance")) {
				return HttpStatus.SERVICE_UNAVAILABLE;
			}
			return HttpStatus.NOT_FOUND;
		}

		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	private boolean hasCause(Throwable error, Class<? extends Throwable> type) {
		Throwable root = NestedExceptionUtils.getMostSpecificCause(error);
		return type.isInstance(error) || (root != null && type.isInstance(root));
	}

	private String buildMessage(Throwable error, HttpStatus status) {
		if (status == HttpStatus.SERVICE_UNAVAILABLE) {
			return "Downstream service is unavailable. Please try again later.";
		}

		if (status == HttpStatus.NOT_FOUND) {
			return "Requested resource was not found.";
		}

		return "An unexpected error occurred.";
	}

	private HttpStatus resolveStatus(int code) {
		HttpStatus resolved = HttpStatus.resolve(code);
		return resolved != null ? resolved : HttpStatus.INTERNAL_SERVER_ERROR;
	}
}
