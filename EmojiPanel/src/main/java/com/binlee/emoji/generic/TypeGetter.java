package com.binlee.emoji.generic;

import android.util.Log;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created on 20-9-17.
 *
 * @author Ben binli@grandstream.cn
 */
public abstract class TypeGetter<T> {

  private final Type mType;

  public TypeGetter() {
    // 获取泛型父类
    Type superclass = getClass().getGenericSuperclass();
    if (!(superclass instanceof ParameterizedType)) {
      throw new IllegalStateException(getClass() + "'s super class is not a generic class.");
    }
    Type[] types = ((ParameterizedType) superclass).getActualTypeArguments();
    mType = types[0];
    Log.d("TypeGetter", "mType: " + mType + ", superclass: " + superclass);

    if (mType instanceof Class<?>) {
      // 获取泛型接口
      final Type[] interfaces = ((Class<?>) mType).getGenericInterfaces();
      for (Type inter : interfaces) {
        Log.d("TypeGetter", "inter: " + inter);
        if (inter instanceof ParameterizedType) {
          types = ((ParameterizedType) inter).getActualTypeArguments();
          for (Type type : types) {
            Log.d("TypeGetter", "type:" + type);
          }
        }
      }
    }
  }

  public Type getType() {
    return mType;
  }
}
