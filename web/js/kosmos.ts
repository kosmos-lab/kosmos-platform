function createElementFromHTML(htmlString) {
    var div = document.createElement('div');
    div.innerHTML = htmlString.trim();

    // Change this to div.childNodes to support multiple top-level nodes.
    return div.firstChild;
}

class kosmos {
    _logins = {};
    _username = "";
    _password = "";
    _base = "";
    _token = "";

    constructor() {
        if (typeof window !== 'undefined' && typeof window.location !== 'undefined') {
            this._base = 'http://' + window.location.hostname + ':' + window.location.port;
        } else {
            this._base = 'http://localhost:18080';
        }
        if( typeof document !==    'undefined') {
            window.addEventListener('DOMContentLoaded',this.onLoad);
        }
    }


    onLoad = function () {
        console.log("starting onLoad init");
        document.getElementById("btn_login").addEventListener('click', this.login);
        const kosmos = this;

        const storage = localStorage.getItem('logins');
        const div = document.getElementById("logindiv");
        let style = 'none';
        if (storage != undefined) {
            //if localstorage was not empty parse it as json
            this._logins = JSON.parse(storage);


            //add all of the buttons to quickly login
            let entries = 0;
            for (const [username, password] of Object.entries(this._logins)) {
                const btn = document.createElement("button");
                btn.addEventListener("click", function (event) {
                    login(kosmos,username, password);

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
        console.log("finished onLoad init");
    }
    login = function (username, password) {
        const xhr = new XMLHttpRequest();

        xhr.open('POST', this._base + "/user/login", true);
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


                    document.getElementById("_login").style.display = 'none';
                    document.getElementById("_main").style.display = 'initial';

                    if (save) {
                        me._logins[me._username] = me._password;
                        localStorage.setItem('logins', JSON.stringify(me._logins));
                    }
                } else if (status === 403) {
                    console.log("login failed!")
                    document.getElementById("login_error").appendChild(createElementFromHTML("<div class=\"alert alert-danger alert-dismissible fade show\" role=\"alert\">\n" +
                        "  <strong>Login Failed!</strong> Username/Password is incorrect" +
                        "  <button type=\"button\" class=\"btn-close\" data-bs-dismiss=\"alert\" aria-label=\"Close\"></button>\n" +
                        "</div>"));
                } else {
                    console.log("login failed!")
                    document.getElementById("login_error").appendChild(createElementFromHTML("<div class=\"alert alert-danger alert-dismissible fade show\" role=\"alert\">\n" +
                        "  <strong>Login Failed!</strong> " + xhr.statusText +
                        "  <button type=\"button\" class=\"btn-close\" data-bs-dismiss=\"alert\" aria-label=\"Close\"></button>\n" +
                        "</div>"));

                }

            }

        };
        xhr.onerror = function (e) {
            console.log("Error Catched" + JSON.stringify(e));
        };
        xhr.send("user=" + this._username + "&pass=" + this._password);
    }


}


function login(kosmos: kosmos, username: any, password: any) {
    kosmos.login(username,password);
}

