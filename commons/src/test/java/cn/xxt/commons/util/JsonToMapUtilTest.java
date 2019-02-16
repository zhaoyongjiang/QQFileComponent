package cn.xxt.commons.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class JsonToMapUtilTest {
    private Gson gson = new Gson();

    @Test
    public void toMap_invalidateParams() {
        String[] invalidateJsonStringArray = {null, "", "1", "[1, 2, 3]", "{haha", "huhu}"};

        for (String jsonString : invalidateJsonStringArray) {
            assertNull(JsonToMapUtil.toMap(jsonString));
        }
    }

    @Test
    public void toMap_primitiveOrStringTypes() {
        Map<String, Object> map = new HashMap<>();

        byte byteVar = 1;
        map.put("byteVar", byteVar);
        short shortVar = 1;
        map.put("shortVar", shortVar);
        int intVar = 1;
        map.put("intVar", intVar);
        long longVar = 1L;
        map.put("longVar", longVar);
        map.put("floatVar", 1.1F);
        map.put("doubleVar", 1.1D);
        map.put("charVar", 'c');
        map.put("booleanVar", true);
        map.put("stringVar", "string");
        map.put("nullVar", null);

        String jsonString = gson.toJson(map);
        Map<String, Object> mapFromJsonString = JsonToMapUtil.toMap(jsonString);

        System.out.println(mapFromJsonString.get("byteVar").getClass());
        System.out.println(mapFromJsonString.get("shortVar").getClass());
        System.out.println(mapFromJsonString.get("intVar").getClass());
        System.out.println(mapFromJsonString.get("longVar").getClass());
        System.out.println(mapFromJsonString.get("floatVar").getClass());
        System.out.println(mapFromJsonString.get("doubleVar").getClass());
        System.out.println(mapFromJsonString.get("charVar").getClass());
        System.out.println(mapFromJsonString.get("booleanVar").getClass());
        System.out.println(mapFromJsonString.get("stringVar").getClass());
//        assertEquals();

        assertEquals(mapFromJsonString.get("byteVar"), 1.0);
        assertEquals(mapFromJsonString.get("shortVar"), 1.0);
        assertEquals(mapFromJsonString.get("intVar"), 1.0);
        assertEquals(mapFromJsonString.get("longVar"), 1.0);
        assertEquals(mapFromJsonString.get("floatVar"), 1.1);
        assertEquals(mapFromJsonString.get("doubleVar"), 1.1);
        assertEquals(mapFromJsonString.get("charVar"), "c");
        assertEquals(mapFromJsonString.get("booleanVar"), true);
        assertEquals(mapFromJsonString.get("stringVar"), "string");
        assertNull(mapFromJsonString.get("nullVar"));
    }

    @Test
    public void toMap_subMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("_rc", "success");

        Student student = new Student();
        student.setAge(100);
        student.setName("寿星");

        map.put("student", student);

        String jsonString = gson.toJson(map);

        Map<String, Object> mapFromJsonString = JsonToMapUtil.toMap(jsonString);
        assertTrue("success".equals(mapFromJsonString.get("_rc")));

        Map<String, Object> studentMap = (Map<String, Object>) mapFromJsonString.get("student");

        assertEquals(studentMap.get("name"), "寿星");
        assertEquals(studentMap.get("age"), 100.0);
    }

    @Test
    public void toMap_fromJsonObject() {
        JsonObject json = new JsonObject();
        json.addProperty("strValue", "strValue");

        Map<String, Object> map = JsonToMapUtil.toMap(json);

        assertEquals(map.get("strValue").getClass(), String.class);
        assertEquals(map.get("strValue"), "strValue");
    }
}

class Student {
    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
