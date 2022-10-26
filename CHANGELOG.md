# Changelog
# v0.8.7
* Added IAuthProvider Interface 
  * added simple Keycloak Auth Provider
    * add `"keycloak": { "server": "https://your.server","clientId": "kosmos","realm": "kosmos","clientSecret": "*"}` to your config.json
    * will try to log in and create a user in the KosmoS system if it did succeed
  * added simple Authelia Auth Provider
    * add `"authelia": {"server": "https://your.server"}` to config.json 
    * will try to log in and create a user in the KosmoS system if it did succeed, no access in Authelia beside the correct end user credentials is needed
  * added simple LDAP Auth Provider
    * add `"ldap": { "server": "ldap://ldap:389", "userdn": "ou=people,dc=example,dc=com"}` to your config.json
    * will try to log in and create a user in the KosmoS system if it did succeed, no access in LDAP beside the correct end user credentials is needed
  * add at most ONE provider
# v0.8.6
* update tests
* added Events
* added Texts
* updated api documentations
* updates to fix a potential concurrency issue with device/set
* updated kosmos home-assistant to better work with rgb like devices

# v0.8.5 
* moved web components to its own package

# v0.8.3
* lots of changes to webserver und websocket workflow
* new annotations to document for openapi specification
* initially based upon the swagger annotations but changed them for our needs
* lots of documentation changes
* fixed a threadsafe bug while changing state of a device

# v0.8.2
* Added Schema StabiloPen2 for LNDW
* minor tweaks to Schreibtrainer flow