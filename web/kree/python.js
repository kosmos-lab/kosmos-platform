Blockly.Python['kosmos_create_device'] = function (block) {
    var text_name = block.getFieldValue('UUID');
    var dropdown_type = block.getFieldValue('TYPE');

    var code = 'home.create_device("'+text_name+'","'+dropdown_type+'")\n';
    return code;
};

Blockly.Python['kosmos_lists_create_with'] = function (block) {
    // Create a list with any number of elements of any type.
    var elements = new Array(block.itemCount_);
    for (var i = 0; i < block.itemCount_; i++) {
        elements[i] = Blockly.Python.valueToCode(block, 'ADD' + i,
            Blockly.Python.ORDER_NONE) || 'None';
    }
    var code = '[' + elements.join(', ') + ']';
    const bname = Blockly.Variables.getVariable(Code.workspace, block.getFieldValue("VARNAME")).name;


    return bname + ' = KosmosList(' + code + ')\n';
};
Blockly.Python['kosmos_ringlists_create_with'] = function (block) {
    // Create a list with any number of elements of any type.
    var elements = [];

    for (var i = 0; i < block.itemCount_; i++) {
        const elem = Blockly.Python.valueToCode(block, 'ADD' + i,
            Blockly.Python.ORDER_NONE);
        if ( elem ) {
            elements.push(elem);
        }
        /*elements[i] = Blockly.Python.valueToCode(block, 'ADD' + i,
            Blockly.Python.ORDER_NONE) || 'None';*/
    }
    var code = '[' + elements.join(', ') + ']';
    const bname = Blockly.Variables.getVariable(Code.workspace, block.getFieldValue("VARNAME")).name;


    return bname + ' = KosmosList(' + code + ',' + block.getFieldValue('MAX') + ')\n';
};
Blockly.Python['variables_set2'] = function(block) {
    // Variable setter.
    var argument0 = Blockly.Python.valueToCode(block, 'VALUE',
        Blockly.Python.ORDER_NONE) || '0';
    var varName = Blockly.Python.variableDB_.getName(block.getFieldValue('VAR'),
        Blockly.VARIABLE_CATEGORY_NAME);
    for (let i = 0;i<kosmos.protectedVariableNames.length;i++) {
        if ( varName.startsWith(kosmos.protectedVariableNames[i])) {
            return "#i will not do this!\n#"+varName + " = " + argument0 + "\n";
        }
    }

    return varName + ' = ' + argument0 + '\n';
};
Blockly.Python['variables_get2'] = function(block) {
    // Variable getter.
    var name = Blockly.Python.variableDB_.getName(block.getFieldValue('VAR'),
        Blockly.VARIABLE_CATEGORY_NAME);
    for (let i = 0; i < kosmos.protectedVariableNames.length; i++) {
        if (name.startsWith(kosmos.protectedVariableNames[i])) {
            name = kosmos.protectedVariableNames[i];
            break;
        }
    }


    return [name, Blockly.Python.ORDER_ATOMIC];
};
Blockly.Python['kosmos_delay'] = function (block) {
    var number_delay = block.getFieldValue('DELAY');
    // TODO: Assemble Python into code variable.
    var code = 'time.sleep(' + number_delay + ')\n';
    return code;
};
Blockly.Python['kosmos_stop_timer'] = function (block) {
    var dropdown_name = block.getFieldValue('TIMERNAME');
    // TODO: Assemble Python into code variable.
    var code = 'if '+dropdown_name+' is not None:\n    '+dropdown_name+'.stop()\n';
    return code;
};
Blockly.Python['kosmos_restart_timer'] = function (block) {
    var dropdown_name = block.getFieldValue('TIMERNAME');
    // TODO: Assemble Python into code variable.
    //var code = 'timers["' + dropdown_name + '"].restart()\n';
    code = [];
    code.push('if '+dropdown_name+' is not None:\n    '+dropdown_name+'.stop()');
    code.push(dropdown_name+' = StoppableThread(target=timer_func_' + dropdown_name + ', args=())')
    code.push(dropdown_name+'.start()')
    return code.join("\n") + "\n";
};
Blockly.Python['kosmos_create_timer'] = function (block) {
    var text_name = kosmos.varNames[block.getFieldValue('VARNAME')];
    var code = [];
    //console.log("DO:", block.childBlocks_);
    //code.push('if '+text_name+':\n    ' + text_name + '.stop()\n');
    if (block.childBlocks_.length == 1) {

        if (block.childBlocks_[0].type == "procedures_callnoreturn") {
            code.push('if '+text_name+' is not None:\n    '+text_name+'.stop()');

            code.push(text_name+' = StoppableThread(target=timer_func_' + text_name + ', args=())')
            //console.log("DO:", block.childBlocks_[0].getProcedureCall());
        } else {
            code.push('if '+text_name+' is not None:\n    '+text_name+'.stop()');
            code.push(text_name+' = StoppableThread(target=timer_func_' + text_name + ', args=())')
        }
    }
    else if ( block.childBlocks_.length == 2) {

        if (block.childBlocks_[1].type == "procedures_callnoreturn") {
            code.push('if '+text_name+' is not None:\n    '+text_name+'.stop()');
            code.push(text_name+' = StoppableThread(target=timer_func_' + text_name + ', args=())')

        }
        else {
            code.push('if '+text_name+' is not None:\n    '+text_name+'.stop()');
            code.push(text_name+' = StoppableThread(target=timer_func_' + text_name + ', args=())')

        }
    }
    else {
        code.push('if '+text_name+' is not None:\n    '+text_name+'.stop()');
        code.push(text_name+' = StoppableThread(target=timer_func_' + text_name + ', args=())')

    }
    //code.push('timers["' + text_name + '"] = StoppableThread(target=timer_func_' + text_name + ', args=())')


    code.push(text_name+'.start()');
    //code.push(Blockly.Python.statementToCode(block, 'DO') || Blockly.Python.PASS);
    return code.join("\n") + "\n";
};
Blockly.Python['kosmos_create_timer_2'] = function (block) {
    var text_name = kosmos.varNames[block.getFieldValue('VARNAME')];
    const start_type = block.getFieldValue('START_TYPE');

    var code = [];
    //console.log("DO:", block.childBlocks_);
    //code.push('if '+text_name+':\n    ' + text_name + '.stop()\n');
    if (block.childBlocks_.length == 1) {

        if (block.childBlocks_[0].type == "procedures_callnoreturn") {
            code.push('if '+text_name+' is not None:\n    '+text_name+'.stop()');

            code.push(text_name+' = StoppableThread(target=timer_func_' + text_name + ', args=())')
            //console.log("DO:", block.childBlocks_[0].getProcedureCall());
        } else {
            code.push('if '+text_name+' is not None:\n    '+text_name+'.stop()');
            code.push(text_name+' = StoppableThread(target=timer_func_' + text_name + ', args=())')
        }
    }
    else if ( block.childBlocks_.length == 2) {

        if (block.childBlocks_[1].type == "procedures_callnoreturn") {
            code.push('if '+text_name+' is not None:\n    '+text_name+'.stop()');
            code.push(text_name+' = StoppableThread(target=timer_func_' + text_name + ', args=())')

        }
        else {
            code.push('if '+text_name+' is not None:\n    '+text_name+'.stop()');
            code.push(text_name+' = StoppableThread(target=timer_func_' + text_name + ', args=())')

        }
    }
    else {
        code.push('if '+text_name+' is not None:\n    '+text_name+'.stop()');
        code.push(text_name+' = StoppableThread(target=timer_func_' + text_name + ', args=())')

    }
    //code.push('timers["' + text_name + '"] = StoppableThread(target=timer_func_' + text_name + ', args=())')

    if ( start_type != "n") {
        code.push(text_name+'.start()');
    }
    //code.push(Blockly.Python.statementToCode(block, 'DO') || Blockly.Python.PASS);
    return code.join("\n") + "\n";
};
Blockly.Python.INDENT = '    ';
Blockly.Python['kosmos_list_next'] = function (block) {
    //const name = Blockly.Python.variableDB_.getName(block.getFieldValue('LISTNAME'), Blockly.Variables.NAME_TYPE);
    const name = block.getFieldValue('LISTDROP');


    return [name + '.next()', 0];
};
Blockly.Python['kosmos_list_prev'] = function (block) {
    const name = block.getFieldValue('LISTDROP');
    //const name = Blockly.Python.variableDB_.getName(block.getFieldValue('LISTNAME'), Blockly.Variables.NAME_TYPE);

    return [name + '.prev()', 0];
};
Blockly.Python['kosmos_list_curr'] = function (block) {
    const name = block.getFieldValue('LISTDROP');
    //const name = Blockly.Python.variableDB_.getName(block.getFieldValue('LISTNAME'), Blockly.Variables.NAME_TYPE);

    return [name + '.curr()', 0];
};
Blockly.Python['kosmos_list_last'] = function (block) {
    const name = block.getFieldValue('LISTDROP');
    //const name = Blockly.Python.variableDB_.getName(block.getFieldValue('LISTNAME'), Blockly.Variables.NAME_TYPE);

    return [name + '.last()', 0];
};
Blockly.Python['kosmos_list_first'] = function (block) {
    const name = block.getFieldValue('LISTDROP');
    //const name = Blockly.Python.variableDB_.getName(block.getFieldValue('LISTNAME'), Blockly.Variables.NAME_TYPE);

    return [name + '.first()', 0];
};
Blockly.Python['kosmos_list_add'] = function (block) {
    const name = block.getFieldValue('LISTDROP');
    var value_name = Blockly.Python.valueToCode(block, 'ITEM', Blockly.Python.ORDER_ATOMIC);

    //const name = Blockly.Python.variableDB_.getName(block.getFieldValue('LISTNAME'), Blockly.Variables.NAME_TYPE);

    return name + '.add(' + value_name + ')\n';
};
Blockly.Python['member'] = function (block) {
    const name = block.getFieldValue('MEMBER_NAME');
    const value = Blockly.Python.valueToCode(block, 'MEMBER_VALUE',
        0);
    const code = '\'' + name + '\': ' + value + ',\n';
    return code;
}
;
Blockly.Python['member2'] = function (block) {
    const name = Blockly.Python.valueToCode(block, 'MEMBER_NAME', 0)
    const value = Blockly.Python.valueToCode(block, 'MEMBER_VALUE',
        0);
    const code = name + ': ' + value + ',\n';
    return code;
}

Blockly.Python['object'] = function (block) {
    const statement_members =
        Blockly.Python.statementToCode(block, 'MEMBERS');
    const code = '{\n' + statement_members + '}';
    return [code, 0];
};
Blockly.Python['kosmos_trigger_on_change'] = function (block) {
    const uuid = block.getFieldValue('UUID');

    return kosmos.Code.createListener(block, uuid, null, null).join('\n') + "\n";
};
Blockly.Python['kosmos_trigger_on_change_all'] = function (block) {



    return kosmos.Code.createListener(block, null, null, "value").join('\n') + "\n";
};
Blockly.Python['kosmos_trigger_on_property_change'] = function (block) {
    var property = block.getFieldValue('PROPERTY');
    var uuid = block.getFieldValue('UUID');
    var varname = Blockly.Python.variableDB_.getName(block.getFieldValue('VARNAME'), Blockly.Variables.NAME_TYPE);
    /*var statements_on_event = Blockly.Python.statementToCode(block, 'on_event');


    var listenerid = kosmos.nextListenerId();
    var code = '', branchCode, conditionCode;
    if (Blockly.Python.STATEMENT_PREFIX) {
        // Automatic prefix insertion is switched off for this block.  Add manually.
        code += Blockly.Python.injectId(Blockly.Python.STATEMENT_PREFIX, block);
    }
    var variables = Blockly.Variables.allUsedVarModels(Code.workspace);
    var globals = "home"
    for (var i = 0; i < variables.length; i++) {
        console.log(variables[i]);
        if ( variables[i].name !=varname ) {
            globals = globals +", "+variables[i].name;
        }

    }
    branchCode = Blockly.Python.INDENT + varname + ' = home.get_value(\'' + uuid + '\', \'' + property + '\')\n' + ((Blockly.Python.statementToCode(block, 'DO') || Blockly.Python.PASS));
    code += '\n\ndef listener' + listenerid + '(uuid, key):\n' + Blockly.Python.INDENT + 'global '+globals+'\n' + Blockly.Python.INDENT + 'if uuid != \'' + uuid + '\' or key != \'' + property + '\':\n' + Blockly.Python.INDENT + Blockly.Python.INDENT + 'return\n' + branchCode + '\n\nhome.subscribe(listener' + listenerid + ', \'onChange_' + uuid + '_' + property + '\')\n';
    */
    //return code;
    return kosmos.Code.createListener(block, uuid, property, varname).join('\n') + "\n";

};
Blockly.Python['kosmos_context_trigger_on_shortpress'] = function (block) {

    var uuid = block.getFieldValue('UUID');
    var varname = Blockly.Python.variableDB_.getName(block.getFieldValue('VARNAME'), Blockly.Variables.NAME_TYPE);
    return kosmos.Code.createListener(block, uuid, 'shortpress', varname).join('\n') + "\n";

};
Blockly.Python['kosmos_context_trigger_on_shortpress2'] = function (block) {

    var uuid = block.getFieldValue('UUID');

    return kosmos.Code.createListener(block, uuid, 'shortpress', null,"if transValue != None:\n"+Blockly.Python.INDENT+Blockly.Python.INDENT+"if transValue is False: return\n"+Blockly.Python.INDENT+"else:\n"+Blockly.Python.INDENT+Blockly.Python.INDENT+"if home.get_value(uuid,property) is False: return").join('\n') + "\n";

};
Blockly.Python['kosmos_context_trigger_on_shortpress3'] = function (block) {

    var uuid = block.getFieldValue('UUID');

    return kosmos.Code.createListener(block, "*", 'shortpress', null,"if transValue != None:\n"+Blockly.Python.INDENT+Blockly.Python.INDENT+"if transValue is False: return\n"+Blockly.Python.INDENT+"else:\n"+Blockly.Python.INDENT+Blockly.Python.INDENT+"if home.get_value(uuid,property) is False: return").join('\n') + "\n";

};
Blockly.Python['kosmos_context_trigger_on_longpress'] = function (block) {

    var uuid = block.getFieldValue('UUID');
    var varname = Blockly.Python.variableDB_.getName(block.getFieldValue('VARNAME'), Blockly.Variables.NAME_TYPE);
    return kosmos.Code.createListener(block, uuid, 'longpress', varname).join('\n') + "\n";

};
Blockly.Python['kosmos_context_trigger_on_longpress2'] = function (block) {

    var uuid = block.getFieldValue('UUID');
    return kosmos.Code.createListener(block, uuid, 'longpress', null,"if transValue != None:\n"+Blockly.Python.INDENT+Blockly.Python.INDENT+"if transValue is False: return\n"+Blockly.Python.INDENT+"else:\n"+Blockly.Python.INDENT+Blockly.Python.INDENT+"if home.get_value(uuid,property) is False: return").join('\n') + "\n";


};
Blockly.Python['kosmos_trigger_on_gesture'] = function (block) {

    var uuid = block.getFieldValue('UUID');
    var varname = Blockly.Python.variableDB_.getName(block.getFieldValue('VARNAME'), Blockly.Variables.NAME_TYPE);
    //return kosmos.Code.createListener(block, uuid, 'gesture', varname).join('\n') + "\n";
    return kosmos.Code.createListener(block, uuid, 'gesture', varname,"if transValue != None:\n"+Blockly.Python.INDENT+Blockly.Python.INDENT+"if transValue == '0': return\n"+Blockly.Python.INDENT+"else:\n"+Blockly.Python.INDENT+Blockly.Python.INDENT+"if home.get_value(uuid,property) == '0': return").join('\n') + "\n";

};
Blockly.Python['kosmos_trigger_on_gesture2'] = function (block) {

    var uuid = block.getFieldValue('UUID');
    var value = block.getFieldValue('VALUE');

    //return kosmos.Code.createListener(block, uuid, 'gesture', varname).join('\n') + "\n";
    return kosmos.Code.createListener(block, uuid, 'gesture', null,"if transValue != None:\n"+Blockly.Python.INDENT+Blockly.Python.INDENT+"if transValue.lower() != '"+value.toLowerCase()+"': return\n"+Blockly.Python.INDENT+"else:\n"+Blockly.Python.INDENT+Blockly.Python.INDENT+"if home.get_value(uuid,property).lower() != '"+value.toLowerCase()+"': return").join('\n') + "\n";

};
Blockly.Python['kosmos_change_property'] = function (block) {
    const property = block.getFieldValue('PROPERTY');
    const uuid = block.getFieldValue('UUID');
    const variable_varname = Blockly.Python.variableDB_.getName(block.getFieldValue('VARNAME'), Blockly.Variables.NAME_TYPE);
    //var statements_on_event = Blockly.Python.statementToCode(block, 'on_event');
    // TODO: Assemble Python into code variable.

    //const listenerid = kosmos.nextListenerId();
    //let code = '', branchCode, conditionCode;
    //if (Blockly.Python.STATEMENT_PREFIX) {
    // Automatic prefix insertion is switched off for this block.  Add manually.
    //    code += Blockly.Python.injectId(Blockly.Python.STATEMENT_PREFIX, block);
    //}
    //const variables = Blockly.Variables.allUsedVarModels(Code.workspace);
    //let globals = "home"
    //for (var i = 0; i < variables.length; i++) {
    //    globals = globals+", "+Blockly.Python.variableDB_.getName(variables[i].getId());
    //}
    //branchCode = Blockly.Python.INDENT + variable_varname + ' = home.get_value(\'' + uuid + '\',\'' + property + '\')\n' + ((Blockly.Python.statementToCode(block, 'DO') || Blockly.Python.PASS));
    //code += '\n\ndef listener' + listenerid + '(uuid, key):\n' + Blockly.Python.INDENT + 'global '+globals+'\n' + Blockly.Python.INDENT + 'if uuid != \'' + uuid + '\' or key != \'' + property + '\':\n' + Blockly.Python.INDENT + Blockly.Python.INDENT + 'return\n' + branchCode + '\n\nhome.subscribe(listener' + listenerid + ', \'onChange_' + uuid + '_' + property + '\')\n';
    return kosmos.Code.createListener(block, uuid, property, variable_varname).join('\n') + "\n";
};

Blockly.Python['kosmos_sleep'] = function(block) {
    var number_value = block.getFieldValue('VALUE');
    // TODO: Assemble Python into code variable.
    var code = 'time.sleep('+number_value+')\n';
    return code;
};
Blockly.Python['kosmos_connect'] = function (block) {
    return '';
    /*const text_username = block.getFieldValue('USERNAME');
    const text_password = block.getFieldValue('PASSWORD');
    // TODO: Assemble Python into code variable.
    const code = 'home = kosmos(\'' + text_username + '\', \'' + text_password + '\')\n' +
        'home.startConnecting()\n';
    return code;*/
};

Blockly.Python['kosmos_get_value'] = function (block) {
    const uuid = block.getFieldValue('UUID');
    const property = block.getFieldValue('PROPERTY');

    const code = kosmos.Code.getValue('\'' + uuid + '\'', '\'' + property + '\'')

    return [code, Blockly.Python.ORDER_ATOMIC];
};
Blockly.Python['kosmos_get_uuid'] = function (block) {
    const uuid = block.getFieldValue('UUID');


    const code = '\'' + uuid + '\''

    return [code, Blockly.Python.ORDER_ATOMIC];
};
Blockly.Python['kosmos_get_name'] = function (block) {
    const uuid = block.getFieldValue('UUID');


    const code = "home.get_name('"+uuid+"')";

    return [code, Blockly.Python.ORDER_ATOMIC];
};
Blockly.Python['kosmos_get_name_var'] = function (block) {
    const uuid = Blockly.Python.valueToCode(block, 'UUID', Blockly.Python.ORDER_ATOMIC);


    const code = "home.get_name("+uuid+")";

    return [code, Blockly.Python.ORDER_ATOMIC];
};
Blockly.Python['kosmos_get_value2'] = function (block) {
    const uuid = Blockly.Python.valueToCode(block, 'UUID', Blockly.Python.ORDER_ATOMIC);
    const property = Blockly.Python.valueToCode(block, 'PROPERTY', Blockly.Python.ORDER_ATOMIC);

    const code = kosmos.Code.getValue(uuid, property);

    return [code, Blockly.Python.ORDER_ATOMIC];
};
Blockly.Python['kosmos_get_value3'] = function (block) {
    const uuid = block.getFieldValue('UUID');
    const property = block.getFieldValue('PROPERTY');

    const code = kosmos.Code.getValue('\'' + uuid + '\'', '\'' + property + '\'')

    return [code, Blockly.Python.ORDER_ATOMIC];
};


Blockly.Python['kosmos_set_value'] = function (block) {
    const uuid = block.getFieldValue('UUID');
    const property = block.getFieldValue('PROPERTY');
    const input = Blockly.Python.valueToCode(block, 'INPUT', Blockly.Python.ORDER_ATOMIC);
    //var code = 'home.set_value(\''+uuid+'\', \''+property+'\', '+input+')\n';
    return kosmos.Code.setValue('\'' + uuid + '\'', '\'' + property + '\'', input);
};
Blockly.Python['kosmos_change_var'] = function (block) {
    const uuid = Blockly.Python.valueToCode(block, 'UUID', Blockly.Python.ORDER_ATOMIC);
    const property = Blockly.Python.valueToCode(block, 'PROPERTY', Blockly.Python.ORDER_ATOMIC);
    const type = block.getFieldValue('TYPE');
    const input = Blockly.Python.valueToCode(block, 'INPUT', Blockly.Python.ORDER_ATOMIC);
    return kosmos.Code.changeValue( uuid , property ,type,input);
};
Blockly.Python['kosmos_change'] = function (block) {
    const uuid = block.getFieldValue('UUID');
    const property = block.getFieldValue('PROPERTY');
    const type = block.getFieldValue('TYPE');
    const input = Blockly.Python.valueToCode(block, 'INPUT', Blockly.Python.ORDER_ATOMIC);

    return kosmos.Code.changeValue('\'' + uuid + '\'', '\'' + property + '\'',type,input);
};
Blockly.Python['kosmos_change_temp'] = function (block) {
    const uuid = block.getFieldValue('UUID');
    const property = block.getFieldValue('PROPERTY');
    const type = block.getFieldValue('TYPE');
    const input = Blockly.Python.valueToCode(block, 'INPUT', Blockly.Python.ORDER_ATOMIC);
    return kosmos.Code.changeValue('\'' + uuid + '\'', '\'' + property + '\'',type,input);
};
Blockly.Python['kosmos_change_temp_var'] = function (block) {
    const uuid = Blockly.Python.valueToCode(block, 'UUID', Blockly.Python.ORDER_ATOMIC);
    const property = Blockly.Python.valueToCode(block, 'PROPERTY', Blockly.Python.ORDER_ATOMIC);
    const type = block.getFieldValue('TYPE');
    const input = Blockly.Python.valueToCode(block, 'INPUT', Blockly.Python.ORDER_ATOMIC);
    return kosmos.Code.changeValue( uuid , property ,type,input);
};
Blockly.Python['kosmos_set_value3'] = function (block) {
    const uuid = block.getFieldValue('UUID');
    const property = block.getFieldValue('PROPERTY');
    const input = Blockly.Python.valueToCode(block, 'INPUT', Blockly.Python.ORDER_ATOMIC);
    //var code = 'home.set_value(\''+uuid+'\', \''+property+'\', '+input+')\n';
    return kosmos.Code.setValue('\'' + uuid + '\'', '\'' + property + '\'', input);
};
Blockly.Python['kosmos_set_value2'] = function (block) {
    const uuid = Blockly.Python.valueToCode(block, 'UUID', Blockly.Python.ORDER_ATOMIC);
    const property = Blockly.Python.valueToCode(block, 'PROPERTY', Blockly.Python.ORDER_ATOMIC);
    const input = Blockly.Python.valueToCode(block, 'INPUT', Blockly.Python.ORDER_ATOMIC);
    //var code = 'home.set_value('+uuid+', '+property+', '+input+')\n';
    return kosmos.Code.setValue(uuid, property, input);
};

Blockly.Python['kosmos_turn_off'] = function (block) {
    const uuid = block.getFieldValue('UUID');




    return 'home.turn_on(\'' + uuid + '\',"false")\n';
};
Blockly.Python['kosmos_turn_on'] = function (block) {
    const uuid = block.getFieldValue('UUID');

    //var code = 'home.set_device(\''+uuid+'\', '+input+')\n';
    console.log("selected_option_:",block.getField("UUID").selectedOption_);

    return 'home.turn_on(\'' + uuid + '\',"true")\n';
};
Blockly.Python['kosmos_turn_on_var'] = function (block) {
    //const uuid =  kosmos.varNames[block.getFieldValue("UUID")];
    const uuid = Blockly.Python.valueToCode(block, 'UUID', Blockly.Python.ORDER_ATOMIC);

    return 'home.turn_on(' + uuid + ',"true")\n';
};

Blockly.Python['kosmos_turn_off_var'] = function (block) {
    //console.log("turn off ",block.getInput("UUID").fieldRow[0].value_);
    const uuid =  Blockly.Python.valueToCode(block, 'UUID', Blockly.Python.ORDER_ATOMIC);
    /*for ( let i= 0;i<block.inputList.length;i++) {
        console.log("input off",block.inputList[i]);
    }*/
    /*const uuid =  block.getInput("UUID");
    console.log('uuid off,',uuid);
    console.log('uuid off,',block.getInput("UUID"));*/
    //return 'home.turn_on(' + block.getInput("UUID").fieldRow[0].value_ + ',"false")\n';
    return 'home.turn_on(' + uuid + ',"false")\n';
};
Blockly.Python['kosmos_set_color'] = function (block) {
    const uuid = block.getFieldValue('UUID');
    const color = Blockly.Python.valueToCode(block, 'COLOR', Blockly.Python.ORDER_ATOMIC);
    //var code = 'home.set_device(\''+uuid+'\', '+input+')\n';


    return 'home.set_color(\'' + uuid + '\',' + color + ')\n';
};
Blockly.Python['kosmos_set_name'] = function (block) {
    const uuid = block.getFieldValue('UUID');
    const name = Blockly.Python.valueToCode(block, 'NAME', Blockly.Python.ORDER_ATOMIC);
    //var code = 'home.set_device(\''+uuid+'\', '+input+')\n';


    return 'home.set_name(\'' + uuid + '\',' + name + ')\n';
};
Blockly.Python['kosmos_set_color_text'] = function (block) {
    const uuid = block.getFieldValue('UUID');
    const color = Blockly.Python.valueToCode(block, 'COLOR', Blockly.Python.ORDER_ATOMIC);
    const text = Blockly.Python.valueToCode(block, 'TEXT', Blockly.Python.ORDER_ATOMIC);

    //var code = 'home.set_device(\''+uuid+'\', '+input+')\n';


    return 'home.set_color_text(\'' + uuid + '\',' + color + ','+text+')\n';
};
Blockly.Python['kosmos_set_color_text_var'] = function (block) {
    const uuid =  Blockly.Python.valueToCode(block, 'UUID', Blockly.Python.ORDER_ATOMIC);
    const color = Blockly.Python.valueToCode(block, 'COLOR', Blockly.Python.ORDER_ATOMIC);
    const text = Blockly.Python.valueToCode(block, 'TEXT', Blockly.Python.ORDER_ATOMIC);

    //var code = 'home.set_device(\''+uuid+'\', '+input+')\n';


    return 'home.set_color_text(' + uuid + ',' + color + ','+text+')\n';
};
Blockly.Python['kosmos_set_text_var'] = function (block) {
    const uuid =  Blockly.Python.valueToCode(block, 'UUID', Blockly.Python.ORDER_ATOMIC);
    const text = Blockly.Python.valueToCode(block, 'TEXT', Blockly.Python.ORDER_ATOMIC);

    //var code = 'home.set_device(\''+uuid+'\', '+input+')\n';


    return 'home.set_text(' + uuid + ','+text+')\n';
};
Blockly.Python['kosmos_set_text'] = function (block) {
    const uuid = block.getFieldValue('UUID');

    const text = Blockly.Python.valueToCode(block, 'TEXT', Blockly.Python.ORDER_ATOMIC);

    //var code = 'home.set_device(\''+uuid+'\', '+input+')\n';


    return 'home.set_text(' + uuid + ','+text+')\n';
};
Blockly.Python['kosmos_set_color_var'] = function (block) {
    //const uuid =  Blockly.Python.valueToCode(block, 'UUID', Blockly.Python.ORDER_ATOMIC);
    const uuid = Blockly.Python.valueToCode(block, 'UUID', Blockly.Python.ORDER_ATOMIC);
    const color = Blockly.Python.valueToCode(block, 'COLOR', Blockly.Python.ORDER_ATOMIC);

    return 'home.set_color(' + uuid + ',' + color + ')\n';
};
Blockly.Python['kosmos_set_color_brightness'] = function (block) {
    const uuid = block.getFieldValue('UUID');

    const color = Blockly.Python.valueToCode(block, 'COLOR', Blockly.Python.ORDER_ATOMIC);
    const brightness = Blockly.Python.valueToCode(block, 'BRIGHTNESS', Blockly.Python.ORDER_ATOMIC);
    //var code = 'home.set_device(\''+uuid+'\', '+input+')\n';


    return 'home.set_color(\'' + uuid + '\',' + color + ',' + brightness + ')\n';
};

Blockly.Python['kosmos_set_color_brightness_var'] = function (block) {
    const uuid = Blockly.Python.valueToCode(block, 'UUID', Blockly.Python.ORDER_ATOMIC);

    const color = Blockly.Python.valueToCode(block, 'COLOR', Blockly.Python.ORDER_ATOMIC);
    const brightness = Blockly.Python.valueToCode(block, 'BRIGHTNESS', Blockly.Python.ORDER_ATOMIC);
    //var code = 'home.set_device(\''+uuid+'\', '+input+')\n';


    return 'home.set_color(' + uuid + ',' + color + ',' + brightness + ')\n';
};
Blockly.Python['kosmos_set_device'] = function (block) {
    const uuid = block.getFieldValue('UUID');
    const input = Blockly.Python.valueToCode(block, 'INPUT', Blockly.Python.ORDER_ATOMIC);
    //var code = 'home.set_device(\''+uuid+'\', '+input+')\n';


    return kosmos.Code.setDevice('\'' + uuid + '\'', input);
};
Blockly.Python['kosmos_set_device2'] = function (block) {
    const uuid = Blockly.Python.valueToCode(block, 'UUID', Blockly.Python.ORDER_ATOMIC);
    const input = Blockly.Python.valueToCode(block, 'INPUT', Blockly.Python.ORDER_ATOMIC);


    //var code = 'home.set_device('+uuid+', '+input+')\n';


    return kosmos.Code.setDevice(uuid, input);
};
Blockly.Python['kosmos_load_variable'] = function (block) {
    const varname = Blockly.Python.valueToCode(block, 'VARNAME', Blockly.Python.ORDER_ATOMIC);
    const deflt = Blockly.Python.valueToCode(block, 'DEFAULT', Blockly.Python.ORDER_ATOMIC);


    return varname + ' = home.load_variable(\'' + varname + '\',\'' + deflt + '\')\n';


    //return kosmos.Code.setDevice(uuid,input);
};
Blockly.Python['kosmos_set_device3'] = function (block) {
    const uuid = block.getFieldValue('UUID');
    const input = Blockly.Python.valueToCode(block, 'INPUT', Blockly.Python.ORDER_ATOMIC);

    return kosmos.Code.setDevice('\'' + uuid + '\'', input);
};
Blockly.Python['kosmos_function'] = function (block) {
    const name = block.getFieldValue('NAME');
    const params = Blockly.JavaScript.valueToCode(block, 'PARAMS', Blockly.Python.ORDER_NONE);
    const p = []
    const cb = block.getInput('PARAMS').fieldRow[2].sourceBlock_.childBlocks_[0].childBlocks_;
    for (let i = 0; i < cb.length; i++) {
        p.push(cb[i].inputList[0].fieldRow[1].value_);
    }


    console.log(p);

    return kosmos.Code.createFunction(block, name, p).join('\n');
};
/*
Blockly.Python['kosmos_timed_block'] = function (block) {
    var number_time = block.getFieldValue('TIME');
    var text_name = block.getFieldValue('NAME');
    var statements_do = Blockly.Python.statementToCode(block, 'DO');
    // TODO: Assemble Python into code variable.
    var code = '...\n';
    return code;
};*/
Blockly.Python['kosmos_clamp_int'] = function(block) {
    var variable_varname = Blockly.Python.variableDB_.getName(block.getFieldValue('VARNAME'), Blockly.Variables.NAME_TYPE);
    var value_min = Blockly.Python.valueToCode(block, 'MIN', Blockly.Python.ORDER_ATOMIC);
    var value_max = Blockly.Python.valueToCode(block, 'MAX', Blockly.Python.ORDER_ATOMIC);
    var value_step = Blockly.Python.valueToCode(block, 'STEP', Blockly.Python.ORDER_ATOMIC);
    var value_value = Blockly.Python.valueToCode(block, 'VALUE', Blockly.Python.ORDER_ATOMIC);
    // TODO: Assemble Python into code variable.
    var code = variable_varname+" = KosmosClampInt("+value_min+","+value_max+","+value_step+","+value_value+")\n";
    return code;
};

Blockly.Python['variables_get'] = function(block) {

    return [kosmos.varNames[block.getFieldValue("VAR")],0];

}

Blockly.Python['variables_set'] = function(block) {
    // Variable setter.

    var argument0 = Blockly.Python.valueToCode(block, 'VALUE',
        Blockly.Python.ORDER_NONE) || '0';
    let varName = "";
    /*try {
    varName = Blockly.Python.variableDB_.getName(block.getFieldValue('VAR'),
        Blockly.VARIABLE_CATEGORY_NAME);
    } catch (e) {
        //return "#something broke, sorry";*/

        varName =  kosmos.varNames[block.getFieldValue("VAR")];


    //}
    const list =kosmos.getForbiddenVarNames();
    for ( let i=0;i<list.length;i++) {
        if ( list[i][0] == varName) {
            const block = list[i][1];
            if ( block.type == "kosmos_clamp_int" || block.type == "kosmos_clamp_float") {
                return varName+".set("+argument0+")\n";
            }
            if ( block.type == "kosmos_lists_create_with" || block.type == "kosmos_ringlists_create_with") {
                return varName+".set_index("+argument0+")\n";
            }
            return "# DONT MANUALLY SET THE VARIABLE "+varName+" TO SOMETHING PLEASE!\n"
        }

    }
    return varName + ' = ' + argument0 + '\n';


};
Blockly.Python['kosmos_clamp_increase2'] = function (block) {
    const name = block.getFieldValue('CLAMPDROP');
    //const name = Blockly.Python.variableDB_.getName(block.getFieldValue('LISTNAME'), Blockly.Variables.NAME_TYPE);
    var value_amount = Blockly.Python.valueToCode(block, 'AMOUNT', Blockly.Python.ORDER_ATOMIC);
    return [name + '.increase('+value_amount+')', 0];
};
Blockly.Python['kosmos_clamp_increase'] = function (block) {
    const name = block.getFieldValue('CLAMPDROP');
    //const name = Blockly.Python.variableDB_.getName(block.getFieldValue('LISTNAME'), Blockly.Variables.NAME_TYPE);
    var value_amount = Blockly.Python.valueToCode(block, 'AMOUNT', Blockly.Python.ORDER_ATOMIC);
    return name + '.increase('+value_amount+')\n';
};
Blockly.Python['kosmos_clamp_decrease4'] = function (block) {
    const name = block.getFieldValue('CLAMPDROP');
    //const name = Blockly.Python.variableDB_.getName(block.getFieldValue('LISTNAME'), Blockly.Variables.NAME_TYPE);

    return [name + '.decrease()', 0];
};
Blockly.Python['kosmos_clamp_decrease3'] = function (block) {
    const name = block.getFieldValue('CLAMPDROP');
    //const name = Blockly.Python.variableDB_.getName(block.getFieldValue('LISTNAME'), Blockly.Variables.NAME_TYPE);

    return name + '.decrease()\n';
};

Blockly.Python['kosmos_clamp_increase2'] = function (block) {
    const name = block.getFieldValue('CLAMPDROP');
    //const name = Blockly.Python.variableDB_.getName(block.getFieldValue('LISTNAME'), Blockly.Variables.NAME_TYPE);
    var value_amount = Blockly.Python.valueToCode(block, 'AMOUNT', Blockly.Python.ORDER_ATOMIC);
    return [name + '.decrease('+value_amount+')', 0];
};
Blockly.Python['kosmos_clamp_decrease'] = function (block) {
    const name = block.getFieldValue('CLAMPDROP');
    //const name = Blockly.Python.variableDB_.getName(block.getFieldValue('LISTNAME'), Blockly.Variables.NAME_TYPE);
    var value_amount = Blockly.Python.valueToCode(block, 'AMOUNT', Blockly.Python.ORDER_ATOMIC);
    return name + '.decrease('+value_amount+')\n';
};
Blockly.Python['kosmos_clamp_decrease4'] = function (block) {
    const name = block.getFieldValue('CLAMPDROP');
    //const name = Blockly.Python.variableDB_.getName(block.getFieldValue('LISTNAME'), Blockly.Variables.NAME_TYPE);

    return [name + '.decrease()', 0];
};
Blockly.Python['kosmos_clamp_increase3'] = function (block) {
    const name = block.getFieldValue('CLAMPDROP');
    //const name = Blockly.Python.variableDB_.getName(block.getFieldValue('LISTNAME'), Blockly.Variables.NAME_TYPE);

    return name + '.increase()\n';
};
Blockly.Python['kosmos_clamp_curr'] = function (block) {
    const name = block.getFieldValue('CLAMPDROP');
    //const name = Blockly.Python.variableDB_.getName(block.getFieldValue('LISTNAME'), Blockly.Variables.NAME_TYPE);

    return [name + '.current()', 0];
};
Blockly.Python['kosmos_regex'] = function(block) {
    var text_regex = block.getFieldValue('regex');
    var value_input = Blockly.Python.valueToCode(block, 'INPUT', Blockly.Python.ORDER_ATOMIC);
    code = [];
    code.push("reg = re.compile('"+text_regex+"', re.IGNORECASE)");
    code.push("match = reg.match("+value_input+")");
    code.push("if match is not None:");

    // TODO: Assemble Python into code variable.
    code.push(Blockly.Python.statementToCode(block, 'DO') || Blockly.Python.PASS);
    return  code.join("\n") + "\n";
};
Blockly.Python['kosmos_create_wordlist'] = function (block) {
    // Create a list with any number of elements of any type.
    var elements = new Array(block.itemCount_);
    for (var i = 0; i < block.itemCount_; i++) {
        elements[i] = Blockly.Python.valueToCode(block, 'ADD' + i,
            Blockly.Python.ORDER_NONE).replaceAll("'","") || 'None';
    }
    var code = '(' + elements.join('| ') + ')';



    return code;
};
Blockly.Python['kosmos_create_named_wordlist'] = function (block) {
    // Create a list with any number of elements of any type.
    var elements = new Array(block.itemCount_);
    for (var i = 0; i < block.itemCount_; i++) {
        elements[i] = Blockly.Python.valueToCode(block, 'ADD' + i,
            Blockly.Python.ORDER_NONE).replaceAll("'","") || 'None';
    }
    var code = '(?P<'+Blockly.Variables.getVariable(Code.workspace, block.getFieldValue("VARNAME")).name+">" + elements.join('|') + ')';



    return code;
};
Blockly.Python['kosmos_word_match'] = function (block) {
    //var text_regex = block.getFieldValue('regex');
    //var value_input = Blockly.Python.valueToCode(block, 'INPUT', Blockly.Python.ORDER_ATOMIC);
    code = [];
    var elements = new Array(block.itemCount_);
    reg = [];
    vars = [];
    reg.push("reg = re.compile('");
    for (let i = 0; i < block.itemCount_; i++) {

        const b = block.getInput("ADD"+i);
        try {
            const v = Blockly.Variables.getVariable(Code.workspace, b.getFieldValue("VARNAME")).name;
            if (v ) {
                vars.push(v)
            }
        } catch (e) {

        }
        let c = Blockly.Python.statementToCode(block, "ADD"+i).replaceAll(Blockly.Python.INDENT,"");
        reg.push(c)
        reg.push(" ")

    }
    reg.push("', re.IGNORECASE)")

    code.push(reg.join(""));
    code.push("match = reg.match('...')")
    code.push("if match is not None:")
    for ( let i = 0;i<vars.length;i++) {
        code.push(Blockly.Python.INDENT+""+vars[i]+"=match.group('"+vars[i]+"')")
    }

    // TODO: Assemble Python into code variable.
    code.push(Blockly.Python.statementToCode(block, 'DO') || Blockly.Python.PASS);
    return  code.join("\n") + "\n";
};
Blockly.Python['kosmos_get_uuid_patch'] = function (block) {
    return Blockly.Python['kosmos_get_uuid'](block);
};
Blockly.Python['kosmos_get_uuid_heating'] = function (block) {
    return Blockly.Python['kosmos_get_uuid'](block);
};
Blockly.Python['kosmos_get_uuid_on'] = function (block) {
    return Blockly.Python['kosmos_get_uuid'](block);
};
Blockly.Python['kosmos_get_uuid_color'] = function (block) {
    return Blockly.Python['kosmos_get_uuid'](block);
};

Blockly.Python['context_heating_patch_1'] = function(block) {
    var value_patch = Blockly.Python.valueToCode(block, 'PATCH', Blockly.Python.ORDER_ATOMIC);
    var value_devices = Blockly.Python.valueToCode(block, 'DEVICES', Blockly.Python.ORDER_ATOMIC);
    var value_min = Blockly.Python.valueToCode(block, 'MIN', Blockly.Python.ORDER_ATOMIC);
    var value_max = Blockly.Python.valueToCode(block, 'MAX', Blockly.Python.ORDER_ATOMIC);
    // TODO: Assemble Python into code variable.
    var code = '...\n';
    return code;
};
Blockly.Python['context_heating_patch_2'] = function(block) {
    var dropdown_patch = block.getFieldValue('PATCH');
    var value_devices = Blockly.Python.valueToCode(block, 'DEVICE', Blockly.Python.ORDER_ATOMIC);

    // TODO: Assemble Python into code variable.
    var code = '...\n';
    return code;
};
Blockly.Python['context_color_patch_1'] = function(block) {
    var value_patch = Blockly.Python.valueToCode(block, 'PATCH', Blockly.Python.ORDER_ATOMIC);
    var value_device = Blockly.Python.valueToCode(block, 'DEVICE', Blockly.Python.ORDER_ATOMIC);

    // TODO: Assemble Python into code variable.
    var code = '...\n';
    return code;
};
Blockly.Python['context_color_patch_2'] = function(block) {
    var dropdown_patch = block.getFieldValue('PATCH');
    var value_devices = Blockly.Python.valueToCode(block, 'DEVICES', Blockly.Python.ORDER_ATOMIC);
    var value_min = Blockly.Python.valueToCode(block, 'MIN', Blockly.Python.ORDER_ATOMIC);
    var value_max = Blockly.Python.valueToCode(block, 'MAX', Blockly.Python.ORDER_ATOMIC);
    // TODO: Assemble Python into code variable.
    var code = '...\n';
    return code;
};
Blockly.Python['patch_heating'] = function(block) {
    var dropdown_uuid = block.getFieldValue('UUID');
    var checkbox_1 = block.getFieldValue('1') == 'TRUE';
    var checkbox_2 = block.getFieldValue('2') == 'TRUE';
    var checkbox_3 = block.getFieldValue('3') == 'TRUE';
    var checkbox_4 = block.getFieldValue('4') == 'TRUE';
    var checkbox_5 = block.getFieldValue('5') == 'TRUE';
    var number_min = block.getFieldValue('min');
    var number_max = block.getFieldValue('max');
    // TODO: Assemble Python into code variable.
    var code = '...\n';
    return code;
};
