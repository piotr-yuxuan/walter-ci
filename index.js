const core = require('@actions/core');
const github = require('@actions/github');
const {exec} = require("child_process");

function loggedExec(commandString) {
    exec(commandString, (error, stdout, stderr) => {
        console.log(`::group::$ ${commandString}`)
        if (error) {
            console.log(`error: ${error.message}`);
            return;
        }
        if (stderr) {
            console.log(`stderr: ${stderr}`);
            return;
        }
        console.log(`stdout: ${stdout}`);
        console.log('::endgroup::')
    });
}

// Ugly but does what I want right now
const delay = ms => new Promise(res => setTimeout(res, ms));

loggedExec("pwd")
loggedExec("id")
delay(5000)
loggedExec("ls -hal")
delay(5000)
loggedExec("mkdir target")
delay(5000)
loggedExec("lein uberjar")
delay(5000)
loggedExec("ls -hal target")
delay(5000)
loggedExec("echo 'blah blah' > blah.txt")
delay(5000)
loggedExec("cat blah.txt")
delay(5000)
loggedExec("rm blah.txt")
delay(5000)
//loggedExec("docker build -t piotryuxuan/walter-ci:latest .")
