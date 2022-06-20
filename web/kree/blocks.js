
if (typeof document === 'undefined') {
    let k = require('./code');
    kosmos = k.kosmos

}

Blockly.Blocks['kosmos_create_device'] = {
    init: function () {
        this.appendDummyInput()
            .appendField("create Device")
            .appendField(new Blockly.FieldTextInput("name", kosmos.findLegalUUID), "UUID")
            .appendField(new Blockly.FieldDropdown([["RGB Light", "LIGHT_RGB"], ["Dimmer Light", "LIGHT_DIM"], ["Toggle Light", "LIGHT_TOGGLE"], ["Motion Sensor", "SENSOR_MOTION"], ["Temperature Sensor", "SENSOR_TEMPERATURE"], ["Temperature Control", "CLIMATE_TEMPERATURE"]]), "TYPE");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
    }
};
Blockly.Blocks['kosmos_list_prev'] = {
    init: function () {
        this.appendDummyInput()
            .appendField("get prev item from")
            //.appendField(new Blockly.FieldLabelSerializable(""), "LISTNAME");
            .appendField(new Blockly.FieldDropdown(
                kosmos.getListNames(this)), 'LISTDROP');
        this.setOutput(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");


    }
};
Blockly.Blocks['kosmos_list_next'] = {
    init: function () {

        this.appendDummyInput()
            .appendField("get next item from")
            //.appendField(new Blockly.FieldLabelSerializable(""), "LISTNAME");
            .appendField(new Blockly.FieldDropdown(
                kosmos.getListNames(this)), 'LISTDROP');
        this.setOutput(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");


    }
};
Blockly.Blocks['kosmos_stop_timer'] = {
    init: function () {

        this.appendDummyInput()
            .appendField("stop timer")
            .appendField(new Blockly.FieldLabelSerializable(""), "TIMERNAME");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
    }
};
Blockly.Blocks['kosmos_restart_timer'] = {
    init: function () {

        this.appendDummyInput()
            .appendField("restart timer")
            .appendField(new Blockly.FieldLabelSerializable(""), "TIMERNAME");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
    }
};

Blockly.Blocks['kosmos_lists_create_with'] = {
    /**
     * Block for creating a list with any number of elements of any type.
     * @this {Blockly.Block}
     */
    init: function () {
        this.appendDummyInput()
            .appendField("name")
            .appendField(new Blockly.FieldVariable("list1"), "VARNAME");
        this.setHelpUrl(Blockly.Msg['LISTS_CREATE_WITH_HELPURL']);
        this.setStyle('list_blocks');
        this.itemCount_ = 3;
        this.updateShape_();
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setMutator(new Blockly.Mutator(['lists_create_with_item']));
        this.setTooltip(Blockly.Msg['LISTS_CREATE_WITH_TOOLTIP']);
    },
    /**
     * Create XML to represent list inputs.
     * @return {!Element} XML storage element.
     * @this {Blockly.Block}
     */
    mutationToDom: function () {
        return kosmos.listMutationToDom(this)
    },
    /**
     * Parse XML to restore the list inputs.
     * @param {!Element} xmlElement XML storage element.
     * @this {Blockly.Block}
     */
    domToMutation: function (xmlElement) {
        return kosmos.listDomToMutation(this, xmlElement);
    },
    /**
     * Populate the mutator's dialog with this block's components.
     * @param {!Blockly.Workspace} workspace Mutator's workspace.
     * @return {!Blockly.Block} Root block in mutator.
     * @this {Blockly.Block}
     */
    decompose: function (workspace) {
        return kosmos.listDecompose(this, workspace);
    },
    /**
     * Reconfigure this block based on the mutator dialog's components.
     * @param {!Blockly.Block} containerBlock Root block in mutator.
     * @this {Blockly.Block}
     */
    compose: function (containerBlock) {
        return kosmos.listCompose(this, containerBlock);
    },
    /**
     * Store pointers to any connected child blocks.
     * @param {!Blockly.Block} containerBlock Root block in mutator.
     * @this {Blockly.Block}
     */
    saveConnections: function (containerBlock) {
        return kosmos.listSaveConnections(this, containerBlock);
    },
    /**
     * Modify this block to have the correct number of inputs.
     * @private
     * @this {Blockly.Block}
     */
    updateShape_: function () {
        return kosmos.listUpdateShape_(this);

    },
    onchange: function (event) {
        return kosmos.listOnChange(this, event);

    }
};
Blockly.Blocks['kosmos_ringlists_create_with'] = {
    /**
     * Block for creating a list with any number of elements of any type.
     * @this {Blockly.Block}
     */
    init: function () {
        this.itemCount_ = 3;
        this.appendDummyInput()
            .appendField("name")
            .appendField(new Blockly.FieldVariable("list1"), "VARNAME");
        this.appendDummyInput()
            .appendField("max entries")
            .appendField(new Blockly.FieldNumber(this.itemCount_, 1, 50), "MAX");
        this.setHelpUrl(Blockly.Msg['LISTS_CREATE_WITH_HELPURL']);
        this.setStyle('list_blocks');

        this.updateShape_();
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setMutator(new Blockly.Mutator(['lists_create_with_item']));
        this.setTooltip(Blockly.Msg['LISTS_CREATE_WITH_TOOLTIP']);
    },
    /**
     * Create XML to represent list inputs.
     * @return {!Element} XML storage element.
     * @this {Blockly.Block}
     */
    mutationToDom: function () {
        return kosmos.listMutationToDom(this)
    },
    /**
     * Parse XML to restore the list inputs.
     * @param {!Element} xmlElement XML storage element.
     * @this {Blockly.Block}
     */
    domToMutation: function (xmlElement) {
        return kosmos.listDomToMutation(this, xmlElement);
    },
    /**
     * Populate the mutator's dialog with this block's components.
     * @param {!Blockly.Workspace} workspace Mutator's workspace.
     * @return {!Blockly.Block} Root block in mutator.
     * @this {Blockly.Block}
     */
    decompose: function (workspace) {
        return kosmos.listDecompose(this, workspace);
    },
    /**
     * Reconfigure this block based on the mutator dialog's components.
     * @param {!Blockly.Block} containerBlock Root block in mutator.
     * @this {Blockly.Block}
     */
    compose: function (containerBlock) {
        return kosmos.listCompose(this, containerBlock);
    },
    /**
     * Store pointers to any connected child blocks.
     * @param {!Blockly.Block} containerBlock Root block in mutator.
     * @this {Blockly.Block}
     */
    saveConnections: function (containerBlock) {
        return kosmos.listSaveConnections(this, containerBlock);
    },
    /**
     * Modify this block to have the correct number of inputs.
     * @private
     * @this {Blockly.Block}
     */
    updateShape_: function () {
        return kosmos.listUpdateShape_(this);

    },
    onchange: function (event) {
//        console.log(event);
        return kosmos.listOnChange(this, event);

    }
};
Blockly.Python['text_print'] = function (block) {
    // Print statement.
    var msg = Blockly.Python.valueToCode(block, 'TEXT',
        Blockly.Python.ORDER_NONE) || '\'\'';
    return 'home.send_log(' + msg + ')\n';
};
Blockly.Blocks['kosmos_word_match'] = {
    /**
     * Block for creating a list with any number of elements of any type.
     * @this {Blockly.Block}
     */
    init: function () {
        this.appendValueInput("INPUT").appendField(Blockly.Msg.INPUT).setAlign(Blockly.ALIGN_RIGHT);
        //this.appendDummyInput().setAlign(Blockly.ALIGN_RIGHT);
        this.setHelpUrl(Blockly.Msg['LISTS_CREATE_WITH_HELPURL']);
        this.setStyle('list_blocks');
        this.itemCount_ = 10;
        this.updateShape_();

        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setMutator(new Blockly.Mutator(['lists_create_with_item']));
        this.setTooltip(Blockly.Msg['LISTS_CREATE_WITH_TOOLTIP']);

    },
    /**
     * Create XML to represent list inputs.
     * @return {!Element} XML storage element.
     * @this {Blockly.Block}
     */
    mutationToDom: function () {
        return kosmos.listMutationToDom(this)
    },
    /**
     * Parse XML to restore the list inputs.
     * @param {!Element} xmlElement XML storage element.
     * @this {Blockly.Block}
     */
    domToMutation: function (xmlElement) {
        return kosmos.listDomToMutation(this, xmlElement);
    },
    /**
     * Populate the mutator's dialog with this block's components.
     * @param {!Blockly.Workspace} workspace Mutator's workspace.
     * @return {!Blockly.Block} Root block in mutator.
     * @this {Blockly.Block}
     */
    decompose: function (workspace) {
        return kosmos.listDecompose(this, workspace);
    },
    /**
     * Reconfigure this block based on the mutator dialog's components.
     * @param {!Blockly.Block} containerBlock Root block in mutator.
     * @this {Blockly.Block}
     */
    compose: function (containerBlock) {
        return kosmos.listCompose(this, containerBlock);
    },
    /**
     * Store pointers to any connected child blocks.
     * @param {!Blockly.Block} containerBlock Root block in mutator.
     * @this {Blockly.Block}
     */
    saveConnections: function (containerBlock) {
        return kosmos.listSaveConnections(this, containerBlock);
    },
    /**
     * Modify this block to have the correct number of inputs.
     * @private
     * @this {Blockly.Block}
     */
    updateShape_: function () {
        return kosmos.listUpdateShape_(this);

    },
    onchange: function (event) {
        return kosmos.listOnChange(this, event);

    }
};

Blockly.Blocks['kosmos_create_wordlist'] = {
    /**
     * Block for creating a list with any number of elements of any type.
     * @this {Blockly.Block}
     */
    init: function () {

        this.setHelpUrl(Blockly.Msg['LISTS_CREATE_WITH_HELPURL']);
        this.setStyle('list_blocks');
        this.itemCount_ = 3;
        this.updateShape_();
        this.setOutput(true);

        this.setMutator(new Blockly.Mutator(['lists_create_with_item']));
        this.setTooltip(Blockly.Msg['LISTS_CREATE_WITH_TOOLTIP']);
    },
    /**
     * Create XML to represent list inputs.
     * @return {!Element} XML storage element.
     * @this {Blockly.Block}
     */
    mutationToDom: function () {
        return kosmos.listMutationToDom(this)
    },
    /**
     * Parse XML to restore the list inputs.
     * @param {!Element} xmlElement XML storage element.
     * @this {Blockly.Block}
     */
    domToMutation: function (xmlElement) {
        return kosmos.listDomToMutation(this, xmlElement);
    },
    /**
     * Populate the mutator's dialog with this block's components.
     * @param {!Blockly.Workspace} workspace Mutator's workspace.
     * @return {!Blockly.Block} Root block in mutator.
     * @this {Blockly.Block}
     */
    decompose: function (workspace) {
        return kosmos.listDecompose(this, workspace);
    },
    /**
     * Reconfigure this block based on the mutator dialog's components.
     * @param {!Blockly.Block} containerBlock Root block in mutator.
     * @this {Blockly.Block}
     */
    compose: function (containerBlock) {
        return kosmos.listCompose(this, containerBlock);
    },
    /**
     * Store pointers to any connected child blocks.
     * @param {!Blockly.Block} containerBlock Root block in mutator.
     * @this {Blockly.Block}
     */
    saveConnections: function (containerBlock) {
        return kosmos.listSaveConnections(this, containerBlock);
    },
    /**
     * Modify this block to have the correct number of inputs.
     * @private
     * @this {Blockly.Block}
     */
    updateShape_: function () {
        return kosmos.listUpdateShape_(this);

    },
    onchange: function (event) {
        return kosmos.listOnChange(this, event);

    }
};
Blockly.Blocks['kosmos_create_named_wordlist'] = {
    /**
     * Block for creating a list with any number of elements of any type.
     * @this {Blockly.Block}
     */
    init: function () {
        this.appendDummyInput()
            .appendField("name")
            .appendField(new Blockly.FieldVariable("name"), "VARNAME");
        this.setHelpUrl(Blockly.Msg['LISTS_CREATE_WITH_HELPURL']);
        this.setStyle('list_blocks');
        this.itemCount_ = 3;
        this.updateShape_();
        this.setOutput(true);
        this.setMutator(new Blockly.Mutator(['lists_create_with_item']));
        this.setTooltip(Blockly.Msg['LISTS_CREATE_WITH_TOOLTIP']);
    },
    /**
     * Create XML to represent list inputs.
     * @return {!Element} XML storage element.
     * @this {Blockly.Block}
     */
    mutationToDom: function () {
        return kosmos.listMutationToDom(this)
    },
    /**
     * Parse XML to restore the list inputs.
     * @param {!Element} xmlElement XML storage element.
     * @this {Blockly.Block}
     */
    domToMutation: function (xmlElement) {
        return kosmos.listDomToMutation(this, xmlElement);
    },
    /**
     * Populate the mutator's dialog with this block's components.
     * @param {!Blockly.Workspace} workspace Mutator's workspace.
     * @return {!Blockly.Block} Root block in mutator.
     * @this {Blockly.Block}
     */
    decompose: function (workspace) {
        return kosmos.listDecompose(this, workspace);
    },
    /**
     * Reconfigure this block based on the mutator dialog's components.
     * @param {!Blockly.Block} containerBlock Root block in mutator.
     * @this {Blockly.Block}
     */
    compose: function (containerBlock) {
        return kosmos.listCompose(this, containerBlock);
    },
    /**
     * Store pointers to any connected child blocks.
     * @param {!Blockly.Block} containerBlock Root block in mutator.
     * @this {Blockly.Block}
     */
    saveConnections: function (containerBlock) {
        return kosmos.listSaveConnections(this, containerBlock);
    },
    /**
     * Modify this block to have the correct number of inputs.
     * @private
     * @this {Blockly.Block}
     */
    updateShape_: function () {
        return kosmos.listUpdateShape_(this);

    },
    onchange: function (event) {
        return kosmos.listOnChange(this, event);

    }
};
Blockly.Blocks['kosmos_create_timer'] = {
    init: function () {
        kosmos.timerblocks.push(this);
        this.appendDummyInput()
            .appendField("create delayed execution");
        this.appendDummyInput()
            .appendField("name")
            .appendField(new Blockly.FieldVariable("timer1"), "VARNAME");
        this.appendDummyInput()
            .appendField("delay")
            .appendField(new Blockly.FieldNumber(1, 1), "DELAY")
            .appendField("seconds");
        this.appendDummyInput()
            .appendField("repeat")
            .appendField(new Blockly.FieldNumber(0, 0), "REPEAT")
            .appendField("times");
        this.appendStatementInput("DO")
            .setCheck(null)
            .appendField("do");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
    },
    onchange: function (event) {
        console.log(event);
        if (event instanceof Blockly.Events.Change) {
            console.log("onc finished with ", this.getFieldValue("VARNAME"));
            const list = Code.workspace.getAllBlocks();

            for (let i = 0; i < list.length; i++) {
                const block = list[i];
                console.log(block);
                //console.log(block.inputList);
                const fieldname = "TIMERNAME";
                const input = block.getField(fieldname);
                if (input) {
                    const bname = block.getFieldValue(fieldname);
                    if (bname == event.oldValue) {
                        block.setFieldValue(event.newValue, fieldname);
                    }
                }
            }

        }
    }
};
Blockly.Blocks['kosmos_create_timer_2'] = {
    init: function () {
        kosmos.timerblocks.push(this);
        this.appendDummyInput()
            .appendField("create delayed execution");
        this.appendDummyInput()
            .appendField("name")
            .appendField(new Blockly.FieldVariable("timer1"), "VARNAME");
        this.appendDummyInput()
            .appendField("delay")
            .appendField(new Blockly.FieldNumber(1, 1), "DELAY")
            .appendField("seconds");
        this.appendDummyInput()
            .appendField("repeat")
            .appendField(new Blockly.FieldNumber(0, 0), "REPEAT")
            .appendField("times");
        this.appendDummyInput()
            .appendField("start now")
            .appendField(new Blockly.FieldDropdown(Blockly.Msg.KOSMOS_TIMER_START_TYPE), "START_TYPE")

        this.appendStatementInput("DO")
            .setCheck(null)
            .appendField("do");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
    },
    onchange: function (event) {
        console.log(event);
        if (event instanceof Blockly.Events.Change) {
            console.log("onc finished with ", this.getFieldValue("VARNAME"));
            const list = Code.workspace.getAllBlocks();

            for (let i = 0; i < list.length; i++) {
                const block = list[i];
                console.log(block);
                //console.log(block.inputList);
                const fieldname = "TIMERNAME";
                const input = block.getField(fieldname);
                if (input) {
                    const bname = block.getFieldValue(fieldname);
                    if (bname == event.oldValue) {
                        block.setFieldValue(event.newValue, fieldname);
                    }
                }
            }

        }
    }
};
Blockly.Blocks['kosmos_delay'] = {
    init: function () {
        this.appendDummyInput()
            .appendField("delay")
            .appendField(new Blockly.FieldNumber(1, 0.05, 60), "DELAY")
            .appendField("seconds");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
    }
};
/*
Blockly.Blocks['kosmos_timed_block'] = {

    init: function () {
        const id = kosmos.nextTimerId();

        this.appendDummyInput()
            .appendField("execute after")
            .appendField(new Blockly.FieldNumber(60, 1, 3600), "TIME")
            .appendField("seconds")
            .appendField(new Blockly.FieldTextInput("timer" + id), "NAME");
        this.appendStatementInput("DO")
            .setCheck(null);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
    }
};*/
//Blockly.Blocks['colour_picker'].getField('COLOUR'setVali).dator(kosmos.colorValidator);
Blockly.Blocks['kosmos_trigger_on_change'] = {
    init: function () {
        this.appendDummyInput()
            .appendField(Blockly.Msg.TRIGGER_ON_DEVICE_CHANGE)
            .appendField(Blockly.Msg.DEVICE)
            .appendField(new Blockly.FieldDropdown(
                kosmos.getUuidList), 'UUID');
        this.setNextStatement(true, null);
        this.setPreviousStatement(true, null);
        this.setColour(kosmos.colors.trigger);
        this.setTooltip(Blockly.Msg.TRIGGER_ON_DEVICE_CHANGE_TOOLTIP);
        this.setHelpUrl("");
    }
};
Blockly.Blocks['kosmos_check_if_command'] = {
    init: function () {
        this.appendValueInput("VALUE")
            .appendField("check if it is device command")
            .appendField(Blockly.Msg.VALUE)
        this.appendStatementInput("DO")
            .setCheck(null);
        this.setNextStatement(true, null);
        this.setPreviousStatement(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.TRIGGER_ON_DEVICE_CHANGE_TOOLTIP);
        this.setHelpUrl("");
    }
};

Blockly.Blocks['kosmos_trigger_on_change_all'] = {
    init: function () {
        this.appendDummyInput()
            .appendField(Blockly.Msg.TRIGGER_ON_DEVICE_CHANGE);
        this.appendDummyInput().appendField(Blockly.Msg.SAVE_UUID_AS)
            .appendField(new Blockly.FieldVariable("uuid", kosmos.nullValidator), "VARUUID");
        this.appendDummyInput().appendField(Blockly.Msg.SAVE_PROPERTY_AS)
            .appendField(new Blockly.FieldVariable("property", kosmos.nullValidator), "VARKEY");
        this.appendDummyInput().appendField(Blockly.Msg.SAVE_VALUE_AS)
            .appendField(new Blockly.FieldVariable("value", kosmos.nullValidator), "VARNAME");
        this.appendStatementInput("DO")
            .setCheck(null);
        this.setNextStatement(true, null);
        this.setPreviousStatement(true, null);
        this.setColour(kosmos.colors.trigger);
        this.setTooltip(Blockly.Msg.TRIGGER_ON_DEVICE_CHANGE_TOOLTIP);
        this.setHelpUrl("");
    }
};
Blockly.Blocks['kosmos_function'] = {
    init: function () {
        this.appendValueInput('PARAMS')
            .appendField(Blockly.Msg.CREATE_FUNCTION)
            .appendField(Blockly.Msg.NAME)
            .appendField(new Blockly.FieldTextInput("name"), 'NAME');
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);


        this.setTooltip(Blockly.Msg.TRIGGER_ON_DEVICE_CHANGE_TOOLTIP);
        this.setHelpUrl("");
    }
};

Blockly.Blocks['kosmos_change_property'] = {
    init: function () {
        const id = kosmos.nextTriggerId();

        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getUuidList, kosmos.changedUuid)
        const propdrop = new Blockly.FieldDropdown(kosmos.states[id])
        this.appendValueInput("STATE")
            .appendField(Blockly.Msg.TRIGGER_ON_PROPERTY_CHANGE)
            .appendField(Blockly.Msg.DEVICE)
            .appendField(uuiddrop, 'UUID')
            .appendField(Blockly.Msg.PROPERTY)
            .appendField(propdrop, "PROPERTY");

        this.appendStatementInput("DO")
            .setCheck(null);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(kosmos.colors.trigger);
        this.setTooltip(Blockly.Msg.TRIGGER_ON_PROPERTY_CHANGE_TOOLTIP);
        this.setHelpUrl("");
        kosmos.uuidPropertyBlocks.push({"block": this, "prop": propdrop, "uuid": uuiddrop})
    }
};

Blockly.Blocks['kosmos_trigger_on_property_change'] = {
    init: function () {

        const id = kosmos.nextTriggerId();
        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getUuidList, kosmos.changedUuid)
        const propdrop = new Blockly.FieldDropdown(kosmos.states[id])
        this.appendDummyInput()
            .appendField(Blockly.Msg.TRIGGER_ON_PROPERTY_CHANGE)
            .appendField(Blockly.Msg.DEVICE)
            .appendField(uuiddrop, 'UUID')
            .appendField(Blockly.Msg.PROPERTY)
            .appendField(propdrop, "PROPERTY").appendField(Blockly.Msg.SAVE_AS)
            .appendField(new Blockly.FieldVariable("value"), "VARNAME");

        this.appendStatementInput("DO")
            .setCheck(null);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(kosmos.colors.trigger);
        this.setTooltip(Blockly.Msg.TRIGGER_ON_PROPERTY_CHANGE_TOOLTIP);
        this.setHelpUrl("");
        kosmos.uuidPropertyBlocks.push({"id": id, "block": this, "prop": propdrop, "uuid": uuiddrop})
    }
};
Blockly.Blocks['kosmos_context_trigger_on_shortpress'] = {
    init: function () {

        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getShortpressList)
        this.appendDummyInput()
            .appendField(Blockly.Msg.TRIGGER_ON_SHORTPRESS)
            .appendField(Blockly.Msg.DEVICE)
            .appendField(uuiddrop, 'UUID').appendField(Blockly.Msg.SAVE_AS)
            .appendField(new Blockly.FieldVariable("value"), "VARNAME");

        this.appendStatementInput("DO")
            .setCheck(null);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(kosmos.colors.trigger);
        this.setTooltip(Blockly.Msg.TRIGGER_ON_SHORTPRESS_TOOLTIP);
        this.setHelpUrl("");

    }
};
Blockly.Blocks['kosmos_context_trigger_on_shortpress3'] = {
    init: function () {

        this.appendDummyInput()
            .appendField(Blockly.Msg.TRIGGER_ON_SHORTPRESS)
            .appendField(Blockly.Msg.SAVE_AS)
            .appendField(new Blockly.FieldVariable("value"), "VARNAME");

        this.appendStatementInput("DO")
            .setCheck(null);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(kosmos.colors.trigger);
        this.setTooltip(Blockly.Msg.TRIGGER_ON_SHORTPRESS_TOOLTIP);
        this.setHelpUrl("");

    }
};
Blockly.Blocks['kosmos_context_trigger_on_shortpress2'] = {
    init: function () {

        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getShortpressList)
        this.appendDummyInput()
            .appendField(Blockly.Msg.TRIGGER_ON_SHORTPRESS2)
            .appendField(Blockly.Msg.DEVICE)
            .appendField(uuiddrop, 'UUID');

        this.appendStatementInput("DO")
            .setCheck(null);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(kosmos.colors.trigger);
        this.setTooltip(Blockly.Msg.TRIGGER_ON_SHORTPRESS2_TOOLTIP);
        this.setHelpUrl("");

    }
};
Blockly.Blocks['kosmos_context_trigger_on_longpress'] = {
    init: function () {

        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getLongpressList)
        this.appendDummyInput()
            .appendField(Blockly.Msg.TRIGGER_ON_LONGPRESS)
            .appendField(Blockly.Msg.DEVICE)
            .appendField(uuiddrop, 'UUID').appendField(Blockly.Msg.SAVE_AS)
            .appendField(new Blockly.FieldVariable("value"), "VARNAME");

        this.appendStatementInput("DO")
            .setCheck(null);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(kosmos.colors.trigger);
        this.setTooltip(Blockly.Msg.TRIGGER_ON_PROPERTY_CHANGE_TOOLTIP);
        this.setHelpUrl("");

    }
};
Blockly.Blocks['kosmos_context_trigger_on_longpress2'] = {
    init: function () {

        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getLongpressList)
        this.appendDummyInput()
            .appendField(Blockly.Msg.TRIGGER_ON_LONGPRESS2)
            .appendField(Blockly.Msg.DEVICE)
            .appendField(uuiddrop, 'UUID')

        this.appendStatementInput("DO")
            .setCheck(null);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(kosmos.colors.trigger);
        this.setTooltip(Blockly.Msg.TRIGGER_ON_LONGPRESS2_TOOLTIP);
        this.setHelpUrl("");

    }
};
Blockly.Blocks['kosmos_trigger_on_gesture'] = {
    init: function () {
        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getGestureList)
        this.appendDummyInput()
            .appendField(Blockly.Msg.TRIGGER_ON_GESTURE)
            .appendField(Blockly.Msg.DEVICE)
            .appendField(uuiddrop, 'UUID').appendField(Blockly.Msg.SAVE_AS)

            .appendField(new Blockly.FieldVariable("value"), "VARNAME");

        this.appendStatementInput("DO")
            .setCheck(null);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(kosmos.colors.trigger);
        this.setTooltip(Blockly.Msg.TRIGGER_ON_PROPERTY_CHANGE_TOOLTIP);
        this.setHelpUrl("");

    }
};
Blockly.Blocks['kosmos_trigger_on_gesture2'] = {
    init: function () {
        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getGestureList)
        this.appendDummyInput()
            .appendField(Blockly.Msg.TRIGGER_ON_GESTURE)
            .appendField(Blockly.Msg.DEVICE)
            .appendField(uuiddrop, 'UUID')
            .appendField(new Blockly.FieldDropdown(kosmos.getGestureTypes), 'VALUE');

        this.appendStatementInput("DO")
            .setCheck(null);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(kosmos.colors.trigger);
        this.setTooltip(Blockly.Msg.TRIGGER_ON_PROPERTY_CHANGE_TOOLTIP);
        this.setHelpUrl("");

    }
};
Blockly.Blocks['kosmos_get_value3'] = {
    init: function () {

        this.appendDummyInput()
            .appendField(Blockly.Msg.KOSMOS_GET_VALUE)
            .appendField(Blockly.Msg.DEVICE)
            .appendField(new Blockly.FieldTextInput("uuid"), 'UUID')
            .appendField(Blockly.Msg.PROPERTY)
            .appendField(new Blockly.FieldTextInput("property"), "PROPERTY");
        this.setOutput(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_GET_VALUE_TOOLTIP);
        this.setCommentText(Blockly.Msg.KOSMOS_GET_VALUE_TOOLTIP);
        this.setHelpUrl("");


    }
};
Blockly.Blocks['kosmos_get_value2'] = {
    init: function () {

        this.appendDummyInput()
            .appendField(Blockly.Msg.KOSMOS_GET_VALUE);
        this.appendValueInput("UUID")
            .appendField(Blockly.Msg.DEVICE);
        this.appendValueInput("PROPERTY")
            .appendField(Blockly.Msg.PROPERTY);

        this.setOutput(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_GET_VALUE_TOOLTIP);

        this.setHelpUrl("");

    }
};
Blockly.Blocks['kosmos_get_uuid'] = {
    init: function () {
        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getUuidList)

        this.appendDummyInput()
            .appendField(Blockly.Msg.KOSMOS_GET_UUID)
            .appendField(Blockly.Msg.DEVICE)
            .appendField(uuiddrop, 'UUID').setAlign(Blockly.ALIGN_RIGHT);

        this.setOutput(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_GET_UUID_TOOLTIP);

        this.setHelpUrl("");


    }
};
Blockly.Blocks['kosmos_get_name'] = {
    init: function () {
        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getUuidList)

        this.appendDummyInput()
            .appendField(Blockly.Msg.KOSMOS_GET_NAME)
            .appendField(Blockly.Msg.DEVICE)
            .appendField(uuiddrop, 'UUID').setAlign(Blockly.ALIGN_RIGHT);

        this.setOutput(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_GET_UUID_TOOLTIP);

        this.setHelpUrl("");


    }
};
Blockly.Blocks['kosmos_get_name_var'] = {
    init: function () {
        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getUuidList)

        this.appendValueInput("UUID")
            .appendField(Blockly.Msg.KOSMOS_GET_NAME)
            .appendField(Blockly.Msg.DEVICE);

        this.setOutput(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_GET_UUID_TOOLTIP);

        this.setHelpUrl("");


    }
};
Blockly.Blocks['kosmos_get_value'] = {
    init: function () {
        const id = kosmos.nextTriggerId();
        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getUuidList, kosmos.changedUuid)
        const propdrop = new Blockly.FieldDropdown(kosmos.states[id])
        this.appendDummyInput()
            .appendField(Blockly.Msg.KOSMOS_GET_VALUE)
            .appendField(Blockly.Msg.DEVICE)
            .appendField(uuiddrop, 'UUID')
            .appendField(Blockly.Msg.PROPERTY)
            .appendField(propdrop, "PROPERTY").setAlign(Blockly.ALIGN_RIGHT);
        this.setOutput(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_GET_VALUE_TOOLTIP);
        this.setCommentText(Blockly.Msg.KOSMOS_GET_VALUE_TOOLTIP);
        this.setHelpUrl("");
        kosmos.uuidPropertyBlocks.push({"id": id, "block": this, "prop": propdrop, "uuid": uuiddrop})

    }
};
Blockly.Blocks['kosmos_set_device2'] = {
    init: function () {


        this.appendDummyInput().appendField(Blockly.Msg.KOSMOS_SET_VALUE);
        this.appendValueInput("UUID")

            .appendField(Blockly.Msg.DEVICE).setAlign(Blockly.ALIGN_RIGHT)
        ;
        this.appendValueInput("INPUT").appendField(Blockly.Msg.TO).setAlign(Blockly.ALIGN_RIGHT);
        this.setPreviousStatement(true, null);
        //this.setInputsInline(true);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_SET_VALUE_DEVICE_TOOLTIP);
        this.setHelpUrl("");
        this.setCommentText(Blockly.Msg.KOSMOS_SET_VALUE_DEVICE_TOOLTIP);


    }
};
Blockly.Blocks['kosmos_set_device3'] = {
    init: function () {


        this.appendValueInput("INPUT")
            .appendField(Blockly.Msg.KOSMOS_SET_VALUE)
            .appendField(Blockly.Msg.DEVICE)
            .appendField(new Blockly.FieldTextInput("uuid"), 'UUID').appendField(Blockly.Msg.TO).setAlign(Blockly.ALIGN_RIGHT);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_SET_VALUE_DEVICE_TOOLTIP);
        this.setHelpUrl("");
        this.setCommentText(Blockly.Msg.KOSMOS_SET_VALUE_DEVICE_TOOLTIP);


    }
};

Blockly.Blocks['kosmos_set_device'] = {
    init: function () {

        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getUuidList)

        this.appendValueInput("INPUT")
            .appendField(Blockly.Msg.KOSMOS_SET_VALUE)
            .appendField(Blockly.Msg.DEVICE)
            .appendField(uuiddrop, 'UUID').appendField(Blockly.Msg.TO).setAlign(Blockly.ALIGN_RIGHT);

        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_SET_VALUE_DEVICE_TOOLTIP);
        this.setHelpUrl("");
        this.setCommentText(Blockly.Msg.KOSMOS_SET_VALUE_DEVICE_TOOLTIP);


    }
};
Blockly.Blocks['kosmos_turn_off'] = {
    init: function () {

        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getOnList)

        this.appendDummyInput()
            .appendField(Blockly.Msg.KOSMOS_TURN_OFF)
            .appendField(Blockly.Msg.DEVICE)
            .appendField(uuiddrop, 'UUID');
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        this.setHelpUrl("");
        this.setCommentText(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        //this.setWarningText(Blockly.Msg.KOSMOS_SELECT_DEVICE_FIRST);

    }
};
Blockly.Blocks['kosmos_turn_on'] = {
    init: function () {

        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getOnList)

        this.appendDummyInput()
            .appendField(Blockly.Msg.KOSMOS_TURN_ON)
            .appendField(Blockly.Msg.DEVICE)
            .appendField(uuiddrop, 'UUID');
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        this.setHelpUrl("");
        this.setCommentText(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        //this.setWarningText(Blockly.Msg.KOSMOS_SELECT_DEVICE_FIRST);

    }
};
Blockly.Blocks['kosmos_turn_off_var'] = {
    init: function () {

        this.appendDummyInput()
            .appendField(Blockly.Msg.KOSMOS_TURN_OFF)
        ;
        this.appendValueInput("UUID").appendField(Blockly.Msg.DEVICE);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        this.setHelpUrl("");
        this.setCommentText(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        //this.setWarningText(Blockly.Msg.KOSMOS_SELECT_DEVICE_FIRST);

    }
};
Blockly.Blocks['kosmos_turn_on_var'] = {
    init: function () {


        this.appendDummyInput()
            .appendField(Blockly.Msg.KOSMOS_TURN_ON);
        this.appendValueInput("UUID").appendField(Blockly.Msg.DEVICE);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        this.setHelpUrl("");
        this.setCommentText(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        //this.setWarningText(Blockly.Msg.KOSMOS_SELECT_DEVICE_FIRST);

    }
};
Blockly.Blocks['kosmos_set_color'] = {
    init: function () {

        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getColorList)

        this.appendValueInput("COLOR")
            .appendField(Blockly.Msg.KOSMOS_SET_COLOR)
            .appendField(Blockly.Msg.DEVICE)
            .appendField(uuiddrop, 'UUID').appendField(Blockly.Msg.COLOR).setAlign(Blockly.ALIGN_RIGHT);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        this.setHelpUrl("");
        this.setCommentText(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        //this.setWarningText(Blockly.Msg.KOSMOS_SELECT_DEVICE_FIRST);

    }
};
Blockly.Blocks['kosmos_set_name'] = {
    init: function () {

        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getUuidList)

        this.appendValueInput("NAME")
            .appendField(Blockly.Msg.KOSMOS_SET_NAME)
            .appendField(Blockly.Msg.DEVICE)
            .appendField(uuiddrop, 'UUID').appendField(Blockly.Msg.NAME).setAlign(Blockly.ALIGN_RIGHT);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        //this.setTooltip(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        this.setHelpUrl("");
        //this.setCommentText(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        //this.setWarningText(Blockly.Msg.KOSMOS_SELECT_DEVICE_FIRST);

    }
};
Blockly.Blocks['kosmos_set_color_text'] = {
    init: function () {

        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getColorList)

        this.appendValueInput("COLOR")
            .appendField(Blockly.Msg.KOSMOS_SET_COLOR_TEXT)
            .appendField(Blockly.Msg.DEVICE)
            .appendField(uuiddrop, 'UUID').appendField(Blockly.Msg.COLOR).setAlign(Blockly.ALIGN_RIGHT);
        this.appendValueInput("TEXT").appendField(Blockly.Msg.KOSMOS_TEXT);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        this.setHelpUrl("");
        this.setCommentText(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        //this.setWarningText(Blockly.Msg.KOSMOS_SELECT_DEVICE_FIRST);

    }
};
Blockly.Blocks['kosmos_set_color_text_var'] = {
    init: function () {

        this.appendDummyInput().appendField(Blockly.Msg.KOSMOS_SET_COLOR_TEXT)
        this.appendValueInput("UUID").appendField(Blockly.Msg.DEVICE);

        this.appendValueInput("COLOR")
            .appendField(Blockly.Msg.COLOR).setAlign(Blockly.ALIGN_RIGHT);
        this.appendValueInput("TEXT").appendField(Blockly.Msg.KOSMOS_TEXT);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        this.setHelpUrl("");
        this.setCommentText(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        //this.setWarningText(Blockly.Msg.KOSMOS_SELECT_DEVICE_FIRST);

    }
};
Blockly.Blocks['kosmos_set_text'] = {
    init: function () {

        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getColorList)

        this.appendDummyInput()
            .appendField(Blockly.Msg.KOSMOS_SET_TEXT)
            .appendField(Blockly.Msg.DEVICE)
            .appendField(uuiddrop, 'UUID').appendField(Blockly.Msg.COLOR).setAlign(Blockly.ALIGN_RIGHT);
        this.appendValueInput("TEXT").appendField(Blockly.Msg.KOSMOS_TEXT).setAlign(Blockly.ALIGN_RIGHT);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        this.setHelpUrl("");
        this.setCommentText(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        //this.setWarningText(Blockly.Msg.KOSMOS_SELECT_DEVICE_FIRST);

    }
};
Blockly.Blocks['kosmos_set_text_var'] = {
    init: function () {

        this.appendDummyInput().appendField(Blockly.Msg.KOSMOS_SET_TEXT)
        this.appendValueInput("UUID").appendField(Blockly.Msg.DEVICE).setAlign(Blockly.ALIGN_RIGHT);
        this.appendValueInput("TEXT").appendField(Blockly.Msg.KOSMOS_TEXT).setAlign(Blockly.ALIGN_RIGHT);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        this.setHelpUrl("");
        this.setCommentText(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        //this.setWarningText(Blockly.Msg.KOSMOS_SELECT_DEVICE_FIRST);

    }
};
Blockly.Blocks['kosmos_change_var'] = {
    init: function () {


        this.appendDummyInput().appendField(Blockly.Msg.KOSMOS_CHANGE_VALUE);
        this.appendValueInput("UUID").appendField(Blockly.Msg.DEVICE);
        this.appendValueInput("PROPERTY").appendField(Blockly.Msg.PROPERTY);
        this.appendValueInput("VALUE").appendField(Blockly.Msg.COLOR).setAlign(Blockly.ALIGN_RIGHT);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        this.setHelpUrl("");
        this.setCommentText(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        //this.setWarningText(Blockly.Msg.KOSMOS_SELECT_DEVICE_FIRST);

    }
};
Blockly.Blocks['kosmos_change_temp_var'] = {
    init: function () {
        this.appendDummyInput().appendField(Blockly.Msg.KOSMOS_CHANGE_TEMP);
        this.appendValueInput("UUID").appendField(Blockly.Msg.DEVICE);
        this.appendValueInput("PROPERTY").appendField(Blockly.Msg.PROPERTY);
        this.appendDummyInput().appendField(new Blockly.FieldDropdown(Blockly.Msg.KOSMOS_CHANGE_TYPE), "TYPE")

        this.appendValueInput("INPUT").setCheck("Number").appendField(Blockly.Msg.BY).setAlign(Blockly.ALIGN_RIGHT);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);


        //this.setWarningText(Blockly.Msg.KOSMOS_SELECT_DEVICE_FIRST);
    }
};
Blockly.Blocks['kosmos_change_var'] = {
    init: function () {
        this.appendDummyInput().appendField(Blockly.Msg.KOSMOS_CHANGE_VALUE);
        this.appendValueInput("UUID").appendField(Blockly.Msg.DEVICE);
        this.appendValueInput("PROPERTY").appendField(Blockly.Msg.PROPERTY);
        this.appendDummyInput().appendField(new Blockly.FieldDropdown(Blockly.Msg.KOSMOS_CHANGE_TYPE), "TYPE")

        this.appendValueInput("INPUT").setCheck("Number").appendField(Blockly.Msg.BY).setAlign(Blockly.ALIGN_RIGHT);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        this.setHelpUrl("");
        this.setCommentText(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);


        //this.setWarningText(Blockly.Msg.KOSMOS_SELECT_DEVICE_FIRST);
    }
};
Blockly.Blocks['kosmos_change'] = {
    init: function () {
        const id = kosmos.nextTriggerId();
        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getUuidList, kosmos.changedUuid)
        const propdrop = new Blockly.FieldDropdown(kosmos.getStates("id"))
        this.appendValueInput("INPUT").setCheck("Number")
            .appendField(Blockly.Msg.KOSMOS_CHANGE_VALUE)
            .appendField(Blockly.Msg.UUID)
            .appendField(uuiddrop, 'UUID')
            .appendField(Blockly.Msg.PROPERTY)
            .appendField(propdrop, "PROPERTY")
            .appendField(new Blockly.FieldDropdown(Blockly.Msg.KOSMOS_CHANGE_TYPE), "TYPE")

            .appendField(Blockly.Msg.BY).setAlign(Blockly.ALIGN_RIGHT);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_SET_VALUE_TOOLTIP);
        this.setHelpUrl("");
        kosmos.uuidPropertyBlocks.push({"id": id, "block": this, "prop": propdrop, "uuid": uuiddrop})


        //this.setWarningText(Blockly.Msg.KOSMOS_SELECT_DEVICE_FIRST);
    }
};
Blockly.Blocks['kosmos_change_temp'] = {
    init: function () {
        const id = kosmos.nextTriggerId();
        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getTemperatureList(), kosmos.changedUuid)
        const propdrop = new Blockly.FieldDropdown(kosmos.getStates("id"))
        this.appendValueInput("INPUT").setCheck("Number")
            .appendField(Blockly.Msg.KOSMOS_CHANGE_TEMP)
            .appendField(Blockly.Msg.UUID)
            .appendField(uuiddrop, 'UUID')
            .appendField(Blockly.Msg.PROPERTY)
            .appendField(propdrop, "PROPERTY")
            .appendField(new Blockly.FieldDropdown(Blockly.Msg.KOSMOS_CHANGE_TYPE), "TYPE")

            .appendField(Blockly.Msg.BY).setAlign(Blockly.ALIGN_RIGHT);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_SET_VALUE_TOOLTIP);
        this.setHelpUrl("");
        kosmos.uuidPropertyBlocks.push({
            "id": id,
            "block": this,
            "prop": propdrop,
            "uuid": uuiddrop,
            "filter": ["heatingTemperatureSetting"]
        })


        //this.setWarningText(Blockly.Msg.KOSMOS_SELECT_DEVICE_FIRST);
    }
};
Blockly.Blocks['kosmos_set_color_var'] = {
    init: function () {


        this.appendDummyInput().appendField(Blockly.Msg.KOSMOS_SET_COLOR);
        this.appendValueInput("UUID").appendField(Blockly.Msg.DEVICE);
        this.appendValueInput("COLOR").appendField(Blockly.Msg.COLOR).setAlign(Blockly.ALIGN_RIGHT);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        this.setHelpUrl("");
        this.setCommentText(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        //this.setWarningText(Blockly.Msg.KOSMOS_SELECT_DEVICE_FIRST);

    }
};
Blockly.Blocks['kosmos_set_temp_var'] = {
    init: function () {

        this.appendDummyInput().appendField(Blockly.Msg.KOSMOS_SET_TEMP)


        this.appendValueInput("UUID").appendField(Blockly.Msg.DEVICE);
        this.appendValueInput("TEMP")
            .appendField(Blockly.Msg.KOSMOS_TEMPERATURE).setAlign(Blockly.ALIGN_RIGHT);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);

        this.setHelpUrl("");


        //this.setWarningText(Blockly.Msg.KOSMOS_SELECT_DEVICE_FIRST);
    }
};
Blockly.Blocks['kosmos_set_temp'] = {
    init: function () {

        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getColorList)

        this.appendValueInput("COLOR")
        this.appendDummyInput().appendField(Blockly.Msg.KOSMOS_SET_TEMP)
            .appendField(Blockly.Msg.DEVICE)
            .appendField(uuiddrop, 'UUID').appendField(Blockly.Msg.KOSMOS_TEMPERATURE).setAlign(Blockly.ALIGN_RIGHT);
        this.appendValueInput("TEMP")
            .appendField(Blockly.Msg.KOSMOS_TEMPERATURE).setAlign(Blockly.ALIGN_RIGHT);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);

        this.setHelpUrl("");

        //this.setWarningText(Blockly.Msg.KOSMOS_SELECT_DEVICE_FIRST);

    }
};
Blockly.Blocks['kosmos_set_color_brightness_var'] = {
    init: function () {

        this.appendDummyInput().appendField(Blockly.Msg.KOSMOS_SET_COLOR);
        this.appendValueInput("UUID").appendField(Blockly.Msg.DEVICE);
        this.appendValueInput("COLOR").appendField(Blockly.Msg.COLOR).setAlign(Blockly.ALIGN_RIGHT);
        this.appendValueInput("BRIGHTNESS")
            .appendField(Blockly.Msg.BRIGHTNESS).setAlign(Blockly.ALIGN_RIGHT);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        this.setHelpUrl("");
        this.setCommentText(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);

        //this.setWarningText(Blockly.Msg.KOSMOS_SELECT_DEVICE_FIRST);
    }
};
Blockly.Blocks['kosmos_set_color_brightness'] = {
    init: function () {

        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getColorList)

        this.appendValueInput("COLOR")
            .appendField(Blockly.Msg.KOSMOS_SET_COLOR)
            .appendField(Blockly.Msg.DEVICE)
            .appendField(uuiddrop, 'UUID').appendField(Blockly.Msg.COLOR).setAlign(Blockly.ALIGN_RIGHT);
        this.appendValueInput("BRIGHTNESS")
            .appendField(Blockly.Msg.BRIGHTNESS).setAlign(Blockly.ALIGN_RIGHT);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        this.setHelpUrl("");
        this.setCommentText(Blockly.Msg.KOSMOS_SET_COLOR_TOOLTIP);
        //this.setWarningText(Blockly.Msg.KOSMOS_SELECT_DEVICE_FIRST);

    }
};
Blockly.Blocks['kosmos_load_variable'] = {
    init: function () {
        this.appendDummyInput().appendField(Blockly.Msg.LOAD_VARIABLE);
        this.appendValueInput("VARNAME")
            .setAlign(Blockly.ALIGN_RIGHT);
        this.appendValueInput("DEFAULT").appendField(Blockly.Msg.DEFAULT).setAlign(Blockly.ALIGN_RIGHT);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_SET_VALUE_DEVICE_TOOLTIP);
        this.setHelpUrl("");
        this.setCommentText(Blockly.Msg.KOSMOS_SET_VALUE_DEVICE_TOOLTIP);
    }
};
Blockly.Blocks['kosmos_set_value'] = {
    init: function () {
        const id = kosmos.nextTriggerId();
        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getUuidList, kosmos.changedUuid)
        const propdrop = new Blockly.FieldDropdown(kosmos.getStates("id"))
        this.appendValueInput("INPUT")
            .appendField(Blockly.Msg.KOSMOS_SET_VALUE)
            .appendField(Blockly.Msg.UUID)
            .appendField(uuiddrop, 'UUID')
            .appendField(Blockly.Msg.PROPERTY)
            .appendField(propdrop, "PROPERTY").appendField(Blockly.Msg.TO).setAlign(Blockly.ALIGN_RIGHT);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_SET_VALUE_TOOLTIP);
        this.setHelpUrl("");
        kosmos.uuidPropertyBlocks.push({"id": id, "block": this, "prop": propdrop, "uuid": uuiddrop})
        this.setCommentText(Blockly.Msg.KOSMOS_SET_VALUE_TOOLTIP)

    }
};
Blockly.Blocks['kosmos_set_value3'] = {
    init: function () {

        this.appendValueInput("INPUT")
            .appendField(Blockly.Msg.KOSMOS_SET_VALUE)
            .appendField(Blockly.Msg.DEVICE)
            .appendField(new Blockly.FieldTextInput("uuid"), 'UUID')
            .appendField(Blockly.Msg.PROPERTY)
            .appendField(new Blockly.FieldTextInput("property"), "PROPERTY").appendField(Blockly.Msg.TO).setAlign(Blockly.ALIGN_RIGHT);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_SET_VALUE_TOOLTIP);
        this.setHelpUrl("");
        this.setCommentText(Blockly.Msg.KOSMOS_SET_VALUE_TOOLTIP)

    }
};

Blockly.Blocks['kosmos_set_value2'] = {
    init: function () {


        this.appendDummyInput().appendField(Blockly.Msg.KOSMOS_SET_VALUE);
        this.appendValueInput("UUID")
            .appendField(Blockly.Msg.DEVICE).setAlign(Blockly.ALIGN_RIGHT);
        this.appendValueInput("PROPERTY")
            .appendField(Blockly.Msg.PROPERTY).setAlign(Blockly.ALIGN_RIGHT);
        this.appendValueInput("INPUT")
            .appendField(Blockly.Msg.TO).setAlign(Blockly.ALIGN_RIGHT);
        this.setPreviousStatement(true, null);
        //this.setInputsInline(true);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_SET_VALUE_TOOLTIP);
        this.setHelpUrl("");

        this.setCommentText(Blockly.Msg.KOSMOS_SET_VALUE_TOOLTIP)
        this.setCommentText(Blockly.Msg.KOSMOS_SET_VALUE_TOOLTIP)

    }
};
Blockly.Blocks['kosmos_connect'] = {
    init: function () {


        this.appendDummyInput()

            .appendField(Blockly.Msg.KOSMOS_CONNECT_CONNECT_WITH, "KOSMOS_CONNECT_CONNECT_WITH");

        this.setColour(230);
        this.setNextStatement(true, null);

        this.setTooltip("connect to the smarthome system");
        this.setHelpUrl("");
        this.setEditable(false);
        this.setDeletable(false);
        this.setCommentText("connect to the smarthome system")
    }
};
Blockly.Blocks['member'] = {
    init: function () {


        this.appendValueInput('MEMBER_VALUE')
            .appendField(new Blockly.FieldTextInput("property", kosmos.Code["lenBigger0Validator"], {isDirty_: true}), "MEMBER_NAME")
            .appendField(':');

        this.setColour(230);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
    }
};

Blockly.Blocks['kosmos_list_first'] = {
    init: function () {

        this.appendDummyInput()
            .appendField("get first item from")
            //.appendField(new Blockly.FieldLabelSerializable(""), "LISTNAME");
            .appendField(new Blockly.FieldDropdown(
                kosmos.getListNames(this)), 'LISTDROP');
        this.setOutput(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");


    }
};
Blockly.Blocks['kosmos_list_last'] = {
    init: function () {

        this.appendDummyInput()
            .appendField("get last item from")
            //.appendField(new Blockly.FieldLabelSerializable(""), "LISTNAME");
            .appendField(new Blockly.FieldDropdown(
                kosmos.getListNames(this)), 'LISTDROP');
        this.setOutput(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");


    }
};
Blockly.Blocks['kosmos_list_add'] = {
    init: function () {

        this.appendValueInput("ITEM")
            .appendField("add item to")
            //.appendField(new Blockly.FieldLabelSerializable(""), "LISTNAME");
            .appendField(new Blockly.FieldDropdown(
                kosmos.getListNames(this)), 'LISTDROP');
        this.setOutput(false, null);

        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);

    }
};
Blockly.Blocks['kosmos_list_curr'] = {
    init: function () {

        this.appendDummyInput()
            .appendField("get current item from")
            //.appendField(new Blockly.FieldLabelSerializable(""), "LISTNAME");
            .appendField(new Blockly.FieldDropdown(
                kosmos.getListNames(this)), 'LISTDROP');
        this.setOutput(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");


    }
};

Blockly.Blocks['kosmos_clamp_int'] = {
    init: function () {
        this.appendDummyInput()
            .appendField("create clamped integer")
            .appendField(new Blockly.FieldVariable("iClamp1"), "VARNAME");
        this.appendValueInput("MIN")
            .setCheck("Number")
            .appendField("minimum");
        this.appendValueInput("MAX")
            .setCheck("Number")
            .appendField("maximum");
        this.appendValueInput("STEP")
            .setCheck("Number")
            .appendField("step");
        this.appendValueInput("VALUE")
            .setCheck("Number")
            .appendField("value");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
    }
};
Blockly.Blocks['kosmos_clamp_float'] = {
    init: function () {
        this.appendDummyInput()
            .appendField("create clamped float")
            .appendField(new Blockly.FieldVariable("fClamp1"), "VARNAME");
        this.appendValueInput("MIN")
            .setCheck("Number")
            .appendField("minimum");
        this.appendValueInput("MAX")
            .setCheck("Number")
            .appendField("maximum");
        this.appendValueInput("STEP")
            .setCheck("Number")
            .appendField("step");
        this.appendValueInput("VALUE")
            .setCheck("Number")
            .appendField("value");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
        //this.setEnabled(false);

    }
};
Blockly.Blocks['kosmos_clamp_increase'] = {
    init: function () {
        this.appendDummyInput()
            .appendField("increase")
            .appendField(new Blockly.FieldDropdown(
                kosmos.getClampNames), 'CLAMPDROP');
        this.appendValueInput("AMOUNT")
            .setCheck("Number")
            .appendField("by");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");


    }
};
Blockly.Blocks['kosmos_clamp_increase2'] = {
    init: function () {
        this.appendDummyInput()
            .appendField("increase")
            .appendField(new Blockly.FieldDropdown(
                kosmos.getClampNames), 'CLAMPDROP');
        this.appendValueInput("AMOUNT")
            .setCheck("Number")
            .appendField("by");
        this.setOutput(true, null);

        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");


    }
};
Blockly.Blocks['kosmos_clamp_increase3'] = {
    init: function () {
        this.appendDummyInput()
            .appendField("increase")
            .appendField(new Blockly.FieldDropdown(
                kosmos.getClampNames), 'CLAMPDROP');
        this.appendDummyInput()
            .appendField("by clamp step");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");


    }
};
Blockly.Blocks['kosmos_clamp_increase4'] = {
    init: function () {
        this.appendDummyInput()
            .appendField("increase")
            .appendField(new Blockly.FieldDropdown(
                kosmos.getClampNames), 'CLAMPDROP');
        this.appendDummyInput()
            .appendField("by clamp step");
        this.setOutput(true, null);

        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
        if (!this.isInFlyout) {
            this.getField("CLAMPDROP").doValueUpdate_();
        }

    }
};
Blockly.Blocks['kosmos_clamp_decrease'] = {
    init: function () {
        this.appendDummyInput()
            .appendField("decrease")
            .appendField(new Blockly.FieldDropdown(
                kosmos.getClampNames), 'CLAMPDROP');
        this.appendValueInput("AMOUNT")
            .setCheck("Number")
            .appendField("by");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");


    }
};
Blockly.Blocks['kosmos_clamp_decrease2'] = {
    init: function () {
        this.appendDummyInput()
            .appendField("decrease")
            .appendField(new Blockly.FieldDropdown(
                kosmos.getClampNames), 'CLAMPDROP');
        this.appendValueInput("AMOUNT")
            .setCheck("Number")
            .appendField("by");
        this.setOutput(true, null);

        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");


    }
};
Blockly.Blocks['kosmos_clamp_decrease3'] = {
    init: function () {
        this.appendDummyInput()
            .appendField("decrease")
            .appendField(new Blockly.FieldDropdown(
                kosmos.getClampNames), 'CLAMPDROP');
        this.appendDummyInput()
            .appendField("by clamp step");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");


    }
};
Blockly.Blocks['kosmos_clamp_decrease4'] = {
    init: function () {
        this.appendDummyInput()
            .appendField("decrease")
            .appendField(new Blockly.FieldDropdown(
                kosmos.getClampNames), 'CLAMPDROP');
        this.appendDummyInput()
            .appendField("by clamp step");
        this.setOutput(true, null);

        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
        if (!this.isInFlyout) {
            this.getField("CLAMPDROP").doValueUpdate_();
        }

    }
};
Blockly.Blocks['kosmos_clamp_curr'] = {
    init: function () {

        this.appendDummyInput()
            .appendField("get current value of")
            //.appendField(new Blockly.FieldLabelSerializable(""), "LISTNAME");
            .appendField(new Blockly.FieldDropdown(
                kosmos.getClampNames), 'CLAMPDROP');
        this.setOutput(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");


    }
};
Blockly.Blocks['kosmos_regex'] = {
    init: function () {
        this.appendDummyInput()
            .appendField("Regular expression")
            .appendField(new Blockly.FieldTextInput(""), "regex");
        this.appendValueInput("INPUT")
            .setCheck(null)
            .appendField("Input");
        this.appendStatementInput("DO")
            .setCheck(null);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
    }
};
Blockly.Blocks['kosmos_get_uuid_patch'] = {
    init: function () {
        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getShortpressList)

        this.appendDummyInput()
            .appendField(Blockly.Msg.KOSMOS_GET_UUID)
            .appendField(Blockly.Msg.PATCH)
            .appendField(uuiddrop, 'UUID').setAlign(Blockly.ALIGN_RIGHT);

        this.setOutput(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_GET_UUID_TOOLTIP);

        this.setHelpUrl("");


    }
}

Blockly.Blocks['kosmos_get_uuid_heating'] = {
    init: function () {
        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getTemperatureList)

        this.appendDummyInput()
            .appendField(Blockly.Msg.KOSMOS_GET_UUID)
            .appendField(Blockly.Msg.HEATING_DEVICE)
            .appendField(uuiddrop, 'UUID').setAlign(Blockly.ALIGN_RIGHT);

        this.setOutput(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_GET_UUID_TOOLTIP);

        this.setHelpUrl("");


    }
};

Blockly.Blocks['kosmos_get_uuid_on'] = {
    init: function () {
        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getOnList)

        this.appendDummyInput()
            .appendField(Blockly.Msg.KOSMOS_GET_UUID)
            .appendField(Blockly.Msg.ON_DEVICE)
            .appendField(uuiddrop, 'UUID').setAlign(Blockly.ALIGN_RIGHT);

        this.setOutput(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_GET_UUID_TOOLTIP);

        this.setHelpUrl("");


    }
};
Blockly.Blocks['kosmos_get_uuid_color'] = {
    init: function () {
        const uuiddrop = new Blockly.FieldDropdown(
            kosmos.getColorList)

        this.appendDummyInput()
            .appendField(Blockly.Msg.KOSMOS_GET_UUID)
            .appendField(Blockly.Msg.COLOR_DEVICE)
            .appendField(uuiddrop, 'UUID').setAlign(Blockly.ALIGN_RIGHT);

        this.setOutput(true, null);
        this.setColour(230);
        this.setTooltip(Blockly.Msg.KOSMOS_GET_UUID_TOOLTIP);

        this.setHelpUrl("");


    }
};Blockly.Blocks['context_heating_patch_1'] = {
    init: function () {
        this.appendDummyInput()
            .appendField("Use Patch for Heating");
        this.appendValueInput("PATCH")
            .setCheck("String")
            .appendField("Patch");
        this.appendValueInput("DEVICES")
            .setCheck("Array")
            .appendField("Devices");
        this.appendValueInput("MIN")
            .setCheck("Number")
            .appendField("Min Temp");
        this.appendValueInput("MAX")
            .setCheck("Number")
            .appendField("Max Temp");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
    }
};
Blockly.Blocks['context_heating_patch_2'] = {
    init: function() {
        this.appendDummyInput()
            .appendField("Use Patch for Heating");
        this.appendDummyInput()
            .appendField("Patch")
            .appendField(new Blockly.FieldDropdown(
                kosmos.getShortpressList), "PATCH");
        this.appendValueInput("DEVICES")
            .setCheck("Array")
            .appendField("Devices");
        this.appendValueInput("MIN")
            .setCheck("Number")
            .appendField("Min Temp");
        this.appendValueInput("MAX")
            .setCheck("Number")
            .appendField("Max Temp");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");

    }
};
Blockly.Blocks['context_light_patch_1'] = {
    init: function () {
        this.appendDummyInput()
            .appendField("Use Patch for Light");
        this.appendValueInput("PATCH")
            .setCheck("String")
            .appendField("Patch");

        this.appendValueInput("DEVICE")
            .setCheck("String")
            .appendField("Device");
        this.appendValueInput("COLORS")
            .setCheck(null)
            .appendField("Colors");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
    }
};
Blockly.Blocks['context_light_patch_2'] = {
    init: function () {
        this.appendDummyInput()
            .appendField("Use Patch for Light");
        this.appendDummyInput()
            .appendField("Patch")
            .appendField(new Blockly.FieldDropdown(
                kosmos.getShortpressList), "PATCH");
        this.appendDummyInput()
            .appendField("Light")
            .appendField(new Blockly.FieldDropdown(
                kosmos.getColorList), "DEVICE");
        this.appendValueInput("COLORS")
            .setCheck(null)
            .appendField("Colors");

        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
    }
};

Blockly.Blocks['patch_heating'] = {
    init: function() {
        this.appendDummyInput()
            .appendField(new Blockly.FieldLabelSerializable("Use Patch for Heating"));
        this.appendDummyInput()
            .appendField("Patch")
            .appendField(new Blockly.FieldDropdown(
                kosmos.getShortpressList), "PATCH");
        this.appendDummyInput()
            .appendField("Rooms:");
        this.appendDummyInput()
            .setAlign(Blockly.ALIGN_RIGHT)
            .appendField("Office")
            .appendField(new Blockly.FieldCheckbox("TRUE"), "1");
        this.appendDummyInput()
            .setAlign(Blockly.ALIGN_RIGHT)
            .appendField("Livingroom")
            .appendField(new Blockly.FieldCheckbox("TRUE"), "2");
        this.appendDummyInput()
            .setAlign(Blockly.ALIGN_RIGHT)
            .appendField("Kitchen")
            .appendField(new Blockly.FieldCheckbox("TRUE"), "3");
        this.appendDummyInput()
            .setAlign(Blockly.ALIGN_RIGHT)
            .appendField("Bedroom")
            .appendField(new Blockly.FieldCheckbox("TRUE"), "4");
        this.appendDummyInput()
            .setAlign(Blockly.ALIGN_RIGHT)
            .appendField("Bathroom")
            .appendField(new Blockly.FieldCheckbox("TRUE"), "5");
        this.appendDummyInput()
            .appendField("Minimum")
            .appendField(new Blockly.FieldNumber(14, 0, 40), "min");
        this.appendDummyInput()
            .appendField("Maximum")
            .appendField(new Blockly.FieldNumber(30, 0, 40), "max");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
    }
};
