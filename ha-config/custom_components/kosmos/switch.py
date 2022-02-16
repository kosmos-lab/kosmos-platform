"""Platform for light integration."""
import logging

_LOGGER = logging.getLogger(__name__)


from .const import DOMAIN, CONF_CONNECTIONS

async def async_setup_entry(hass, config_entry, async_add_entities):
    _LOGGER.warn("KOSMOS SWITCH INIT")
    hub = hass.data[DOMAIN][CONF_CONNECTIONS][config_entry.entry_id]
    hub.async_add_entities_switch = async_add_entities
