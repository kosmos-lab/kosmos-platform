"""Config flow for KosmoS integration."""
import logging

import voluptuous as vol
from aiohttp import ClientSession

from homeassistant import config_entries, core, exceptions
from homeassistant.helpers.aiohttp_client import async_get_clientsession

from .const import DOMAIN  # pylint:disable=unused-import
import requests
from .kosmos import KosmoSHub


_LOGGER = logging.getLogger(__name__)

STEP_USER_DATA_SCHEMA = vol.Schema({"host": str, "username": str, "password": str})


def setup_platform(hass, config, add_entities, discovery_info=None):
    _LOGGER.warn("KOSMOS setup platform")




async def validate_input(hass: core.HomeAssistant, data):
    """Validate the user input allows us to connect.

    """
    _LOGGER.warn(data)

    hub = KosmoSHub(hass, data["host"],  data["username"], data["password"])

    if not await hub.authenticate():
        raise InvalidAuth

    return {"title": "Kosmos on " + data["host"] + " as user " + data["username"], "unique_id": data["host"]}


class ConfigFlow(config_entries.ConfigFlow, domain=DOMAIN):
    """Handle a config flow for KosmoS."""

    VERSION = 1
    CONNECTION_CLASS = config_entries.CONN_CLASS_LOCAL_PUSH

    async def async_step_user(self, user_input=None):
        """Handle the initial step."""
        if user_input is None:
            return self.async_show_form(
                step_id="user", data_schema=STEP_USER_DATA_SCHEMA
            )

        errors = {}

        try:
            info = await validate_input(self.hass, user_input)
        except CannotConnect:
            errors["base"] = "cannot_connect"
        except InvalidAuth:
            errors["base"] = "invalid_auth"
        except Exception:  # pylint: disable=broad-except
            _LOGGER.exception("Unexpected exception")
            errors["base"] = "unknown"
        else:
            await self.async_set_unique_id(info["unique_id"])
            self._abort_if_unique_id_configured()

            return self.async_create_entry(title=info["title"], data=user_input)

        return self.async_show_form(
            step_id="user", data_schema=STEP_USER_DATA_SCHEMA, errors=errors
        )

    async def async_step_import(self, user_input=None):
        """Handle a config flow for KosmoS. - used for yaml"""

        return await self.async_step_user(user_input)


class CannotConnect(exceptions.HomeAssistantError):
    """Error to indicate we cannot connect."""


class InvalidAuth(exceptions.HomeAssistantError):
    """Error to indicate there is invalid auth."""
