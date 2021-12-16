import com.google.gson.Gson
import com.google.gson.GsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.apache.commons.lang3.StringUtils
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
    def sdfDate = new SimpleDateFormat("yyyy-MM-dd")
    def sdfDatetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    def now = new Date()
    Context webContainer
    DataSource dbSource
    Connection dbConn
    Sql sqlObject
    ArrayList<GroovyRowResult> sqlResult = new ArrayList()

    //這種 Groovy SQL 的寫法, 至少要參照一個 map 中的屬性, 否則會產生錯誤
    paramFields.dummyOne = 1
    def sqlCmdArray = []
    sqlCmdArray.add(GString.EMPTY +
            """ 
                select * from "$tableName"
                where 1 = :dummyOne
                  and IFNULL("deleteFlag", FALSE)  <> TRUE  
            """)

    def searchFiledNameLow = "Low"
    def searchFieldNameHigh = "High"
    for( def searchFieldName : tableFields) {
        searchFiledNameLow = searchFieldName + "Low"
        searchFieldNameHigh = searchFieldName + "High"
        if (StringUtils.isBlank(paramFields.get(searchFiledNameLow))) {
            //do nothing
        } else if (StringUtils.isBlank(paramFields.get(searchFieldNameHigh))) {
            sqlCmdArray.add(GString.EMPTY + """ and "$searchFieldName" = :$searchFiledNameLow """)
        } else {
            sqlCmdArray.add(GString.EMPTY + """ and "$searchFieldName" between :$searchFiledNameLow and :$searchFieldNameHigh """)
        }
    }

//    //stockId
//    if (StringUtils.isBlank(paramFields.stockIdLow)) {
//        //do nothing
//    } else if (StringUtils.isBlank(paramFields.stockIdHigh)) {
//        sqlCmdArray.add(GString.EMPTY + """ and "stockId" = :stockIdLow """)
//    } else {
//        sqlCmdArray.add(GString.EMPTY + """ and "stockId" between :stockIdLow and :stockIdHigh """)
//    }
//
//    //stockDate
//    if (StringUtils.isBlank(paramFields.stockDateLow)) {
//        //do nothing
//    } else if (StringUtils.isBlank(paramFields.stockDateHigh)) {
//        sqlCmdArray.add(GString.EMPTY + """ and "stockDate" = :stockDateLow """)
//    } else {
//        sqlCmdArray.add(GString.EMPTY + """ and "stockDate" between :stockDateLow and :stockDateHigh """)
//    }
//
//    //stockOpen
//    if (StringUtils.isBlank(paramFields.stockOpenLow)) {
//        //do nothing
//    } else if (StringUtils.isBlank(paramFields.stockOpenHigh)) {
//        sqlCmdArray.add(GString.EMPTY + """ and "stockOpen" = :stockOpenLow """)
//    } else {
//        sqlCmdArray.add(GString.EMPTY + """ and "stockOpen" between :stockOpenLow and :stockOpenHigh """)
//    }
//
//    //stockHigh
//    if (StringUtils.isBlank(paramFields.stockHighLow)) {
//        //do nothing
//    } else if (StringUtils.isBlank(paramFields.stockHighHigh)) {
//        sqlCmdArray.add(GString.EMPTY + """ and "stockHigh" = :stockHighLow """)
//    } else {
//        sqlCmdArray.add(GString.EMPTY + """ and "stockHigh" between :stockHighLow and :stockHighHigh """)
//    }
//
//    //stockLow
//    if (StringUtils.isBlank(paramFields.stockLowLow)) {
//        //do nothing
//    } else if (StringUtils.isBlank(paramFields.stockLowHigh)) {
//        sqlCmdArray.add(GString.EMPTY + """ and "stockLow" = :stockLowLow """)
//    } else {
//        sqlCmdArray.add(GString.EMPTY + """ and "stockLow" between :stockLowLow and :stockLowHigh """)
//    }
//
//    //stockClose
//    if (StringUtils.isBlank(paramFields.stockCloseLow)) {
//        //do nothing
//    } else if (StringUtils.isBlank(paramFields.stockCloseHigh)) {
//        sqlCmdArray.add(GString.EMPTY + """ and "stockClose" = :stockCloseLow """)
//    } else {
//        sqlCmdArray.add(GString.EMPTY + """ and "stockClose" between :stockCloseLow and :stockCloseHigh """)
//    }
//
//    //stockVol
//    if (StringUtils.isBlank(paramFields.stockVolLow)) {
//        //do nothing
//    } else if (StringUtils.isBlank(paramFields.stockVolHigh)) {
//        sqlCmdArray.add(GString.EMPTY + """ and "stockVol" = :stockVolLow """)
//    } else {
//        sqlCmdArray.add(GString.EMPTY + """ and "stockVol" between :stockVolLow and :stockVolHigh """)
//    }


    def sqlCmd = sqlCmdArray.join(" ")
    logger.info("SQL Cmd:" + getClass().getName() + sqlCmd)
    try {
        webContainer = new InitialContext()
        dbSource = (DataSource) webContainer.lookup(db)
        dbConn =dbSource.getConnection()
        sqlObject = new Sql(dbConn)
        sqlResult = sqlObject.rows(sqlCmd, paramFields)

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

//刪除技術性質欄位 id / created / createBy / updated /updateBy / deleteFlag
def queryFields =
        ["stockId",
         "stockDate",
         "stockOpen",
         "stockHigh",
         "stockLow",
         "stockClose",
         "stockAdjClose",
         "stockVol"]
def logger = LoggerFactory.getLogger(tableName)
def paramFields = get_params()
def resultFields = process(paramFields, db, tableName, queryFields, logger)

output(resultFields)



