package org.onebusaway.siri.core.versioning;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ListPropertyConverter implements PropertyConverter {

  private final VersionConverter _converter;

  private final Method _from;

  private final Method _to;

  public ListPropertyConverter(VersionConverter converter, Method from,
      Method to) {
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

    List<?> sourceList = (List<?>) sourceProperty;
    if (sourceList.isEmpty())
      return;

    List<Object> targetList = new ArrayList<Object>(sourceList.size());
    for (Object sourceValue : sourceList) {
      Object targetProperty = _converter.convert(sourceValue);
      targetList.add(targetProperty);
    }

    PropertyConverterSupport.setTargetPropertyValues(target, _to, targetList);
  }
}
