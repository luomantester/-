封装出一个通用的接口响应校验方法，降低接口响应校验的编写和维护成本。
支持String、json、jsonarray,list等校验

1、使用时，将JSONCompareUtil.java拷到工程里就可以使用。
cmpResp(String actualResp, String expectResp)为通用校验入口
cmpJsonObject(JSONObject actualJson, JSONObject expectedJson) 是进行JSONObject的比对
cmpJsonArray(JSONArray actualJsonArray, JSONArray expectedJsonArray)是进行JSONArray的比对


2、expectResp可以是某个字段，此时校验时会检查整个响应是否包含expectResp字段。例如
assertTrue(JSONCompareUtil.cmpResp(actualResp, "userId"))

3、expectResp可以是一个完整的响应

4、expectResp可以是响应的部分字段。但是此时响应结构必须保持完成，层级不能出错。并列的字段或json可以删除一些

5、key对应的value值是List，可以取原来部分

6、key对应的value值是“,”相连的一系列值，可以取部分

7、响应体是“,”相连的一系列String，可以取部分

8、接口响应只是简单的false或true也可以校验

9、有些字段希望有，但是对值不是很关心，或者值是个变量，怎么弄？设置期望字段值为"testNotCare"，则只检验字段是否存在，不校验值

10、响应里面有个字段是数值，且会变化，目前支持>=和==，可根据需要在cmpJsonObject下自己添加

*校验的json最好是压缩格式的。可以格式化处理后再压缩回去。推荐工具：https://www.sojson.com/

*其他的，可以自己改代码
