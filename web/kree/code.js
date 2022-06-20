/**
 * @license
 * Copyright 2012 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * @fileoverview JavaScript for Blockly's Code demo.
 * @author fraser@google.com (Neil Fraser)
 */
'use strict';

//import {user} from "../../web_2/blockly/typings/blockly";

/**
 * Create a namespace for the application.
 */
const Code = {};
let logins = {}

/**
 * Lookup for names of supported languages.  Keys should be in ISO 639 format.
 */
Code.LANGUAGE_NAME = {

    //'de': 'Deutsch', //disable for now

    'en': 'English'

};

/**
 * List of RTL languages.
 */

Code.LANGUAGE_RTL = ['ar', 'fa', 'he', 'lki'];

/**
 * Blockly's main workspace.
 * @type {Blockly.WorkspaceSvg}
 */
Code.workspace = null;

/**
 * Extracts a parameter from the URL.
 * If the parameter is absent default_value is returned.
 * @param {string} name The name of the parameter.
 * @param {string} defaultValue Value to return if parameter not found.
 * @return {string} The parameter value or the default value if not found.
 */
Code.getStringParamFromUrl = function (name, defaultValue) {
    const val = location.search.match(new RegExp('[?&]' + name + '=([^&]+)'));
    return val ? decodeURIComponent(val[1].replace(/\+/g, '%20')) : defaultValue;
};

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

const kosmos = {}
Blockly.defineBlocksWithJsonArray([  // BEGIN JSON EXTRACT
    // Block for variable getter.
    {
        "type": "variables_get2",
        "message0": "%1",
        "args0": [
            {
                "type": "field_variable",
                "name": "VAR",
                "variable": "%{BKY_VARIABLES_DEFAULT_NAME}"
            }
        ],
        "output": null,
        "style": "variable_blocks",
        "helpUrl": "%{BKY_VARIABLES_GET_HELPURL}",
        "tooltip": "%{BKY_VARIABLES_GET_TOOLTIP}",
        "extensions": ["kosmos_contextMenu_variableSetterGetter"]
    },
    // Block for variable setter.
    {
        "type": "variables_set2",
        "message0": "%{BKY_VARIABLES_SET}",
        "args0": [
            {
                "type": "field_variable",
                "name": "VAR",
                "variable": "%{BKY_VARIABLES_DEFAULT_NAME}"
            },
            {
                "type": "input_value",
                "name": "VALUE"
            }
        ],
        "previousStatement": null,
        "nextStatement": null,
        "style": "variable_blocks",
        "tooltip": "%{BKY_VARIABLES_SET_TOOLTIP}",
        "helpUrl": "%{BKY_VARIABLES_SET_HELPURL}",
        "extensions": ["kosmos_contextMenu_variableSetterGetter"]
    }
]);  // END JSON EXTRACT (Do not delete this comment.)

kosmos.CUSTOM_CONTEXT_MENU_VARIABLE_GETTER_SETTER_MIXIN = {
    /**
     * Add menu option to create getter/setter block for this setter/getter.
     * @param {!Array} options List of menu options to add to.
     * @this {Blockly.Block}
     */
    customContextMenu: function (options) {
        if (!this.isInFlyout) {
            // Getter blocks have the option to create a setter block, and vice versa.
            if (this.type == 'variables_get') {
                var opposite_type = 'variables_set';
                var contextMenuMsg = Blockly.Msg['VARIABLES_GET_CREATE_SET'];
            } else {
                var opposite_type = 'variables_get';
                var contextMenuMsg = Blockly.Msg['VARIABLES_SET_CREATE_GET'];
            }

            var option = {enabled: this.workspace.remainingCapacity() > 0};
            var name = this.getField('VAR').getText();
            option.text = contextMenuMsg.replace('%1', name);
            var xmlField = Blockly.utils.xml.createElement('field');
            xmlField.setAttribute('name', 'VAR');
            xmlField.appendChild(Blockly.utils.xml.createTextNode(name));
            var xmlBlock = Blockly.utils.xml.createElement('block');
            xmlBlock.setAttribute('type', opposite_type);
            xmlBlock.appendChild(xmlField);
            option.callback = Blockly.ContextMenu.callbackFactory(this, xmlBlock);
            options.push(option);

            // Getter blocks have the option to rename or delete that variable.
        } else {
            if (this.type == 'variables_get' || this.type == 'variables_get_reporter') {
                var renameOption = {
                    text: Blockly.Msg.RENAME_VARIABLE,
                    enabled: true,
                    callback: Blockly.Constants.Variables.RENAME_OPTION_CALLBACK_FACTORY(this)
                };
                var name = this.getField('VAR').getText();
                var deleteOption = {
                    text: Blockly.Msg.DELETE_VARIABLE.replace('%1', name),
                    enabled: true,
                    callback: Blockly.Constants.Variables.DELETE_OPTION_CALLBACK_FACTORY(this)
                };
                //options.unshift(renameOption);
                options.unshift(deleteOption);
            }
        }
    }
};
Blockly.FieldVariable.dropdownCreate = function () {
    if (!this.variable_) {
        throw Error('Tried to call dropdownCreate on a variable field with no' +
            ' variable selected.');
    }
    var name = this.getText();
    var variableModelList = [];
    if (this.sourceBlock_ && this.sourceBlock_.workspace) {
        var variableTypes = this.getVariableTypes_();
        // Get a copy of the list, so that adding rename and new variable options
        // doesn't modify the workspace's list.
        for (var i = 0; i < variableTypes.length; i++) {
            var variableType = variableTypes[i];
            var variables =
                this.sourceBlock_.workspace.getVariablesOfType(variableType);
            variableModelList = variableModelList.concat(variables);
        }
    }
    variableModelList.sort(Blockly.VariableModel.compareByName);

    var options = [];
    console.log("sb");
    console.log(this.sourceBlock_);
    for (var i = 0; i < variableModelList.length; i++) {
        // Set the UUID as the internal representation of the variable.
        if (this.sourceBlock_.type == "variables_set2") {
            if (kosmos.isProtectedVariable(variableModelList[i].name)) {
                continue;
            }
        }
        if (this.sourceBlock_.type.startsWith("kosmos_trigger")) {

            if (variableModelList[i].name != "value" && kosmos.isProtectedVariable(variableModelList[i].name)) {
                continue;
            }
        }
        options.push([variableModelList[i].name, variableModelList[i].getId()]);
    }
    if (!kosmos.isProtectedVariable(name)) {
        options.push([Blockly.Msg['RENAME_VARIABLE'], Blockly.RENAME_VARIABLE_ID]);
        if (Blockly.Msg['DELETE_VARIABLE']) {
            options.push(
                [
                    Blockly.Msg['DELETE_VARIABLE'].replace('%1', name),
                    Blockly.DELETE_VARIABLE_ID
                ]
            );
        }
    }

    console.log("options");
    console.log(options);
    return options;
};
kosmos.isProtectedVariable = function (name) {
    for (let i = 0; i < kosmos.protectedVariableNames.length; i++) {
        if (name.startsWith(kosmos.protectedVariableNames[i])) {
            return true;
        }
    }
    return false;
}


/**
 * Callback for rename variable dropdown menu option associated with a
 * variable getter block.
 * @param {!Blockly.Block} block The block with the variable to rename.
 * @return {!function()} A function that renames the variable.
 */
Blockly.Constants.Variables.RENAME_OPTION_CALLBACK_FACTORY = function (block) {
    return function () {
        var workspace = block.workspace;
        var variable = block.getField('VAR').getVariable();
        Blockly.Variables.renameVariable(workspace, variable);
    };
};

/**
 * Callback for delete variable dropdown menu option associated with a
 * variable getter block.
 * @param {!Blockly.Block} block The block with the variable to delete.
 * @return {!function()} A function that deletes the variable.
 */
Blockly.Constants.Variables.DELETE_OPTION_CALLBACK_FACTORY = function (block) {
    return function () {
        var workspace = block.workspace;
        var variable = block.getField('VAR').getVariable();
        workspace.deleteVariableById(variable.getId());
        workspace.refreshToolboxSelection();
    };
};

Blockly.Extensions.registerMixin('kosmos_contextMenu_variableSetterGetter',
    kosmos.CUSTOM_CONTEXT_MENU_VARIABLE_GETTER_SETTER_MIXIN);


kosmos.Code = {}
kosmos.timers = ['timer1'];
kosmos.listBlocks = [];
kosmos.timerblocks = [];
kosmos.uuidValidator = function () {
    return "uuid";
}
kosmos.propertyValidator = function () {
    return "uuid";
}
kosmos.nullValidator = function () {
    return null;
}
kosmos.getGlobalVarNames = function (skip) {
    const list = []
    if (skip == undefined || skip == null) {
        skip = []
    }
    const variables = Blockly.Variables.allUsedVarModels(Code.workspace);


    for (let i = 0; i < variables.length; i++) {
        const n = variables[i].name;
        let add = true;
        for (let j = 0; j < skip.length; j++) {
            if (n == skip[j]) {
                add = false;
                break;
            }

        }
        if (add) {
            list.push(n);

        }

    }
    for (let i = 0; i < kosmos.listBlocks.length; i++) {
        const block = kosmos.listBlocks[i];
        if (kosmos.isInWorkspace(block)) {
            const bname = block.getFieldValue("VARNAME");
            list.push(bname);
        }


    }
    return list;

}
kosmos.getGlobals = function (skip) {
    const list = kosmos.getGlobalVarNames(skip);
    let globals = "home, timers";
    for (let i = 0; i < list.length; i++) {
        globals = globals + ", " + list[i];
    }
    return "global " + globals;
}
kosmos.Code["createFunction"] = function (block, name, params) {

    const code = []
    if (Blockly.Python.STATEMENT_PREFIX) {
        // Automatic prefix insertion is switched off for this block.  Add manually.
        console.log("prefix," + Blockly.Python.injectId(Blockly.Python.STATEMENT_PREFIX, block));
        code.push = Blockly.Python.injectId(Blockly.Python.STATEMENT_PREFIX, block);
    }

    let p = ""
    for (let i = 0; i < params.length; i++) {
        p = p + params[i] + ", "

    }
    code.push('def ' + name + ' (' + p + '):');
    code.push(Blockly.Python.INDENT + kosmos.getGlobals());
    //code.push(Blockly.Python.INDENT + 'if uuid != \'' + uuid + '\' or key != \'' + property + '\':');
    //code.push(Blockly.Python.INDENT + Blockly.Python.INDENT + 'return');
    //code.push(Blockly.Python.INDENT + varname + ' = home.get_value(\''+uuid+'\',\''+property+'\')');
    code.push(Blockly.Python.statementToCode(block, 'DO') || Blockly.Python.PASS);


    return code;

}
kosmos.Code["createListener"] = function (block, uuid, property, varname, beforedo = null) {
    const listenerid = kosmos.nextListenerId();
    const code = []
    if (Blockly.Python.STATEMENT_PREFIX) {
        // Automatic prefix insertion is switched off for this block.  Add manually.
        console.log("prefix," + Blockly.Python.injectId(Blockly.Python.STATEMENT_PREFIX, block));
        code.push = Blockly.Python.injectId(Blockly.Python.STATEMENT_PREFIX, block);
    }
    code.push('# listener ' + uuid + " " + property);
    code.push('def listener' + listenerid + '(uuid, property,transValue=None):');

    code.push(Blockly.Python.INDENT + kosmos.getGlobals(["uuid", "property", varname]))


    if (beforedo != null) {
        code.push(Blockly.Python.INDENT + beforedo);

    }
    if (varname != null) {
        //if ( uuid == null && property == null ) {
        code.push(Blockly.Python.INDENT + 'if transValue is None:');
        code.push(Blockly.Python.INDENT + Blockly.Python.INDENT + varname + ' = home.get_value(uuid,property)');
        code.push(Blockly.Python.INDENT + 'else:');

        code.push(Blockly.Python.INDENT + Blockly.Python.INDENT + varname + ' = transValue');
        //}

    }
    let doc = ((Blockly.Python.statementToCode(block, 'DO') || Blockly.Python.PASS));
    //.replaceAll("property2", "property").replaceAll("value2", "value").replaceAll("uuid2", "uuid")
    console.log("doc", doc);
    code.push(doc);
    if (uuid != null) {
        if (property != null) {
            code.push('home.subscribe(listener' + listenerid + ', \'onChange~_~' + uuid + '~_~' + property + '\')');
        } else {
            code.push('home.subscribe(listener' + listenerid + ', \'onChange~_~' + uuid + '\')');
        }
    } else {
        code.push('home.subscribe(listener' + listenerid + ', \'onChange\')');
    }

    return code;

}
kosmos.Code.changeValue = function (uuid, property, type, input) {
    let v = "";
    if (type === "inc")
        v = 'home.get_value(' + uuid + ', ' + property + ')+' + input;
    else if (type === "dec")
        v = 'home.get_value(' + uuid + ', ' + property + ')-' + input;
    if (type === "incp")
        v = '(home.get_value(' + uuid + ', ' + property + ')/100)*(100+' + input + ')';
    else if (type === "decp")
        v = '(home.get_value(' + uuid + ', ' + property + ')/100)*(100-' + input + ')';


    return kosmos.Code.setValue(uuid, property, v);
}
kosmos.Code["lenBigger0Validator"] = function (newValue) {
    if (newValue.length == 0) {
        return null;
    }
    return newValue;
}
kosmos.colors = {
    "trigger": 260

}
kosmos.uuidPropertyBlocks = [];
kosmos.changedUuid = function (event) {
    console.log(this.getSourceBlock());
    console.log("got changedUUID: " + event);
    for (let i = 0; i < kosmos.uuidPropertyBlocks.length; i++) {
        const block = kosmos.uuidPropertyBlocks[i];
        /*if ( block.block.rendered) {
            if ( block.uuid.selectedOption_[1] == event) {
                console.log("FOUND IT!");
            }
            else {
                console.log("currently selected "+block.uuid.selectedOption_[1]);
            }
            console.log(block);

        }*/
        if (block.block == this.getSourceBlock()) {
            console.log(block);
            kosmos.updateStates(block.id, event);
            let list = kosmos.states[block.id];
            if (block.filter != null) {
                block.prop.menuGenerator_ = [["no", "no"]]
                let list2 = [];
                for (let j = 0; j < list.length; j++) {
                    let s = list[j];
                    for (let k = 0; k < block.filter.length; k++) {
                        let f = block.filter[k];
                        if (s[1].toLowerCase() == f.toLowerCase()) {
                            list2.push(s);
                        }
                    }
                }
                list = list2;
            }
            block.prop.menuGenerator_ = list
            //force select the first entry from the list and force a rerender if the selected thing is NOT the default one
            block.prop.selectedOption_ = kosmos.states[block.id][0];
            block.prop.value_ = kosmos.states[block.id][0][0];
            console.log("value: ",kosmos.states[block.id][0][1]);
            if ( kosmos.states[block.id][0][1] != "-") {
                //stupid workaround to force a rerender, but "it just works"
                Code.workspace.setVisible(false);
                Code.workspace.setVisible(true);
            }
        }
    }
}

kosmos.getStates = function (uuid) {
    const device = kosmos.devices[uuid];
    //console.log(device);
    if (device && device.state) {
        const list = []
        for (const [key, value] of Object.entries(device.state)) {
            if (key == "friendly_name" || key == "dmx_values" || key.startsWith("dmx_") || key == "supported_features") {
                continue;

            }

            list.push([key, key]);

        }

        //sort by first entry
        list.sort((a, b) => a[0].toUpperCase().localeCompare(b[0].toUpperCase()));
        return list;
    }
    return [["-", "-"]];


}
kosmos.updateStates = function (id, uuid) {
    console.log("updating states for " + id + " based on " + uuid);
    if (uuid != "-") {
        kosmos.states[id] = kosmos.getStates(uuid);
    }
}
kosmos.propertyList = function (uuid) {
    console.log("looking for properties for " + uuid)
    if (uuid == "HSVLamp1") {
        return [["color", "color"], ["state", "state"]]
    }

    return [["state", "state"]]

}
kosmos.username = "user";
kosmos.password = "pass";
kosmos.getXML = function (url, callback) {
    const xhr = new XMLHttpRequest();
    xhr.open('GET', url, true);
    xhr.responseType = 'text';
    xhr.setRequestHeader("Authorization", "Bearer " + kosmos.token);
    xhr.onload = function () {
        const status = xhr.status;
        if (status === 200) {
            callback(null, xhr.response);
        } else {
            callback(status, xhr.response);
        }
    };
    xhr.send();
};
kosmos.getJSON = function (url, callback) {
    const xhr = new XMLHttpRequest();
    xhr.open('GET', url, true);
    xhr.responseType = 'json';
    xhr.setRequestHeader("Authorization", "Bearer " + kosmos.token);
    xhr.onload = function () {
        const status = xhr.status;
        if (status === 200) {
            callback(null, xhr.response);
        } else {
            callback(status, xhr.response);
        }
    };
    xhr.send();
};
kosmos.states = {}
kosmos.triggerID = 0;
kosmos.timerID = 0;

kosmos.emptyDropList = [[Blockly.Msg.KOSMOS_PLEASE_WAIT, '-']];
kosmos.nextTriggerId = function () {
    kosmos.triggerID = kosmos.triggerID + 1;
    kosmos.states[kosmos.triggerID] = [[Blockly.Msg.KOSMOS_SELECT_DEVICE_FIRST, '-']];

    return kosmos.triggerID;

}
kosmos.nextTimerId = function () {
    kosmos.timerID = kosmos.timerID + 1;


    return kosmos.timerID;

}
kosmos.listenerid = 0
kosmos.nextListenerId = function () {
    kosmos.listenerid = kosmos.listenerid + 1;
    return kosmos.listenerid;

}
if (typeof window !== 'undefined' && typeof window.location !== 'undefined') {
    kosmos.base = window.location.protocol+'//' + window.location.hostname + ':' + window.location.port;
} else {
    kosmos.base = 'http://localhost:18080';
}
kosmos.login = function (username, password) {
    const xhr = new XMLHttpRequest();

    xhr.open('POST',  kosmos.base+"/user/login", true);
    xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    if (username == null || password == null) {
        kosmos.username = document.getElementById("username").value;
        kosmos.password = document.getElementById("password").value;
    } else {
        kosmos.username = username;
        kosmos.password = password;
    }
    const save = document.getElementById('save').checked;
    xhr.onreadystatechange = function (oEvent) {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            const status = xhr.status;
            if (status === 200) {
                kosmos.token = xhr.response;


                document.getElementById("kosmos_login").style.display = 'none';
                document.getElementById("kosmos_workspace").style.display = 'initial';
                Code.init();
                kosmos.connect();
                if (save) {
                    logins[kosmos.username] = kosmos.password;
                    localStorage.setItem('logins', JSON.stringify(logins));
                }
            }
            else if (status === 403) {
                console.log("login failed!")
                document.getElementById("login_error").appendChild(createElementFromHTML("<div class=\"alert alert-danger alert-dismissible fade show\" role=\"alert\">\n" +
                    "  <strong>Login Failed!</strong> Username/Password is incorrect" +
                    "  <button type=\"button\" class=\"btn-close\" data-bs-dismiss=\"alert\" aria-label=\"Close\"></button>\n" +
                    "</div>"));
            }
            else {
                console.log("login failed!")
                document.getElementById("login_error").appendChild(createElementFromHTML("<div class=\"alert alert-danger alert-dismissible fade show\" role=\"alert\">\n" +
                    "  <strong>Login Failed!</strong> "+xhr.statusText +
                    "  <button type=\"button\" class=\"btn-close\" data-bs-dismiss=\"alert\" aria-label=\"Close\"></button>\n" +
                    "</div>"));

            }

        }

    };
    xhr.onerror = function(e) {
        console.log("Error Catched" + JSON.stringify(e));
    };
    xhr.send("user=" + kosmos.username + "&pass=" + kosmos.password);
}
kosmos.download = function () {
    var text = Blockly.Xml.domToText(Blockly.Xml.workspaceToDom(Code.workspace));
    var element = document.createElement('a');
    var d = new Date();

    var datestring = d.getFullYear() + "." + (d.getMonth() + 1) + "." + d.getDate() + "_" + d.getHours() + ":" + d.getMinutes();
    d.getHours() + ":" + d.getMinutes();
    element.setAttribute('href', 'data:application/xml;charset=utf-8,' + encodeURIComponent(text));
    element.setAttribute('download', "kree_rules_" + kosmos.username + "_" + datestring + ".kreexml");

    element.style.display = 'none';
    document.body.appendChild(element);

    element.click();

    document.body.removeChild(element);
}
function pingws() {
    if (!kosmos.ws) return;
    if (kosmos.ws.readyState !== 1) return;
    kosmos.ws.send("ping");
    setTimeout(pingws, 15000);
}
function createElementFromHTML(htmlString) {
    var div = document.createElement('div');
    div.innerHTML = htmlString.trim();

    // Change this to div.childNodes to support multiple top-level nodes.
    return div.firstChild;
}
function handleFileSelect(evt) {
    var files = evt.target.files; // FileList object

    // Loop through the FileList and render image files as thumbnails.
    for (var i = 0, f; f = files[i]; i++) {


        var reader = new FileReader();

        // Closure to capture the file information.
        reader.onload = (function (theFile) {
            return function (e) {


                kosmos.loadFromText(e.target.result);
                //console.log(e.target.result);

            };
        })(f);

        // Read in the image file as a data URL.
        reader.readAsText(f);
    }
}


kosmos.upload = function () {
    document.getElementById('fileid').click();

}

kosmos.saveBlocks = function () {
    let xhr = new XMLHttpRequest();

    xhr.open('POST', "saveXML", true);
    xhr.setRequestHeader("Authorization", "Bearer " + kosmos.token);

    xhr.onload = function () {
        const status = xhr.status;
        if (status === 200) {

        } else {
            console.log("failed!")
        }
    };
    xhr.send(Blockly.Xml.domToText(Blockly.Xml.workspaceToDom(Code.workspace)))
    xhr = new XMLHttpRequest();

    xhr.open('POST', "savePython", true);
    xhr.setRequestHeader("Authorization", "Bearer " + kosmos.token);

    xhr.onload = function () {
        const status = xhr.status;
        if (status === 200) {

        } else {
            console.log("failed!")
        }
    };

    xhr.send(kosmos.getPython())


}
kosmos.isInWorkspace = function (block) {
    if (!block.disposed && !block.isInFlyout) {
        return true;
    }
    return false;
}

kosmos.getPython = function () {

    const generator = Blockly.Python;
    if (Code.checkAllGeneratorFunctionsDefined(generator)) {
        let code = []
        code.push('import os.path');
        code.push('import sys');
        code.push('sys.path.append(os.path.join(os.path.dirname(__file__), ".."))');
        code.push('from kosmos import *');

        code.push('import threading');
        code.push('import time');
        code.push("try:\n")

        let list = kosmos.getGlobalVarNames();
        for (let i = 0; i < list.length; i++) {
            code.push('    ' + list[i] + ' = None');
        }
        code.push('    home = kosmos(\'' + kosmos.username + '\', \'' + kosmos.password + '\')\n' +
            '    home.startConnecting()');
        code.push('    timers = {}');
        for (let i = 0; i < kosmos.timerblocks.length; i++) {
            const block = kosmos.timerblocks[i];
            //console.log("id " + block.id);
            //console.log("disposed " + block.disposed);
            if (kosmos.isInWorkspace(block)) {
                console.log(block);

                const bname = kosmos.varNames[block.getFieldValue("VARNAME")];
                code.push('    def timer_func_' + bname + '(mytimer):');
                code.push('        ' + kosmos.getGlobals());
                const repeat = block.getFieldValue("REPEAT");
                //block.inputList[3].fieldRow[1].value_;
                if (repeat == 0) {
                    code.push('        while not mytimer.stopped():');
                } else {
                    code.push('        for i in range(' + repeat + '):');
                    //code.push('        if timers["' + bname + '"].stopped():');
                    //code.push('            return');
                }
                //block.inputList[2].fieldRow[1].value_
                code.push('            time.sleep(' + block.getFieldValue("DELAY") + ')')

                code.push('            if mytimer.stopped():');
                code.push('                return');

                let c = Blockly.Python.statementToCode(block, 'DO');
                if (c !== undefined) {
                    const cc = c.split('\n');
                    for (let j = 0; j < cc.length; j++) {
                        code.push("        " + cc[j]);
                    }
                } else {
                    code.push("            pass");
                }

            }


        }

        const cc = generator.workspaceToCode(Code.workspace).split("\n");
        for (let i = 0; i < cc.length; i++) {
            code.push("    " + cc[i]);
        }
        code.push("except BaseException:\n")
        code.push("    traceback.print_exc(file=sys.stdout)\n")

        return code.join('\n');
    }
    return '';
}
kosmos.paintInColor = function (block) {
    if (block.type == "colour_picker") {
        block.getField('COLOUR').setValidator(kosmos.colorValidator);
        block.getField('COLOUR').doClassValidation_(block.getField('COLOUR').getValue());

    }
    if (block.type == "colour_rgb") {
        const r = Blockly.Python.valueToCode(block, 'RED',
            Blockly.Python.ORDER_NONE) || 0;
        const g = Blockly.Python.valueToCode(block, 'GREEN',
            Blockly.Python.ORDER_NONE) || 0;
        const b = Blockly.Python.valueToCode(block, 'BLUE',
            Blockly.Python.ORDER_NONE) || 0;
        const color = kosmos.rgbToHex(parseInt(parseFloat(Math.max(Math.min(r, 100), 0)) * 2.55), parseInt(parseFloat(Math.max(Math.min(g, 100), 0)) * 2.55), parseInt(parseFloat(Math.max(Math.min(b, 100), 0)) * 2.55));
        /*console.log(r+"_"+g+"_"+b);
        console.log(color);
        const complement =kosmos.hexToComplimentary(color);

        console.log(complement)*/
        block.setColour(color);
        //create 4 drop shadows on the root
        block.getSvgRoot().style.textShadow = "-2px 0 black, 0 2px black, 2px 0 black, 0 -2px black";
        const children = block.getChildren();
        //remove the drop shadow again on its children, because they are ugly ...
        for (let i = 0; i < children.length; i++) {
            children[i].getSvgRoot().style.textShadow = "0 0 white";
        }
    }


}
kosmos.cleanup2 = function () {
    Code.tabClick("python");
    window.setTimeout(kosmos.cleanup3, 250);
}
kosmos.cleanup3 = function () {
    Code.tabClick("blocks");
    window.setTimeout(kosmos.cleanup4, 50);

}

kosmos.cleanup4 = function () {


    if (kosmos.cleanuplist > Code.workspace.getAllBlocks().length) {

        Code.workspace.undo(false);
        window.setTimeout(kosmos.cleanup4, 10);
    }


}
kosmos.cleanuplist = 0;
kosmos.deleted = 0;
kosmos.cleanup = function () {
    const list = Code.workspace.getAllBlocks();

    //console.log("cleaning up..."+list.length);
    let deleted = 0;
    for (let i = 0; i < list.length; i++) {
        const block = list[i];
        kosmos.paintInColor(block);
    }
    for (let i = 0; i < list.length; i++) {
        const block = list[i];
        try {
            if (block.type.startsWith("procedures_defnoreturn")) {
                block.select();
                Blockly.Events.setGroup(true);
                Blockly.hideChaff();
                Blockly.selected.dispose(/* heal */ true, true);
                Blockly.Events.setGroup(false);
                //deleted++;
                kosmos.deleted++;

                //break;
            }
        } catch (exception_var) {

        }
    }
    /*if ( deleted > 0 ) {
        window.setTimeout(kosmos.cleanup, 250);
    }
    else {*/
    if (kosmos.deleted > 0) {
        //kosmos.cleanup2();
        window.setTimeout(kosmos.cleanup2, 250);

    }
    /*}*/


}
kosmos.loadFromText = function (data) {
    Code.discard();
    console.log("loading from text");
    console.log(data);
    let xml = ""
    if (data.length == 0) {
        data = "<xml xmlns=\"https://developers.google.com/blockly/xml\">\n" +
            "  <block type=\"kosmos_connect\" x=\"13\" y=\"88\">\n" +
            "  </block>\n" +
            "</xml>";

    }
    xml = Blockly.Xml.textToDom(data);

    Blockly.Xml.domToWorkspace(xml, Code.workspace);
    const list = Code.workspace.getAllBlocks();
    for (let i = 0; i < kosmos.protectedVariableNames.length; i++) {
        let n = kosmos.protectedVariableNames[i];
        var existing =
            Blockly.Variables.nameUsedWithAnyType(n, Code.workspace);
        if (!existing) {
            Code.workspace.createVariable(n, '');
        }
    }
    //Blockly.Variables.getOrCreateVariablePackage(Code.workspace,"uuid");
    //Blockly.Variables.getOrCreateVariablePackage(Code.workspace,"property");
    //Blockly.Variables.getOrCreateVariablePackage(Code.workspace,"value");
    kosmos.cleanuplist = list.length;
    window.setTimeout(kosmos.cleanup, 250);


}

kosmos.loadBlocks = function () {
    kosmos.getXML('loadXML', function (err, data) {
        if (err !== null) {
            alert('Something went wrong: ' + err);

        } else {


            kosmos.loadFromText(data);


        }
    });
}
kosmos.getForbiddenVarNames = function () {
    const forbidden = []
    const list = Code.workspace.getAllBlocks();
    console.log(list.length);
    for (let i = 0; i < list.length; i++) {
        const block = list[i];
        let input = block.getField("VARNAME");
        if (input) {
            try {
                const b = Blockly.Variables.getVariable(Code.workspace, block.getFieldValue("VARNAME"));
                if (b) {
                    const bname = b.name;
                    forbidden.push([bname, block]);
                }
            } catch (e) {

            }

        }

    }
    return forbidden;
}
kosmos.getNewUuids = function () {
    kosmos.getJSON(kosmos.base + '/device/list', function (err, data) {
        if (err !== null) {
            //alert('Something went wrong: ' + err);
            kosmos.uuidList = kosmos.emptyDropList;
        } else {
            //alert(data);
            if (data != null) {
                kosmos.devices = []
                const list = [];
                for (let i = 0; i < data.length; i++) {

                    kosmos.devices[data[i].uuid] = data[i];
                    if (kosmos.getStates(data[i].uuid).length > 0) {
                        list.push([data[i].name + " (" + data[i].uuid + ")", data[i].uuid]);
                    }

                }
                //sort by first entry
                list.sort((a, b) => a[0].toUpperCase().localeCompare(b[0].toUpperCase()));
                //add select thingie to the beginning
                list.unshift([Blockly.Msg.KOSMOS_SELECT_DEVICE, "-"])
                kosmos.uuidList = list;
                kosmos.loadBlocks();
            }

        }
    });

}
kosmos.onLoad = function () {
    console.log("starting onLoad init");
    document.getElementById("btn_login").addEventListener('click', kosmos.login);


    const storage = localStorage.getItem('logins');
    const div = document.getElementById("logindiv");
    let style = 'none';
    if (storage != undefined) {
        //if localstorage was not empty parse it as json
        logins = JSON.parse(storage);


        //add all of the buttons to quickly login
        let entries = 0;
        for (const [key, value] of Object.entries(logins)) {
            const btn = document.createElement("button");
            btn.addEventListener("click", function (event) {
                kosmos.login(key, value);

            });
            btn.classList.add("btn");
            btn.classList.add("btn-outline-primary");
            btn.innerText = `login as ${key}`;
            div.appendChild(btn);
            entries++;
        }
        if (entries > 0) {
            console.log("have at least 1 entry");
            style = 'initial';
        }
    }
    div.style.display = style;


    //add keydown eventlistener to send login on enter
    document.querySelector("#username").addEventListener("keydown", (evt) => {
        if (evt.key === "Enter") {
            kosmos.login();
        }
    });
    //add keydown eventlistener to send login on enter
    document.querySelector("#password").addEventListener("keydown", (evt) => {
        if (evt.key === "Enter") {
            kosmos.login();
        }
    });
    console.log("finished onLoad init");
}
kosmos.uuidList = [];
kosmos.userdirty = false;
kosmos.getUuidList = function () {
    if (kosmos.uuidList.length > 0) {

        return kosmos.uuidList;
    }
    const list = [];
    list.unshift([Blockly.Msg.KOSMOS_SELECT_DEVICE, "-"]);
    return list;


}
kosmos.changeTypeList = [["increase by amount", "inc"], ["decrease by amount", "dec"], ["increase by percent", "incp"], ["decrease by percent", "decp"]];
kosmos.getChangeType = function () {
    return kosmos.changeTypeList;


}
kosmos.gestureTypeList = [["up", "up"], ["down", "down"], ["left", "left"], ["right", "right"]];
kosmos.getGestureTypes = function () {
    if (kosmos.gestureTypeList.length > 0) {

        return kosmos.gestureTypeList;
    }
    const list = [];
    list.unshift([Blockly.Msg.KOSMOS_SELECT_GESTURE, "-"]);
    return list;


}
kosmos.hasProperties = function (uuid, properties) {
    const states = kosmos.getStates(uuid);

    if (states) {

        const slist = []
        for (let j = 0; j < states.length; j++) {

            const sname = states[j][0].toLowerCase();
            slist.push(sname);


        }
        for (let k = 0; k < properties.length; k++) {
            let notFound = false;
            for (let l = 0; l < properties[k].length; l++) {
                const prop = properties[k][l].split("~");
                if (!slist.includes(prop[0].toLowerCase().trim())) {

                    notFound = true;
                    break;
                }
                if (prop.length > 1) {
                    if (prop[1] == "b") {
                        if (kosmos.devices[uuid] && kosmos.devices[uuid].state) ;
                        const s = kosmos.devices[uuid].state[prop[0]].toLowerCase().trim();
                        if (s != true && s != "true" && s != "false" && s != false && s != "on" && s != "off") {
                            notFound = true;
                            break;
                        }
                    }

                }

            }
            if (!notFound) {
                return properties[k];

            }
        }


    }
}
kosmos.getUuidWithProperties = function (properties) {
    const ret = [];
    for (let i = 0; i < kosmos.uuidList.length; i++) {
        try {
            const uuid = kosmos.uuidList[i][1];
            const r = kosmos.hasProperties(uuid, properties);
            if (r != null) {
                ret.push([kosmos.uuidList[i][0], kosmos.uuidList[i][1], r]);
            }
        } catch (e) {

        }

    }

    if (ret.length == 0) {
        ret.push(["no device found", "-"]);
    } else {
        ret.unshift([Blockly.Msg.KOSMOS_SELECT_DEVICE, "-"])
    }
    return ret;
}
kosmos.getColorList = function () {
    return kosmos.getUuidWithProperties([["hue", "saturation"], ["hsv_color"], ["rgb_color"], ["xy_color"], ["hs_color"], ["color"], ["red", "green", "blue"], ["r", "g", "b"]]);
}
kosmos.getColorText = function () {
    return kosmos.getUuidWithProperties([["color", "text"]]);
}
kosmos.getShortpressList = function () {
    return kosmos.getUuidWithProperties([["shortpress"]]);
}
kosmos.getTemperatureList = function () {
    return kosmos.getUuidWithProperties([["heatingTemperatureSetting"]]);
}
kosmos.getLongpressList = function () {
    return kosmos.getUuidWithProperties([["longpress"]]);
}
kosmos.getGestureList = function () {
    return kosmos.getUuidWithProperties([["gesture"]]);
}
kosmos.getOnList = function () {
    return kosmos.getUuidWithProperties([["on"], ["state~b"]]);
}
kosmos.getBrightnessList = function () {
    return kosmos.getUuidWithProperties([["brightness"], ["dimmingLevel"], ["hue", "value"]]);
}
kosmos.init = function () {
    console.log("kosmos init!");
    kosmos.getNewUuids();
};


/**
 * Get the language of this user from the URL.
 * @return {string} User's language.
 */
Code.getLang = function () {
    let lang = 'en';
    if (typeof location !== 'undefined') {

        lang = Code.getStringParamFromUrl('lang', '');

        if (Code.LANGUAGE_NAME[lang] === undefined) {
            // Default to English.
            lang = 'en';
        }
    }

    return lang;
};

/**
 * Is the current language (Code.LANG) an RTL language?
 * @return {boolean} True if RTL, false if LTR.
 */
Code.isRtl = function () {
    return Code.LANGUAGE_RTL.indexOf(Code.LANG) != -1;
};

/**
 * Load blocks saved on App Engine Storage or in session/local storage.
 * @param {string} defaultXml Text representation of default blocks.
 */
Code.loadBlocks = function (defaultXml) {
    let loadOnce = null;
    try {
        loadOnce = window.sessionStorage.loadOnceBlocks;
    } catch (e) {
        // Firefox sometimes throws a SecurityError when accessing sessionStorage.
        // Restarting Firefox fixes this, so it looks like a bug.
        loadOnce = null;
    }
    if ('BlocklyStorage' in window && window.location.hash.length > 1) {
        // An href with #key trigers an AJAX call to retrieve saved blocks.
        BlocklyStorage.retrieveXml(window.location.hash.substring(1));
    } else if (loadOnce) {
        // Language switching stores the blocks during the reload.
        delete window.sessionStorage.loadOnceBlocks;
        const xml = Blockly.Xml.textToDom(loadOnce);
        Blockly.Xml.domToWorkspace(xml, Code.workspace);
    } else if (defaultXml) {
        // Load the editor with default starting blocks.
        const xml = Blockly.Xml.textToDom(defaultXml);
        Blockly.Xml.domToWorkspace(xml, Code.workspace);
    } else if ('BlocklyStorage' in window) {
        // Restore saved blocks in a separate thread so that subsequent
        // initialization is not affected from a failed load.
        window.setTimeout(BlocklyStorage.restoreBlocks, 0);
    }
};

/**
 * Save the blocks and reload with a different language.
 */
Code.changeLanguage = function () {
    // Store the blocks for the duration of the reload.
    // MSIE 11 does not support sessionStorage on file:// URLs.
    if (window.sessionStorage) {
        const xml = Blockly.Xml.workspaceToDom(Code.workspace);
        const text = Blockly.Xml.domToText(xml);
        window.sessionStorage.loadOnceBlocks = text;
    }

    const languageMenu = document.getElementById('languageMenu');
    const newLang = encodeURIComponent(
        languageMenu.options[languageMenu.selectedIndex].value);
    let search = window.location.search;
    if (search.length <= 1) {
        search = '?lang=' + newLang;
    } else if (search.match(/[?&]lang=[^&]*/)) {
        search = search.replace(/([?&]lang=)[^&]*/, '$1' + newLang);
    } else {
        search = search.replace(/\?/, '?lang=' + newLang + '&');
    }

    window.location = window.location.protocol + '//' +
        window.location.host + window.location.pathname + search;
};

/**
 * Changes the output language by clicking the tab matching
 * the selected language in the codeMenu.
 */
Code.changeCodingLanguage = function () {
    const codeMenu = document.getElementById('code_menu');
    Code.tabClick(codeMenu.options[codeMenu.selectedIndex].value);
}

/**
 * Bind a function to a button's click event.
 * On touch enabled browsers, ontouchend is treated as equivalent to onclick.
 * @param {!Element|string} el Button element or ID thereof.
 * @param {!Function} func Event handler to bind.
 */
Code.bindClick = function (el, func) {
    if (typeof el == 'string') {
        el = document.getElementById(el);
    }
    el.addEventListener('click', func, true);
    el.addEventListener('touchend', func, true);
};

/**
 * Load the Prettify CSS and JavaScript.
 */
Code.importPrettify = function () {
    const script = document.createElement('script');
    script.setAttribute('src', 'https://cdn.rawgit.com/google/code-prettify/master/loader/run_prettify.js');
    document.head.appendChild(script);
};

/**
 * Compute the absolute coordinates and dimensions of an HTML element.
 * @param {!Element} element Element to match.
 * @return {!Object} Contains height, width, x, and y properties.
 * @private
 */
Code.getBBox_ = function (element) {
    const height = element.offsetHeight;
    const width = element.offsetWidth;
    let x = 0;
    let y = 0;
    do {
        x += element.offsetLeft;
        y += element.offsetTop;
        element = element.offsetParent;
    } while (element);
    return {
        height: height,
        width: width,
        x: x,
        y: y
    };
};

/**
 * User's language (e.g. "en").
 * @type {string}
 */
Code.LANG = Code.getLang();

/**
 * List of tab names.
 * @private
 */
Code.TABS_ = ['blocks', 'python', 'xml'];

/**
 * List of tab names with casing, for display in the UI.
 * @private
 */
Code.TABS_DISPLAY_ = [
    'Blocks', 'Python', 'XML',
];

Code.selected = 'blocks';

/**
 * Switch the visible pane when a tab is clicked.
 * @param {string} clickedName Name of tab clicked.
 */
Code.tabClick = function (clickedName) {
    // If the XML tab was open, save and render the content.
    if (document.getElementById('tab_xml').classList.contains('tabon')) {
        const xmlTextarea = document.getElementById('content_xml');
        const xmlText = xmlTextarea.value;
        let xmlDom = null;
        try {
            xmlDom = Blockly.Xml.textToDom(xmlText);
        } catch (e) {
            const q =
                window.confirm(MSG['badXml'].replace('%1', e));
            if (!q) {
                // Leave the user on the XML tab.
                return;
            }
        }
        if (xmlDom) {
            Code.workspace.clear();
            Blockly.Xml.domToWorkspace(xmlDom, Code.workspace);
        }
    }

    if (document.getElementById('tab_blocks').classList.contains('tabon')) {
        Code.workspace.setVisible(false);
    }
    // Deselect all tabs and hide all panes.
    for (let i = 0; i < Code.TABS_.length; i++) {
        const name = Code.TABS_[i];
        const tab = document.getElementById('tab_' + name);
        console.log('tab_' + name)
        tab.classList.add('taboff');
        tab.classList.remove('tabon');
        document.getElementById('content_' + name).style.visibility = 'hidden';
    }

    // Select the active tab.
    Code.selected = clickedName;
    const selectedTab = document.getElementById('tab_' + clickedName);
    selectedTab.classList.remove('taboff');
    selectedTab.classList.add('tabon');
    // Show the selected pane.
    document.getElementById('content_' + clickedName).style.visibility =
        'visible';
    Code.renderContent();
    // The code menu tab is on if the blocks tab is off.
    const codeMenuTab = document.getElementById('tab_code');
    if (clickedName == 'blocks') {
        Code.workspace.setVisible(true);
        codeMenuTab.className = 'taboff';
    } else {
        codeMenuTab.className = 'tabon';
    }
    // Sync the menu's value with the clicked tab value if needed.
    const codeMenu = document.getElementById('code_menu');
    for (let i = 0; i < codeMenu.options.length; i++) {
        if (codeMenu.options[i].value == clickedName) {
            codeMenu.selectedIndex = i;
            break;
        }
    }
    Blockly.svgResize(Code.workspace);
};

/**
 * Populate the currently selected pane with content generated from the blocks.
 */
Code.renderContent = function () {
    const content = document.getElementById('content_' + Code.selected);
    // Initialize the pane.
    if (content.id == 'content_xml') {
        const xmlTextarea = document.getElementById('content_xml');
        const xmlDom = Blockly.Xml.workspaceToDom(Code.workspace);
        const xmlText = Blockly.Xml.domToPrettyText(xmlDom);
        xmlTextarea.value = xmlText;
        xmlTextarea.focus();

    } else if (content.id == 'content_python') {
        Code.attemptCodeGeneration(Blockly.Python);

    }
    if (typeof PR == 'object') {
        PR.prettyPrint();
    }
};

/**
 * Attempt to generate the code and display it in the UI, pretty printed.
 * @param generator {!Blockly.Generator} The generator to use.
 */
Code.attemptCodeGeneration = function (generator) {
    const content = document.getElementById('content_' + Code.selected);
    content.textContent = '';
    let code = '';
    if (generator == Blockly.Python) {
        code = kosmos.getPython();
    } else {
        if (Code.checkAllGeneratorFunctionsDefined(generator)) {
            code = generator.workspaceToCode(Code.workspace);

        }
    }
    content.textContent = code;
    // Remove the 'prettyprinted' class, so that Prettify will recalculate.
    content.className = content.className.replace('prettyprinted', '');

}
;

/**
 * Check whether all blocks in use have generator functions.
 * @param generator {!Blockly.Generator} The generator to use.
 */
Code.checkAllGeneratorFunctionsDefined = function (generator) {
    const blocks = Code.workspace.getAllBlocks(false);
    const missingBlockGenerators = [];
    for (let i = 0; i < blocks.length; i++) {
        const blockType = blocks[i].type;
        if (!generator[blockType]) {
            if (missingBlockGenerators.indexOf(blockType) == -1) {
                missingBlockGenerators.push(blockType);
            }
        }
    }

    const valid = missingBlockGenerators.length == 0;
    if (!valid) {
        const msg = 'The generator code for the following blocks not specified for ' +
            generator.name_ + ':\n - ' + missingBlockGenerators.join('\n - ');
        Blockly.alert(msg);  // Assuming synchronous. No callback.
    }
    return valid;
};

/**
 * Initialize Blockly.  Called on page load.
 */
Code.init = function () {
    kosmos.init();
    Code.initLanguage();

    const rtl = Code.isRtl();
    const container = document.getElementById('content_area');
    const onresize = function (e) {
        const bBox = Code.getBBox_(container);
        for (let i = 0; i < Code.TABS_.length; i++) {
            const el = document.getElementById('content_' + Code.TABS_[i]);
            el.style.top = bBox.y + 'px';
            el.style.left = bBox.x + 'px';
            // Height and width need to be set, read back, then set again to
            // compensate for scrollbars.
            el.style.height = bBox.height + 'px';
            el.style.height = (2 * bBox.height - el.offsetHeight) + 'px';
            el.style.width = bBox.width + 'px';
            el.style.width = (2 * bBox.width - el.offsetWidth) + 'px';
        }
        // Make the 'Blocks' tab line up with the toolbox.
        if (Code.workspace && Code.workspace.getToolbox().width) {
            document.getElementById('tab_blocks').style.minWidth =
                (Code.workspace.getToolbox().width - 38) + 'px';
            // Account for the 19 pixel margin and on each side.
        }
    };

    window.addEventListener('resize', onresize, false);

    // The toolbox XML specifies each category name using Blockly's messaging
    // format (eg. `<category name="%{BKY_CATLOGIC}">`).
    // These message keys need to be defined in `Blockly.Msg` in order to
    // be decoded by the library. Therefore, we'll use the `MSG` dictionary that's
    // been defined for each language to import each category name message
    // into `Blockly.Msg`.
    // TODO: Clean up the message files so this is done explicitly instead of
    // through this for-loop.
    for (let messageKey in MSG) {
        if (messageKey.indexOf('cat') == 0) {
            Blockly.Msg[messageKey.toUpperCase()] = MSG[messageKey];
        }
    }

    // Construct the toolbox XML, replacing translated variable names.
    let toolboxText = document.getElementById('toolbox').outerHTML;

    toolboxText = toolboxText.replace(/(^|[^%]){(\w+)}/g,
        function (m, p1, p2) {
            //console.log("p1 "+p1+" p2 "+p2+" m "+m)
            return p1 + MSG[p2];
        });
    const toolboxXml = Blockly.Xml.textToDom(toolboxText);

    Code.workspace = Blockly.inject('content_blocks',
        {
            grid:
                {
                    spacing: 25,
                    length: 3,
                    colour: '#ccc',
                    snap: true
                },
            media: 'media/',
            rtl: rtl,
            toolbox: toolboxXml,
            zoom:
                {
                    controls: true,
                    wheel: true
                }
        });


    //Code.loadBlocks('');

    if ('BlocklyStorage' in window) {
        // Hook a save function onto unload.
        BlocklyStorage.backupOnUnload(Code.workspace);
    }

    Code.tabClick(Code.selected);

    Code.bindClick('trashButton',
        function () {
            Code.discard();
            Code.renderContent();
        });

    Code.bindClick('saveButton', kosmos.saveBlocks);
    Code.bindClick('downloadButton', kosmos.download);
    Code.bindClick('uploadButton', kosmos.upload);
    document.getElementById('fileid').addEventListener('change', handleFileSelect, false);
    // Disable the link button if page isn't backed by App Engine storage.

    for (let i = 0; i < Code.TABS_.length; i++) {
        const name = Code.TABS_[i];
        Code.bindClick('tab_' + name,
            function (name_) {
                return function () {
                    Code.tabClick(name_);
                };
            }(name));
    }
    Code.bindClick('tab_code', function (e) {
        if (e.target !== document.getElementById('tab_code')) {
            // Prevent clicks on child codeMenu from triggering a tab click.
            return;
        }
        Code.changeCodingLanguage();
    });

    onresize();
    Blockly.svgResize(Code.workspace);

    // Lazy-load the syntax-highlighting.
    window.setTimeout(Code.importPrettify, 1);
    Code.workspace.addChangeListener(onEvent);
    Code.workspace.registerToolboxCategoryCallback(
        'KOSMOS_TIMER', kosmos.timerFlyOutCallback);
    Code.workspace.registerToolboxCategoryCallback(
        'KOSMOS_LISTS', kosmos.listsFlyOutCallback);
    Code.workspace.registerToolboxCategoryCallback(
        'KOSMOS_CLAMP', kosmos.clampFlyOutCallback);
    Code.workspace.registerToolboxCategoryCallback(
        'VARS', kosmos.varsFlyOutCallback);
};
kosmos.createVariableButtonHandler = function (
    workspace, opt_callback, opt_type) {
    var type = opt_type || '';
    // This function needs to be named so it can be called recursively.
    var promptAndCheckWithAlert = function (defaultName) {
        Blockly.Variables.promptName(Blockly.Msg['NEW_VARIABLE_TITLE'], defaultName,
            function (text) {
                if (text) {


                    var existing =
                        Blockly.Variables.nameUsedWithAnyType(text, workspace);

                    if (existing) {
                        if (existing.type == type) {
                            var msg = Blockly.Msg['VARIABLE_ALREADY_EXISTS'].replace(
                                '%1', existing.name);
                        } else {
                            var msg =
                                Blockly.Msg['VARIABLE_ALREADY_EXISTS_FOR_ANOTHER_TYPE'];
                            msg = msg.replace('%1', existing.name).replace('%2', existing.type);
                        }
                        Blockly.alert(msg,
                            function () {
                                promptAndCheckWithAlert(text);  // Recurse
                            });
                    } else if (kosmos.isProtectedVariable(text)) {
                        var msg = Blockly.Msg['VARIABLE_PROTECTED'].replace(
                            '%1', text);
                        Blockly.alert(msg,
                            function () {
                                promptAndCheckWithAlert(text);  // Recurse
                            });
                    } else {
                        // No conflict
                        workspace.createVariable(text, type);
                        if (opt_callback) {
                            opt_callback(text);
                        }
                    }
                } else {
                    // User canceled prompt.
                    if (opt_callback) {
                        opt_callback(null);
                    }
                }
            });
    };
    promptAndCheckWithAlert('');
};
kosmos.varsFlyOutCallback = function (workspace) {
    var xmlList = [];
    var button = document.createElement('button');
    button.setAttribute('text', '%{BKY_NEW_VARIABLE}');
    button.setAttribute('callbackKey', 'CREATE_VARIABLE');

    workspace.registerButtonCallback('CREATE_VARIABLE', function (button) {
        kosmos.createVariableButtonHandler(button.getTargetWorkspace());
    });

    xmlList.push(button);

    var blockList = kosmos.varsFlyOutCallbackflyoutCategoryBlocks(workspace);
    xmlList = xmlList.concat(blockList);
    return xmlList;
};

/**
 * Construct the blocks required by the flyout for the variable category.
 * @param {!Blockly.Workspace} workspace The workspace containing variables.
 * @return {!Array.<!Element>} Array of XML block elements.
 */
kosmos.varFlyoutwasactive = false;
kosmos.varsFlyOutCallbackflyoutCategoryBlocks = function (workspace) {
    kosmos.varFlyoutwasactive = true;
    var variableModelList = workspace.getVariablesOfType('');

    var xmlList = [];
    if (variableModelList.length > 0) {
        // New variables are added to the end of the variableModelList.
        var mostRecentVariable = null;
        for (let i = 1; i < variableModelList.length; i++) {
            const v = variableModelList[variableModelList.length - 1];

            if (kosmos.isProtectedVariable(v.name)) {
                continue
            }
            mostRecentVariable = v;
            break;

        }
        if (mostRecentVariable != null) {
            if (Blockly.Blocks['variables_set']) {
                var block = Blockly.utils.xml.createElement('block');
                block.setAttribute('type', 'variables_set');
                block.setAttribute('gap', Blockly.Blocks['math_change'] ? 8 : 24);
                block.appendChild(
                    Blockly.Variables.generateVariableFieldDom(mostRecentVariable));
                xmlList.push(block);
            }

        }

        if (Blockly.Blocks['variables_get']) {
            variableModelList.sort(Blockly.VariableModel.compareByName);
            for (var i = 0, variable; (variable = variableModelList[i]); i++) {
                console.log("var ");
                console.log(variable);
                var block = Blockly.utils.xml.createElement('block');
                block.setAttribute('type', 'variables_get');
                block.setAttribute('gap', 8);
                block.appendChild(Blockly.Variables.generateVariableFieldDom(variable));
                xmlList.push(block);
            }

        }
    }
    console.log(xmlList);
    return xmlList;
};
kosmos.varNames = {};
kosmos.checkBlock = function (block) {
    if (block == null) {
        return;
    }
    let error = false;
    console.log("check block", block);
    try {
        const list = block.inputList;


        if (list != null) {
            console.log("check inputs", list.length);

            for (let i = 0; i < list.length; i++) {

                try {

                    const liste = list[i];
                    console.log("checking " + liste);
                    if (liste !== undefined) {

                        if (liste.name != "") {
                            try {
                                const v = Blockly.Python.valueToCode(block, liste.name,
                                    Blockly.Python.ORDER_NONE);
                                console.log("input ", list[i], " ", block.getFieldValue(liste), liste.value, v);
                                if (v === undefined || v == "-" || v == "") {
                                    console.log("ERROR BECAUSE OF ", v);
                                    error = true;
                                    break;
                                }
                            } catch (e) {

                            }

//            console.log("input ", list[i], " ", block.getFieldValue(list[i]), list[i].value);
                        }

                        for (let j = 0; j < liste.fieldRow.length; j++) {
                            const field = liste.fieldRow[j];

                            if (field.name) {
                                console.log("field ", field);
                                let v = block.getFieldValue(field.name);
                                if (v === undefined || v == "-") {
                                    console.log("ERROR BECAUSE OF MISSING VALUE ", v, (v === undefined), (v == "-"), (v == ""), field.name);
                                    error = true;
                                    break;
                                } else {
                                    try {
                                        if (v.toLowerCase().trim() == field.name.toLowerCase().trim()) {
                                            error = true;
                                            break;
                                        }
                                    } catch (e) {

                                    }
                                }

                            }
                        }
                    }
                } catch (e) {
                    console.log(e);
                }
            }
        }
    } catch (e) {
        console.log(e);
    }
    if (error) {
        console.log("missing input");
        block.setWarningText(Blockly.Msg.KOSMOS_FILL_ALL_INPUTS);
    } else {
        console.log("NO missing input");

        block.setWarningText(null);
    }

}

function onEvent(event) {
    //console.log("E V E N T",event,event.type,(event.type === Blockly.Events.MOVE) );
    if (event.type === Blockly.Events.MOVE) {

        if (event.blockId) {
            const block = Code.workspace.getBlockById(event.blockId);

            if (block) {
                console.log("got block!");
                kosmos.checkBlock(block);
            }
        }

        if (event.newParentId) {
            const parent = Code.workspace.getBlockById(event.newParentId);

            if (parent && parent.type == "colour_rgb") {
                if (block.type == "math_number") {
                    if (block.getFieldValue("NUM") > 100) {
                        block.setFieldValue(100, "NUM");
                    }
                }
                kosmos.paintInColor(parent);
            }
        }

        if (event.newParentId) {
            const block2 = Code.workspace.getBlockById(event.newParentId);
            kosmos.checkBlock(block2);
        }
        if (event.oldParentId) {
            const block2 = Code.workspace.getBlockById(event.oldParentId);
            kosmos.checkBlock(block2);
        }
    } else if (event.type == Blockly.Events.CREATE) {

        const block = Code.workspace.getBlockById(event.blockId);
        if (block === undefined || block == null) {
            return;
        }
        kosmos.checkBlock(block);
        if (block.type == "colour_picker") {
            block.getField('COLOUR').setValidator(kosmos.colorValidator);
            block.getField('COLOUR').doClassValidation_(block.getField('COLOUR').getValue());
        }
        if (block.type == "colour_rgb") {
            kosmos.paintInColor(block);
        }
        /*if (block.type == "kosmos_create_device") {
        block.getField('NAME').setValidator(kosmos.createDeviceNameValidator);
        }*/

        //console.log(block);
    } else if (event.type == Blockly.Events.CHANGE) {
        const block = Code.workspace.getBlockById(event.blockId);
        kosmos.checkBlock(block);
        console.log("event changed");

        const parent = block.getParent();
        console.log(parent);
        if (parent && parent.type == "colour_rgb") {
            kosmos.paintInColor(parent);
        }

        /*if ( block.type == "kosmos_clamp_increase3" || block.type == "kosmos_clamp_increase4" ) {
            console.log("CHANGE IN CLAMP!");
            const list = Code.workspace.getAllBlocks();

            for (let i = 0; i < list.length; i++) {

                const block2 = list[i];
                let input = block2.getField("VARNAME");
                if (input) {
                    console.log(input.selectedOption_[0]+" == "+event.newValue+"?");
                    if ( input.selectedOption_[0] == event.newValue) {
                        let input2 = block2.getInput("STEP");
                        if ( input2) {
                            console.log(input2);
                            const r = Blockly.Python.valueToCode(block2, 'STEP',
                                Blockly.Python.ORDER_NONE) || 0;
                            console.log("NEW STEP IS: "+r);
                            let input3 = block.getField("BY");
                            if (input3) {
                                console.log("DID find input");

                                    input3.setValue(r);


                            }

                        }

                        break;
                    }

                    }
                }
        }*/
    } else if (event.type == Blockly.Events.VAR_CREATE) {
        kosmos.varNames[event.varId] = event.varName;

    } else if (event.type == Blockly.Events.VAR_DELETE) {
        console.log("var_delete");
        console.log(event);

        if (kosmos.isProtectedVariable(event.varName)) {

            Code.workspace.setVisible(false);

            Code.workspace.undo(false);
            Code.workspace.setVisible(true);
            alert("this is a protected variable, you can NOT delete it.");

        }


    } else if (event.type == Blockly.Events.VAR_RENAME) {
        kosmos.varNames[event.varId] = event.newName;

        for (let i = 0; i < kosmos.protectedVariableNames.length; i++) {
            if (event.oldName == kosmos.protectedVariableNames[i]) {

                const v = Blockly.Variables.getVariable(Code.workspace, event.varId);
                v.name = event.oldName;
                Code.workspace.setVisible(false);
                alert("this is a protected variable, please dont rename it! it might break stuff");
                Code.workspace.setVisible(true);
            }
        }
        const list = Code.workspace.getAllBlocks();

        for (let i = 0; i < list.length; i++) {

            const block = list[i];
            let input = block.getField("LISTNAME");

            if (input) {
                console.log("type: " + typeof (input));
                console.log(input);
                console.log("has input", input.getValue(), name);
                if (input.getValue() == event.oldName) {
                    console.log("FOUND!")
                    input.setValue(event.newName);
                }

            }
            input = block.getField("LISTDROP");
            if (input) {
                console.log(input);
                if (input.selectedOption_[0] == event.oldName) {
                    console.log("FOUND3!")

                    input.selectedOption_ = [event.newName, event.newName];
                    input.doValueUpdate_(event.newName);
                    input.forceRerender();
                }
            }
            input = block.getField("CLAMPDROP");
            if (input) {
                console.log(input);
                if (input.selectedOption_[0] == event.oldName) {


                    input.selectedOption_ = [event.newName, event.newName];
                    input.doValueUpdate_(event.newName);
                    input.forceRerender();
                }
            }
            input = block.getField("TIMERNAME");
            if (input) {
                console.log("has input", input.getValue(), name);
                if (input.getValue() == event.oldName) {
                    console.log("FOUND2!")
                    input.setValue(event.newName);

                }
            }
        }

        //console.log(block);


    } else {
        console.log("event:");
        console.log(event);
    }

}

/**
 * Initialize the page language.
 */
Code.initLanguage = function () {
    // Set the HTML's language and direction.
    const rtl = Code.isRtl();
    document.dir = rtl ? 'rtl' : 'ltr';
    document.head.parentElement.setAttribute('lang', Code.LANG);

    // Sort languages alphabetically.
    const languages = [];
    for (let lang in Code.LANGUAGE_NAME) {
        languages.push([Code.LANGUAGE_NAME[lang], lang]);
    }
    const comp = function (a, b) {
        // Sort based on first argument ('English', 'Русский', '简体字', etc).
        if (a[0] > b[0]) return 1;
        if (a[0] < b[0]) return -1;
        return 0;
    };
    languages.sort(comp);
    // Populate the language selection menu.
    const languageMenu = document.getElementById('languageMenu');
    languageMenu.options.length = 0;
    for (let i = 0; i < languages.length; i++) {
        const tuple = languages[i];
        const lang = tuple[tuple.length - 1];
        const option = new Option(tuple[0], lang);
        if (lang == Code.LANG) {
            option.selected = true;
        }
        languageMenu.options.add(option);
    }
    languageMenu.addEventListener('change', Code.changeLanguage, true);

    // Populate the coding language selection menu.
    const codeMenu = document.getElementById('code_menu');
    codeMenu.options.length = 0;
    for (let i = 1; i < Code.TABS_.length; i++) {
        codeMenu.options.add(new Option(Code.TABS_DISPLAY_[i], Code.TABS_[i]));
    }
    codeMenu.addEventListener('change', Code.changeCodingLanguage);

    // Inject language strings.

    document.getElementById('tab_blocks').textContent = MSG['blocks'];

    //document.getElementById('runButton').title = MSG['runTooltip'];
    document.getElementById('trashButton').title = MSG['trashTooltip'];
};

/**
 * Discard all blocks from the workspace.
 */
Code.discard = function () {
    const count = Code.workspace.getAllBlocks(false).length;
    if (count < 2 ||
        window.confirm(Blockly.Msg['DELETE_ALL_BLOCKS'].replace('%1', count))) {
        Code.workspace.clear();
        if (window.location.hash) {
            window.location.hash = '';
        }
    }
};
kosmos.createDeviceNameValidator = function (newValue) {
    return kosmos.findLegalUUID(newValue, null);
    /*const list = kosmos.getUuidList();
    for ( let i = 0;i<list.length;i++) {
        const entry = list[i][1];
        console.log("nw:"+newValue+"=="+entry);
        if (newValue == entry) {
            return newValue+"_1";
        }

    }
    return newValue;*/

}
kosmos.protectedVariableNames = ["uuid", "property", "value"];
kosmos.colorValidator = function (newValue) {
    this.getSourceBlock().setColour(newValue);
    return newValue;
}
kosmos.componentToHex = function (c) {
    const hex = c.toString(16);
    return hex.length == 1 ? "0" + hex : hex;
}
kosmos.rgbToHex = function (r, g, b) {
    console.log("r: " + r);
    console.log("g: " + g);
    console.log("b: " + b);
    return "#" + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
}
/* hexToComplimentary : Converts hex value to HSL, shifts
 * hue by 180 degrees and then converts hex, giving complimentary color
 * as a hex value
 * @param  [String] hex : hex value
 * @return [String] : complimentary color as hex value
 */
kosmos.hexToComplimentary = function (hex) {

    // Convert hex to rgb
    // Credit to Denis http://stackoverflow.com/a/36253499/4939630
    var rgb = 'rgb(' + (hex = hex.replace('#', '')).match(new RegExp('(.{' + hex.length / 3 + '})', 'g')).map(function (l) {
        return parseInt(hex.length % 2 ? l + l : l, 16);
    }).join(',') + ')';

    // Get array of RGB values
    rgb = rgb.replace(/[^\d,]/g, '').split(',');

    var r = rgb[0], g = rgb[1], b = rgb[2];

    // Convert RGB to HSL
    // Adapted from answer by 0x000f http://stackoverflow.com/a/34946092/4939630
    r /= 255.0;
    g /= 255.0;
    b /= 255.0;
    var max = Math.max(r, g, b);
    var min = Math.min(r, g, b);
    var h, s, l = (max + min) / 2.0;

    if (max == min) {
        h = s = 0;  //achromatic
    } else {
        var d = max - min;
        s = (l > 0.5 ? d / (2.0 - max - min) : d / (max + min));

        if (max == r && g >= b) {
            h = 1.0472 * (g - b) / d;
        } else if (max == r && g < b) {
            h = 1.0472 * (g - b) / d + 6.2832;
        } else if (max == g) {
            h = 1.0472 * (b - r) / d + 2.0944;
        } else if (max == b) {
            h = 1.0472 * (r - g) / d + 4.1888;
        }
    }

    h = h / 6.2832 * 360.0 + 0;

    // Shift hue to opposite side of wheel and convert to [0-1] value
    h += 180;
    if (h > 360) {
        h -= 360;
    }
    h /= 360;

    // Convert h s and l values into r g and b values
    // Adapted from answer by Mohsen http://stackoverflow.com/a/9493060/4939630
    if (s === 0) {
        r = g = b = l; // achromatic
    } else {
        var hue2rgb = function hue2rgb(p, q, t) {
            if (t < 0) t += 1;
            if (t > 1) t -= 1;
            if (t < 1 / 6) return p + (q - p) * 6 * t;
            if (t < 1 / 2) return q;
            if (t < 2 / 3) return p + (q - p) * (2 / 3 - t) * 6;
            return p;
        };

        var q = l < 0.5 ? l * (1 + s) : l + s - l * s;
        var p = 2 * l - q;

        r = hue2rgb(p, q, h + 1 / 3);
        g = hue2rgb(p, q, h);
        b = hue2rgb(p, q, h - 1 / 3);
    }

    r = Math.round(r * 255);
    g = Math.round(g * 255);
    b = Math.round(b * 255);

    // Convert r b and g values to hex
    rgb = b | (g << 8) | (r << 16);
    return "#" + (0x1000000 | rgb).toString(16).substring(1);
}
kosmos.timerFlyOutCallback = function (workspace) {


    const xmlList = [];

    xmlList.push(Blockly.Xml.textToDom(' <block type="kosmos_create_timer"><field name="VARNAME">' + kosmos.findLegalName("timer1", null) + '</field></block>'));
    xmlList.push(Blockly.Xml.textToDom(' <block type="kosmos_create_timer_2"><field name="VARNAME">' + kosmos.findLegalName("timer1", null) + '</field></block>'));

    const list = Code.workspace.getAllBlocks();

    for (let i = 0; i < list.length; i++) {

        const block = list[i];
        if (block.type == "kosmos_create_timer" || block.type == "kosmos_create_timer_2") {
            const bname = block.getFieldValue("VARNAME");
            xmlList.push(Blockly.Xml.textToDom(' <block type="kosmos_stop_timer"><field name="TIMERNAME">' + kosmos.varNames[bname] + '</field></block>'));
            xmlList.push(Blockly.Xml.textToDom(' <block type="kosmos_restart_timer"><field name="TIMERNAME">' + kosmos.varNames[bname] + '</field></block>'));

        }
    }
    // for (var i = 0; i < kosmos.timers.length; i++) {
    //     /*var blockText = '<block type="colour_picker">' +
    //         '<field name="COLOUR">' + colourList[i] + '</field>' +
    //         '</block>';*/
    //     /*var blockText = '<block type="variables_set">' +
    //         '<field name="VAR">' + colourList[i] + '</field>' +
    //         '</block>';
    //     var block = Blockly.Xml.textToDom(blockText);
    //     xmlList.push(block);*/
    //     var blockText = '<block type="variables_get">' +
    //         '<field name="VAR">' + kosmos.timers[i] + '</field>' +
    //         '</block>';
    //     var block = Blockly.Xml.textToDom(blockText);
    //     xmlList.push(block);
    // }

    return xmlList;
};


kosmos.clampFlyOutCallback = function (workspace) {
    let xmlList = [];
    xmlList.push(Blockly.Xml.textToDom('<block type="kosmos_clamp_int"><field name="VARNAME">' + kosmos.findLegalName("iClamp1", null) + '</field><value name="MIN"><block type="math_number"><field name="NUM">0</field></block></value><value name="MAX"><block type="math_number"><field name="NUM">10</field></block></value><value name="STEP"><block type="math_number"><field name="NUM">10</field></block></value><value name="VALUE"><block type="math_number"><field name="NUM">0</field></block></value></block>'))
    xmlList.push(Blockly.Xml.textToDom('<block type="kosmos_clamp_float"><field name="VARNAME">' + kosmos.findLegalName("fClamp1", null) + '</field><value name="MIN"><block type="math_number"><field name="NUM">0</field></block></value><value name="MAX"><block type="math_number"><field name="NUM">10</field></block></value><value name="STEP"><block type="math_number"><field name="NUM">10</field></block></value><value name="VALUE"><block type="math_number"><field name="NUM">0</field></block></value></block>'))
    const list = Code.workspace.getAllBlocks();
    let hasClamp = false;

    for (let i = 0; i < list.length; i++) {
        const block = list[i];

        if (block.type === "kosmos_clamp_float" || block.type === "kosmos_clamp_int") {
            hasClamp = true;
        }

    }
    if (hasClamp) {
        xmlList.push(Blockly.Xml.textToDom(' <block type="kosmos_clamp_curr"></block>'));
        xmlList.push(Blockly.Xml.textToDom(' <block type="kosmos_clamp_increase"><value name="AMOUNT"><block type="math_number"><field name="NUM">1</field></block></value></block>'));
        xmlList.push(Blockly.Xml.textToDom(' <block type="kosmos_clamp_increase2"><value name="AMOUNT"><block type="math_number"><field name="NUM">1</field></block></value></block>'));
        xmlList.push(Blockly.Xml.textToDom(' <block type="kosmos_clamp_increase3"></block>'));
        xmlList.push(Blockly.Xml.textToDom(' <block type="kosmos_clamp_increase4"></block>'));
        xmlList.push(Blockly.Xml.textToDom(' <block type="kosmos_clamp_decrease"><value name="AMOUNT"><block type="math_number"><field name="NUM">1</field></block></value></block>'));
        xmlList.push(Blockly.Xml.textToDom(' <block type="kosmos_clamp_decrease2"><value name="AMOUNT"><block type="math_number"><field name="NUM">1</field></block></value></block>'));
        xmlList.push(Blockly.Xml.textToDom(' <block type="kosmos_clamp_decrease3"></block>'));
        xmlList.push(Blockly.Xml.textToDom(' <block type="kosmos_clamp_decrease4"></block>'));
    }
    return xmlList;

}
kosmos.listsFlyOutCallback = function (workspace) {


    let xmlList = [];

    xmlList.push(Blockly.Xml.textToDom(' <block type="kosmos_lists_create_with"><field name="VARNAME">' + kosmos.findLegalName("list1", null) + '</field></block>'));
    xmlList.push(Blockly.Xml.textToDom(' <block type="kosmos_ringlists_create_with"><field name="VARNAME">' + kosmos.findLegalName("list1", null) + '</field></block>'));
    const list = Code.workspace.getAllBlocks();
    let hasList = false;
    let hasRingList = false;
    for (let i = 0; i < list.length; i++) {
        const block = list[i];

        if (block.type === "kosmos_lists_create_with" || block.type === "kosmos_ringlists_create_with") {
            hasList = true;
            //const bname = block.getFieldValue("NAME");
            console.log("vname: ");
            //const bname = Blockly.Variables.getVariable(Code.workspace,block.getFieldValue("NAME")).name;
            //const bname = Blockly.Python.variableDB_.getName(block.getFieldValue('NAME'), Blockly.Variables.NAME_TYPE);


            //const bname = Blockly.JavaScript.variableDB_.getName(block.getFieldValue('LISTNAME'), Blockly.Variables.NAME_TYPE);


        }
        if (block.type === "kosmos_ringlists_create_with") {
            //const bname = block.getFieldValue("NAME");
            //const bname = block.getFieldValue("NAME");
            //const bname = Blockly.Python.variableDB_.getName(block.getFieldValue('NAME'), Blockly.Variables.NAME_TYPE);
            /*const bname = Blockly.Variables.getVariable(Code.workspace,block.getFieldValue("NAME")).name;

            xmlList.push(Blockly.Xml.textToDom(' <block type="kosmos_list_first"><field name="LISTNAME">' + bname + '</field></block>'));
            xmlList.push(Blockly.Xml.textToDom(' <block type="kosmos_list_last"><field name="LISTNAME">' + bname + '</field></block>'));
            xmlList.push(Blockly.Xml.textToDom(' <block type="kosmos_list_add"><field name="LISTNAME">' + bname + '</field></block>'));*/
            hasRingList = true;

        }
    }
    if (hasList) {
        xmlList.push(Blockly.Xml.textToDom(' <block type="kosmos_list_curr"></block>'));
        xmlList.push(Blockly.Xml.textToDom(' <block type="kosmos_list_prev"></block>'));
        xmlList.push(Blockly.Xml.textToDom(' <block type="kosmos_list_next"></block>'));
    }
    if (hasRingList) {
        xmlList.push(Blockly.Xml.textToDom(' <block type="kosmos_list_first"></block>'));
        xmlList.push(Blockly.Xml.textToDom(' <block type="kosmos_list_last"></block>'));
        xmlList.push(Blockly.Xml.textToDom(' <block type="kosmos_list_add"></block>'));
    }


    return xmlList;
};

kosmos.timeroptions = function () {
    let opts = [];
    for (let i = 0; i < kosmos.timerblocks.length; i++) {
        const block = kosmos.timerblocks[i];
        const bname = block.getFieldValue("VARNAME");
        if (bname === undefined || bname == null) {
            opts.push(["-", "-"])
        } else {
            opts.push([bname, bname])
        }

    }
    return opts;
}


kosmos.blocksWithList = [];
kosmos.isLegalUUID = function (name, workspace, opt_exclude) {
    let list = kosmos.getUuidList();
    for (let i = 0; i < list.length; i++) {
        const entry = list[i][1];
        if (entry == name) {
            return false;
        }
    }
    list = Code.workspace.getAllBlocks();
    for (let i = 0; i < list.length; i++) {
        const block = list[i];
        if (block.type === "kosmos_create_device") {
            if (block.getFieldValue("UUID") == name) {
                return false;
            }
        }
    }

    return true;
};
kosmos.isLegalName = function (name, workspace, opt_exclude) {
    return !kosmos.isNameUsed(name, workspace, opt_exclude);
};
kosmos.findLegalUUID = function (name, block) {
    if (block && block.isInFlyout) {
        // Flyouts can have multiple procedures called 'do something'.
        return name;
    }
    let ws = Code.workspace
    if (block) {
        ws = block.workspace;
    }
    name = name || Blockly.Msg['UNNAMED_KEY'] || 'unnamed';
    while (!kosmos.isLegalUUID(name, ws, block)) {
        // Collision with another procedure.
        console.log("COLLISION!");
        let r = name.match(/^(.*?)(\d+)$/);
        if (!r) {
            console.log("NO R");
            name += '2';
        } else {
            console.log("YES R");
            name = r[1] + (parseInt(r[2], 10) + 1);
        }
    }

    return name;
};
kosmos.findLegalName = function (name, block) {
    if (block && block.isInFlyout) {
        // Flyouts can have multiple procedures called 'do something'.
        return name;
    }
    let ws = Code.workspace
    if (block) {
        ws = block.workspace;
    }
    name = name || Blockly.Msg['UNNAMED_KEY'] || 'unnamed';
    while (!kosmos.isLegalName(name, ws, block)) {
        // Collision with another procedure.
        console.log("COLLISION!");
        let r = name.match(/^(.*?)(\d+)$/);
        if (!r) {
            console.log("NO R");
            name += '2';
        } else {
            console.log("YES R");
            name = r[1] + (parseInt(r[2], 10) + 1);
        }
    }
    console.log("FOUND SAFE NAME:" + name)
    return name;
};
kosmos.isNameUsed = function (name, workspace, opt_exclude) {
    const blocks = workspace.getAllBlocks(false);
    // Iterate through every block and check the name.
    for (let i = 0; i < blocks.length; i++) {
        const block = blocks[i];
        if (opt_exclude == block) {
            continue;
        }
        const input = blocks[i].getField("VARNAME");

        if (input) {
            console.log("haz input", input.getValue(), name);
            if (input.getValue() == name) {
                console.log("HIT!");
                return true;
            }
            try {
                const bname = Blockly.Variables.getVariable(Code.workspace, block.getFieldValue("VARNAME")).name;
                console.log("bname:" + bname);
                if (bname == name) {
                    console.log("HIT!");
                    return true;
                }
            } catch (e) {

            }
        }
    }
    return false;
};
kosmos.listMutationToDom = function (block) {
    const container = Blockly.utils.xml.createElement('mutation');
    container.setAttribute('items', block.itemCount_);
    return container;
}
kosmos.listDomToMutation = function (block, xmlElement) {
    block.itemCount_ = parseInt(xmlElement.getAttribute('items'), 10);
    block.updateShape_();
}
kosmos.listDecompose = function (block, workspace) {
    console.log("decompose");
    const containerBlock = workspace.newBlock('lists_create_with_container');
    containerBlock.initSvg();
    let connection = containerBlock.getInput('STACK').connection;
    for (let i = 0; i < block.itemCount_; i++) {
        const itemBlock = workspace.newBlock('lists_create_with_item');
        itemBlock.initSvg();
        connection.connect(itemBlock.previousConnection);
        connection = itemBlock.nextConnection;
    }
    return containerBlock;
}
kosmos.listCompose = function (block, containerBlock) {
    console.log("compose");
    console.log(block);
    console.log(containerBlock);
    let itemBlock = containerBlock.getInputTargetBlock('STACK');
    // Count number of inputs.
    let connections = [];
    while (itemBlock && !itemBlock.isInsertionMarker()) {
        connections.push(itemBlock.valueConnection_);
        itemBlock = itemBlock.nextConnection &&
            itemBlock.nextConnection.targetBlock();
    }
    // Disconnect any children that don't belong.
    for (let i = 0; i < block.itemCount_; i++) {
        const connection = block.getInput('ADD' + i).connection.targetConnection;
        if (connection && connections.indexOf(connection) == -1) {
            connection.disconnect();
        }
    }
    block.itemCount_ = connections.length;
    block.updateShape_();
    // Reconnect any child blocks.
    for (let i = 0; i < block.itemCount_; i++) {
        Blockly.Mutator.reconnect(connections[i], block, 'ADD' + i);
    }
}
kosmos.listSaveConnections = function (block, containerBlock) {
    let itemBlock = containerBlock.getInputTargetBlock('STACK');
    let i = 0;
    while (itemBlock) {
        const input = block.getInput('ADD' + i);
        itemBlock.valueConnection_ = input && input.connection.targetConnection;
        i++;
        itemBlock = itemBlock.nextConnection &&
            itemBlock.nextConnection.targetBlock();
    }
}
kosmos.addListEntry = function (block, i) {
    if (!block.getInput('ADD' + i)) {
        const input = block.appendValueInput('ADD' + i)
            .setAlign(Blockly.ALIGN_RIGHT);
        switch (block.type) {
            case "kosmos_word_match":
                input.setCheck(["String", "kosmos_create_wordlist", "kosmos_create_named_wordlist"]);

                break;
            case "kosmos_create_wordlist":
            case "kosmos_create_named_wordlist":
                input.setCheck(["String"]);
                break;
            default:
                break;
            //input.appendField(Blockly.Msg['LISTS_CREATE_WITH_INPUT_WITH']);
        }
        if (i == 0) {
            switch (block.type) {
                case "kosmos_word_match":
                    input.appendField(Blockly.Msg['KOSMOS_MATCH_WORDS']);

                    break;
                case "kosmos_create_wordlist":
                case "kosmos_create_named_wordlist":
                    input.appendField(Blockly.Msg['KOSMOS_WORDLIST_ONE_OF']);
                    break;


                default:
                    input.appendField(Blockly.Msg['LISTS_CREATE_WITH_INPUT_WITH']);
            }


        }
    }
}
kosmos.listUpdateShape_ = function (block, oneempty = false) {
    if (block.itemCount_ && block.getInput('EMPTY')) {
        block.removeInput('EMPTY');
    } else if (!block.itemCount_ && !block.getInput('EMPTY')) {
        block.appendDummyInput('EMPTY')
            .appendField(Blockly.Msg['LISTS_CREATE_EMPTY_TITLE']);
    }
    // Add new inputs.
    let i = 0;
    for (; i < block.itemCount_; i++) {
        kosmos.addListEntry(block, i);
    }
    // Remove deleted inputs.
    while (block.getInput('ADD' + i)) {
        block.removeInput('ADD' + i);
        i++;
    }
    switch (block.type) {
        case "kosmos_word_match":
            let block_do = block.getInput('DO');


            if (!block_do) {

                block.appendStatementInput("DO")
                    .setCheck(null)
                    .appendField("do");
            } else {
                /*                block_do.getInput("")
                                block.removeInput('DO');

                                block.appendStatementInput("DO")
                                    .setCheck(null)
                                    .appendField("do");*/
            }

            break;

        default:
            break;
    }

}
kosmos.getListNames = function (block) {

    const list = Code.workspace.getAllBlocks();
    const ret = []
    for (let i = 0; i < list.length; i++) {

        const block = list[i];
        if (block.type === "kosmos_lists_create_with" || block.type === "kosmos_ringlists_create_with") {
            try {
                const bname = Blockly.Variables.getVariable(Code.workspace, block.getFieldValue("VARNAME")).name;
                ret.push([bname, bname]);
            } catch (e) {

            }


        }
    }
    if (ret.length == 0) {

        ret.push(["-", "-"]);
    } else {

    }

    return ret;
}
kosmos.getClampNames = function () {
    const list = Code.workspace.getAllBlocks();
    const ret = []
    for (let i = 0; i < list.length; i++) {

        const block = list[i];
        if (block.type === "kosmos_clamp_int" || block.type === "kosmos_clamp_float") {
            const bname = Blockly.Variables.getVariable(Code.workspace, block.getFieldValue("VARNAME")).name;
            ret.push([bname, bname]);
        }
    }
    if (ret.length == 0) {
        ret.push(["-", "-"]);
    }
    return ret;
}
kosmos.listOnChange = function (block, event) {
    console.log(event.blockId + " == " + block.id);
    console.log(block);


    if (event.blockId == block.id) {
        if (event.name === "MAX") {

            block.itemCount_ = event.newValue;
            block.updateShape_();
        }
    }
    /*//console.log(event);
    if (event instanceof Blockly.Events.Change) {
        console.log("onc finished with ", block.getFieldValue("NAME"));
        const list = Code.workspace.getAllBlocks();
        for (let i = 0; i < list.length; i++) {
            const block = list[i];
            console.log(block);
            //console.log(block.inputList);
            const fieldname = "LISTNAME";
            const input = block.getField(fieldname);
            if (input) {
                const bname = block.getFieldValue(fieldname);
                if (bname == event.oldValue) {
                    block.setFieldValue(event.newValue, fieldname);
                }
            }
        }


    }*/
}

Blockly.Python.init = function (workspace) {
    /**
     * Empty loops or conditionals are not allowed in Python.
     */

    Blockly.Python.PASS = this.INDENT + 'pass\n';
    // Create a dictionary of definitions to be printed before the code.
    Blockly.Python.definitions_ = Object.create(null);
    // Create a dictionary mapping desired function names in definitions_
    // to actual function names (to avoid collisions with user functions).
    Blockly.Python.functionNames_ = Object.create(null);

    if (!Blockly.Python.variableDB_) {
        Blockly.Python.variableDB_ =
            new Blockly.Names(Blockly.Python.RESERVED_WORDS_);
    } else {
        Blockly.Python.variableDB_.reset();
    }

    Blockly.Python.variableDB_.setVariableMap(Code.workspace.getVariableMap());

    /*var defvars = [];
    // Add developer variables (not created or named by the user).
    var devVarList = Blockly.Variables.allDeveloperVariables(workspace);
    for (var i = 0; i < devVarList.length; i++) {
        defvars.push(Blockly.Python.variableDB_.getName(devVarList[i],
            Blockly.Names.DEVELOPER_VARIABLE_TYPE) + ' = None');
    }*/
    /* dont define variables globally, we dont want that..ever...
    // Add user variables, but only ones that are being used.
    var variables = Blockly.Variables.allUsedVarModels(workspace);
    for (var i = 0; i < variables.length; i++) {
        console.log(Blockly.Python.variableDB_.getName(variables[i].getId(),
            Blockly.VARIABLE_CATEGORY_NAME))
        defvars.push(Blockly.Python.variableDB_.getName(variables[i].getId(),
            Blockly.VARIABLE_CATEGORY_NAME) + ' = None');
    }

    //Blockly.Python.definitions_['variables'] = defvars.join('\n');

     */
};


Blockly.defineBlocksWithJsonArray([{
    "type": "object",
    "message0": "{ %1 %2 }",
    "args0": [
        {
            "type": "input_dummy"
        },
        {
            "type": "input_statement",
            "name": "MEMBERS"
        }
    ],
    "output": null,
    "colour": 230,
},
    {
        "type": "member3",
        "message0": "%1 %2 %3",
        "args0": [
            {
                "type": "field_input",
                "name": "MEMBER_NAME",
                "text": ""
            },
            {
                "type": "field_label",
                "name": "COLON",
                "text": ":"
            },
            {
                "type": "input_value",
                "name": "MEMBER_VALUE"
            }
        ],

        "previousStatement": null,
        "nextStatement": null,
        "colour": 230,
    }, {
        "type": "member2",
        "message0": "%1 %2 %3",
        "args0": [
            {
                "type": "input_value",
                "name": "MEMBER_NAME",
                "text": ""
            },
            {
                "type": "field_label",
                "name": "COLON",
                "text": ":"
            },
            {
                "type": "input_value",
                "name": "MEMBER_VALUE"
            }
        ],
        "inputsInline": true,
        "previousStatement": null,
        "nextStatement": null,
        "colour": 230,
    }]);


kosmos.Code["getValue"] = function (uuid, property) {
    return 'home.get_value(' + uuid + ', ' + property + ')';


}
kosmos.Code["setValue"] = function (uuid, property, value) {
    return 'home.set_value(' + uuid + ', ' + property + ', ' + value + ')\n';


}
kosmos.Code["setDevice"] = function (uuid, value) {
    return 'home.set_device(' + uuid + ', ' + value + ')\n';
}

if (typeof document !== 'undefined') {

// Load the Code demo's language strings.
    document.write('<script src="msg/' + Code.LANG + '.js"></script>\n');
// Load Blockly's language strings.
    if (Code.LANG != "en") {
        //inject english anyways, so we always can fall back to it
        document.write('<script src="msg/js/en.js"></script>\n');
    }
}
kosmos.timestamp = function () {
    const now = new Date();
    let h = now.getHours();
    if (h < 10) {
        h = "0" + h;
    }
    let m = now.getMinutes();
    if (m < 10) {
        m = "0" + m;
    }
    let s = now.getSeconds();
    if (s < 10) {
        s = "0" + s;
    }
    return h + ":" + m + ":" + s;

}
kosmos.ws = undefined;
kosmos.connect = function () {
    const ws = new WebSocket('ws://' + window.location.hostname + ':' + window.location.port + '/kreews');
    ws.onopen = function () {
        kosmos.ws = ws;

        ws.send('user/auth:{"user":"' + kosmos.username + '","pass":"' + kosmos.password + '"}');


        pingws();
    };

    ws.onmessage = function (e) {
        //console.log('Message:', e);
        if ( e.data == "pong") {
            return;
        }
        if ( e.data == "ping") {
            ws.send("pong");
            return;
        }
        const msg = JSON.parse(e.data);
        if (msg.type == "log") {
            console.log("got log")
            //document.getElementById("log").innerText =  + document.getElementById("log").innerText;
            const span = document.createElement("span");
            span.innerHTML = kosmos.timestamp() + ": " + msg.value + "<br>";
            document.getElementById("log").prepend(span)
        } else if (msg.type == "err") {
            console.log("got err")

            const span = document.createElement("span");
            span.innerHTML = kosmos.timestamp() + ": " + msg.value + "<br>";
            span.style.color = "red";
            document.getElementById("log").prepend(span)
        } else {
            console.log(msg.type)
        }
    };

    ws.onclose = function (e) {
        console.log('Socket is closed. Reconnect will be attempted in 1 second.', e.reason);
        setTimeout(function () {
            kosmos.connect();
        }, 1000);
    };

    ws.onerror = function (err) {
        console.error('Socket encountered error: ', err.message, 'Closing socket');
        ws.close();
    };
}

if (typeof document !== 'undefined') {
    document.write('<script src="msg/js/' + Code.LANG + '.js"></script>\n');
    document.write('<script src="blocks.js"></script>\n');
    document.write('<script src="python.js"></script>\n');
    window.addEventListener('load', kosmos.onLoad);
} else {
    var XMLHttpRequest = require("xmlhttprequest").XMLHttpRequest;
    var xhr = new XMLHttpRequest();

    exports.kosmos = kosmos;
    exports.Code = Code;

    Code.init();


}
