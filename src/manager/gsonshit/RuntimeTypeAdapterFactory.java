//Taken from Google, not my code.

package manager.gsonshit;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.internal.Streams;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Supports serializing and deserializing objects using a type field
 * to determine the actual subtype.
 */
public class RuntimeTypeAdapterFactory<T> implements TypeAdapterFactory {
  private final Class<?> baseType;
  private final String typeFieldName;
  private final Map<String, Class<?>> labelToSubtype = new LinkedHashMap<>();
  private final Map<Class<?>, String> subtypeToLabel = new LinkedHashMap<>();

  private RuntimeTypeAdapterFactory(Class<?> baseType, String typeFieldName) {
    if (typeFieldName == null || baseType == null) {
      throw new NullPointerException();
    }
    this.baseType = baseType;
    this.typeFieldName = typeFieldName;
  }

  public static <T> RuntimeTypeAdapterFactory<T> of(Class<T> baseType, String typeFieldName) {
    return new RuntimeTypeAdapterFactory<>(baseType, typeFieldName);
  }

  public static <T> RuntimeTypeAdapterFactory<T> of(Class<T> baseType) {
    return new RuntimeTypeAdapterFactory<>(baseType, "type");
  }

  public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> type, String label) {
    if (type == null || label == null) {
      throw new NullPointerException();
    }
    if (subtypeToLabel.containsKey(type) || labelToSubtype.containsKey(label)) {
      throw new IllegalArgumentException("Type or label already registered.");
    }
    labelToSubtype.put(label, type);
    subtypeToLabel.put(type, label);
    return this;
  }

  public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> type) {
    return registerSubtype(type, type.getSimpleName());
  }

  @Override
  public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> type) {
    if (!baseType.isAssignableFrom(type.getRawType())) {
      return null;
    }

    final Map<String, TypeAdapter<?>> labelToDelegate = new LinkedHashMap<>();
    final Map<Class<?>, TypeAdapter<?>> subtypeToDelegate = new LinkedHashMap<>();

    for (Map.Entry<String, Class<?>> entry : labelToSubtype.entrySet()) {
      TypeAdapter<?> delegate = gson.getDelegateAdapter(this, TypeToken.get(entry.getValue()));
      labelToDelegate.put(entry.getKey(), delegate);
      subtypeToDelegate.put(entry.getValue(), delegate);
    }

    return new TypeAdapter<R>() {
      @Override
      public void write(JsonWriter out, R value) throws IOException {
        Class<?> srcType = value.getClass();
        String label = subtypeToLabel.get(srcType);
        @SuppressWarnings("unchecked")
        TypeAdapter<R> delegate = (TypeAdapter<R>) subtypeToDelegate.get(srcType);
        if (delegate == null) {
          throw new JsonParseException("Cannot serialize: unknown type " + srcType);
        }

        JsonObject jsonObject = delegate.toJsonTree(value).getAsJsonObject();
        JsonObject clone = new JsonObject();

        clone.add(typeFieldName, new JsonPrimitive(label));
        for (Map.Entry<String, JsonElement> e : jsonObject.entrySet()) {
          clone.add(e.getKey(), e.getValue());
        }

        Streams.write(clone, out);
      }

      @Override
      public R read(JsonReader in) throws IOException {
        JsonElement jsonElement = Streams.parse(in);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        JsonElement labelJsonElement = jsonObject.remove(typeFieldName);
        if (labelJsonElement == null) {
          throw new JsonParseException("Missing type field: " + typeFieldName);
        }

        String label = labelJsonElement.getAsString();
        @SuppressWarnings("unchecked")
        TypeAdapter<R> delegate = (TypeAdapter<R>) labelToDelegate.get(label);
        if (delegate == null) {
          throw new JsonParseException("Unknown type label: " + label);
        }

        return delegate.fromJsonTree(jsonObject);
      }
    };
  }
}