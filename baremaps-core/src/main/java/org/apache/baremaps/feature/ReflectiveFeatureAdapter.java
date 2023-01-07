/*
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

package org.apache.baremaps.feature;



import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ReflectiveFeatureAdapter implements Feature {

  private final Object feature;

  public ReflectiveFeatureAdapter(Object feature) {
    this.feature = feature;
  }

  @Override
  public FeatureType getType() {
    var type = feature.getClass();
    var name = type.getSimpleName();
    var fields = type.getDeclaredFields();
    var properties = new HashMap<String, PropertyType>();
    for (Field field : fields) {
      field.setAccessible(true);
      var propertyName = field.getName();
      var propertyType = new PropertyType(propertyName, field.getType());
      properties.put(propertyName, propertyType);
    }
    return new FeatureType(name, properties);
  }

  @Override
  public Object getProperty(String name) {
    try {
      var field = this.getClass().getDeclaredField(name);
      field.setAccessible(true);
      return field.get(this);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException(e);
    } catch (NoSuchFieldException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public void setProperty(String name, Object value) throws IllegalArgumentException {
    try {
      var field = this.getClass().getDeclaredField(name);
      field.setAccessible(true);
      field.set(this, value);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException(e);
    } catch (NoSuchFieldException e) {
      throw new IllegalArgumentException(e);
    }
  }


  @Override
  public Map<String, Object> getProperties() {
    var type = feature.getClass();
    var fields = type.getDeclaredFields();
    var properties = new HashMap<String, Object>();
    for (Field field : fields) {
      field.setAccessible(true);
      var propertyName = field.getName();
      try {
        var propertyValue = field.get(this);
        properties.put(propertyName, propertyValue);
      } catch (IllegalAccessException e) {
        // Ignore the field
      }
    }
    return properties;
  }


}
