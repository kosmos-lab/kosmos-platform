from __future__ import print_function
import os.path
import sys

# sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
import inspect
import json
import threading
import time
import traceback

try:
    import thread
except ImportError:
    import _thread as thread
import websocket

from pubsub import pub

import sys


def eprint(*args, **kwargs):
    print(*args, file=sys.stderr, **kwargs)


class StoppableThread(threading.Thread):
    """Thread class with a stop() method. The thread itself has to check
    regularly for the stopped() condition."""

    def __init__(self, *args, **kwargs):
        super(StoppableThread, self).__init__(*args, **kwargs)
        self._args = [self]

        self._stop_event = threading.Event()
        # eprint("starting...", id(self))

    def stop(self):
        # eprint("stopping...", id(self))
        self._stop_event.set()

    def stopped(self):
        return self._stop_event.is_set()


# class ListIterator:
#    ''' Iterator class '''
#    def __init__(self, list):
#        # Team object reference
#        self._kl = list
#        # member variable to keep track of current index
#        self._index = 0
#
#    def __next__(self):
#        ''''Returns the next value from team object's lists '''
#        if self._index < (len(self._kl.entries)) :
#
#            result = self._kl.entries[self._index];
#
#            self._index +=1
#            return result
#        # End of Iteration
#        raise StopIteration
# class ListMeta(type):
#     def __iter__(self):
#         return self.entries.iteritems()
class KosmosClampFloat(float):
    ##some magic to make KosmosClamp behave like an int :)
    def __new__(cls, min, max, step, value):
        return float.__new__(cls, value)

    def __init__(self, min=0, max=255, step=1, value=None):
        self.min = min
        self.max = max
        self.step = step
        if value is not None:
            self.value = self._clamp(value)
        else:
            self.value = float(min)

    def _clamp(self, input):
        return float(max(min(input, self.max), self.min))

    def set(self, input):
        self.value = self._clamp(input)

    def _change(self, amount):
        self.value = self._clamp(self.value + amount)
        return self.value

    def increase(self, amount=None):
        if amount is None:
            amount = self.step
        return self._change(amount)

    def decrease(self, amount=None):
        if amount is None:
            amount = self.step
        return self._change(-amount)

    def current(self):
        return self.value

    def __repr__(self):
        return self.value

    def __str__(self):
        return str(self.value)

    def __isub__(self, amount):
        return KosmosClampFloat(self.min, self.max, self.step, self.value - amount)

    def __iadd__(self, amount):
        return KosmosClampFloat(self.min, self.max, self.step, self.value + amount)

    def __imul__(self, amount):
        return KosmosClampFloat(self.min, self.max, self.step, (self.value * amount))

    def __rtruediv__(self, amount):
        return KosmosClampFloat(self.min, self.max, self.step, (self.value / amount))

    def __itruediv__(self, amount):
        return KosmosClampFloat(self.min, self.max, self.step, (self.value / amount))

    def __rfloordiv__(self, amount):
        return KosmosClampFloat(self.min, self.max, self.step, (self.value / amount))

    def __ifloordiv__(self, amount):
        return KosmosClampFloat(self.min, self.max, self.step, (self.value / amount))


class KosmosClampInt(int):
    ##some magic to make KosmosClamp behave like an int :)
    def __new__(cls, min, max, step, value):
        return int.__new__(cls, value)

    def __init__(self, min=0, max=255, step=1, value=None):

        self.min = min
        self.max = max
        self.step = step
        if value is not None:
            self.value = self._clamp(value)
        else:
            self.value = int(min)

    def _clamp(self, input):
        return int(max(min(input, self.max), self.min))

    def set(self, input):
        self.value = self._clamp(input)

    def _change(self, amount):
        self.value = self._clamp(self.value + amount)
        return self.value

    def increase(self, amount=None):
        if amount is None:
            amount = self.step
        return self._change(amount)

    def decrease(self, amount=None):
        if amount is None:
            amount = self.step
        return self._change(-amount)

    def current(self):
        return self.value

    def __repr__(self):
        return self.value

    def __str__(self):
        return str(self.value)

    def __isub__(self, amount):
        return KosmosClampInt(self.min, self.max, self.step, self.value - amount)

    def __iadd__(self, amount):
        return KosmosClampInt(self.min, self.max, self.step, self.value + amount)

    def __imul__(self, amount):
        return KosmosClampInt(self.min, self.max, self.step, (self.value * amount))

    def __rtruediv__(self, amount):
        return KosmosClampInt(self.min, self.max, self.step, (self.value / amount))

    def __itruediv__(self, amount):
        return KosmosClampInt(self.min, self.max, self.step, (self.value / amount))

    def __rfloordiv__(self, amount):
        return KosmosClampInt(self.min, self.max, self.step, (self.value / amount))

    def __ifloordiv__(self, amount):
        return KosmosClampInt(self.min, self.max, self.step, (self.value / amount))


class KosmosList:
    #    __metaclass__ = ListMeta # We need that meta class...
    def __init__(self, entries, maxLen=None):
        self.entries = entries
        self.myIndex = None
        self.maxLen = maxLen

    def set_index(self, index):
        self.myIndex = index % len(self.entries)

    def next(self):
        if self.myIndex is None:
            self.myIndex = -1

        self.set_index(self.myIndex + 1)
        return self.entries[self.myIndex]

    def prev(self):
        if self.myIndex is None:
            self.myIndex = 0
        self.set_index(self.myIndex - 1)
        return self.entries[self.myIndex]

    def curr(self):
        if self.myIndex is None:
            self.myIndex = 0
        return self.entries[self.myIndex]

    def add(self, item):
        if self.maxLen is not None:
            while len(self.entries) >= self.maxLen:
                self.entries.pop(0)
        self.entries.append(item)

    def __iter__(self):
        ''' Returns the Iterator object '''
        return iter(self.entries)

    def __len__(self):
        return len(self.entries)

    def __getitem__(self, index):
        return self.entries[index]

    def index(self, key):
        return self.entries.index(key)


class kosmos(websocket.WebSocketApp, threading.Thread):
    def send_log(self, message):
        text = f'kree/stdout:{message}'
        print(text)
        if self != None:
            try:
                self.ws.send(text)
            except:
                pass

    def send_error(self, message):
        text = f'kree/stderr:{message}'
        print(text)
        if self != None:
            try:
                self.ws.send(text)
            except:
                pass

    def create_device(self, uuid, type):
        if type == "LIGHT_TOGGLE":
            self.ws.send(
                uuid + '/config:{"name": "' + uuid + '", "uuid": "' + uuid + '","schema": "https://kosmos-lab.de/schema/Lamp.json","state": {"on":false}}')
        elif type == "LIGHT_RGB":
            self.ws.send(
                uuid + '/config:{"name": "' + uuid + '", "uuid": "' + uuid + '","schema": "https://kosmos-lab.de/schema/HSVLamp.json","state": {"on":false,"hue":0,"saturation":0,"dimmingLevel":0}}')
        elif type == "LIGHT_DIM":
            self.ws.send(
                uuid + '/config:{"name": "' + uuid + '", "uuid": "' + uuid + '","schema": "https://kosmos-lab.de/schema/DimmableLamp.json","state": {"on":false,"dimmingLevel":0}}')
        elif type == "SENSOR_MOTION":
            self.ws.send(
                uuid + '/config:{"name": "' + uuid + '", "uuid": "' + uuid + '","schema": "https://kosmos-lab.de/schema/MovementSensor.json","state": {"movementDetected":false}}')
        elif type == "SENSOR_TEMPERATURE":
            self.ws.send(
                uuid + '/config:{"name": "' + uuid + '", "uuid": "' + uuid + '","schema": "https://kosmos-lab.de/schema/TemperatureSensor.json","state": {"currentEnvironmentTemperature":21.5}}')
        elif type == "CLIMATE_TEMPERATURE":
            self.ws.send(
                uuid + '/config:{"name": "' + uuid + '", "uuid": "' + uuid + '","schema": "https://kosmos-lab.de/schema/Heater.json","state": {"heatingTemperatureSetting":21}}')

    def __init__(self, username, password):
        threading.Thread.__init__(self)
        self.stop = False
        self.ws = None
        self.send_log("connecting to kosmos...")
        # print("connecting to kosmos...")
        # websocket.enableTrace(True)

        self.username = username
        self.password = password
        self.devs = {}
        self.ddevs = {}

    def run(self):

        self.ws = websocket.WebSocketApp("ws://localhost:18080/ws",
                                         on_message=self.on_message,
                                         on_error=self.on_error,
                                         on_close=self.on_close,
                                         on_open=self.on_open)
        while not self.stop:
            self.send_log("start connecting...")
            self.ws.run_forever(ping_interval=10)

            if self.stop:
                break

            time.sleep(5)

    def subscribe(self, topic, callback):
        pub.subscribe(topic, callback)
    def getRGBFromHEX(self,input):
        if input.startswith("#"):
            h = input.lstrip('#')
            # print("rgb:",h)
            return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))
    def getXYZFromHEX(self,input):
        (sR,sG,sB) = self.getRGBFromHEX(input)
        var_R = ( sR / 255 )
        var_G = ( sG / 255 )
        var_B = ( sB / 255 )

        if var_R > 0.04045:
            var_R = pow(( ( var_R + 0.055 ) / 1.055 ), 2.4)
        else:
            var_R = var_R / 12.92
        if var_G > 0.04045:
            var_G = pow(( ( var_G + 0.055 ) / 1.055 ) , 2.4)
        else:
            var_G = var_G / 12.92
        if var_B > 0.04045:
            var_B = pow(( ( var_B + 0.055 ) / 1.055 ), 2.4)
        else:
            var_B = var_B / 12.92

        var_R = var_R * 100
        var_G = var_G * 100
        var_B = var_B * 100

        X = var_R * 0.4124 + var_G * 0.3576 + var_B * 0.1805
        Y = var_R * 0.2126 + var_G * 0.7152 + var_B * 0.0722
        Z = var_R * 0.0193 + var_G * 0.1192 + var_B * 0.9505
        return (X,Y,Z)
    def getXYFromHEX(self,input):
        (X,Y,Z) = self.getXYZFromHEX(input)
        return (X / (X + Y + Z),Y / (X + Y + Z))
    def parseValue(self, value, key=None):
        cleaned = {}
        try:
            if type(value) is dict:
                for key in value:
                    if type(value[key]) is dict:
                        cleaned[key] = self.parseValue(value[key])
                    elif key == '_rgb':
                        # print("RGB FOUND!",value)
                        (r,g,b) = self.getRGBFromHEX(value[key])
                        if "brightness" in value:
                            r = int(r / 255 * (int(value["brightness"])))

                            g = int(g / 255 * (int(value["brightness"])))
                            b = int(b / 255 * (int(value["brightness"])))
                        cleaned["r"] = r
                        cleaned["g"] = g
                        cleaned["b"] = b
                    else:
                        cleaned[key] = value[key]
        except BaseException:
            traceback.print_exc(file=sys.stdout)
        return cleaned

    def startConnecting(self):
        self.connected = False
        self.start()
        while not self.connected:
            time.sleep(0.5)
        return

    def get_name(self, uuid):
        return self.ddevs[uuid]['name']

    def on_message(self, message):
        self.connected = True
        # print(f"incoming: {message}")

        idx = message.find(':')
        if idx != -1:
            url = message[:idx]
            if url.startswith("device/") or url.startswith("kosmos/"):
                url = url[7:]
            args = message[idx + 1:]
            # print("url " + url)
            # print("args " + args)
            if url == "devices":
                # init devices
                d = json.loads(args)

                for de in d:
                    self.devs[de['uuid']] = de['state']
                    self.ddevs[de['uuid']] = de

            elif url.endswith("/state"):
                dev = url[:-6]
                # print("dev " + dev)
                d = json.loads(args)
                sends = []
                if dev in self.devs:
                    dd = self.devs[dev]
                    for key in d:
                        if key not in dd or dd[key] != d[key]:
                            # print("found diff in ",key)
                            # collect changes
                            sends.append((dev, key))
                            dd[key] = d[key]
                # send AFTER change was saved
                for (dev, key) in sends:
                    pub.sendMessage("onChange~_~" + dev + "~_~" + key, uuid=dev, property=key,
                                    transValue=self.devs[dev][key])
                    pub.sendMessage("onChange~_~" + dev, uuid=dev, property=key, transValue=self.devs[dev][key])
                    pub.sendMessage("onChange~_~*~_~" + key, uuid=dev, property=key, transValue=self.devs[dev][key])

                    pub.sendMessage("onChange", uuid=dev, property=key, transValue=self.devs[dev][key])
                # print(self.devs)

    def get_value(self, uuid, key):
        if uuid in self.devs:
            d = self.devs[uuid]
            if key == "brightness":
                if key in d:
                    return int(d[key] / 2.55)
                if "dimmingLevel" in d:
                    return d["dimmingLevel"]
            if key in d:
                return d[key]
        print(f"could not find {key} in {uuid}")
        return None

    def set_color(self, uuid, color, brightness=None):
        print(f"looking for xy_color in {uuid}")
        if uuid not in self.devs:
            print(f"NOOO device found for {uuid}")
            return
        scm = self.get_value(uuid,"supported_color_modes")
        if self.get_value(uuid,"xy_color") is not None or (scm is not None and "xy" in scm):
            self.set_value(uuid, "xy_color", self.getXYFromHEX(str(color)))
        else:
            print(f"NOOO xy_color in {uuid} {self.devs[uuid]}")
            self.set_value(uuid, "_rgb", str(color))
        if brightness is not None:
            if type(brightness) == KosmosClampInt:
                b = int(brightness.current() * 2.55)
                self.set_value(uuid, "brightness", b)
            else:
                self.set_value(uuid, "brightness", (int(brightness) * 2.55))

        self.turn_on(uuid,True)
    def set_color_text(self, uuid, color, text="", brightness=None):

        self.set_value(uuid, "text", text)
        self.set_color(uuid,color,brightness)

    def set_text(self, uuid, text=""):
        self.turn_on(uuid,True)
        self.set_value(uuid, "text", text)

    def turn_on(self, uuid, state):
        if self.get_value(uuid,"state") is not None:
            print(f"has state")
            if state==True or str(state).lower() == "on" or str(state).lower() == "true":
                print(f"turn on")
                self.set_value(uuid, "state", "on")
            else:
                print(f"turn off")
                self.set_value(uuid, "state", "off")
        if self.get_value(uuid,"on") is not None:
            print("has on")
            if state==True or str(state).lower == "on" or str(state).lower() == "true":
                self.set_value(uuid, "on", True)
            else:
                self.set_value(uuid, "on", False)
            return



        if state==True or str(state).lower() == "on" or str(state).lower() == "true":
            self.set_value(uuid, "state", "on")
        else:
            self.set_value(uuid, "state", "off")
        return

    def set_device(self, uuid, value):
        # print("type ", type(value))
        if type(value) is dict:
            value = self.parseValue(value)
            value = json.dumps(value)
        elif type(value) is list:
            value = self.parseValue(value)
            value = json.dumps(value)
        sending = uuid + "/set:" + str(value)
        # print("sending ", sending)
        self.ws.send(sending)
        pass

    def set_value(self, uuid, key, value):
        if uuid is None:
            return
        if value is None:
            return
        if key is None:
            return
        if uuid not in self.devs:
            print(f"device not found {uuid}!")
            return
        print(f"setting {uuid}/{key} to {value}")
        print(f"current {self.devs[uuid]}")
        stri = ''
        if key == "on":
            if "on" not in self.devs[uuid]:
                if "state" in self.devs[uuid]:
                    if value is True is True or str(value).lower() == "on" or str(value).lower() == "true":
                        value = "on"
                    else:
                        value = "off"
                    key = "state"
            else:
                if "state" in self.devs[uuid]:
                    if value is True or str(value).lower() == "on" or str(value).lower() == "true":
                        stri = f'{uuid}/set:{{"state":"on"}}'
                    else:
                        stri = f'{uuid}/set:{{"state":"off"}}'
                    print(f"sending 3:{stri}")
                    self.ws.send(stri)
            if "state" not in self.devs[uuid]:
                if "on" in self.devs[uuid]:
                    if value is True or str(value).lower() == "on" or str(value).lower() == "true":
                        stri = f'{uuid}/set:{{"on":true}}'
                    else:
                        stri = f'{uuid}/set:{{"on":false}}'
                    print(f"sending 4:{stri}")
                    self.ws.send(stri)
        if "rgb" in key:
            if value.startswith("#"):

                h = value.lstrip('#')
                (r, g, b) = tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))
                sending = uuid + "/set:{\"r\":" + str(r) + ",\"g\":" + str(g) + ",\"b\":" + str(b) + "}"
                if 'state' in self.ddevs[uuid]:
                    # print(self.ddevs[uuid]['state'])
                    if 'color' in self.ddevs[uuid]['state']:
                        sending = uuid + "/set:{\"color\":{\"r\":" + str(r) + ",\"g\":" + str(g) + ",\"b\":" + str(
                            b) + "}}"

                print("sending:" + sending)
                self.ws.send(sending)
                return
        # self.devs[key] = value

        if type(value) == int or type(value) == float:
            stri = uuid + "/set:{\"" + key + "\":" + str(value) + "}"
        elif type(value) == bool:
            stri = uuid + "/set:{\"" + key + "\":" + str(value) + "}"
        elif type(value) == list or type(value) == tuple:
            stri = uuid + "/set:{\"" + key + "\":" + json.dumps(value) + "}"

        else:
            stri = uuid + "/set:{\"" + key + "\":\"" + str(value) + "\"}"
        print(f"sending:{stri}")
        self.ws.send(stri)
        pass

    def set_name(self, uuid, name):
        self.ws.send(uuid + "/setname:" + name)
        self.ddevs[uuid]['name'] = name
        pass

    def on_error(self, error):
        self.connected = False
        self.send_error("error connecting")
        self.send_error(error)
        # print(error)
        pass

    def on_close(self):
        self.connected = False
        # print("### closed ###")
        pass

    def on_open(self):
        # print(self)

        # print("connected")
        self.send_log("connected")

        self.connected = True
        self.ws.send('user/auth:{"user":"' + self.username + '","pass":"' + self.password + '"}')
        self.ws.send('user/type:KREE')

        def run(*args):
            while (True):
                time.sleep(30)
                self.ws.send("ping")

        thread.start_new_thread(run, ())

    def is_not_used(self):
        pass
