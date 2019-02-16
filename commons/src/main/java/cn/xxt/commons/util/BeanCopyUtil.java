package cn.xxt.commons.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by wangxiuli on 2017/5/17.
 */

public class BeanCopyUtil {

        /**
         * 复制sour里属性不为空的值到obje为空的属性
         *
         * @param obje
         *            目标实体类
         * @param sour
         *            源实体类
         * @param isCover
         *            是否保留obje类里不为null的属性值(保留源值，属性为null则赋值)
         * @return obje
         */
        public static Object copy(Object obje, Object sour, boolean isCover) {
            Field[] fields = sour.getClass().getDeclaredFields();
            for (int i = 0, j = fields.length; i < j; i++) {
                String propertyName = fields[i].getName();
                Object propertyValue = getProperty(sour, propertyName);
                if (isCover) {
                    if (getProperty(obje, propertyName) == null
                            && propertyValue != null) {
                        Object setProperty = setProperty(obje, propertyName,
                                propertyValue);
                    }
                } else {
                    Object setProperty = setProperty(obje, propertyName,
                            propertyValue);
                }

            }
            return obje;
        }

        /**
         * 得到值
         *
         * @param bean
         * @param propertyName
         * @return
         */
        private static Object getProperty(Object bean, String propertyName) {
            Class clazz = bean.getClass();
            try {
                Field field = clazz.getDeclaredField(propertyName);
                Method method = clazz.getDeclaredMethod(
                        getGetterName(field.getName()), new Class[] {});
                return method.invoke(bean, new Object[] {});
            } catch (Exception e) {
            }
            return null;
        }

        /**
         * 给bean赋值
         *
         * @param bean
         * @param propertyName
         * @param value
         * @return
         */
        private static Object setProperty(Object bean, String propertyName,
                                          Object value) {
            Class clazz = bean.getClass();
            try {
                Field field = clazz.getDeclaredField(propertyName);
                Method method = clazz.getDeclaredMethod(
                        getSetterName(field.getName()),
                        new Class[] { field.getType() });
                return method.invoke(bean, new Object[] { value });
            } catch (Exception e) {
            }
            return null;
        }

        /**
         * 根据变量名得到get方法
         *
         * @param propertyName
         * @return
         */
        private static String getGetterName(String propertyName) {
            String method = "get" + propertyName.substring(0, 1).toUpperCase()
                    + propertyName.substring(1);
            return method;
        }

        /**
         * 得到setter方法
         *
         * @param propertyName
         *            变量名
         * @return
         */
        private static String getSetterName(String propertyName) {
            String method = "set" + propertyName.substring(0, 1).toUpperCase()
                    + propertyName.substring(1);
            return method;
        }

//    public static void main(String[] args) {
//        User u = new User();
//        u.setId(1l);
//        u.setAge(30);
//        User u1 = new User();
//        u1.setAge(10);
//        u1.setBirthday(new Date());
//        u1.setFirtsName("aaaa");
//        u1.setName("adf");
//        u1.setSchool("aaaa");
//        Field[] fields = u1.getClass().getDeclaredFields();
//        u.setSchool("bbbbbbbbb");
//        System.out.println("u1--------->  " + u1);
//        System.out.println("u---------->  " + u);
//        System.out.println(copy(u, u1, false));
//    }

}
