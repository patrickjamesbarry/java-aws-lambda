/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.opentracing.aws;


import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import software.amazon.awssdk.http.SdkHttpRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class HeadersParser {

  private HeadersParser() {}

  static <Input> SpanContext parseAndExtract(Tracer tracer, Input input) {
    try {
      if (input instanceof Map) {
        Map map = (Map) input;
        final Object headers = map.get("headers");
        if (headers instanceof Map) {
          final Map<String, String> headerStr = (Map<String, String>) headers;
          return tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapAdapter(headerStr));
        }
      } else if (input instanceof SdkHttpRequest) {
        final SdkHttpRequest request = (SdkHttpRequest) input;
        Map<String, String> headers = toSimpleMap(request.headers());

        return tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapAdapter(headers));
      }
    } catch (IllegalArgumentException exception) {
    }
    return null;
  }

  private static Map<String, String> toSimpleMap(Map<String, List<String>> mulitValuedMap){
    Map<String,String> simpleMap = new HashMap<>();
    for(Map.Entry<String, List<String>> entry : mulitValuedMap.entrySet()){
      simpleMap.put(entry.getKey(), String.join(", ", entry.getValue()));
    }
    return simpleMap;
  }
}
