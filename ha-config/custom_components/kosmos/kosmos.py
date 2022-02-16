import logging
import json

from homeassistant import core

try:
    import thread
except ImportError:
    import _thread as thread
import time
import threading
from .const import DOMAIN  # pylint:disable=unused-import
import asyncio
import re
from kosmos_client import KosmosClient, KosmosEvent, KosmosDevice, KosmosScope, KosmosLocation, \
    KosmosNotFoundError, KosmosError

from typing import List

from homeassistant.components.switch import (
    SwitchEntity
)
from homeassistant.components.binary_sensor import (
    BinarySensorEntity,
)
from homeassistant.components.light import (
    ATTR_BRIGHTNESS,
    ATTR_BRIGHTNESS_PCT,
    ATTR_COLOR_TEMP,
    ATTR_HS_COLOR,
    ATTR_XY_COLOR,

    SUPPORT_BRIGHTNESS,
    SUPPORT_COLOR,
    SUPPORT_COLOR_TEMP,
    LightEntity)
from homeassistant.helpers.entity import Entity

from homeassistant.const import (

    TEMP_CELSIUS
)
import homeassistant.util.color as color_util
from homeassistant.components.climate import ClimateEntity
from homeassistant.components.climate.const import (

    HVAC_MODE_HEAT,
    HVAC_MODE_OFF,
    PRESET_COMFORT,
    PRESET_ECO,
    SUPPORT_PRESET_MODE,
    SUPPORT_TARGET_TEMPERATURE,
    DEFAULT_MAX_TEMP,
    DEFAULT_MIN_TEMP

)


def ordered(obj):
    if isinstance(obj, dict):
        return sorted((k, ordered(v)) for k, v in obj.items())
    if isinstance(obj, list):
        return sorted(ordered(x) for x in obj)
    else:
        return obj


_LOGGER = logging.getLogger(__name__)

DEBUG = True

ATTR_ON = "on"
ATTR_RGB = "RGB"
ATTR_R = "r"
ATTR_G = "g"
ATTR_B = "b"
ATTR_HUE = "hue"
ATTR_COLOR = "color"
ATTR_SAT = "sat"
ATTR_TEMP = "temp"

clean_pattern = re.compile('[\\W_]+')


def set_value_in_place(target_object, key, new_value):
    """
    updates the value at key in the given object in place
    :param target_object: the object to change the value of
    :param key:  the key to change - can be a path like color/r for the key r in the underlying object color
    :param new_value: the value to set it to

    """

    # split the key, so we can have key be multilevel.
    k = key.split("/", 1)
    # print("k ", k, len(k))
    if len(k) > 1:
        if k[0] not in target_object:
            target_object[k[0]] = set_value_in_place({}, k[1], new_value)

        else:
            target_object[k[0]] = set_value_in_place(target_object[k[0]], k[1], new_value)

    else:
        target_object[key] = new_value
    return target_object


def get_value(state: dict, key: str):

    """
    get the value of a given key in the state
    :param state: the dict
    :param key: key, can be a path like color/r
    :return: the value if found or None
    """
    k = key.split("/", 1)
    if len(k) > 1:
        if k[0] not in state:
            return None
        else:
            return get_value(state[k[0]], k[1])
    else:
        if key in state:
            return state[key]
        return None


ignored_keys = ["supported_features", "attributes", "last_changed", "last_updated", "context", "friendly_name", "uuid"]


def get_sensor_class(prop):
    if "temperature" in prop or "Temperature" in prop:
        return "temperature"
    if "current" in prop or "Current" in prop:
        return "current"
    if "battery" in prop or "Battery" in prop:
        return "battery"
    if "energy" in prop or "Energy" in prop:
        return "energy"
    if "illuminance" in prop or "Illuminance" in prop:
        return "illuminance"
    if "signal" in prop or "Signal" in prop:
        return "signal_strength"
    if "powerfactor" in prop or "PowerFactore" in prop:
        return "power_factor"
    if "power" in prop or "Power" in prop:
        return "power"
    if "pressure" in prop or "Pressure" in prop:
        return "pressure"
    if "voltage" in prop or "Voltage" in prop:
        return "voltage"
    if "timestamp" in prop or "Timestamp" in prop:
        return "timestamp"
    return "None"


def get_sensor_icon(prop):
    if prop == "longpress":
        return "gesture-double-tap"
    if prop == "shortpress":
        return "gesture-tap"
    if "distance" in prop or "Distance" in prop:
        return "measure"
    if "temperature" in prop or "Temperature" in prop:
        return "thermometer"
    return "api"


def get_send_json(eid, ns):
    o = dict()
    sk = ns.as_dict()

    o["uuid"] = eid

    o["state"] = {}
    a = ns.attributes
    for k in sk:
        if k not in ignored_keys:
            if isinstance(sk[k], set):
                o["state"][k] = list(sk[k])
            else:
                o["state"][k] = sk[k]
    for k in a:
        if k not in ignored_keys:
            if isinstance(a[k], set):
                o["state"][k] = list(a[k])
            else:
                o["state"][k] = a[k]
    if "restored" in o["state"]:
        if o["state"]["restored"] is True:
            if "state" in o["state"]:
                if o["state"]["state"] == "unavailable":
                    del o["state"]["state"]
        del o["state"]["restored"]
    # delete duplicated or "unwished" entries
    if "rgb_color" in o["state"]:
        if "supported_color_modes" not in o["state"]:
            del o["state"]["rgb_color"]
        else:
            if "rgb" not in o["state"]["supported_color_modes"] and "rgbw" not in o["state"][
                "supported_color_modes"] and "rgbww" not in o["state"]["supported_color_modes"]:
                del o["state"]["rgb_color"]
    if "xy_color" in o["state"]:
        if "supported_color_modes" not in o["state"]:
            del o["state"]["xy_color"]
        else:
            if "xy" not in o["state"]["supported_color_modes"]:
                del o["state"]["xy_color"]
    if "hs_color" in o["state"]:
        if "supported_color_modes" not in o["state"]:
            del o["state"]["hs_color"]
        else:
            if "hs" not in o["state"]["supported_color_modes"]:
                del o["state"]["hs_color"]
    if "hs_color" in o["state"]:
        if "supported_color_modes" not in o["state"]:
            del o["state"]["hs_color"]
        else:
            if "hs" not in o["state"]["supported_color_modes"]:
                del o["state"]["hs_color"]
    if "brightness" in o["state"]:
        if "supported_color_modes" not in o["state"]:
            del o["state"]["brightness"]
        else:
            if "brightness" not in o["state"]["supported_color_modes"] and "xy" not in o["state"][
                "supported_color_modes"] and "color_temp" not in o["state"]["supported_color_modes"]:
                del o["state"]["brightness"]
    if "color_temp" in o["state"]:
        if "supported_color_modes" not in o["state"]:
            del o["state"]["color_temp"]
        else:
            if "color_temp" not in o["state"]["supported_color_modes"]:
                del o["state"]["color_temp"]
    if "color_temp" in o["state"]:
        if "supported_color_modes" not in o["state"]:
            del o["state"]["color_temp"]
        else:
            if "color_temp" not in o["state"]["supported_color_modes"]:
                del o["state"]["color_temp"]
    if "white_value" in o["state"]:
        if "supported_color_modes" not in o["state"]:
            del o["state"]["white_value"]
        else:
            if "white" not in o["state"]["supported_color_modes"]:
                del o["state"]["white_value"]
    return o


class KosmoSHub(threading.Thread):
    def is_own_device(self, eid: str):
        if eid is None:
            return False

        for f in self.my_entities:
            try:
                if f.entity_id == eid:
                    _LOGGER.warning(f"found {eid}")
                    return True
                # _LOGGER.warning(f.entity_id)
            except Exception as e:
                _LOGGER.error(e)
        # _LOGGER.error(f"{eid} is not a kosmos device")
        return False

    def __init__(self, hass: core.HomeAssistant, host: str, username: str, password: str):
        threading.Thread.__init__(self)
        # we do need the reference to the add_entity entities functions for each domain we need to add_entity stuff to
        self.async_add_entities_light = None
        self.async_add_entities_number = None
        self.async_add_entities_sensor = None
        self.async_add_entities_climate = None
        self.async_add_entities_switch = None
        self.async_add_entities_binary_sensor = None
        self.kosmos = None
        # _LOGGER.info(f"adding kosmoshub")
        self.stop = False

        self.hass = hass
        """Initialize."""
        self.username = username
        self.password = password
        self.initHADevices = False
        self.host = host
        _LOGGER.info(f"adding kosmoshub for {host}")
        self.devs = {}
        self.ddevs = {}
        self.initdone = False
        self.my_entities = []
        self.schemas = {}
        self.devschemas = {}
        self.doneDevices = {}
        self.initDevices = {}

    def has_entity(self, entity_id: str):
        for d in self.my_entities:

            if d.entity_id == entity_id:
                _LOGGER.info(f"{d.entity_id} == {entity_id} is True")
                return True
        return False

    async def get_schema(self, id: str):
        if id in self.schemas:
            return self.schemas[id]
        s = await self.kosmos.get_schema_async(id)
        if s is not None:
            self.schemas[id] = s
        return s

    async def create_light(self, device: KosmosDevice, schema: dict):
        while self.async_add_entities_light is None:
            await asyncio.sleep(0.1)
        _LOGGER.warning("adding LIGHT for " + device.uuid)
        d = KosmoSLight(device, self, self.hass, schema)
        self.async_add_entities_light([d])

    async def create_if_needed(self, device: KosmosDevice):
        _LOGGER.info(f"create if needed for {device}")
        if self.hass.states.get(device.uuid) is not None:
            _LOGGER.info(f"skipping {device.uuid} - is already known by states")
            return
        for domain in ["alarm_control_panel", "alert", "alexa", "automation", "binary_sensor", "camera", "climate",
                       "device_tracker", "ENTITY", "fan", "group", "image_processing", "input_boolean", "input_select",
                       "light", "media_player", "person", "remote", "script", "sensor", "sun", "switch", "timer",
                       "weather", "zigbee2mqtt_networkmap", "zone"]:
            if self.has_entity(f"{domain}.{device.uuid}"):
                _LOGGER.info(f"skipping {device} - is already known")
                return

        if device.schema is None:
            if device.uuid in self.devschemas:
                device.schema = self.devschemas[device.uuid]
        if device.schema is not None:
            self.devschemas[device.uuid] = device.schema
            if device.schema == "https://kosmos-lab.de/schema/Heaters.json":
                _LOGGER.warning("adding CLIMATE for " + device.uuid + "/heatingTemperatureSetting")
                d = KosmoSHeater(device, self, self.hass, "heatingTemperatureSetting", "currentEnvironmentTemperature",
                                 0.5)
                self.async_add_entities_climate([d])
                return
            schema = await self.get_schema(device.schema)
            # _LOGGER.info(f"schema for {device} {schema}")

            if "properties" in schema:
                if "on" in schema["properties"]:
                    for k in ["dimmingLevel", "brightness", "color", "xy_color", "hue", "saturation",
                              "colorTemperature",
                              "hs_color"]:
                        if k in schema["properties"]:
                            await self.create_light(device, schema)
                            return
            else:
                _LOGGER.warning("no properties in schema!")
        if device.state is not None:
            s = device.state
            if 'on' in s:
                await self.create_light(device, None)
                return

            if 'state' in s:
                try:
                    ss = str(s['state']).lower()
                    if ss == 'on' or ss == 'off':
                        await self.create_light(device, None)
                        return
                except AttributeError:
                    pass
        if device.schema is not None:
            if 'properties' in schema:
                for p in schema["properties"]:
                    if device.uuid + "_" + p in self.doneDevices:
                        continue
                    if device.state is not None and p not in device.state:
                        continue
                    prop = schema["properties"][p]
                    # print(p+":"+str(prop))
                    if "type" in prop:
                        if prop["type"] == "integer" or prop["type"] == "number":
                            if "readOnly" in prop and prop["readOnly"] is False:
                                try:
                                    ctemp = p.replace("TargetTemperature", "Temperature")
                                    if ctemp in schema["properties"]:
                                        _LOGGER.warning("adding CLIMATE for " + device.uuid + "/" + p)
                                        while self.async_add_entities_climate is None:
                                            await asyncio.sleep(0.1)
                                        d = KosmoSHeater(device, self, self.hass, p,
                                                         ctemp,
                                                         prop, schema["properties"][ctemp])
                                        self.async_add_entities_climate([d])
                                        continue
                                    else:
                                        _LOGGER.warning("adding CLIMATE SENSOR for " + device.uuid + "/" + p)
                                        while self.async_add_entities_sensor is None:
                                            await asyncio.sleep(0.1)
                                        d = KosmoSSensor(device, self, self.hass, p,
                                                         None, prop, None)
                                        self.async_add_entities_sensor([d])
                                        continue
                                except ValueError:
                                    pass
                        if prop["type"] == "boolean":
                            if "readOnly" not in prop or prop["readOnly"] is False:
                                try:
                                    _LOGGER.warning("adding SWITCH for " + device.uuid + "/" + p)
                                    while self.async_add_entities_switch is None:
                                        await asyncio.sleep(0.1)
                                    d = KosmoSSwitch(device, self, self.hass, p)
                                    self.async_add_entities_switch([d])
                                    continue
                                except ValueError:
                                    pass
                            if "readOnly" in prop and prop["readOnly"] is True:
                                try:
                                    _LOGGER.warning("adding BINARY_SENSOR for " + device.uuid + "/" + p)
                                    while self.async_add_entities_binary_sensor is None:
                                        await asyncio.sleep(0.1)

                                    d = KosmoSBinarySensor(device, self, self.hass, p, get_sensor_class(p),
                                                           get_sensor_icon(prop),
                                                           prop)
                                    self.async_add_entities_binary_sensor([d])
                                    continue
                                except ValueError:
                                    pass
                        if prop["type"] == "string" or prop["type"] == "integer" or prop["type"] == "number":
                            try:
                                if "readOnly" in prop and prop["readOnly"] is True:
                                    _LOGGER.warning("adding SENSOR for " + device.uuid + "/" + p)
                                    while self.async_add_entities_sensor is None:
                                        await asyncio.sleep(0.1)
                                    d = KosmoSSensor(device, self, self.hass, p, get_sensor_class(p),
                                                     get_sensor_icon(prop),
                                                     prop)
                                    self.async_add_entities_sensor([d])
                                    continue
                            except ValueError:
                                pass
                            # temporary fix to get it running with text for now
                            _LOGGER.warning("adding SENSOR for " + device.uuid + "/" + p)
                            while self.async_add_entities_sensor is None:
                                await asyncio.sleep(0.1)
                            try:
                                d = KosmoSSensor(device, self, self.hass, p, get_sensor_class(p), get_sensor_icon(prop),
                                                 prop)

                                self.async_add_entities_sensor([d])
                            except ValueError:
                                pass
                            continue
        else:
            _LOGGER.warning(f"no state for {device}!")

    async def handle_hass_event(self, call):
        if call.event_type == "time_changed":
            return

        if DEBUG:
            _LOGGER.warning("Event %s received: %s", call.event_type, call.data)

        if call.event_type == "state_changed":

            while self.kosmos is None:
                await asyncio.sleep(0.1)
            while self.kosmos.is_connected() is not True:
                await asyncio.sleep(0.1)
            while self.initHADevices is not True:
                await asyncio.sleep(0.1)
            entity_id = call.data["entity_id"]
            inhere = self.has_entity(entity_id)
            if not inhere:
                if "new_state" in call.data:
                    if DEBUG:
                        _LOGGER.info(f"state changed\n{call.data}")
                    new_state = call.data['new_state']
                    if new_state is not None:

                        send_json = get_send_json(entity_id, new_state)
                        if DEBUG:
                            _LOGGER.warning(f"final state changed {send_json}")
                        self.initDevices[entity_id] = send_json
                        while not self.initdone:
                            asyncio.sleep(1)
                        while self.kosmos is None:
                            asyncio.sleep(1)
                        if not self.is_own_device(entity_id):
                            await self.kosmos.set_async(entity_id, send_json)

            return
        if call.event_type == "homeassistant_stop":
            self.quit()
            return

    def send_init(self):
        if DEBUG:
            _LOGGER.warning(f"sending all init states")
            _LOGGER.warning(f"all states: {(self.hass.states)}")
            for s in self.hass.states.all():
                if not self.has_entity(s.entity_id):
                    if not s.entity_id in self.initDevices:
                        oo = get_send_json(s.entity_id, s)
                        _LOGGER.warning(f"s: {oo} {type(oo)} {s}")
                        # sending = f"{s.entity_id}/haset:{json.dumps(oo, default=str)}"
                        sending = oo

                        self.initDevices[s.entity_id] = oo
        for (eid, sending) in self.initDevices.items():
            if self.has_entity(eid) or self.is_own_device(eid):
                _LOGGER.warning(f"skipping init for {eid} because its part of kosmos anyway")
            else:
                self.kosmos.set(eid, sending)

    def add_entity(self, entity: Entity):
        self.my_entities.append(entity)

    def on_init_done(self, kosmos: KosmosClient):
        _LOGGER.info(f"init done {kosmos}")
        self.initdone = True
        self.initHADevices = True
        pass

    async def on_device_created(self, kosmos: KosmosClient, device: KosmosDevice):
        _LOGGER.info(f"new device on {kosmos} {device}")
        await self.create_if_needed(device)

    def on_device_updated(self, kosmos: KosmosClient, device: KosmosDevice):
        _LOGGER.info(f"change on {kosmos} {device} - {device.state}")
        self.parse_update(device)

    def run(self):

        while not self.stop:
            self.initdone = False
            with KosmosClient(self.host, self.username, self.password,
                              subs={
                                  KosmosEvent.init_done: self.on_init_done,
                                  KosmosEvent.device_created: self.on_device_created,
                                  KosmosEvent.device_updated: self.on_device_updated
                              }, type="HAIntegration", debug=True) as self.kosmos:
                _LOGGER.info(f"is connected:{self.kosmos.is_connected()}")

                while self.stop is False and self.kosmos.is_connected():
                    time.sleep(10)

    async def wait_for_init(self):
        while True:
            if self.initdone:
                return
            await asyncio.sleep(0.1)

    def quit(self):
        self.stop = True
        self.kosmos.stop()

    def parse_update(self, device: KosmosDevice) -> None:
        """
        parse an update received from the kosmos system
        :param device: the device an update was received for

        """
        try:
            if device.uuid.startswith("sun.") or device.uuid.startswith("weather."):
                return
            found = False
            state_in_hass = self.hass.states.get(device.uuid)
            if state_in_hass is not None:
                found = True
                raw_old_state = state_in_hass.as_dict()
                if DEBUG:
                    _LOGGER.warning(f"found dev {device.uuid} again \nd:{device.state}\nraw_old_state:{raw_old_state}")
                clean_old_state = {}
                # collect only the state we are interested in
                for key in raw_old_state:
                    if key not in ignored_keys:
                        # convert sets to lists
                        # since JSON does not know the concept of sets
                        if isinstance(raw_old_state[key], set):
                            clean_old_state[key] = (list(raw_old_state[key]))
                        if isinstance(raw_old_state[key], tuple):
                            clean_old_state[key] = (list(raw_old_state[key]))
                        else:
                            # clean_old_state[key] = json.dumps(raw_old_state[key], default=str)
                            clean_old_state[key] = raw_old_state[key]

                if "attributes" in raw_old_state:
                    attributes = raw_old_state["attributes"]
                    for key in attributes:
                        if key not in ignored_keys:
                            if isinstance(attributes[key], set):
                                clean_old_state[key] = (list(attributes[key]))
                            if isinstance(attributes[key], tuple):
                                clean_old_state[key] = (list(attributes[key]))
                            else:
                                # clean_old_state[key] = json.dumps(attributes[key], default=str)
                                clean_old_state[key] = attributes[key]

                dirty = False
                for key in device.state:
                    if key not in ignored_keys:
                        new_value = device.state[key]
                        if key == "state":
                            if type(new_value) is dict:
                                if "state" in new_value:
                                    new_value = (new_value["state"])

                        if key in clean_old_state:
                            if clean_old_state[key] == new_value:
                                continue
                            if isinstance(clean_old_state[key], str):
                                # HomeAssistant tends to encapsulate the string again
                                so = (clean_old_state[key].strip('" '))
                            else:
                                so = (clean_old_state[key])

                            if isinstance(new_value, str):
                                sv = (new_value.strip())
                            else:
                                sv = (new_value)
                            if so == sv:
                                continue

                            dirty = True

                            if DEBUG:
                                _LOGGER.warning(
                                    f"DIRTY KEY {key} {so} ({type(clean_old_state[key])}) vs {sv} ({type(new_value)})")

                        clean_old_state[key] = new_value

                if "state" in clean_old_state:
                    state = clean_old_state["state"]
                    if type(state) is dict:
                        if "state" in state:
                            state = state["state"]
                    del clean_old_state["state"]
                    if state is not None:
                        state = clean_pattern.sub('', state)
                    if DEBUG:
                        _LOGGER.warning(f"has {state} {clean_old_state} {dirty}")
                    if dirty:
                        #
                        if device.uuid.startswith("light."):
                            if DEBUG:
                                _LOGGER.warning(f"state is: {state} {device.state} {clean_old_state}")
                            state_in_hass = str(state).lower()
                            if state_in_hass == "off" or state_in_hass == "false" or state_in_hass == "0":
                                service_data = {"entity_id": device.uuid}
                                if DEBUG:
                                    _LOGGER.warning(f"calling light turnOFF with {service_data}")
                                self.hass.services.call("light", "turn_off", service_data, False)
                            else:
                                service_data = {"entity_id": device.uuid}
                                for key in ["transition", "white_value", "kelvin", "color_name", "brightness",
                                            "brightness_pct", "brightness_step", "brightness_step_pct",
                                            "flash",
                                            "effect"]:
                                    if key in clean_old_state:
                                        service_data[key] = clean_old_state[key]

                                if "attributes" in raw_old_state:
                                    if "supported_color_modes" in raw_old_state["attributes"]:
                                        supported_color_modes = raw_old_state["attributes"]["supported_color_modes"]
                                        if DEBUG:
                                            _LOGGER.warning(f"supported_color_modes {supported_color_modes}")
                                            _LOGGER.warning(f"d {device.state}")

                                        if "xy" in supported_color_modes:
                                            if "xy_color" in device.state:
                                                service_data["xy_color"] = device.state["xy_color"]
                                        if "hs" in supported_color_modes:
                                            if "hs_color" in device.state:
                                                service_data["hs_color"] = device.state["hs_color"]
                                        if "color_temp" in supported_color_modes:
                                            if "color_temp" in device.state:
                                                service_data["color_temp"] = device.state["color_temp"]
                                else:
                                    if DEBUG:
                                        _LOGGER.warning(f"raw_old_state {raw_old_state}")
                                if DEBUG:
                                    _LOGGER.warning(f"calling light turnON with {service_data}")
                                self.hass.services.call("light", "turn_on", service_data, False)

                        elif device.uuid.startswith("switch."):
                            if state == "on" or state is True or state == "True":
                                service_data = {"entity_id": device.uuid}
                                self.hass.services.call("switch", "turn_on", service_data, False)
                            else:
                                service_data = {"entity_id": device.uuid}
                                self.hass.services.call("switch", "turn_off", service_data, False)
                        elif device.uuid.startswith("cover."):
                            if state == "CLOSE":
                                service_data = {"entity_id": device.uuid}
                                self.hass.services.call("cover", "close_cover", service_data, False)
                            elif state == "OPEN":
                                service_data = {"entity_id": device.uuid}
                                self.hass.services.call("cover", "open_cover", service_data, False)
                            elif state == "STOP":
                                service_data = {"entity_id": device.uuid}
                                self.hass.services.call("cover", "stop_cover", service_data, False)
                        else:
                            self.hass.states.set(device.uuid, state, clean_old_state)

            if not found:
                for dd in self.my_entities:
                    if dd.device.uuid == device.uuid:
                        for key in device.state:
                            if f"{device.uuid}_{key}" not in self.doneDevices:
                                _thread = threading.Thread(target=asyncio.run,
                                                           args=(self.create_if_needed(device),))
                                _thread.start()
                        dd.refreshFrom(device.state)
                        found = True
                        dd.device.state = device.state
            if not found:
                # create new device - needs to be done in a thread because we cannot await here
                _thread = threading.Thread(target=asyncio.run, args=(self.create_if_needed(device),))
                _thread.start()

        except Exception as e:
            _LOGGER.info(e)

    def set_device(self, uuid, payload):
        self.kosmos.set(uuid, payload)

    def set_value(self, uuid, key, value):
        if "rgb" in key:
            if value.startswith("#"):
                h = value.lstrip('#')
                (r, g, b) = tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))
                self.kosmos.set(uuid, {{"r": {r}, "g": {g}, "b": {b}}})

                return
        self.kosmos.set(uuid, {key: value})
        pass

    async def authenticate(self) -> bool:
        """Test if we can authenticate with the host."""
        kosmos = KosmosClient(self.host, self.username, self.password)

        return await kosmos.login_async()


class KosmoSLight(LightEntity):

    def __init__(self, device, hub: KosmoSHub, hass, schema=None):
        self.hub = hub
        hub.add_entity(self)
        self.device = device
        if device.name is not None:
            self._name = device.name
        else:
            self._name = device.uuid
            self.device.name = device.uuid
        self._uuid = device.uuid

        self._state = None
        self._attr = device.state
        self.propkeys = {}
        self._brightness = None
        self._hs = None
        self._rgb = None
        self._color_temp = None
        self._max_mireds = None
        self._min_mireds = None
        self._min_k = None
        self._max_k = None
        self._xy = None

        hub.doneDevices[device.uuid + "_on"] = True

        if device.state is not None:
            if isinstance(device.state, dict):
                if "colorTemperature" in device.state:
                    if "colorTemperature" in schema["properties"]:
                        s = schema["properties"]["colorTemperature"]
                        if "maximum" in s:
                            self._min_mireds = color_util.color_temperature_kelvin_to_mired(s["maximum"])
                            self._max_k = (s["maximum"])
                        if "minimum" in s:
                            self._max_mireds = color_util.color_temperature_kelvin_to_mired(s["minimum"])
                            self._min_k = (s["minimum"])
                    self.propkeys[ATTR_COLOR_TEMP] = "colorTemperature"
                    hub.doneDevices[device.uuid + "_colorTemperature"] = True

                if "dimmingLevel" in device.state:
                    self.propkeys[ATTR_BRIGHTNESS_PCT] = "dimmingLevel"
                    hub.doneDevices[device.uuid + "_dimmingLevel"] = True
                if "brightness" in device.state:
                    self.propkeys[ATTR_BRIGHTNESS] = "brightness"
                    hub.doneDevices[device.uuid + "_brightness"] = True
                if all(key in device.state for key in ('hue', 'saturation')):
                    hub.doneDevices[device.uuid + "_hue"] = True
                    hub.doneDevices[device.uuid + "_saturation"] = True
                    # print("hsv")
                    self.propkeys[ATTR_HUE] = 'hue'
                    self.propkeys[ATTR_SAT] = 'saturation'
                for key in ["color", "rgb"]:
                    # check if it is given in light state
                    if key in device.state:
                        if isinstance(device.state[key], dict):

                            for k in ["r", "red"]:
                                if k in device.state[key]:
                                    self.propkeys[ATTR_R] = key + "/" + k
                                    for k2 in ["g", "green"]:
                                        if k2 in device.state[key]:
                                            self.propkeys[ATTR_G] = key + "/" + k2
                                            for k3 in ["b", "blue"]:
                                                if k3 in device.state[key]:
                                                    self.propkeys[ATTR_B] = key + "/" + k3
                                                    hub.doneDevices[device.uuid + "_" + key] = True

                                                    break
                                            break
                                    break

                self.refreshFrom(device.state)

        self.hass = hass

        # self.unique_id = "kosmos_"+light["uuid"]

    @property
    def max_mireds(self):
        return self._max_mireds

    @property
    def min_mireds(self):
        return self._min_mireds

    @property
    def hs_color(self):
        """Return the HS color value."""
        if ATTR_B in self.propkeys:
            if self._rgb:
                return color_util.color_RGB_to_hs(*self._rgb)

        if ATTR_HUE in self.propkeys:
            if self._hs:
                return self._hs

        return None

    @property
    def color_temp(self):
        """Return the HS color value."""
        return self._color_temp

    def refreshFrom(self, state):
        # print("refresh from ", state)
        if ATTR_ON in state:
            self._state = state[ATTR_ON]
        else:
            self._state = False
        if ATTR_BRIGHTNESS in self.propkeys:
            if self.propkeys[ATTR_BRIGHTNESS] in state:
                self._brightness = state[self.propkeys[ATTR_BRIGHTNESS]]
        if ATTR_BRIGHTNESS_PCT in self.propkeys:
            if self.propkeys[ATTR_BRIGHTNESS_PCT] in state:
                self._brightness = int(state[self.propkeys[ATTR_BRIGHTNESS_PCT]] * 2.55)
        if ATTR_HUE in self.propkeys and ATTR_SAT in self.propkeys:
            if self.propkeys[ATTR_HUE] in state and self.propkeys[ATTR_SAT]:
                self._hs = (get_value(state, self.propkeys[ATTR_HUE]), get_value(state, self.propkeys[ATTR_SAT]))
        if ATTR_COLOR_TEMP in self.propkeys:
            if self.propkeys[ATTR_COLOR_TEMP] in state:
                self._color_temp = color_util.color_temperature_kelvin_to_mired(state[self.propkeys[ATTR_COLOR_TEMP]])
        if ATTR_COLOR in self.propkeys:
            if self.propkeys[ATTR_COLOR] in state:
                v = get_value(state, self.propkeys[ATTR_COLOR])
                if v is not None:
                    if "x" in v and "y" in v:
                        self._xy = (float(v["x"]), float(v["y"]))

        if ATTR_XY_COLOR in self.propkeys:
            v = get_value(state, self.propkeys[ATTR_XY_COLOR])
            if v is not None:
                self._xy = (float(v[0]), float(v[1]))

        if self.hass is not None:
            self.async_write_ha_state()

        # self.update()

    @property
    def xy_color(self):
        return self._xy

    @property
    def unique_id(self):
        return self._uuid

    @property
    def orly_list(self) -> List[str]:
        """Return the list of supported effects."""
        return ["yes", "no", "maybe", "probably"]

    @property
    def brightness(self):
        """Return the brightness of the light.
        This method is optional. Removing it indicates to Home Assistant
        that brightness is not supported for this light.
        """
        return self._brightness

    @property
    def name(self):
        """Return the display name of this light."""
        return self._name

    @property
    def device_state_attributes(self):
        data = {}

        if self.device.schema is not None:
            data["schema"] = self.device.schema

        return data

    @property
    def device_info(self):
        return {
            "identifiers": {
                # Serial numbers are unique identifiers within a specific domain
                (DOMAIN, self.device.uuid)
            },
            "name": self.device.name,
            "via_device": (DOMAIN, self.hub.host),
        }

    @property
    def supported_features(self) -> int:
        """Flag supported features."""
        flags = 0
        if ATTR_BRIGHTNESS in self.propkeys:
            flags |= SUPPORT_BRIGHTNESS
        elif ATTR_BRIGHTNESS_PCT in self.propkeys:
            flags |= SUPPORT_BRIGHTNESS
        if ATTR_COLOR in self.propkeys:
            flags |= SUPPORT_COLOR
        if ATTR_HS_COLOR in self.propkeys:
            flags |= SUPPORT_COLOR
        if ATTR_HUE in self.propkeys:
            flags |= SUPPORT_COLOR
        if ATTR_B in self.propkeys:
            flags |= SUPPORT_COLOR
        if ATTR_COLOR_TEMP in self.propkeys:
            flags |= SUPPORT_COLOR_TEMP
        if ATTR_XY_COLOR in self.propkeys:
            flags |= SUPPORT_COLOR
        return flags

    @property
    def orly(self):
        return "yes"

    @property
    def is_on(self):
        """Return true if light is on."""
        return self._state

    def turn_on(self, **kwargs):
        if kwargs is not None:
            _LOGGER.warning(f"turning on with arguments: {kwargs}")
        else:
            _LOGGER.warning("turning on")

        state = {ATTR_ON: True}
        if ATTR_HS_COLOR in kwargs:
            # state[self.propkeys[ATTR_BRIGHTNESS]] = kwargs.get(ATTR_BRIGHTNESS, 255)
            if ATTR_HUE in self.propkeys and ATTR_SAT in self.propkeys:
                state[self.propkeys[ATTR_HUE]] = kwargs.get(ATTR_HS_COLOR)[0]
                state[self.propkeys[ATTR_SAT]] = kwargs.get(ATTR_HS_COLOR)[1]
            if ATTR_R in self.propkeys and ATTR_G in self.propkeys and ATTR_B in self.propkeys:
                r, g, b = color_util.color_hsv_to_RGB(kwargs.get(ATTR_HS_COLOR)[0], kwargs.get(ATTR_HS_COLOR)[1],
                                                      (float(self._brightness / 2.55)))
                # the key can be something like color/r so we need to use our little helper here
                set_value_in_place(state, self.propkeys[ATTR_R], r)
                set_value_in_place(state, self.propkeys[ATTR_G], g)
                set_value_in_place(state, self.propkeys[ATTR_B], b)

        if ATTR_BRIGHTNESS in kwargs:
            if ATTR_BRIGHTNESS in self.propkeys:
                state[self.propkeys[ATTR_BRIGHTNESS]] = kwargs.get(ATTR_BRIGHTNESS, self._brightness)
            if ATTR_BRIGHTNESS_PCT in self.propkeys:
                state[self.propkeys[ATTR_BRIGHTNESS_PCT]] = int(kwargs.get(ATTR_BRIGHTNESS, self._brightness) / 2.55)
        if ATTR_COLOR_TEMP in kwargs:
            if ATTR_COLOR_TEMP in self.propkeys:
                state[self.propkeys[ATTR_COLOR_TEMP]] = color_util.color_temperature_mired_to_kelvin(
                    kwargs.get(ATTR_COLOR_TEMP))
                # print(state[self.propkeys[ATTR_COLOR_TEMP]])
                if self._max_k is not None:
                    state[self.propkeys[ATTR_COLOR_TEMP]] = min(self._max_k, state[self.propkeys[ATTR_COLOR_TEMP]])
                    # print(state[self.propkeys[ATTR_COLOR_TEMP]])
                if self._min_k is not None:
                    state[self.propkeys[ATTR_COLOR_TEMP]] = max(self._min_k, state[self.propkeys[ATTR_COLOR_TEMP]])
                    # print(state[self.propkeys[ATTR_COLOR_TEMP]])
        # self._hub.postSync("/device/set", data={"id": self._uuid, "on": True})
        self.hub.set_device(self._uuid, state)

    def turn_off(self, **kwargs):
        """Instruct the light to turn off."""
        state = {ATTR_ON: False}

        # self._hub.postSync("/device/set", data={"id": self._uuid, "on": False})
        self.hub.set_device(self._uuid, state)

    def update(self):
        """Fetch new state data for this light.
        This is the only method that should fetch new data for Home Assistant.
        """
        pass
        # self.refreshFrom(asyncio.run(self._hub.getState(self._uuid)))


class KosmoSSwitch(SwitchEntity):

    def __init__(self, device, hub: KosmoSHub, hass, attr="state"):
        self.hub = hub
        hub.add_entity(self)
        hub.doneDevices[device.uuid + "_" + attr] = True
        self.device = device
        if device.name is not None:
            self._name = device.name + "_" + attr
        else:
            if attr != "state":
                self._name = device.uuid + "_" + attr
            else:
                self._name = device.uuid
            self.device.name = device.uuid

        self._uuid = device.uuid + "_" + attr
        self._state = None
        self.attrkey = attr
        self._attr = device.state[self.attrkey]
        self.propkeys = {}
        self._brightness = None
        self._hs = None
        self._rgb = None

        if device.state is not None:
            self.refreshFrom(device.state)

        self.hass = hass

        # self.unique_id = "kosmos_"+light["uuid"]

    def refreshFrom(self, state):
        # print("refresh from ", state)
        if self.attrkey in state:
            # print("found "+self.attrkey+" "+str(state[self.attrkey]))
            self._state = state[self.attrkey]
        else:
            self._state = False
        if self.hass is not None:
            self.async_write_ha_state()
        # self.update()

    @property
    def unique_id(self):
        return self._uuid

    @property
    def name(self):
        """Return the display name of this light."""
        return self._name

    @property
    def is_on(self):
        """Return true if light is on."""
        return self._state

    def turn_on(self, **kwargs):
        """Instruct the light to turn on.
        You can skip the brightness part if your light does not support
        brightness control.
        """

        state = {self.attrkey: True}
        self.hub.set_device(self.device.uuid, state)

    def turn_off(self, **kwargs):
        """Instruct the light to turn off."""
        state = {self.attrkey: False}

        # self._hub.postSync("/device/set", data={"id": self._uuid, "on": False})
        self.hub.set_device(self.device.uuid, state)

    def update(self):
        """Fetch new state data for this light.
        This is the only method that should fetch new data for Home Assistant.
        """
        pass
        # self.refreshFrom(asyncio.run(self._hub.getState(self._uuid)))

    @property
    def device_state_attributes(self):
        data = {}

        if self.device.schema is not None:
            data["schema"] = self.device.schema
        return data

    @property
    def device_info(self):
        return {
            "identifiers": {
                # Serial numbers are unique identifiers within a specific domain
                (DOMAIN, self.device.uuid)
            },
            "name": self.device.name,
            "via_device": (DOMAIN, self.hub.host),
        }


class KosmoSSwitch(SwitchEntity):

    def __init__(self, device, hub: KosmoSHub, hass, attr="state"):
        self.hub = hub
        hub.add_entity(self)
        hub.doneDevices[device.uuid + "_" + attr] = True
        self.device = device
        if device.name is not None:
            self._name = device.name + "_" + attr
        else:
            if attr != "state":
                self._name = device.uuid + "_" + attr
            else:
                self._name = device.uuid
            self.device.name = device.uuid

        self._uuid = device.uuid + "_" + attr
        self._state = None
        self.attrkey = attr
        self._attr = device.state[self.attrkey]
        self.propkeys = {}
        self._brightness = None
        self._hs = None
        self._rgb = None

        if device.state is not None:
            self.refreshFrom(device.state)

        self.hass = hass

        # self.unique_id = "kosmos_"+light["uuid"]

    def refreshFrom(self, state):
        # print("refresh from ", state)
        if self.attrkey in state:
            # print("found "+self.attrkey+" "+str(state[self.attrkey]))
            self._state = state[self.attrkey]
        else:
            self._state = False
        if self.hass is not None:
            self.async_write_ha_state()
        # self.update()

    @property
    def unique_id(self):
        return self._uuid

    @property
    def name(self):
        """Return the display name of this light."""
        return self._name

    @property
    def is_on(self):
        """Return true if light is on."""
        return self._state

    def turn_on(self, **kwargs):
        """Instruct the light to turn on.
        You can skip the brightness part if your light does not support
        brightness control.
        """

        state = {self.attrkey: True}
        self.hub.set_device(self.device.uuid, state)

    def turn_off(self, **kwargs):
        """Instruct the light to turn off."""
        state = {self.attrkey: False}

        # self._hub.postSync("/device/set", data={"id": self._uuid, "on": False})
        self.hub.set_device(self.device.uuid, state)

    def update(self):
        """Fetch new state data for this light.
        This is the only method that should fetch new data for Home Assistant.
        """
        pass
        # self.refreshFrom(asyncio.run(self._hub.getState(self._uuid)))

    @property
    def device_state_attributes(self):
        data = {}

        if self.device.schema is not None:
            data["schema"] = self.device.schema
        return data

    @property
    def device_info(self):
        return {
            "identifiers": {
                # Serial numbers are unique identifiers within a specific domain
                (DOMAIN, self.device.uuid)
            },
            "name": self.device.name,
            "via_device": (DOMAIN, self.hub.host),
        }


class KosmoSHeater(ClimateEntity):
    def __init__(self, device, hub: KosmoSHub, hass, key_target_temperature=None, key_current_temperature=None,
                 prop=None, ctemp_prop=None, presets=[PRESET_ECO, PRESET_COMFORT], unit=TEMP_CELSIUS):
        self.hub = hub
        self.device = device
        self._presets = presets
        hub.add_entity(self)

        hub.doneDevices[device.uuid + "_" + key_target_temperature] = True
        if key_current_temperature is not None:
            hub.doneDevices[device.uuid + "_" + key_current_temperature] = True

        self._key_target_temperature = key_target_temperature
        self._key_current_temperature = key_current_temperature
        self.dev = device
        # precision = 0.5
        self._precision = 0.1
        self._min = None
        self._max = None
        self._unit = unit
        self.preset = presets[0]
        if prop is not None:
            if "multipleOf" in prop:
                self._precision = prop["multipleOf"]
            if "maximum" in prop:
                self._max = prop["maximum"]
            if "minimum" in prop:
                self._min = prop["minimum"]
            if "unit" in prop:

                self._unit = prop["unit"]
                if prop["unit"] == "C" or prop["unit"] == "K" or prop["unit"] == "F":
                    self._unit = "°" + prop["unit"]
            if self._unit == "%":
                self._unit = ""
            if self._unit == "":
                self._unit = "°C"
        if device.name is not None:
            if "TargetTemperature" in key_target_temperature:
                self._name = device.name + "_" + key_target_temperature.replace("TargetTemperature", "Temperature")
            else:
                self._name = device.name + "_" + key_target_temperature

        else:
            self._name = device.uuid + "_" + key_target_temperature
            self.device.name = device.uuid
        self._uuid = device.uuid + "_" + key_target_temperature
        self._attr = device.state
        self.propkeys = {}

        self._maxTemperature = None
        self._target = None
        self._current = None

        self.refreshFrom(device.state)
        self.hass = hass

        # _LOGGER.error(self._uuid+ " : +" +str(self.__dict__))

    @property
    def unit_of_measurement(self):
        return self._unit

    @property
    def preset_mode(self):
        """Return current preset mode."""

        return self.preset

    @property
    def name(self):
        """Return the name of the device."""
        return self._name

    @property
    def preset_modes(self):
        """Return supported preset modes."""
        return self._presets

    def set_preset_mode(self, preset_mode):
        """Set preset mode."""
        print(preset_mode)

    @property
    def hvac_mode(self):
        if self._current != None and self._target != None:
            if self._current < self._target:
                return HVAC_MODE_HEAT
            return HVAC_MODE_OFF
        return HVAC_MODE_HEAT

    @property
    def precision(self):

        return self._precision

    @property
    def max_temp(self):
        if self._max is not None:
            return self._max
        if "max_temp" in self._attr:
            return self._attr["max_temp"]
        return DEFAULT_MAX_TEMP

    @property
    def min_temp(self):
        if self._max is not None:
            return self._min
        if "min_temp" in self._attr:
            return self._attr["min_temp"]
        return DEFAULT_MIN_TEMP

    @property
    def hvac_modes(self):
        """Return the list of available operation modes."""
        return [HVAC_MODE_HEAT, HVAC_MODE_OFF]

    @property
    def temperature_unit(self):
        """Return the unit of measurement that is used."""
        return self._unit

    @property
    def should_poll(self):
        """Return the polling state."""
        return False

    @property
    def current_temperature(self):
        return self._current

    @property
    def target_temperature(self):
        return self._target

    def set_temperature(self, **kwargs) -> None:
        print(kwargs)
        if "temperature" in kwargs:
            self.hub.set_value(self.device.uuid, self._key_target_temperature, kwargs["temperature"])

    def refreshFrom(self, state):
        # print("refresh from ", state)
        if self._key_target_temperature in state:
            self._target = state[self._key_target_temperature]
            if self.hass is not None:
                self.async_write_ha_state()
        if self._key_current_temperature in state:
            self._current = state[self._key_current_temperature]
            if self.hass is not None:
                self.async_write_ha_state()

    @property
    def supported_features(self):
        """Return the list of supported features."""
        return SUPPORT_TARGET_TEMPERATURE | SUPPORT_PRESET_MODE

    @property
    def unique_id(self):
        return self._uuid

    @property
    def device_state_attributes(self):
        data = {}

        if self.device.schema is not None:
            data["schema"] = self.device.schema

        return data

    @property
    def device_info(self):
        return {
            "identifiers": {
                # Serial numbers are unique identifiers within a specific domain
                (DOMAIN, self.device.uuid)
            },
            "name": self.device.name,
            "via_device": (DOMAIN, self.hub.host),
        }


class KosmoSSensor(Entity):
    def __init__(self, device, hub: KosmoSHub, hass, attr, clazz="None", icon="api", prop=None):
        hub.doneDevices[f"{device.uuid}_{attr}"] = True
        hub.add_entity(self)
        self.hub = hub
        self.device = device
        self._attr = attr
        self._class = clazz
        self._state = None
        self._unit = None
        if prop is not None:
            if "unit" in prop:
                self._unit = prop["unit"]

        self._icon = f"mdi:{icon}"
        if device.name is not None:
            self._name = f'{device.name}_{attr}'
        else:
            self._name = f'{device.uuid}_{attr}'
            self.device.name = device.uuid
        self._uuid = f'{device.uuid}_{attr}'
        self.refreshFrom(device.state)
        self.hass = hass
        self._support_flags = SUPPORT_TARGET_TEMPERATURE | SUPPORT_PRESET_MODE

    @property
    def icon(self):
        return self._icon

    @property
    def device_class(self):
        return self._class

    @property
    def name(self):
        """Return the name of the device."""
        return self._name

    @property
    def unit_of_measurement(self):
        return self._unit

    @property
    def should_poll(self):
        """Return the polling state."""
        return False

    def refreshFrom(self, state):
        # print("refresh "+self._uuid+"from ", state)

        if self._attr in state:
            # print("found "+self._attr+" "+str(state[self._attr]))
            self._state = state[self._attr]
            if self.hass is not None:
                self.async_write_ha_state()

    @property
    def state(self):
        return self._state

    @property
    def supported_features(self):
        """Return the list of supported features."""
        return self._support_flags

    @property
    def unique_id(self):
        return self._uuid

    @property
    def device_state_attributes(self):
        data = {}

        if self.device.schema is not None:
            data["schema"] = self.device.schema

        return data

    @property
    def device_info(self):
        return {
            "identifiers": {
                # Serial numbers are unique identifiers within a specific domain
                (DOMAIN, self.device.uuid)
            },
            "name": self.device.name,
            "via_device": (DOMAIN, self.hub.host),
        }


class KosmoSBinarySensor(BinarySensorEntity):
    def __init__(self, device, hub: KosmoSHub, hass, attr, clazz="None", icon="api", prop=None):
        hub.doneDevices[device.uuid + "_" + attr] = True

        self.hub = hub
        self.device = device
        hub.add_entity(self)

        self._attr = attr
        self._class = clazz
        self._state = None
        self._unit = None
        if prop is not None:
            if "unit" in prop:
                self._unit = prop["unit"]

        self._icon = "mdi:" + icon
        if device.name is not None:
            self._name = device.name + "_" + attr
        else:
            self._name = device.uuid + "_" + attr
            self.device.name = device.uuid
        self._uuid = device.uuid + "_" + attr
        self.refreshFrom(device.state)
        self.hass = hass
        self._support_flags = SUPPORT_TARGET_TEMPERATURE | SUPPORT_PRESET_MODE

    def is_on(self):
        return self._state

    @property
    def icon(self):
        return self._icon

    @property
    def device_class(self):
        return self._class

    @property
    def name(self):
        """Return the name of the device."""
        return self._name

    @property
    def unit_of_measurement(self):
        return self._unit

    @property
    def should_poll(self):
        """Return the polling state."""
        return False

    def refreshFrom(self, state):
        # print("refresh "+self._uuid+"from ", state)

        if self._attr in state:
            # print("found "+self._attr+" "+str(state[self._attr]))
            self._state = state[self._attr]
            if self.hass is not None:
                self.async_write_ha_state()

    @property
    def state(self):
        return self._state

    @property
    def supported_features(self):
        """Return the list of supported features."""
        return self._support_flags

    @property
    def unique_id(self):
        return self._uuid

    @property
    def device_state_attributes(self):
        data = {}

        if self.device.schema is not None:
            data["schema"] = self.device.schema

        return data

    @property
    def device_info(self):
        return {
            "identifiers": {
                # Serial numbers are unique identifiers within a specific domain
                (DOMAIN, self.device.uuid)
            },
            "name": self.device.name,
            "via_device": (DOMAIN, self.hub.host),
        }
