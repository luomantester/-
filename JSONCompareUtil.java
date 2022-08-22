/*
 * JSONCompareUtil.java
 * Copyright 2022 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

/**
 * @author: luoman
 * @date: 2022-07-07 20:23
 * @desc:
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qunhe.test.apollo.util.StringUtils;
import lombok.extern.log4j.Log4j;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Log4j
public class JSONCompareUtil {

//    public static void main(final String[] args){
//        String actualResp="\"abc,def,efg,hij,klm\"";
//        String expectResp="\"abc,hij,klm\"";
//        Boolean result = cmpResp(actualResp,expectResp);
//        log.info("比对结果："+result);
//        return;
//    }

    public static Boolean cmpResp(String actualResp, String expectResp) {
        log.info("response compare start");
        if (actualResp == null && expectResp == null) {
            log.warn("期望结果或者响应结果都为null");
            return true;
        } else if (actualResp == null || expectResp == null) {
            log.warn("期望结果或者响应结果中有一个为null");
            return false;
        }

        Boolean compareResult = true;
        //期望的结果是不是实际结果的部分String
        if (!actualResp.contains(expectResp)) {
            if (verifyJsonObject(actualResp) && verifyJsonObject(expectResp)) {
                //如果字段值都是json串，就转为json比对
                log.info("请求和响应使用josn方式比对");
                compareResult = cmpJsonObject(stringToJson(actualResp), stringToJson(expectResp));
            } else if (verifyJsonArray(actualResp) && verifyJsonArray(expectResp)) {
                //如果字段值都是jsonarray，就转为jsonarray比对
                log.info("请求和响应使用josnarray方式比对");
                compareResult = cmpJsonArray(stringToJsonArray((actualResp)), stringToJsonArray(expectResp));
            } else {
                //都不是，看看是不是乱序的部分字段
                List<String> respsplit0 = Arrays.asList(expectResp.split(","));
                List<String> respsplit1 = Arrays.asList(actualResp.split(","));
                for (String fragExpect: respsplit0){
                    if (respsplit1.contains(fragExpect)){
                        continue;
                    }else {
                        log.warn("响应结果不包含目标字段，"+fragExpect);
                        compareResult = false;
                        break;
                    }
                }
            }
        }
        if (compareResult) {
            log.info("response包含期望结果，case通过");
        } else {
            log.warn("response未包含期望结果，case失败");
            log.info("actual response：" + actualResp);
            log.info("expect response：" + expectResp);
        }
        return compareResult;
    }

    /**
     * 判断实际结果和预期结果是否一致，在预期结果中可以去除不需要验证的key
     *
     * @param actualJson
     * @param expectedJson
     * @return
     */
    public static Boolean cmpJsonObject(JSONObject actualJson, JSONObject expectedJson) {
        if (actualJson == null && expectedJson == null) {
            return true;
        } else if (actualJson == null || expectedJson == null) {
            return false;
        }
        Boolean compareResult = true;
        for (Map.Entry<String, Object> expectedEntry : expectedJson.entrySet()) {
            Integer hasKey = 0;//判断key是否存在
            for (Map.Entry<String, Object> actualEntry : actualJson.entrySet()) {
                if (actualEntry.getKey().equals(expectedEntry.getKey())) {
                    hasKey = 1;
                    if (expectedEntry.getValue() == null) {
                        if (actualEntry.getValue() == null) {
                            compareResult = true;
                            break;
                        } else {
                            log.warn(actualEntry.getKey() + "值为null,期望为null");
                            return false;
                        }
                    }
                    //特殊处理，如果希望有这个key，但不关心value值，可以置value值为testNotCare
                    if (expectedEntry.getValue().equals("testNotCare")){
                        compareResult = true;
                        break;
                    }
                    //期望value不为null，但是返回value为null时，直接失败
                    if (actualEntry.getValue() == null) {
                        log.warn(actualEntry.getKey() + "值为null,期望不为null");
                        return false;
                    }

                    //特殊处理，希望key的值大于等于0，或者等于0。可根据情况添加代码
                    if (expectedEntry.getValue().equals(">=0")){
                        if (Integer.parseInt(actualEntry.getValue().toString())>0){
                            compareResult = true;
                        }else {
                            compareResult = false;
                            log.warn(actualEntry.getKey() + "期望值大于0，实际为"+actualEntry.getValue().toString());
                        }

                        break;
                    }
                    //特殊处理，希望key的值等于0
                    if (expectedEntry.getValue().equals("==0")){
                        if (Integer.parseInt(actualEntry.getValue().toString())==0){
                            compareResult = true;
                        }else {
                            compareResult = false;
                            log.warn(actualEntry.getKey() + "期望值等于0，实际为"+actualEntry.getValue().toString());
                        }
                        break;
                    }

                    //进行value比较
                    if (!actualEntry.getValue().equals(expectedEntry.getValue())) {
                        //字段值和期望值不等时
                        if (actualEntry.getValue().toString().contains(expectedEntry.getValue().toString())){
                            compareResult = true;
                        }else if (verifyJsonObject(actualEntry.getValue().toString()) && verifyJsonObject(expectedEntry.getValue().toString())) {
                            //如果字段值都是json串，就转为json比对
                            compareResult = cmpJsonObject(stringToJson(actualEntry.getValue().toString()), stringToJson(expectedEntry.getValue().toString()));
                        } else if (verifyJsonArray(actualEntry.getValue().toString()) && verifyJsonArray(expectedEntry.getValue().toString())) {
                            //如果字段值都是jsonarray，就转为jsonarray比对
                            compareResult = cmpJsonArray(stringToJsonArray((actualEntry.getValue().toString())), stringToJsonArray(expectedEntry.getValue().toString()));
                        } else {
                            //都不是，看看是不是乱序的部分字段
                            //log.warn("字段value非json或jsonarray，且不相等，看看是否为包含关系");
                            List<String> respsplit0 = Arrays.asList(expectedEntry.getValue().toString().split(","));
                            List<String> respsplit1 = Arrays.asList(actualEntry.getValue().toString().split(","));
                            for (String fragExpect: respsplit0){
                                if (respsplit1.contains(fragExpect)){
                                    continue;
                                }else {
                                    compareResult = false;
                                }
                            }
                        }
                        if (!compareResult) {
                            log.warn("字段值未匹配："+expectedEntry.getKey());
                            log.warn("期望："+expectedEntry.getValue());
                            return false;
                        }
                    }
                    break;
                }
            }
            if (hasKey == 0) {
                //没有找到期望的key，比对失败
                log.warn("key not found："+expectedEntry.getKey());
                return false;
            }
        }
        return compareResult;
    }

    /**
     * @param actualJsonArray
     * @param expectedJsonArray
     * @return
     */
    public static Boolean cmpJsonArray(JSONArray actualJsonArray, JSONArray expectedJsonArray) {
        if (actualJsonArray == null && expectedJsonArray == null) {
            return true;
        } else if (actualJsonArray == null || expectedJsonArray == null) {
            return false;
        }
        if (actualJsonArray.size() >= expectedJsonArray.size()) {
            for (int j = 0; j < expectedJsonArray.size(); j++) {
                Boolean partCompareResult = false;
                for (int i = 0; i < actualJsonArray.size(); i++) {
                    if (verifyJsonObject(actualJsonArray.getString(i)) && verifyJsonObject(expectedJsonArray.getString(j))) {
                        if (cmpJsonObject(actualJsonArray.getJSONObject(i), expectedJsonArray.getJSONObject(j))) {
                            partCompareResult = true;
                        }
                    } else if (verifyJsonArray(actualJsonArray.getString(i)) && verifyJsonArray(expectedJsonArray.getString(j))) {
                        if (cmpJsonArray(JSONArray.parseArray(actualJsonArray.getString(i)), JSONArray.parseArray(expectedJsonArray.getString(j)))) {
                            partCompareResult = true;
                        }
                    } else {
                        if (actualJsonArray.get(i).equals(expectedJsonArray.get(j))) {
                            partCompareResult = true;
                        }
                    }
                    if (partCompareResult){
                        log.info("jsonarray找到期望数据，"+expectedJsonArray.get(j));
                        break;
                    }
                }
                if (!partCompareResult) {
                    //有一个期望的json没找到，则认为比对失败
                    return false;
                }
            }

        } else {
            return false;
        }
        return true;
    }

    /**
     * 校验是否是json串
     *
     * @param jsonString
     * @return Boolean
     */
    private static Boolean verifyJson(String jsonString) {
        try {
            JSON.parse(jsonString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 校验是否是json串,0不是，1json,2jsonarray
     *
     * @param str
     * @return
     */
    public static Integer getJSONType(String str) {
        Integer result = 0;
        if (StringUtils.isNotBlank(str)) {
            str = str.trim();
            if (str.startsWith("{") && str.endsWith("}")) {
                result = 1;
            } else if (str.startsWith("[") && str.endsWith("]")) {
                result = 2;
            }
        }
        return result;

    }

    /**
     * 校验是否是JSONObject类型的json
     *
     * @param jsonString
     * @return Boolean
     */
    public static Boolean verifyJsonObject(String jsonString) {
        if (!verifyJson(jsonString)) {
            return false;
        }
        try {
            stringToJson(jsonString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 校验是否是JSONArray类型的json
     *
     * @param jsonString
     * @return Boolean
     */
    public static Boolean verifyJsonArray(String jsonString) {
        if (!verifyJson(jsonString)) {
            return false;
        }
        try {
            stringToJsonArray(jsonString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 有转移去除转义，然后转为json
     *
     * @param Msg
     * @return
     */
    public static JSONObject stringToJson(String Msg) {
        if (StringUtils.isBlank(Msg)) {
            return null;
        }
        String newMsg = Msg.replaceAll("\"\\{", "\\{").replaceAll("\\}\"", "\\}").replaceAll(
                "\\\\", "");//去掉字符串两头的"",并去转义
        if (!verifyJson(newMsg)) {
            //如果去转义后json格式破坏，则使用去转义前的String
            return JSONObject.parseObject(Msg);
        }else {
            final JSONObject reJsonObject = JSONObject.parseObject(newMsg);
            return reJsonObject;
        }

    }

    /**
     * 有转移去除转义，然后转为jsonArray
     *
     * @param Msg
     * @return
     */
    public static JSONArray stringToJsonArray(String Msg) {
        if (StringUtils.isBlank(Msg)) {
            return null;
        }
        String newMsg = Msg.replaceAll("\"\\[", "\\[").replaceAll("\\]\"", "\\]").replaceAll(
                "\\\\", "");//去掉字符串两头的"",并去转义

        if (!verifyJson(newMsg)) {
            //如果去转义后json格式破坏，则使用去转义前的String
            return JSONArray.parseArray(Msg);
        }else {
            final JSONArray reJsonObject = JSONArray.parseArray(newMsg);
            return reJsonObject;
        }
    }


}
