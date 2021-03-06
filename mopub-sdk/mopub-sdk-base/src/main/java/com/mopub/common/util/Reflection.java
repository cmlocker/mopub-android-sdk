package com.mopub.common.util;

import android.support.annotation.NonNull;

import com.mopub.common.Preconditions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Reflection {
    public static class MethodBuilder {
        private final Object mInstance;
        private final String mMethodName;
        private Class<?> mClass;

        private List<Class<?>> mParameterClasses;
        private List<Object> mParameters;
        private boolean mIsAccessible;
        private boolean mIsStatic;

        public MethodBuilder(final Object instance, final String methodName) {
            mInstance = instance;
            mMethodName = methodName;

            mParameterClasses = new ArrayList<Class<?>>();
            mParameters = new ArrayList<Object>();

            mClass = (instance != null) ? instance.getClass() : null;
        }

        public <T> MethodBuilder addParam(Class<T> clazz, T parameter) {
            mParameterClasses.add(clazz);
            mParameters.add(parameter);

            return this;
        }

        public MethodBuilder setAccessible() {
            mIsAccessible = true;

            return this;
        }

        public MethodBuilder setStatic(Class<?> clazz) {
            mIsStatic = true;
            mClass = clazz;

            return this;
        }

        public Object execute() throws Exception {
            Class<?>[] classArray = new Class<?>[mParameterClasses.size()];
            Class<?>[] parameterTypes = mParameterClasses.toArray(classArray);

            Method method = getDeclaredMethodWithTraversal(mClass, mMethodName, parameterTypes);

            if (mIsAccessible) {
                method.setAccessible(true);
            }

            Object[] parameters = mParameters.toArray();

            if (mIsStatic) {
                return method.invoke(null, parameters);
            } else {
                return method.invoke(mInstance, parameters);
            }
        }
    }

    public static Method getDeclaredMethodWithTraversal(Class<?> clazz, String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Class<?> currentClass = clazz;

        while (currentClass != null) {
            try {
                return currentClass.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                currentClass = currentClass.getSuperclass();
            }
        }

        throw new NoSuchMethodException();
    }

    public static boolean classFound(final String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static <T> T instantiateClassWithEmptyConstructor(@NonNull final String className,
            @NonNull final Class<? extends T> superclass)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException, NullPointerException {
        Preconditions.checkNotNull(className);

        final Class<? extends T> clazz = Class.forName(className).asSubclass(superclass);
        // noinspection unchecked
        final Constructor<? extends T> constructor = clazz.getDeclaredConstructor((Class[]) null);
        constructor.setAccessible(true);

        return constructor.newInstance();
    }

    public static <T> T instantiateClassWithConstructor(@NonNull final String className,
            @NonNull final Class<? extends T> superClass, @NonNull Class[] classes,
            @NonNull Object[] parameters)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        Preconditions.checkNotNull(className);
        Preconditions.checkNotNull(superClass);
        Preconditions.checkNotNull(classes);
        Preconditions.checkNotNull(parameters);

        final Class<? extends T> clazz = Class.forName(className).asSubclass(superClass);
        // noinspection unchecked
        final Constructor<? extends T> constructor = clazz.getDeclaredConstructor(classes);
        constructor.setAccessible(true);

        return constructor.newInstance(parameters);
    }
}
