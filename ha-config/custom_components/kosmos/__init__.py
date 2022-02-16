"""The KosmoS integration."""
import asyncio
import logging

from homeassistant.config_entries import SOURCE_IMPORT
from homeassistant.helpers.aiohttp_client import async_get_clientsession
import time
from homeassistant.config_entries import ConfigEntry
from homeassistant.core import HomeAssistant
from homeassistant.const import (
    CONF_DEVICES,
    CONF_HOST,
    CONF_PASSWORD,
    CONF_USERNAME,
    EVENT_HOMEASSISTANT_STOP,
    MATCH_ALL
)
from .kosmos import KosmoSHub

from .const import DOMAIN, CONF_CONNECTIONS

_LOGGER = logging.getLogger(__name__)

PLATFORMS = ["light", "climate", "switch", "sensor", "binary_sensor", "cover"]


async def async_setup(hass: HomeAssistant, config: dict):
    """Set up the KosmoS component."""
    _LOGGER.warn("starting KosmoS")
    hass.states.async_set("kosmos.running", "1")
    if DOMAIN in config:
        for entry_config in config[DOMAIN][CONF_DEVICES]:
            hass.async_create_task(

                hass.config_entries.flow.async_init(
                    DOMAIN, context={"source": SOURCE_IMPORT}, data=entry_config
                )
            )

    return True


async def async_setup_entry(hass: HomeAssistant, entry: ConfigEntry):
    """Set up KosmoS from a config entry."""

    _LOGGER.warn("KOSMOS INIT of - %s", entry.as_dict())
    hub = KosmoSHub(hass, entry.data["host"],  entry.data["username"], entry.data["password"])
    hass.data.setdefault(DOMAIN, {CONF_CONNECTIONS: {}, CONF_DEVICES: set()})
    hass.data[DOMAIN][CONF_CONNECTIONS][entry.entry_id] = hub
    hub.start()

    task = asyncio.create_task(hub.wait_for_init())

    await task

    for component in PLATFORMS:
        hass.async_create_task(
            hass.config_entries.async_forward_entry_setup(entry, component)
        )
    _LOGGER.warn("KOSMOS INIT9")
    hass.bus.async_listen(MATCH_ALL, hub.handle_hass_event)
    _LOGGER.warn("KOSMOS INIT10")

    return True


async def async_unload_entry(hass: HomeAssistant, entry: ConfigEntry):
    """Unload a config entry."""

    unload_ok = all(
        await asyncio.gather(
            *[
                hass.config_entries.async_forward_entry_unload(entry, component)
                for component in config[DOMAIN][CONF_DEVICES]
            ]
        )
    )
    if unload_ok:
        hass.data[DOMAIN][CONF_CONNECTIONS].pop(entry.entry_id)
    return unload_ok

    return None
