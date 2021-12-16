class Crud {
    constructor(entity,
                formSelector = 'form.soe-form',
                tableSelector = 'table.soe-datatable',
                idParamName = 'id') {
        this.entityName    = entity
        this.formSelector  = formSelector
        this.tableSelector = tableSelector
        this.idParamName = idParamName

        let groovyPath     = '../../scripts/'
        this.urlCreate     = groovyPath + entity + 'Create.groovy'
        this.urlRead       = groovyPath + entity + 'Read.groovy'
        this.urlUpdate     = groovyPath + entity + 'Update.groovy'
        this.urlDelete     = groovyPath + entity + 'Delete.groovy'
        this.urlIndex      = groovyPath + entity + 'Index.groovy'
    }

    call_service_form_submit(requestUrl){
        let promiseObj = new Promise(function (resolve, reject) {
            let requestObj = $(this.formSelector).serializeObject()
            let si = getSi()
            //ES6
            //requestObj.assign(si)
            //ES5
            requestObj = $.extend({}, requestObj, si)
            let request_str = JSON.stringify(requestObj);
            $.ajax({
                type: "post",
                url: requestUrl,
                dataType: "json",
                data: {
                    request: request_str
                },
                success: function (data) {
                    resolve(data)
                },
                error: function (error) {
                    reject(error)
                }
            });
        });
        return promiseObj;
    }

    call_service_read_by_id(requestUrl) {
        let promiseObj = new Promise(function (resolve, reject) {

            let urlParam = new URLSearchParams(window.location.search)
            let requestObj = {
                id : urlParam.get(this.idParamName)
            }
            let si = getSi()
            //ES6
            //requestObj.assign(si)
            //ES5
            requestObj = $.extend({}, requestObj, si)
            let request_str = JSON.stringify(requestObj);
            $.ajax({
                type: "post",
                url: requestUrl,
                dataType: "json",
                data: {
                    request: request_str
                },
                success: function (data) {
                    resolve(data)
                },
                error: function (error) {
                    reject(error)
                }
            });
        });
        return promiseObj;
    }

    show_toast(messageType, messageText) {
        switch (messageType) {
            case 'S':
                $('#soe-toast-success-text').text(messageText)
                $('#soe-toast-success').toast('show')
                break;
            case 'E':
                $('#soe-toast-error-text').text(messageText)
                $('#soe-toast-error').toast('show')
                break;
        }
    }

    getSi() {
        let urlParam = new URLSearchParams(window.location.search)
        let si = {
            si : urlParam.get("si"),
            ui : urlParam.get("ui"),
            li : null
        }
        if(!si.ui) {
            si.ui = 'ui'
        }
    }

    getSiParam(prefix) {
        let si = getSi()
        return prefix + 'si=' + si.si + '&ui=' + si.ui + '&li' + si.li
    }

    show_dataTable(data, column_option_array, rowIdPrperty = 'id') {

        let dataTable = $(this.tableSelector).DataTable({
            "searching": true,
            "paging": true,
            "pageLength": 100,
            "ordering": true,
            "info": true,
            "autoWidth": false,
            "responsive": true,
            "scroller": true,
            // "scrollX": true,
            // "scrollCollapse": true,
            "language": {
                "info": "顯示 _PAGE_ 至 _PAGES_",
                "search": "搜尋 :",
                "paginate": {
                    "previous": "上一頁",
                    "next": "下一頁"
                },
                "lengthMenu": "顯示 _MENU_ 筆資料"
            },
            dom: "<'row'<'col-xl-8'B><'col-xl-4'fr>>" +
                "<'row'<'col-sm-12'tlp>>",
            buttons: [
                'excel'
            ],
            destroy: true,
            data: data,
            columns: column_option_array,
            rowId: rowIdPrperty,
            order: [[1, "desc"]]
        });

        dataTable.columns.adjust()
    }

    fillForm(responseObject) {
        if (!responseObject) {
            return
        }

        for (const key in responseObject) {
            if (!responseObject.hasOwnProperty(key)) {
                continue
            }

            let field = $(this.formSelector+' #' +key)

            if (field.length == 0) {
                continue
            }

            field.val(responseObject[key])
        }
    }
}