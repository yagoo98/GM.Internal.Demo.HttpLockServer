import com.example.tablemaintain01.HttpRequester
import com.google.gson.Gson
import groovy.json.JsonSlurper
import org.slf4j.LoggerFactory

def get_params() {
    def paramFields = request.getParameterValues("request")
    def resultFields = [soetekId: "999", status: -1, error: "_"]

    if (paramFields) {
        def _fields = new JsonSlurper().parseText(paramFields[0])

        try {
            _fields.entrySet().forEach(entry -> {
                resultFields.put(entry.key, entry.value)
            })

            resultFields.status = 0//W3InfoLib.checkLockerId(resultFields.soetekId, session.getId())
            resultFields.error = ""
        } catch (Exception err) {
            resultFields.status = -1
            resultFields.error = "參數錯誤:" + err.toString() + " ${_fields}"
        }
    }
    return resultFields
}

def process(paramFields, db, tableName, tableFields, logger) {
    def resultFields = [result: "", status: 1, error: ""]
    //資安檢查
    if (0 == 1) {
        resultFields.result = ""
        resultFields.status = 1
        resultFields.error = "權限不足"
        return resultFields
    }
    //欄位檢查
    if (0 == 1) {
        resultFields.result = ""
        resultFields.status = 1
        resultFields.error = "輸入錯誤"
        return resultFields
    }

    def http = new HttpRequester("dequeue")
    Map map = new HashMap()
    map.put("TABLE_NAME", "STOCK")
    map.put("VARKEY", paramFields.id)
    String input = "request=" + new Gson().toJson(map)
    def response = http.connect(input)

    resultFields.result = response
    resultFields.status = 0
    resultFields.error = ""
    return resultFields
}

def output(resultFields) {
    response.contentType = 'application/json'

    json."response" {
        "status" resultFields.status
        "result" resultFields.result
        "error" resultFields.error
    }
}


if (!session) {
    session = request.getSession(true);
}

def db = "java:/comp/env/jdbc/h2mem"
def tableName = "StockInfo"
def tableFields =
        ["id",
         "stockId",
         "stockDate",
         "stockOpen",
         "stockHigh",
         "stockLow",
         "stockClose",
         "stockAdjClose",
         "stockVol",
         "created",
         "createBy",
         "updated",
         "updateBy",
         "deleteFlag"]

//只須去除id
//remove id 欄位, 因為id欄位是自動生成
def insertFields =
        ["stockId",
         "stockDate",
         "stockOpen",
         "stockHigh",
         "stockLow",
         "stockClose",
         "stockAdjClose",
         "stockVol",
         "created",
         "createBy",
         "updated",
         "updateBy",
         "deleteFlag"]
def logger = LoggerFactory.getLogger(tableName)
def paramFields = get_params()
def resultFields = process(paramFields, db, tableName, insertFields, logger)

output(resultFields)