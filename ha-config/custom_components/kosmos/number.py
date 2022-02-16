"""Platform for light integration."""
import logging
from .kosmos import KosmoSHub
from typing import Any, Callable, Dict, List, Optional, Tuple, Union
from homeassistant.helpers.entity import Entity

import voluptuous as vol
import asyncio
import homeassistant.helpers.config_validation as cv
# Import the device class from the component that you want to support
from homeassistant.helpers.entity import Entity
from homeassistant.components.number import NumberEntity
import time

from homeassistant.const import (
    ATTR_BATTERY_LEVEL,
    ATTR_TEMPERATURE,
    CONF_DEVICES,
    PRECISION_HALVES,
    TEMP_CELSIUS,
    CONF_HOST, CONF_PASSWORD, CONF_USERNAME
)
import homeassistant.util.color as color_util


_LOGGER = logging.getLogger(__name__)

from .const import DOMAIN, CONF_CONNECTIONS


async def async_setup_entry(hass, config_entry, async_add_entities):

    _LOGGER.warn("KOSMOS Number INIT")

    entities = []
    devices = hass.data[DOMAIN][CONF_DEVICES]
    hub = hass.data[DOMAIN][CONF_CONNECTIONS][config_entry.entry_id]
    hub.async_add_entities_number = async_add_entities


class KosmoSNumber(NumberEntity):
    def __init__(self, device, hub: KosmoSHub, hass, attr, clazz="None",icon="api",prop=None):
        hub.doneDevices[device["uuid"] + "_" + attr] = True
        hub.add_entity(self)

        self.hub = hub
        self.device = device
        self._attr = attr
        self._class = clazz
        self._state = None
        self._unit = None
        self._step = None
        self._min = None
        self._max = None
        if prop is not None:
            if "unit" in prop:
                self._unit = prop["unit"]
            if "maximum" in prop:
                self._max = prop["maximum"]
            if "minimum" in prop:
                self._min = prop["minimum"]
            if "multipleOf" in prop:
                self._step = prop["multipleOf"]
        self._icon = "mdi:"+icon
        if "name" in device:
            self._name = device["name"] + "_" + attr
        else:
            self._name = device["uuid"] + "_" + attr
            self.device["name"] = device["uuid"]
        self._uuid = device["uuid"] + "_" + attr

        self.refreshFrom(device["state"])
        self.hass = hass

    @property
    def max_value(self):
        return self._max

    @property
    def min_value(self):
        return self._min

    @property
    def step(self):
        return self._step

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
        #print("refresh "+self._uuid+"from ", state)

        if self._attr in state:
            #print("found "+self._attr+" "+str(state[self._attr]))
            self._state = state[self._attr]
            if self.hass is not None:
                self.async_write_ha_state()

    @property
    def value(self):
        return self._state

    def set_value(self, value):
        state = {self._attr: value}
        self.hub.set_device(self.device["uuid"], state)

    @property
    def unique_id(self):
        return self._uuid
    @property
    def device_state_attributes(self):
        data = {}

        if "schema" in self.device:
            data["schema"] = self.device["schema"]

        return data

    @property
    def device_info(self):
        return {
            "identifiers": {
                # Serial numbers are unique identifiers within a specific domain
                (DOMAIN, self.device["uuid"])
            },
            "name": self.device["name"],
            "via_device": (DOMAIN, self.hub.host),
        }