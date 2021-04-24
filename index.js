const core = require('@actions/core');
const github = require('@actions/github');
const {exec} = require("child_process");

try {
    const payload = JSON.stringify(github.context.payload, undefined, 2)
    console.log(`The event payload: ${payload}`);
} catch (error) {
    core.setFailed(error.message);
}

function loggedExec(commandString) {
    console.log(`$ ${commandString}`)
    exec(commandString, (error, stdout, stderr) => {
        if (error) {
            console.log(`error: ${error.message}`);
            return;
        }
        if (stderr) {
            console.log(`stderr: ${stderr}`);
            return;
        }
        console.log(`stdout: ${stdout}`);
    });
}

loggedExec("id")
loggedExec("env")
loggedExec("ls -hal")
loggedExec("which docker")
loggedExec("docker --help")
loggedExec("docker --version")
//loggedExec("docker build -t piotryuxuan/walter-ci:latest .")
