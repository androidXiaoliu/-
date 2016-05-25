package com.baofeng.aone;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import android.os.UserHandle;
import android.util.Log;

public class ReflectUtil {
	public static Object getField(Class c,String fieldName ){
		 return getField(c, fieldName, null);
	}
	public static Object getField(Class c,String fieldName,Object obj){
		try{
		Field filed = c.getField(fieldName);
        filed.setAccessible(true);
        Object value = filed.get(obj);
        return value;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static Object invorkMethod(Class c,Object obj,String methodname, Object... args){
		try{
		  Method method = c.getDeclaredMethod(methodname);
		  method.setAccessible(true);
		  Object returnValue = method.invoke(obj,args);
	        return returnValue;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	} 
	
	
	public static Constructor Constructor(String className,Class...paramTypes){
		try{
		 Class cl = Class.forName(className);                      
         Constructor constructor = cl
                 .getDeclaredConstructor(paramTypes);
         return constructor;
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
        return null;
	}
	
	public static Object newInstance(Constructor constructor, Object...objects){
		try{
		Object object = (Object) constructor.newInstance(objects);
		return object;
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}
	
	/**
     * 直接读取对象属性值,无视private/protected修饰符,不经过getter函数.
     */
    public static Object getFieldValue(final Object object, final String fieldName) {
        Field field = getDeclaredField(object, fieldName);

        if (field == null)
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on " +
                    "target [" + object + "]");

        makeAccessible(field);

        Object result = null;
        try {
            result = field.get(object);
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 直接设置对象属性值,无视private/protected修饰符,不经过setter函数.
     */
    public static void setFieldValue(final Object object, final String fieldName, final Object
            value) {
        Field field = getDeclaredField(object, fieldName);

        if (field == null)
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on " +
                    "target [" + object + "]");

        makeAccessible(field);

        try {
            field.set(object, value);
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 循环向上转型,获取对象的DeclaredField.
     */
    protected static Field getDeclaredField(final Object object, final String fieldName) {
        return getDeclaredField(object.getClass(), fieldName);
    }

    /**
     * 循环向上转型,获取类的DeclaredField.
     */
    @SuppressWarnings("unchecked")
    protected static Field getDeclaredField(final Class clazz, final String fieldName) {
        for (Class superClass = clazz; superClass != Object.class; superClass = superClass
                .getSuperclass()) {
            try {
                return superClass.getDeclaredField(fieldName);
            }
            catch (NoSuchFieldException e) {
                // Field不在当前类定义,继续向上转型
            }
        }
        return null;
    }

    /**
     * 强制转换fileld可访问.
     */
    protected static void makeAccessible(final Field field) {
        if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field
                .getDeclaringClass().getModifiers())) {
            field.setAccessible(true);
        }
    }

    /**
     * 通过反射,获得定义Class时声明的父类的泛型参数的类型. 如public UserDao extends HibernateDao<User>
     *
     * @param clazz The class to introspect
     * @return the first generic declaration, or Object.class if cannot be
     * determined
     */
    public static Class getSuperClassGenricType(final Class clazz) {
        return getSuperClassGenricType(clazz, 0);
    }

    /**
     * 通过反射,获得定义Class时声明的父类的泛型参数的类型. 如public UserDao extends
     * HibernateDao<User,Long>
     *
     * @param clazz clazz The class to introspect
     * @param index the Index of the generic ddeclaration,start from 0.
     * @return the index generic declaration, or Object.class if cannot be
     * determined
     */

    public static Class getSuperClassGenricType(final Class clazz, final int index) {

        Type genType = clazz.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType)) {
            Log.w(clazz.getSimpleName(), "'s superclass not ParameterizedType");
            return Object.class;
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (index >= params.length || index < 0) {
            Log.w("Index: " + index + ", Size of " + clazz.getSimpleName(), "'s Parameterized " +
                    "Type: " + params.length);
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            Log.w(clazz.getSimpleName(), " not set the actual class on superclass generic " +
                    "parameter");
            return Object.class;
        }
        return (Class) params[index];
    }

    public static Class getClassFromName(String name) {
        try {
            return Class.forName(name);
        }
        catch (Exception e) {
            return null;
        }

    }
    /**
     * 根据对象，返回一个class对象，用于获取方法
     */
    public static Class<?> getClass(Object obj) {
        try {
            return Class.forName(obj.getClass().getName());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据对象，获取某个方法
     *
     * @param obj
     *            对象
     * @param methodName
     *            方法名
     * @param parameterTypes
     *            该方法需传的参数类型，如果不需传参，则不传
     */
    public static Method getMethod(Object obj, String methodName,
                                   Class<?>... parameterTypes) {
        try {
            Method method = getClass(obj).getDeclaredMethod(methodName,
                    parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Method getMethod(Class<?> cls, String methodName,
                                   Class<?>... parameterTypes) {
        try {
            Method method = cls.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 直接传入对象、方法名、参数，即可使用该对象的隐藏方法
     *
     * @param obj
     * @param methodName
     * @param parameter
     */
    public static Object invoke(Object obj, String methodName,
                                Object... parameter) {
        Class<?>[] parameterTypes = new Class<?>[parameter.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = parameter[i].getClass();
        }
        try {
            return getMethod(obj, methodName, parameterTypes).invoke(obj,
                    parameter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 直接传入类名、方法名、参数，即可使用该对象的隐藏静态方法
     *
     * @param cls
     * @param methodName
     * @param parameter
     */
    public static Object invoke(Class<?> cls, String methodName,
                                Object... parameter) {
        Class<?>[] parameterTypes = new Class<?>[parameter.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = parameter[i].getClass();
        }
        try {
            return getMethod(cls, methodName, parameterTypes).invoke(null,
                    parameter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
		
	 

}
