import com.google.gson.Gson
import com.google.gson.GsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.naming.Context
import javax.naming.InitialContext
import javax.sql.DataSource
import java.sql.Connection
import java.text.SimpleDateFormat

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
    if (0==1) {
        resultFields.result = ""
        resultFields.status = 1
        resultFields.error = "權限不足"
        return resultFields
    }
    //欄位檢查
    if (0==1) {
        resultFields.result = ""
        resultFields.status = 1
        resultFields.error = "輸入錯誤"
        return resultFields
    }

    //資料庫操作
    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd")
    SimpleDateFormat sdfDatetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    Date now = new Date()
    Context webContainer
    DataSource dbSource
    Connection dbConn
    Sql sqlObject
    ArrayList<GroovyRowResult> sqlResult = new ArrayList()

    paramFields.updated = now
    paramFields.updateBy = paramFields.ui


    def sqlCmd = GString.EMPTY +
            """
                select * from "$tableName" where "id" = :id and IFNULL("deleteFlag", FALSE)  <> TRUE;
            """
    logger.info("SQL Cmd:" + getClass().getName() + sqlCmd)
    try {
        webContainer = new InitialContext()
        dbSource = (DataSource) webContainer.lookup(db)
        dbConn =dbSource.getConnection()
        sqlObject = new Sql(dbConn)

        sqlResult = sqlObject.rows(sqlCmd, paramFields)
        if (sqlResult.size() == 0) {
            resultFields.result = ""
            resultFields.status = 1
            resultFields.error = "找不到這筆資料. id = " + paramFields.id
            return resultFields
        }
    } catch (Exception ex) {
        ex.printStackTrace( )
        resultFields.result = ""
        resultFields.status = 1
        resultFields.error = ex.getMessage()
        return resultFields
    } finally {
    }

    Gson gson = new GsonBuilder().serializeNulls().create()

    resultFields.result = gson.toJson(sqlResult)
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
def logger = LoggerFactory.getLogger(tableName)
def paramFields = get_params()
def resultFields = process(paramFields, db, tableName, tableFields, logger)

output(resultFields)







