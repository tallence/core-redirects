package com.tallence.core.redirects.studio.method;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallence.core.redirects.helper.RedirectHelper;
import com.tallence.core.redirects.model.RedirectSourceParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * This converter is needed so that we can directly use a list of source parameters as request parameters in the
 * {@link com.tallence.core.redirects.studio.rest.RedirectsResource#validateRedirect(String, String, String, String, Boolean, List)}:
 * <code>@RequestParam List<RedirectSourceParameter> sourceParameters</code>
 */
@Component
public class StringToSourceUrlParameterConverter implements Converter<String, RedirectSourceParameter> {

  private static final Logger LOG = LoggerFactory.getLogger(StringToSourceUrlParameterConverter.class);

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public RedirectSourceParameter convert(String redirectSourceParameter) {
    return Optional.of(redirectSourceParameter)
            .map(this::decode)
            .map(RedirectHelper::parseRedirectSourceParameter)
            .orElse(null);
  }

  private String decode(String value) {
    return URLDecoder.decode(value, StandardCharsets.UTF_8);
  }

}
