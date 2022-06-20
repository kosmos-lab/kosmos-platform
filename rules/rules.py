#!/usr/bin/env python
"""Static file server, using Python's CherryPy. Should be used when Django's static development server just doesn't cut."""

import os.path
import sys
sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
import _thread
import glob
import json
import subprocess
import jwt
import time
import traceback
from random import randint

import cherrypy

from ws4py.server.cherrypyserver import WebSocketPlugin, WebSocketTool
from ws4py.websocket import WebSocket

import io

jwtkey = ''
with open('../config/config.json') as json_file:
    data = json.load(json_file)
    print(data)
    jwtkey = data["jwt"]
websockets = {}
import signal
import time
python_cmd = sys.executable
class GracefulKiller:
    kill_now = False
    def __init__(self):
        signal.signal(signal.SIGINT, self.exit_gracefully)
        signal.signal(signal.SIGTERM, self.exit_gracefully)

    def exit_gracefully(self,signum, frame):
        global processes
        for p in processes:
            try:
                processes[p].kill()
            except BaseException:
                pass
        self.kill_now = True
killer = GracefulKiller()
def parseProcess(key):
    global websockets, processes, killer
    while not killer.kill_now:
        try:
            time.sleep(1)
            for stdout_line in iter(processes[key].stdout.readline, ""):
                print("out ", key, stdout_line)
                list = websockets[key]
                print("spreading to ",len(list),"clients")
                deadlist = []
                for entry in list:
                    try:
                        entry.send(bytes(json.dumps({'type':'log','value':stdout_line}),encoding='ASCII'))
                    except AttributeError:
                        deadlist.append(entry)
                        pass
                    except BaseException:
                        traceback.print_exc(file=sys.stdout)
                for entry in deadlist:
                    websockets[key].remove(entry)
        except BaseException:
            traceback.print_exc(file=sys.stdout)
def parseProcess2(key):
    global websockets, processes, killer
    while not killer.kill_now:
        try:
            time.sleep(1)
            for stdout_line in iter(processes[key].stderr.readline, ""):
                print("out ", key, stdout_line)
                list = websockets[key]
                print("spreading to ",len(list),"clients")
                deadlist = []
                for entry in list:
                    try:
                        entry.send(bytes(json.dumps({'type':'err','value':stdout_line}),encoding='ASCII'))
                    except AttributeError:
                        deadlist.append(entry)
                        pass
                    except BaseException:
                        traceback.print_exc(file=sys.stdout)
                for entry in deadlist:
                    websockets[key].remove(entry)
        except BaseException:
            traceback.print_exc(file=sys.stdout)

def wspinger():

    global websockets,  killer
    while not killer.kill_now:
        try:
            time.sleep(10)
            for key in websockets:
                list = websockets[key]

                deadlist = []
                for entry in list:
                    try:
                        entry.send(bytes(json.dumps({'type':'ping','value':randint(0,100000000)}), encoding='ASCII'))
                    except AttributeError:
                        deadlist.append(entry)
                        pass
                    except RuntimeError:
                        deadlist.append(entry)
                        pass
                    except BaseException:
                        traceback.print_exc(file=sys.stdout)
                for entry in deadlist:
                    websockets[key].remove(entry)
        except BaseException:
            traceback.print_exc(file=sys.stdout)


class ChatWebSocketHandler(WebSocket):
    def received_message(self, m):
        try:
            #print("incoming", m)

            s = str(m)
            if s.startswith("Bearer "):
                user = jwt.decode(s[7:], jwtkey, algorithms=['HS256'])
                if 'id' in user:
                    uid = str(user["id"])
                    print("authed as ",uid)
                    if user["id"] not in websockets:
                        websockets[uid] = []
                    websockets[uid].append(self)

        except BaseException:
            traceback.print_exc(file=sys.stdout)

    def closed(self, code, reason="A client left the room without a proper explanation."):
        print("closed", code)


class Root:
    @cherrypy.expose
    def loadXML(self):
        header = cherrypy.request.headers['Authorization'][7:]
        print("have jwt:" + header)

        user = jwt.decode(header, jwtkey, algorithms=['HS256'])
        if 'id' in user:
            f = 'rules/' + str(user["id"]) + '.xml';
            if os.path.exists(f):
                with open(f, 'r') as file:
                    data = file.read()

                return data
        return ""

    @cherrypy.expose
    def saveXML(self):
        cl = cherrypy.request.headers['Content-Length']
        rawbody = (cherrypy.request.body.read(int(cl))).decode('utf-8')
        header = cherrypy.request.headers['Authorization'][7:]
        print("have jwt:" + header)

        user = jwt.decode(header, jwtkey, algorithms=['HS256'])
        if 'id' in user:
            f = 'rules/' + str(user["id"]) + '.xml';

            with open(f, 'w') as file:
                file.write(rawbody)
        return ""

    @cherrypy.expose
    def savePython(self):
        cl = cherrypy.request.headers['Content-Length']
        rawbody = (cherrypy.request.body.read(int(cl))).decode('utf-8')
        header = cherrypy.request.headers['Authorization'][7:]
        print("have jwt:" + header)

        user = jwt.decode(header, jwtkey, algorithms=['HS256'])
        if 'id' in user:
            u = str(user["id"])
            f = 'rules/' + u + '.py'

            with io.open(f, 'w',encoding="utf-8") as file:
                file.write(rawbody)

            if u in processes:
                # if there was already a process running stop it
                processes[u].kill()
            # start a new process
            processes[u] = subprocess.Popen([python_cmd, f], bufsize=1, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                                            universal_newlines=True)

        return ""

    @cherrypy.expose
    def test(self):
        return "test"

    @cherrypy.expose
    def ws(self, encoding=None):
        cherrypy.log("Handler created: %s" % repr(cherrypy.request.ws_handler))
        pass


processes = {}
if __name__ == '__main__':

    # glob over all current rules
    for f in glob.glob("rules/*.py"):
        user = os.path.basename(f)[:-3]
        # print(user)
        # create a process for it, and save it in our dictionary
        processes[user] = subprocess.Popen([python_cmd, f], bufsize=1, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                                           universal_newlines=True)

        websockets[user] = []
        _thread.start_new_thread(parseProcess, (user,))
        _thread.start_new_thread(parseProcess2, (user,))

    static_dir = os.path.dirname(os.path.abspath(__file__)) + "/web/"  # Root static dir is this file's directory.
    print("\nstatic_dir: %s\n" % static_dir)
    WebSocketPlugin(cherrypy.engine).subscribe()
    cherrypy.tools.websocket = WebSocketTool()
    _thread.start_new_thread(wspinger, ())

    cherrypy.config.update({  # I prefer configuring the server here, instead of in an external file.
        'server.socket_host': '0.0.0.0',
        'server.socket_port': 8001,
    })
    WebSocketPlugin(cherrypy.engine).subscribe()
    cherrypy.tools.websocket = WebSocketTool()

    cherrypy.quickstart(Root(), '', config={
        '/ws': {'tools.websocket.on': True,
                'tools.websocket.handler_cls': ChatWebSocketHandler},
        '/': {  # Root folder.
            'tools.staticdir.on': True,  # Enable or disable this rule.
            'tools.staticdir.root': static_dir,
            'tools.staticdir.dir': '',
            'tools.staticdir.index': 'index.html'

        }})
    # ..and LAUNCH ! :)
