global.DOMParser = require('xmldom').DOMParser;

require("google-closure-library");


global.Blockly = require('./blockly_compressed.js');
require('./blocks_compressed.js');
require('./python_compressed.js');
require('./blocks.js');
require('./python.js');
require('./msg/en.js');
require('./msg/js/en.js');

require('./code.js');
let glob = require("glob")


let fs = require('fs');
var myArgs = process.argv.slice(2);
//glob("../rules/*.xml", {}, function (er, files) {
try {
    //console.log(files);
    //for (let i = 0; i < files.length; i++) {
    fs.readFile("../rules/" + myArgs[0] + ".xml", 'utf8', (err, data) => {
        if (err) {
            console.error(err)

        }
        kosmos.username = myArgs[1]
        kosmos.password = myArgs[2]
        //var xml = Blockly.Xml.textToDom(data);
        // Create a headless workspace.
        Code.workspace = new Blockly.Workspace();
        //code.init();

        Blockly.Events.disable();

        kosmos.loadFromText(data);
        //Blockly.Xml.domToWorkspace(xml, workspace);
        //var code = Blockly.Python.workspaceToCode(workspace);
        //console.log(code);
    })


    //}


} catch (e) {
    console.log(e);
}
//});

