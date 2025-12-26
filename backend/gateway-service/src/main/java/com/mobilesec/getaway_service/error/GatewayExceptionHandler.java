package com.mobilesec.getaway_service.error;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

@Component
@Order(-2)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnMissingBean(AbstractErrorWebExceptionHandler.class)
public class GatewayExceptionHandler extends AbstractErrorWebExceptionHandler {

	public GatewayExceptionHandler(ErrorAttributes errorAttributes,
		WebProperties.Resources resources,
		ApplicationContext applicationContext,
		ServerCodecConfigurer codecConfigurer) {
		super(errorAttributes, resources, applicationContext);
		super.setMessageWriters(codecConfigurer.getWriters());
		super.setMessageReaders(codecConfigurer.getReaders());
	}

	@Override
	protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
		return RouterFunctions.route(request -> true, this::renderErrorResponse);
	}

	private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
		Map<String, Object> errorAttributes = getErrorAttributes(request,
			ErrorAttributeOptions.defaults());
		return ServerResponse.status((int) errorAttributes.get("status"))
			.contentType(MediaType.APPLICATION_JSON)
			.body(BodyInserters.fromValue(errorAttributes));
	}
}
