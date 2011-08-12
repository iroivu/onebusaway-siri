/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.siri.core.versioning;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class ElementToListPropertyConverter implements PropertyConverter {

  private final VersionConverter _converter;

  private final Method _from;

  private final Method _to;

  public ElementToListPropertyConverter(VersionConverter converter,
      Method from, Method to) {
    _converter = converter;
    _from = from;
    _to = to;
  }

  @Override
  public void convert(Object source, Object target) {

    Object sourceProperty = PropertyConverterSupport.getSourcePropertyValue(
        source, _from);

    if (sourceProperty == null)
      return;

    Object targetProperty = _converter.convert(sourceProperty);
    List<Object> targetList = Arrays.asList(targetProperty);
    PropertyConverterSupport.setTargetPropertyValues(target, _to, targetList);
  }
}
