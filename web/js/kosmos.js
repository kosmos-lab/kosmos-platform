/**
 * create an element from the given html string
 * @param htmlString
 * @returns {ChildNode}
 */
function createElementFromHTML(htmlString) {
    var div = document.createElement('div');
    div.innerHTML = htmlString.trim();
    return div.firstChild;
}

/**
 * a class to talk to the kosmos platform
 */
class Kosmos {
    constructor() {
        this._logins = {};
        this._username = "";
        this._password = "";
        this._base = "";
        this._token = "";
        if (typeof window !== 'undefined' && typeof window.location !== 'undefined') {
            this._base = `${window.location.protocol}//${window.location.hostname}:${window.location.port}`;
        }
        if (typeof document !== 'undefined') {
            window.addEventListener('DOMContentLoaded', this, false);
        }
        this._self = this
    }
    getBasicAuth() {
        return btoa(`${this._username}:${this._password}`);
    }
    download(uri,filename,method='GET') {
        /*return (this.prepareRequest(uri, data, method)).then(
            //response => response.json()
            $("a")
                .attr({
                    "href": response,
                    "download": filename
                })
                .html($("a").attr("download"))
                .get(0).click()
        )*/
        var xhr = new XMLHttpRequest();

        xhr.open('GET', uri+"?filename="+filename,true);
        xhr.setRequestHeader("Authorization", `Basic ${this.getBasicAuth()}`);
        xhr.responseType = 'blob';
        xhr.set
        xhr.onload = function () {
            var urlCreator = window.URL || window.webkitURL;
            var imageUrl = urlCreator.createObjectURL(this.response);
            var tag = document.createElement('a');
            tag.href = imageUrl;
            tag.target = '_blank';
            tag.download = filename;
            document.body.appendChild(tag);
            tag.click();
            document.body.removeChild(tag);
        };
        xhr.onerror = err => {
            console.log(err);
            alert('Failed to download picture');
        };
        xhr.send();

    }
    getInit(method,body) {

        return {
            method: method,
            mode: 'cors', // no-cors, *cors, same-origin
            cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
            credentials: 'same-origin', // include, *same-origin, omit
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Basic ${this.getBasicAuth()}`
                // 'Content-Type': 'application/x-www-form-urlencoded',
            },
            redirect: 'follow', // manual, *follow, error
            referrerPolicy: 'no-referrer', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
            body: body // body data type must match "Content-Type" header
        }
    }

    async prepareRequest(raw_uri = '', data = {}, method = 'GET') {
        //data["username"] = this._username;
        //data["password"] = this._password;
        let body = null;
        if (!raw_uri.startsWith("http://") && !raw_uri.startsWith("https://")) {
            raw_uri = `${this._base}${raw_uri}`;
        }
        console.log(`got url: ${raw_uri}`);
        const url = new URL(raw_uri)

        if (method == 'GET') {
            url.search = new URLSearchParams(data).toString();
        }
        if (method == 'POST') {
            body = JSON.stringify(data)
        }


        return await fetch(url, this.getInit(method,body));
    }

    fetchJSON(raw_uri = '', data = {}, method = 'GET') {
        // Default options are marked with *

        return (this.prepareRequest(raw_uri, data, method)).then(response => response.json())
            ; // parses JSON response into native JavaScript objects
    }



    fetchData(raw_uri = '', data = {}, method = 'GET') {
        // Default options are marked with *

        return (this.prepareRequest(raw_uri, data, method));
    }

    onLogin(username) {
        console.log(`logged in as ${username}`);
    };

    login(username, password) {
        const xhr = new XMLHttpRequest();

        xhr.open('POST', `/user/login`, true);
        xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        if (username == null || password == null) {
            // @ts-ignore
            this._username = document.getElementById("username").value;
            // @ts-ignore
            this._password = document.getElementById("password").value;
        } else {
            this._username = username;
            this._password = password;
        }
        const me = this;
        // @ts-ignore
        const save = document.getElementById('save').checked;
        xhr.onreadystatechange = function (oEvent) {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                const status = xhr.status;
                if (status === 200) {
                    me._token = xhr.response;
                    document.getElementById("kosmos_login").style.display = 'none';
                    document.getElementById("kosmos_main").style.display = 'initial';
                    if (save) {
                        if (me._logins == undefined) {
                            me._logins = {};
                        }

                        me._logins[me._username] = me._password;
                        localStorage.setItem('logins', JSON.stringify(me._logins));
                    }
                    //me.loadCameras();
                    me.onLogin(me._username);
                } else if (status === 403) {
                    console.log("login failed!")
                    document.getElementById("login_error").appendChild(createElementFromHTML(`
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        <strong>Login Failed!</strong>
        Username/Password is incorrect
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="close"></button>
    </div>`));
                } else {
                    console.log("login failed!")
                    document.getElementById("login_error").appendChild(createElementFromHTML(`
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        <strong>Login Failed!</strong>
        ${xhr.statusText}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>`));

                }

            }

        };
        xhr.onerror = function (e) {
            console.log("Error Catched", JSON.stringify(e));
        };
        xhr.send(`user=${this._username}&pass=${this._password}`);
    }


    /**
     * will be executed on DOMContentLoaded,
     * needs to be done this way to get a reference to the kosmos event as this
     * @param event
     */
    handleEvent = function (event) {

        switch (event.type) {
            case 'DOMContentLoaded':
                console.log("starting onLoad init");
                document.getElementById("btn_login").addEventListener('click', this.login);

                const storage = localStorage.getItem('logins');
                const div = document.getElementById("logindiv");
                let style = 'none';
                if (storage != undefined) {
                    //if localstorage was not empty parse it as json
                    this._logins = JSON.parse(storage);
                    const me = this;

                    //add all of the buttons to quickly login
                    let entries = 0;
                    for (const [username, password] of Object.entries(this._logins)) {
                        const btn = document.createElement("button");
                        btn.addEventListener("click", function (event) {
                            console.log(`trying to login with ${me}`)
                            me.login(username, password);


                        });
                        btn.classList.add("btn");
                        btn.classList.add("btn-outline-primary");
                        btn.innerText = `login as ${username}`;
                        div.appendChild(btn);
                        entries++;
                    }
                    if (entries > 0) {
                        console.log("have at least 1 entry");
                        style = 'initial';
                    }
                }
                div.style.display = style;


//add keydown eventlistener to send login on enter
                document.querySelector("#username").addEventListener("keydown", (evt) => {
                    // @ts-ignore
                    if (evt.key === "Enter") {
                        this.login();
                    }
                });
//add keydown eventlistener to send login on enter
                document.querySelector("#password").addEventListener("keydown", (evt) => {
                    // @ts-ignore
                    if (evt.key === "Enter") {
                        this.login();
                    }
                });
                console.log(`finished onLoad init for ${this}`);
                break;
        }


    }
}


