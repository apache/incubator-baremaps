/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.baremaps.server;


import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Get;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.function.Supplier;

public class TemplateService {

  private final Template template;

  public final Supplier<?> dataModelSupplier;

  public TemplateService(String template, Supplier<?> dataModelSupplier) throws IOException {
    Configuration config = new Configuration(Configuration.VERSION_2_3_29);
    config.setLocale(Locale.US);
    config.setClassForTemplateLoading(this.getClass(), "/");
    config.setDefaultEncoding("UTF-8");
    config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    config.setLogTemplateExceptions(false);
    config.setWrapUncheckedExceptions(true);
    config.setFallbackOnNullLoopVariable(false);
    this.template = config.getTemplate(template);
    this.dataModelSupplier = dataModelSupplier;
  }

  @Get(produces = MediaType.TEXT_PLAIN)
  public String render() throws IOException, TemplateException {
    StringWriter output = new StringWriter();
    template.process(this.dataModelSupplier.get(), output);
    return output.toString();
  }

}
