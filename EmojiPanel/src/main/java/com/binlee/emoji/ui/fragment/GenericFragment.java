package com.binlee.emoji.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.binlee.emoji.R;
import com.binlee.emoji.generic.BaseGeneric;
import com.binlee.emoji.generic.BaseGenericImpl2;
import com.binlee.emoji.generic.ComplexGeneric;
import com.binlee.emoji.generic.SimpleGeneric;
import com.binlee.emoji.generic.StrList;
import com.binlee.emoji.generic.TypeGetter;
import com.binlee.emoji.helper.LogHelper;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created on 20-9-16.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class GenericFragment extends Fragment {

  private static final String TAG = "GenericFragment";

  private TextView mLogContent;

  public GenericFragment() {
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    Log.d(TAG, "onAttach() called ");
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate() called ");
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
    @Nullable Bundle savedInstanceState) {
    LogHelper.debug(TAG, "onCreateView() called");
    return inflater.inflate(R.layout.fragment_generic, container, false);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    Log.d(TAG, "onActivityCreated() called ");
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    LogHelper.debug(TAG, "onViewCreated() -> savedInstanceState: " + savedInstanceState);
    // view.findViewById(R.id.run).setOnClickListener(v -> startRunning());
    mLogContent = view.findViewById(R.id.logContent);
    startRunning();
  }

  @Override
  public void onStart() {
    super.onStart();
    Log.d(TAG, "onStart() called");
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d(TAG, "onResume() called");
  }

  @Override
  public void onPause() {
    super.onPause();
    Log.d(TAG, "onPause() called");
  }

  @Override
  public void onStop() {
    super.onStop();
    Log.d(TAG, "onStop() called");
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    Log.d(TAG, "onDestroyView() called");
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.d(TAG, "onDestroy() called");
  }

  @Override
  public void onDetach() {
    super.onDetach();
    Log.d(TAG, "onDetach() called");
  }

  private void startRunning() {
    appendI("泛型擦除：<br/>");
    appendV("new ArrayList<String>().getClass() -> " + new ArrayList<String>().getClass());
    appendV("<br/>new ArrayList<Integer>().getClass() -> " + new ArrayList<Integer>().getClass());

    appendI("<br/>泛型参数:<br/>");
    appendV("class StrList extends ArrayList<String><br/>");
    final Type type = StrList.class.getGenericSuperclass();
    appendV("StrList.class.getGenericSuperclass() -> " + type);
    if (type != null) {
      if (type instanceof ParameterizedType) {
        final Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();
        for (Type arg : typeArguments) {
          appendV("<br/>param type: " + arg);
        }
      }
    }
    appendV("<br/>strList.getClass().getGenericInterfaces() ->");
    final Type[] types = StrList.class.getGenericInterfaces();
    for (Type t : types) {
      appendV("<br/>type: " + t);
    }

    foo(new SimpleGeneric("sleticalboy", 26));
    foo(new ComplexGeneric());

    typeGetter();

    try {
      // 上边界：是 BaseGeneric 子类的元素，都能放入此集合
      final List<? extends BaseGeneric> upperList = new ArrayList<>();
      upperBonds(upperList);
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      // 下边界：任何是 BaseGenericImpl2 父类/接口 的元素，都能放入此集合
      final List<? super BaseGenericImpl2> lowerList = new ArrayList<>();
      lowerBonds(lowerList);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void upperBonds(final List<? extends BaseGeneric> upper) throws Exception {
    Log.d(TAG, "upperBonds() param.getClass(): " + upper.getClass());
    final Method method = getClass().getDeclaredMethod("upperBonds", List.class);
    final Type[] parameterTypes = method.getGenericParameterTypes();
    for (Type type : parameterTypes) {
      Log.d(TAG, "upperBonds() param.type: " + type);
      if (type instanceof ParameterizedType) {
        Log.d(TAG, "type.getRawType(): " + ((ParameterizedType) type).getRawType());
        Log.d(TAG, "type.getOwnerType(): " + ((ParameterizedType) type).getOwnerType());
        final Type[] types = ((ParameterizedType) type).getActualTypeArguments();
        for (Type t : types) {
          Log.d(TAG, "param actual type: " + t);
          if (t instanceof WildcardType) {
            Type[] bounds = ((WildcardType) t).getUpperBounds();
            if (bounds.length == 0 || (bounds.length == 1 && bounds[0] == Object.class)) {
              Log.w(TAG, t + " has no upper bonds.");
            }
            for (Type bound : bounds) {
              Log.d(TAG, "param wildcard type upper bond: " + bound);
            }
            bounds = ((WildcardType) t).getLowerBounds();
            if (bounds.length == 0 || (bounds.length == 1 && bounds[0] == Object.class)) {
              Log.w(TAG, t + " has no lower bonds.");
            }
            for (Type bound : bounds) {
              Log.d(TAG, "param wildcard type lower bond: " + bound);
            }
          }
        }
      }
    }
  }

  private void lowerBonds(List<? super BaseGenericImpl2> lower) throws Exception {
    Log.d(TAG, "\r\nlowerBonds() param.getClass(): " + lower.getClass());
    final Method method = getClass().getDeclaredMethod("lowerBonds", List.class);
    final Type[] parameterTypes = method.getGenericParameterTypes();
    for (Type type : parameterTypes) {
      Log.d(TAG, "lowerBonds() param.type: " + type);
      if (type instanceof ParameterizedType) {
        Log.d(TAG, "type.getRawType(): " + ((ParameterizedType) type).getRawType());
        Log.d(TAG, "type.getOwnerType(): " + ((ParameterizedType) type).getOwnerType());
        final Type[] types = ((ParameterizedType) type).getActualTypeArguments();
        for (Type t : types) {
          Log.d(TAG, "param actual type: " + t);
          if (t instanceof WildcardType) {
            Type[] bounds = ((WildcardType) t).getUpperBounds();
            for (Type bound : bounds) {
              Log.d(TAG, "param wildcard type upper bond: " + bound);
            }
            if (bounds.length == 0 || (bounds.length == 1 && bounds[0] == Object.class)) {
              Log.w(TAG, t + " has no upper bonds.");
            }

            bounds = ((WildcardType) t).getLowerBounds();
            for (Type bound : bounds) {
              Log.d(TAG, "param wildcard type lower bond: " + bound);
            }
            if (bounds.length == 0 || (bounds.length == 1 && bounds[0] == Object.class)) {
              Log.w(TAG, t + " has no lower bonds.");
            }
          }
        }
      }
    }
  }

  private void typeGetter() {
    appendV("<br/>List<String>'s generic parameter type is " + new TypeGetter<List<String>>() {
    }.getType());
    appendV("<br/>ArrayList<String>'s generic parameter type is " + new TypeGetter<ArrayList<String>>() {
    }.getType());
    appendV("<br/>List<ArrayList<String>>'s generic parameter type is " + new TypeGetter<List<ArrayList<String>>>() {
    }.getType());
    appendV("<br/>SimpleGeneric<String, Integer>'s generic parameter type is " + new TypeGetter<SimpleGeneric>() {
    }.getType());
    appendV("<br/>ComplexGeneric<>'s generic parameter type is " + new TypeGetter<ComplexGeneric>() {
    }.getType());
  }

  private void foo(Object obj) {
    final Type superclass = obj.getClass().getGenericSuperclass();
    appendV("<br/>" + obj.getClass().getName() + ".class.getGenericSuperclass() -> " + superclass);
    if (superclass instanceof ParameterizedType) {
      final Type[] arguments = ((ParameterizedType) superclass).getActualTypeArguments();
      for (Type arg : arguments) {
        appendV("<br/>param type: " + arg);
      }
    }
    final Type[] interfaces = obj.getClass().getGenericInterfaces();
    appendV("<br/>" + obj.getClass().getName() + ".class.getGenericInterfaces() -> " + Arrays.toString(interfaces));
    for (Type inter : interfaces) {
      if (inter instanceof ParameterizedType) {
        final Type[] arguments = ((ParameterizedType) inter).getActualTypeArguments();
        for (Type arg : arguments) {
          appendV("<br/>param type: " + arg);
        }
      }
    }
    newLine();
  }

  private void appendV(Object obj) {
    log(Log.VERBOSE, obj);
  }

  private void appendD(Object obj) {
    log(Log.DEBUG, obj);
  }

  private void appendI(Object obj) {
    log(Log.INFO, obj);
  }

  private void appendW(Object obj) {
    log(Log.ERROR, obj);
  }

  private void newLine() {
    mLogContent.append("<br/>");
  }

  private static final String LOG_FORMAT = "<font color='%s'>%s</font>";

  private void log(int priority, Object obj) {
    String msg;
    if (obj instanceof String) {
      msg = ((String) obj);
    } else {
      msg = String.valueOf(obj);
    }
    mLogContent.append(Html.fromHtml(
      String.format(Locale.ENGLISH, LOG_FORMAT, getColor(priority), msg)));
    Log.println(priority, TAG, msg.replace("<br/>", ""));
  }

  private String getColor(int priority) {
    switch (priority) {
      case Log.VERBOSE:
        return "#000000";
      case Log.DEBUG:
        return "#0DEBFF";
      case Log.INFO:
        return "#48BB31";
      case Log.WARN:
        return "#BBBB23";
      default:
        return "#ff0000";
    }
  }
}
