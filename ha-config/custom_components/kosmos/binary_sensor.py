"""
Platform for binary sensor integration.
We just use this to get the add_entity async_add_entities instance we need
"""
import logging


_LOGGER = logging.getLogger(__name__)


from .const import DOMAIN, CONF_CONNECTIONS



async def async_setup_entry(hass, config_entry, async_add_entities):
    _LOGGER.warn("KOSMOS Binary Sensor INIT")
    hub = hass.data[DOMAIN][CONF_CONNECTIONS][config_entry.entry_id]
    hub.async_add_entities_binary_sensor = async_add_entities
